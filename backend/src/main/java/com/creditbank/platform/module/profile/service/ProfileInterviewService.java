package com.creditbank.platform.module.profile.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.creditbank.platform.common.BusinessException;
import com.creditbank.platform.entity.SysOrganization;
import com.creditbank.platform.entity.SysUser;
import com.creditbank.platform.mapper.SysOrganizationMapper;
import com.creditbank.platform.mapper.SysUserMapper;
import com.creditbank.platform.module.enterprise.dto.InterviewInvitationVO;
import com.creditbank.platform.module.enterprise.dto.InterviewRtcCredentialsVO;
import com.creditbank.platform.module.enterprise.entity.InterviewInvitation;
import com.creditbank.platform.module.enterprise.entity.JobPosting;
import com.creditbank.platform.module.enterprise.mapper.InterviewInvitationMapper;
import com.creditbank.platform.module.enterprise.mapper.JobPostingMapper;
import com.creditbank.platform.module.enterprise.service.EnterpriseInterviewService;
import com.creditbank.platform.module.enterprise.service.InterviewRtcService;
import com.creditbank.platform.module.enterprise.entity.JobApplication;
import com.creditbank.platform.module.enterprise.mapper.JobApplicationMapper;
import com.creditbank.platform.module.enterprise.support.ApplicationStatus;
import com.creditbank.platform.module.enterprise.support.InterviewMode;
import com.creditbank.platform.security.AuthSupport;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class ProfileInterviewService {

    private static final int INVITE_PENDING = 0;
    private static final int INVITE_ACCEPTED = 1;
    private static final int INVITE_REJECTED = 2;

    private final AuthSupport authSupport;
    private final SysUserMapper userMapper;
    private final SysOrganizationMapper orgMapper;
    private final JobPostingMapper jobPostingMapper;
    private final InterviewInvitationMapper interviewInvitationMapper;
    private final InterviewRtcService interviewRtcService;
    private final JobApplicationMapper jobApplicationMapper;

    public List<InterviewInvitationVO> listMyInvitations() {
        SysUser user = authSupport.requireStudent();
        List<InterviewInvitation> invitations = interviewInvitationMapper.selectList(
                new LambdaQueryWrapper<InterviewInvitation>()
                        .eq(InterviewInvitation::getToUserId, user.getId())
                        .orderByDesc(InterviewInvitation::getCreateTime));
        return toVOList(invitations);
    }

    public InterviewInvitationVO getMyInvitation(Long invitationId) {
        SysUser user = authSupport.requireStudent();
        InterviewInvitation invitation = getOwnedInvitationOrThrow(invitationId, user.getId());
        return toVO(invitation);
    }

    @Transactional
    public InterviewInvitationVO acceptInvitation(Long invitationId) {
        return respond(invitationId, INVITE_ACCEPTED);
    }

    @Transactional
    public InterviewInvitationVO rejectInvitation(Long invitationId) {
        return respond(invitationId, INVITE_REJECTED);
    }

    public InterviewRtcCredentialsVO getRtcCredentials(Long invitationId) {
        SysUser user = authSupport.requireStudent();
        InterviewInvitation invitation = getOwnedInvitationOrThrow(invitationId, user.getId());
        return interviewRtcService.issueCredentials(invitation, user.getId());
    }

    private InterviewInvitationVO respond(Long invitationId, int targetStatus) {
        SysUser user = authSupport.requireStudent();
        InterviewInvitation invitation = getOwnedInvitationOrThrow(invitationId, user.getId());
        if (invitation.getStatus() == null || invitation.getStatus() != INVITE_PENDING) {
            throw new BusinessException(400, "该邀请已处理，无法重复操作");
        }

        invitation.setStatus(targetStatus);
        interviewInvitationMapper.updateById(invitation);

        if (targetStatus == INVITE_REJECTED) {
            syncApplicationOnInviteReject(invitation);
        }

        return toVO(invitation);
    }

    private void syncApplicationOnInviteReject(InterviewInvitation invitation) {
        if (invitation.getApplicationId() == null) {
            return;
        }
        JobApplication application = jobApplicationMapper.selectById(invitation.getApplicationId());
        if (application == null) {
            return;
        }
        if (Objects.equals(application.getStatus(), ApplicationStatus.INTERVIEW)) {
            application.setStatus(ApplicationStatus.INTERVIEW_CANCELLED);
            jobApplicationMapper.updateById(application);
        }
    }

    private InterviewInvitation getOwnedInvitationOrThrow(Long invitationId, Long userId) {
        InterviewInvitation invitation = interviewInvitationMapper.selectById(invitationId);
        if (invitation == null) {
            throw new BusinessException(404, "面试邀请不存在");
        }
        if (!Objects.equals(invitation.getToUserId(), userId)) {
            throw new BusinessException(403, "无权操作该面试邀请");
        }
        return invitation;
    }

    private InterviewInvitationVO toVO(InterviewInvitation invitation) {
        JobPosting job = jobPostingMapper.selectById(invitation.getJobId());
        SysOrganization org = orgMapper.selectById(invitation.getOrgId());
        Map<Long, String> nameMap = loadNameMap(invitation);
        JobApplication application = invitation.getApplicationId() != null
                ? jobApplicationMapper.selectById(invitation.getApplicationId())
                : null;
        Integer applicationStatus = application != null ? application.getStatus() : null;
        return InterviewInvitationVO.builder()
                .id(invitation.getId())
                .jobId(invitation.getJobId())
                .jobTitle(job != null ? job.getTitle() : null)
                .orgId(invitation.getOrgId())
                .orgName(org != null ? org.getName() : null)
                .fromUserId(invitation.getFromUserId())
                .fromUserName(nameMap.get(invitation.getFromUserId()))
                .toUserId(invitation.getToUserId())
                .toUserName(nameMap.get(invitation.getToUserId()))
                .applicationId(invitation.getApplicationId())
                .messageId(invitation.getMessageId())
                .status(invitation.getStatus())
                .statusName(EnterpriseInterviewService.inviteStatusName(invitation.getStatus()))
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
        Map<Long, JobApplication> applicationMap = loadApplicationMap(invitations);
        return invitations.stream()
                .map(item -> {
                    JobApplication application = applicationMap.get(item.getApplicationId());
                    Integer applicationStatus = application != null ? application.getStatus() : null;
                    return InterviewInvitationVO.builder()
                        .id(item.getId())
                        .jobId(item.getJobId())
                        .jobTitle(jobTitleMap.get(item.getJobId()))
                        .orgId(item.getOrgId())
                        .orgName(orgNameMap.get(item.getOrgId()))
                        .fromUserId(item.getFromUserId())
                        .fromUserName(nameMap.get(item.getFromUserId()))
                        .toUserId(item.getToUserId())
                        .toUserName(nameMap.get(item.getToUserId()))
                        .applicationId(item.getApplicationId())
                        .messageId(item.getMessageId())
                        .status(item.getStatus())
                        .statusName(EnterpriseInterviewService.inviteStatusName(item.getStatus()))
                        .inviteTime(item.getInviteTime())
                        .location(item.getLocation())
                        .interviewMode(item.getInterviewMode())
                        .interviewModeName(InterviewMode.modeName(item.getInterviewMode()))
                        .roomId(item.getRoomId())
                        .applicationStatus(applicationStatus)
                        .applicationStatusName(ApplicationStatus.statusName(applicationStatus))
                        .canJoinVideo(interviewRtcService.canJoinVideo(item))
                        .createTime(item.getCreateTime())
                        .build();
                })
                .toList();
    }

    private Map<Long, JobApplication> loadApplicationMap(List<InterviewInvitation> invitations) {
        List<Long> applicationIds = invitations.stream()
                .map(InterviewInvitation::getApplicationId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        if (applicationIds.isEmpty()) {
            return Map.of();
        }
        return jobApplicationMapper.selectList(
                        new LambdaQueryWrapper<JobApplication>().in(JobApplication::getId, applicationIds))
                .stream()
                .collect(Collectors.toMap(JobApplication::getId, app -> app));
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
}
