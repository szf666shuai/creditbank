package com.creditbank.platform.module.enterprise.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.creditbank.platform.common.BusinessException;
import com.creditbank.platform.entity.SysOrganization;
import com.creditbank.platform.entity.SysUser;
import com.creditbank.platform.mapper.SysOrganizationMapper;
import com.creditbank.platform.mapper.SysUserMapper;
import com.creditbank.platform.module.enterprise.dto.ApplicationManageVO;
import com.creditbank.platform.module.enterprise.entity.InterviewInvitation;
import com.creditbank.platform.module.enterprise.entity.JobApplication;
import com.creditbank.platform.module.enterprise.entity.JobPosting;
import com.creditbank.platform.module.enterprise.mapper.InterviewInvitationMapper;
import com.creditbank.platform.module.enterprise.mapper.JobApplicationMapper;
import com.creditbank.platform.module.enterprise.mapper.JobPostingMapper;
import com.creditbank.platform.module.enterprise.support.ApplicationStatus;
import com.creditbank.platform.module.message.entity.UserMessage;
import com.creditbank.platform.module.message.mapper.UserMessageMapper;
import com.creditbank.platform.security.AuthSupport;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EnterpriseApplicationService {

    private static final int MSG_TYPE_NORMAL = 1;
    private static final int INVITE_PENDING = 0;
    private static final int UNREAD = 0;
    private static final String REF_TYPE_APPLICATION = "application";

    private final AuthSupport authSupport;
    private final SysUserMapper userMapper;
    private final SysOrganizationMapper orgMapper;
    private final JobPostingMapper jobPostingMapper;
    private final JobApplicationMapper jobApplicationMapper;
    private final InterviewInvitationMapper interviewInvitationMapper;
    private final UserMessageMapper userMessageMapper;

    @Transactional
    public List<ApplicationManageVO> listApplications() {
        SysUser user = authSupport.requireEnterprise();
        Long orgId = user.getOrgId();

        List<JobPosting> jobs = jobPostingMapper.selectList(new LambdaQueryWrapper<JobPosting>()
                .eq(JobPosting::getOrgId, orgId));
        if (jobs.isEmpty()) {
            return List.of();
        }

        Map<Long, String> jobTitleMap = jobs.stream()
                .collect(Collectors.toMap(JobPosting::getId, JobPosting::getTitle));
        List<Long> jobIds = jobs.stream().map(JobPosting::getId).toList();

        List<JobApplication> applications = jobApplicationMapper.selectList(
                new LambdaQueryWrapper<JobApplication>()
                        .in(JobApplication::getJobId, jobIds)
                        .orderByDesc(JobApplication::getCreateTime));
        if (applications.isEmpty()) {
            return List.of();
        }

        markApplicationsViewed(applications);

        List<Long> userIds = applications.stream().map(JobApplication::getUserId).distinct().toList();
        Map<Long, String> applicantNameMap = userMapper.selectList(
                        new LambdaQueryWrapper<SysUser>().in(SysUser::getId, userIds)).stream()
                .collect(Collectors.toMap(
                        SysUser::getId,
                        u -> StringUtils.hasText(u.getRealName()) ? u.getRealName() : u.getUsername()
                ));

        List<Long> applicationIds = applications.stream().map(JobApplication::getId).toList();
        Map<Long, Long> pendingInviteMap = interviewInvitationMapper.selectList(
                        new LambdaQueryWrapper<InterviewInvitation>()
                                .in(InterviewInvitation::getApplicationId, applicationIds)
                                .eq(InterviewInvitation::getStatus, INVITE_PENDING))
                .stream()
                .filter(item -> item.getApplicationId() != null)
                .collect(Collectors.toMap(InterviewInvitation::getApplicationId, InterviewInvitation::getId, (a, b) -> a));

        return applications.stream()
                .filter(app -> jobTitleMap.containsKey(app.getJobId()))
                .map(app -> ApplicationManageVO.builder()
                        .id(app.getId())
                        .jobId(app.getJobId())
                        .jobTitle(jobTitleMap.get(app.getJobId()))
                        .userId(app.getUserId())
                        .applicantName(applicantNameMap.get(app.getUserId()))
                        .coverMessage(app.getCoverMessage())
                        .status(app.getStatus())
                        .statusName(ApplicationStatus.statusName(app.getStatus()))
                        .hasPendingInvite(pendingInviteMap.containsKey(app.getId()))
                        .createTime(app.getCreateTime())
                        .build())
                .toList();
    }

    @Transactional
    public ApplicationManageVO hireApplication(Long applicationId) {
        return updateTerminalStatus(applicationId, ApplicationStatus.HIRED);
    }

    @Transactional
    public ApplicationManageVO rejectApplication(Long applicationId) {
        return updateTerminalStatus(applicationId, ApplicationStatus.REJECTED);
    }

    private ApplicationManageVO updateTerminalStatus(Long applicationId, int targetStatus) {
        SysUser user = authSupport.requireEnterpriseWritable();
        Long orgId = user.getOrgId();
        JobApplication application = authSupport.requireOrgApplication(applicationId, orgId);
        if (ApplicationStatus.isTerminal(application.getStatus())) {
            throw new BusinessException(400, "该投递已处理，无法重复操作");
        }
        if (!ApplicationStatus.canEnterInterview(application.getStatus())) {
            throw new BusinessException(400, "仅面试中的投递可标记录用或拒绝");
        }

        JobPosting job = jobPostingMapper.selectById(application.getJobId());
        if (job == null || !orgId.equals(job.getOrgId())) {
            throw new BusinessException(404, "职位不存在");
        }
        SysOrganization org = orgMapper.selectById(orgId);

        application.setStatus(targetStatus);
        jobApplicationMapper.updateById(application);
        notifyApplicant(user, application, job, org, targetStatus);
        return toManageVO(application, orgId);
    }

    private void notifyApplicant(SysUser sender, JobApplication application, JobPosting job,
                                 SysOrganization org, int targetStatus) {
        String orgName = org != null ? org.getName() : "企业";
        String jobTitle = job.getTitle();
        String title;
        String content;
        if (targetStatus == ApplicationStatus.HIRED) {
            title = "录用通知：" + jobTitle;
            content = "恭喜！" + orgName + " 已录用您对「" + jobTitle + "」的投递。\n\n"
                    + "请在「投递记录」查看详情。";
        } else {
            title = "投递结果：" + jobTitle;
            content = orgName + " 暂未录用您对「" + jobTitle + "」的投递。\n\n"
                    + "请在「投递记录」查看详情。";
        }

        UserMessage message = new UserMessage();
        message.setFromUserId(sender.getId());
        message.setToUserId(application.getUserId());
        message.setMsgType(MSG_TYPE_NORMAL);
        message.setTitle(title);
        message.setContent(content);
        message.setRefType(REF_TYPE_APPLICATION);
        message.setRefId(application.getId());
        message.setReadStatus(UNREAD);
        userMessageMapper.insert(message);
    }

    private void markApplicationsViewed(List<JobApplication> applications) {
        for (JobApplication application : applications) {
            if (Objects.equals(application.getStatus(), ApplicationStatus.APPLIED)) {
                application.setStatus(ApplicationStatus.VIEWED);
                jobApplicationMapper.updateById(application);
            }
        }
    }

    private ApplicationManageVO toManageVO(JobApplication app, Long orgId) {
        JobPosting job = jobPostingMapper.selectById(app.getJobId());
        if (job == null || !orgId.equals(job.getOrgId())) {
            throw new BusinessException(404, "职位不存在");
        }
        SysUser applicant = userMapper.selectById(app.getUserId());
        String applicantName = applicant == null
                ? null
                : (StringUtils.hasText(applicant.getRealName()) ? applicant.getRealName() : applicant.getUsername());
        boolean hasPendingInvite = interviewInvitationMapper.selectCount(
                new LambdaQueryWrapper<InterviewInvitation>()
                        .eq(InterviewInvitation::getApplicationId, app.getId())
                        .eq(InterviewInvitation::getStatus, INVITE_PENDING)) > 0;
        return ApplicationManageVO.builder()
                .id(app.getId())
                .jobId(app.getJobId())
                .jobTitle(job.getTitle())
                .userId(app.getUserId())
                .applicantName(applicantName)
                .coverMessage(app.getCoverMessage())
                .status(app.getStatus())
                .statusName(ApplicationStatus.statusName(app.getStatus()))
                .hasPendingInvite(hasPendingInvite)
                .createTime(app.getCreateTime())
                .build();
    }
}
