package com.creditbank.platform.module.admin.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.creditbank.platform.constant.UserRole;
import com.creditbank.platform.entity.ForumReport;
import com.creditbank.platform.entity.SysOrganization;
import com.creditbank.platform.entity.SysUser;
import com.creditbank.platform.mapper.ForumReportMapper;
import com.creditbank.platform.mapper.SysOrganizationMapper;
import com.creditbank.platform.mapper.SysUserMapper;
import com.creditbank.platform.module.admin.dto.AdminDashboardStatsVO;
import com.creditbank.platform.module.enterprise.entity.Activity;
import com.creditbank.platform.module.enterprise.entity.JobPosting;
import com.creditbank.platform.module.enterprise.mapper.ActivityMapper;
import com.creditbank.platform.module.enterprise.mapper.JobPostingMapper;
import com.creditbank.platform.module.message.entity.UserMessage;
import com.creditbank.platform.module.message.mapper.UserMessageMapper;
import com.creditbank.platform.security.AuthSupport;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminDashboardService {

    private static final int UNREAD = 0;

    private final AuthSupport authSupport;
    private final SysUserMapper sysUserMapper;
    private final SysOrganizationMapper sysOrganizationMapper;
    private final ForumReportMapper forumReportMapper;
    private final JobPostingMapper jobPostingMapper;
    private final ActivityMapper activityMapper;
    private final UserMessageMapper userMessageMapper;

    public AdminDashboardStatsVO getStats() {
        authSupport.requireAdmin();
        Long userId = authSupport.requireUserId();

        long totalUsers = countUsers(null);
        long studentCount = countUsers(UserRole.STUDENT);
        long enterpriseCount = countUsers(UserRole.ENTERPRISE);
        long adminCount = countUsers(UserRole.ADMIN);

        long totalOrganizations = countOrgs(null, null);
        long pendingOrganizations = countOrgs(AdminSupport.JOIN_PENDING, null);
        long activeOrganizations = countOrgs(AdminSupport.JOIN_APPROVED, 1);

        long pendingReports = countReports(AdminSupport.REPORT_PENDING);
        long totalJobs = countJobs(null);
        long activeJobs = countJobs(1);
        long totalActivities = countActivities(null);
        long activeActivities = countActivities(1);

        Long unread = userMessageMapper.selectCount(new LambdaQueryWrapper<UserMessage>()
                .eq(UserMessage::getToUserId, userId)
                .eq(UserMessage::getReadStatus, UNREAD));

        return AdminDashboardStatsVO.builder()
                .totalUsers(totalUsers)
                .studentCount(studentCount)
                .enterpriseCount(enterpriseCount)
                .adminCount(adminCount)
                .totalOrganizations(totalOrganizations)
                .pendingOrganizations(pendingOrganizations)
                .activeOrganizations(activeOrganizations)
                .pendingReports(pendingReports)
                .totalJobs(totalJobs)
                .activeJobs(activeJobs)
                .totalActivities(totalActivities)
                .activeActivities(activeActivities)
                .unreadMessages(unread != null ? unread : 0)
                .build();
    }

    private long countUsers(Integer role) {
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        if (role != null) {
            wrapper.eq(SysUser::getRole, role);
        }
        Long count = sysUserMapper.selectCount(wrapper);
        return count != null ? count : 0;
    }

    private long countOrgs(Integer joinStatus, Integer status) {
        LambdaQueryWrapper<SysOrganization> wrapper = new LambdaQueryWrapper<>();
        if (joinStatus != null) {
            wrapper.eq(SysOrganization::getJoinStatus, joinStatus);
        }
        if (status != null) {
            wrapper.eq(SysOrganization::getStatus, status);
        }
        Long count = sysOrganizationMapper.selectCount(wrapper);
        return count != null ? count : 0;
    }

    private long countReports(Integer status) {
        LambdaQueryWrapper<ForumReport> wrapper = new LambdaQueryWrapper<>();
        if (status != null) {
            wrapper.eq(ForumReport::getStatus, status);
        }
        Long count = forumReportMapper.selectCount(wrapper);
        return count != null ? count : 0;
    }

    private long countJobs(Integer status) {
        LambdaQueryWrapper<JobPosting> wrapper = new LambdaQueryWrapper<>();
        if (status != null) {
            wrapper.eq(JobPosting::getStatus, status);
        }
        Long count = jobPostingMapper.selectCount(wrapper);
        return count != null ? count : 0;
    }

    private long countActivities(Integer status) {
        LambdaQueryWrapper<Activity> wrapper = new LambdaQueryWrapper<>();
        if (status != null) {
            wrapper.eq(Activity::getStatus, status);
        }
        Long count = activityMapper.selectCount(wrapper);
        return count != null ? count : 0;
    }
}
