package com.creditbank.platform.module.enterprise.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.creditbank.platform.common.BusinessException;
import com.creditbank.platform.constant.UserRole;
import com.creditbank.platform.entity.SysOrganization;
import com.creditbank.platform.entity.SysUser;
import com.creditbank.platform.mapper.SysOrganizationMapper;
import com.creditbank.platform.mapper.SysUserMapper;
import com.creditbank.platform.module.enterprise.dto.InterviewInvitationVO;
import com.creditbank.platform.module.enterprise.dto.InterviewRtcCredentialsVO;
import com.creditbank.platform.module.enterprise.dto.SendInterviewInviteRequest;
import com.creditbank.platform.module.enterprise.entity.InterviewInvitation;
import com.creditbank.platform.module.enterprise.entity.JobApplication;
import com.creditbank.platform.module.enterprise.entity.JobPosting;
import com.creditbank.platform.module.enterprise.mapper.InterviewInvitationMapper;
import com.creditbank.platform.module.enterprise.mapper.JobApplicationMapper;
import com.creditbank.platform.module.enterprise.mapper.JobPostingMapper;
import com.creditbank.platform.module.enterprise.support.ApplicationStatus;
import com.creditbank.platform.module.enterprise.support.InterviewMode;
import com.creditbank.platform.module.message.entity.UserMessage;
import com.creditbank.platform.module.message.mapper.UserMessageMapper;
import com.creditbank.platform.security.AuthSupport;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class EnterpriseInterviewService {

    private static final int MSG_TYPE_INTERVIEW = 2;
    private static final String REF_TYPE_INTERVIEW = "interview";
    private static final int UNREAD = 0;
    private static final int INVITE_PENDING = 0;
    private static final int INVITE_ACCEPTED = 1;
    private static final int INVITE_REJECTED = 2;

    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final AuthSupport authSupport;
    private final SysUserMapper userMapper;
    private final SysOrganizationMapper orgMapper;
    private final JobPostingMapper jobPostingMapper;
    private final JobApplicationMapper jobApplicationMapper;
    private final InterviewInvitationMapper interviewInvitationMapper;
    private final UserMessageMapper userMessageMapper;
    private final InterviewRtcService interviewRtcService;

    public List<InterviewInvitationVO> listSentInvitations() {
        SysUser user = authSupport.requireEnterprise();
        List<InterviewInvitation> invitations = interviewInvitationMapper.selectList(
                new LambdaQueryWrapper<InterviewInvitation>()
                        .eq(InterviewInvitation::getOrgId, user.getOrgId())
                        .orderByDesc(InterviewInvitation::getCreateTime));
        return toVOList(invitations);
    }

    @Transactional
    public InterviewInvitationVO sendInvitation(SendInterviewInviteRequest request) {
        SysUser sender = authSupport.requireEnterpriseWritable();
        Long orgId = sender.getOrgId();

        Long jobId;
        Long toUserId;
        Long applicationId = request.getApplicationId();
        JobApplication application = null;

        if (applicationId != null) {
            application = authSupport.requireOrgApplication(applicationId, orgId);
            jobId = application.getJobId();
            toUserId = application.getUserId();
        } else if (request.getJobId() != null && request.getToUserId() != null) {
            jobId = request.getJobId();
            toUserId = request.getToUserId();
            authSupport.requireOrgJob(jobId, orgId);
            validateStudentUser(toUserId);
        } else {
            throw new BusinessException(400, "请指定投递记录，或同时提供职位与学员");
        }

        if (Objects.equals(sender.getId(), toUserId)) {
            throw new BusinessException(400, "不能向自己发送面试邀请");
        }

        ensureNoPendingInvite(jobId, toUserId, applicationId);

        JobPosting job = authSupport.requireOrgJob(jobId, orgId);
        SysOrganization org = orgMapper.selectById(orgId);
        String orgName = org != null ? org.getName() : "企业";
        int interviewMode = request.getInterviewMode() != null ? request.getInterviewMode() : InterviewMode.VIDEO;
        if (interviewMode != InterviewMode.ONSITE && interviewMode != InterviewMode.VIDEO) {
            throw new BusinessException(400, "无效的面试方式");
        }
        String location = resolveLocation(request.getLocation(), interviewMode);

        UserMessage message = new UserMessage();
        message.setFromUserId(sender.getId());
        message.setToUserId(toUserId);
        message.setMsgType(MSG_TYPE_INTERVIEW);
        message.setTitle("面试邀请：" + job.getTitle());
        message.setContent(buildMessageContent(orgName, job.getTitle(), request.getInviteTime(), location,
                interviewMode, request.getRemark()));
        message.setRefType(REF_TYPE_INTERVIEW);
        message.setReadStatus(UNREAD);
        userMessageMapper.insert(message);

        InterviewInvitation invitation = new InterviewInvitation();
        invitation.setJobId(jobId);
        invitation.setOrgId(orgId);
        invitation.setFromUserId(sender.getId());
        invitation.setToUserId(toUserId);
        invitation.setMessageId(message.getId());
        invitation.setApplicationId(applicationId);
        invitation.setStatus(INVITE_PENDING);
        invitation.setInviteTime(request.getInviteTime());
        invitation.setLocation(location);
        invitation.setInterviewMode(interviewMode);
        interviewInvitationMapper.insert(invitation);

        if (interviewMode == InterviewMode.VIDEO) {
            InterviewInvitation roomUpdate = new InterviewInvitation();
            roomUpdate.setId(invitation.getId());
            roomUpdate.setRoomId("interview-" + invitation.getId());
            interviewInvitationMapper.updateById(roomUpdate);
            invitation.setRoomId(roomUpdate.getRoomId());
        }

        message.setRefId(invitation.getId());
        userMessageMapper.updateById(message);

        if (application != null && shouldMoveToInterview(application.getStatus())) {
            application.setStatus(ApplicationStatus.INTERVIEW);
            jobApplicationMapper.updateById(application);
        }

        InterviewInvitation saved = interviewInvitationMapper.selectById(invitation.getId());
        return toVO(saved, job.getTitle(), orgName, loadNameMap(saved));
    }

    public InterviewRtcCredentialsVO getRtcCredentials(Long invitationId) {
        SysUser user = authSupport.requireEnterprise();
        InterviewInvitation invitation = interviewRtcService.requireInvitation(invitationId);
        if (!Objects.equals(invitation.getOrgId(), user.getOrgId())) {
            throw new BusinessException(403, "无权访问该面试");
        }
        return interviewRtcService.issueCredentials(invitation, user.getId());
    }

    private boolean shouldMoveToInterview(Integer status) {
        if (status == null) {
            return true;
        }
        return status < ApplicationStatus.INTERVIEW || status == ApplicationStatus.INTERVIEW_CANCELLED;
    }

    private void ensureNoPendingInvite(Long jobId, Long toUserId, Long applicationId) {
        LambdaQueryWrapper<InterviewInvitation> wrapper = new LambdaQueryWrapper<InterviewInvitation>()
                .eq(InterviewInvitation::getStatus, INVITE_PENDING);
        if (applicationId != null) {
            wrapper.eq(InterviewInvitation::getApplicationId, applicationId);
        } else {
            wrapper.eq(InterviewInvitation::getJobId, jobId)
                    .eq(InterviewInvitation::getToUserId, toUserId);
        }
        Long count = interviewInvitationMapper.selectCount(wrapper);
        if (count != null && count > 0) {
            throw new BusinessException(400, "该投递已有待回复的面试邀请");
        }
    }

    private String resolveLocation(String location, int interviewMode) {
        if (StringUtils.hasText(location)) {
            return location.trim();
        }
        if (interviewMode == InterviewMode.VIDEO) {
            return "平台视频面试";
        }
        throw new BusinessException(400, "现场面试请填写面试地点");
    }

    private String buildMessageContent(String orgName, String jobTitle,
                                       java.time.LocalDateTime inviteTime, String location,
                                       int interviewMode, String remark) {
        StringBuilder sb = new StringBuilder();
        sb.append("您好，").append(orgName).append(" 邀请您参加「").append(jobTitle).append("」的面试。\n\n");
        sb.append("面试时间：").append(inviteTime.format(TIME_FORMAT)).append('\n');
        sb.append("面试方式：").append(InterviewMode.modeName(interviewMode)).append('\n');
        sb.append("面试地点/方式：").append(location).append("\n\n");
        if (interviewMode == InterviewMode.VIDEO) {
            sb.append("接受邀请后，可在「面试邀请」页面点击「进入面试」加入平台视频房间。\n\n");
        }
        if (StringUtils.hasText(remark)) {
            sb.append(remark.trim()).append("\n\n");
        }
        sb.append("请在「面试邀请」页面回复是否参加。");
        return sb.toString();
    }

    private void validateStudentUser(Long userId) {
        SysUser user = userMapper.selectById(userId);
        if (user == null || user.getRole() == null || user.getRole() != UserRole.STUDENT) {
            throw new BusinessException(400, "收件人必须是学员用户");
        }
    }

    private List<InterviewInvitationVO> toVOList(List<InterviewInvitation> invitations) {
        if (invitations.isEmpty()) {
            return List.of();
        }
        Map<Long, String> jobTitleMap = jobPostingMapper.selectList(new LambdaQueryWrapper<JobPosting>()
                        .in(JobPosting::getId, invitations.stream().map(InterviewInvitation::getJobId).distinct().toList()))
                .stream()
                .collect(Collectors.toMap(JobPosting::getId, JobPosting::getTitle));
        Map<Long, String> orgNameMap = orgMapper.selectList(new LambdaQueryWrapper<SysOrganization>()
                        .in(SysOrganization::getId, invitations.stream().map(InterviewInvitation::getOrgId).distinct().toList()))
                .stream()
                .collect(Collectors.toMap(SysOrganization::getId, SysOrganization::getName));
        Map<Long, String> nameMap = loadNameMap(invitations);
        return invitations.stream()
                .map(item -> toVO(item, jobTitleMap.get(item.getJobId()), orgNameMap.get(item.getOrgId()), nameMap))
                .toList();
    }

    private InterviewInvitationVO toVO(InterviewInvitation invitation, String jobTitle, String orgName,
                                       Map<Long, String> nameMap) {
        JobApplication application = invitation.getApplicationId() != null
                ? jobApplicationMapper.selectById(invitation.getApplicationId())
                : null;
        Integer applicationStatus = application != null ? application.getStatus() : null;
        return InterviewInvitationVO.builder()
                .id(invitation.getId())
                .jobId(invitation.getJobId())
                .jobTitle(jobTitle)
                .orgId(invitation.getOrgId())
                .orgName(orgName)
                .fromUserId(invitation.getFromUserId())
                .fromUserName(nameMap.get(invitation.getFromUserId()))
                .toUserId(invitation.getToUserId())
                .toUserName(nameMap.get(invitation.getToUserId()))
                .applicationId(invitation.getApplicationId())
                .messageId(invitation.getMessageId())
                .status(invitation.getStatus())
                .statusName(inviteStatusName(invitation.getStatus()))
                .inviteTime(invitation.getInviteTime())
                .location(invitation.getLocation())
                .interviewMode(invitation.getInterviewMode())
                .interviewModeName(InterviewMode.modeName(invitation.getInterviewMode()))
                .roomId(invitation.getRoomId())
                .applicationStatus(applicationStatus)
                .applicationStatusName(ApplicationStatus.statusName(applicationStatus))
                .canJoinVideo(interviewRtcService.canJoinVideo(invitation))
                .createTime(invitation.getCreateTime())
                .build();
    }

    private Map<Long, String> loadNameMap(InterviewInvitation invitation) {
        return loadNameMap(List.of(invitation));
    }

    private Map<Long, String> loadNameMap(List<InterviewInvitation> invitations) {
        List<Long> userIds = invitations.stream()
                .flatMap(item -> Stream.of(item.getFromUserId(), item.getToUserId()))
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        if (userIds.isEmpty()) {
            return Map.of();
        }
        return userMapper.selectList(new LambdaQueryWrapper<SysUser>().in(SysUser::getId, userIds)).stream()
                .collect(Collectors.toMap(
                        SysUser::getId,
                        user -> StringUtils.hasText(user.getRealName()) ? user.getRealName() : user.getUsername()
                ));
    }

    public static String inviteStatusName(Integer status) {
        if (status == null) {
            return "未知";
        }
        return switch (status) {
            case INVITE_PENDING -> "待回复";
            case INVITE_ACCEPTED -> "已接受";
            case INVITE_REJECTED -> "已拒绝";
            default -> "未知";
        };
    }
}
