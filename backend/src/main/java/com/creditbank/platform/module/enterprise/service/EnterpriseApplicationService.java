package com.creditbank.platform.module.enterprise.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.creditbank.platform.common.BusinessException;
import com.creditbank.platform.entity.SysUser;
import com.creditbank.platform.mapper.SysUserMapper;
import com.creditbank.platform.module.enterprise.dto.ApplicationManageVO;
import com.creditbank.platform.module.enterprise.entity.InterviewInvitation;
import com.creditbank.platform.module.enterprise.entity.JobApplication;
import com.creditbank.platform.module.enterprise.entity.JobPosting;
import com.creditbank.platform.module.enterprise.mapper.InterviewInvitationMapper;
import com.creditbank.platform.module.enterprise.mapper.JobApplicationMapper;
import com.creditbank.platform.module.enterprise.mapper.JobPostingMapper;
import com.creditbank.platform.security.AuthSupport;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EnterpriseApplicationService {

    private static final int APP_STATUS_APPLIED = 0;
    private static final int APP_STATUS_VIEWED = 1;
    private static final int APP_STATUS_INTERVIEW = 2;
    private static final int APP_STATUS_HIRED = 3;
    private static final int APP_STATUS_REJECTED = 4;
    private static final int INVITE_PENDING = 0;

    private final AuthSupport authSupport;
    private final SysUserMapper userMapper;
    private final JobPostingMapper jobPostingMapper;
    private final JobApplicationMapper jobApplicationMapper;
    private final InterviewInvitationMapper interviewInvitationMapper;

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
                .map(app -> ApplicationManageVO.builder()
                        .id(app.getId())
                        .jobId(app.getJobId())
                        .jobTitle(jobTitleMap.get(app.getJobId()))
                        .userId(app.getUserId())
                        .applicantName(applicantNameMap.get(app.getUserId()))
                        .coverMessage(app.getCoverMessage())
                        .status(app.getStatus())
                        .statusName(applicationStatusName(app.getStatus()))
                        .hasPendingInvite(pendingInviteMap.containsKey(app.getId()))
                        .createTime(app.getCreateTime())
                        .build())
                .toList();
    }

    private String applicationStatusName(Integer status) {
        if (status == null) {
            return "未知";
        }
        return switch (status) {
            case APP_STATUS_APPLIED -> "已投递";
            case APP_STATUS_VIEWED -> "已查看";
            case APP_STATUS_INTERVIEW -> "面试中";
            case APP_STATUS_HIRED -> "录用";
            case APP_STATUS_REJECTED -> "已拒绝";
            default -> "未知";
        };
    }
}
