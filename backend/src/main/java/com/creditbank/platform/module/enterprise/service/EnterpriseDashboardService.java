package com.creditbank.platform.module.enterprise.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.creditbank.platform.common.BusinessException;
import com.creditbank.platform.constant.UserRole;
import com.creditbank.platform.context.UserContext;
import com.creditbank.platform.entity.SysOrganization;
import com.creditbank.platform.entity.SysUser;
import com.creditbank.platform.mapper.SysOrganizationMapper;
import com.creditbank.platform.mapper.SysUserMapper;
import com.creditbank.platform.module.admin.service.AdminSupport;
import com.creditbank.platform.module.enterprise.dto.EnterpriseDashboardVO;
import com.creditbank.platform.module.enterprise.entity.Activity;
import com.creditbank.platform.module.enterprise.entity.InterviewInvitation;
import com.creditbank.platform.module.enterprise.entity.JobPosting;
import com.creditbank.platform.module.enterprise.entity.OrgMaterial;
import com.creditbank.platform.module.enterprise.mapper.ActivityMapper;
import com.creditbank.platform.module.enterprise.mapper.InterviewInvitationMapper;
import com.creditbank.platform.module.enterprise.mapper.JobApplicationMapper;
import com.creditbank.platform.module.enterprise.mapper.JobPostingMapper;
import com.creditbank.platform.module.enterprise.mapper.OrgMaterialMapper;
import com.creditbank.platform.module.enterprise.support.ActivityStatus;
import com.creditbank.platform.security.AuthSupport;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EnterpriseDashboardService {

    private static final int JOB_OPEN = 1;
    private static final int ACTIVITY_REGISTERING = 1;
    private static final int ACTIVITY_ONGOING = 2;
    private static final int MATERIAL_ACTIVE = 1;
    private static final int INVITE_PENDING = 0;

    private final AuthSupport authSupport;
    private final SysUserMapper userMapper;
    private final SysOrganizationMapper orgMapper;
    private final JobPostingMapper jobPostingMapper;
    private final ActivityMapper activityMapper;
    private final JobApplicationMapper jobApplicationMapper;
    private final InterviewInvitationMapper interviewInvitationMapper;
    private final OrgMaterialMapper orgMaterialMapper;
    private final ActivityLifecycleService activityLifecycleService;

    public EnterpriseDashboardVO getDashboard() {
        SysUser user = authSupport.requireEnterprise();
        Long orgId = user.getOrgId();

        SysOrganization org = orgMapper.selectById(orgId);
        String orgName = org != null ? org.getName() : null;
        Integer joinStatus = org != null ? org.getJoinStatus() : null;
        boolean writable = org != null
                && joinStatus != null && joinStatus == 1
                && (org.getStatus() == null || org.getStatus() == 1);

        Long pendingApplications = jobApplicationMapper.countPendingByOrgId(orgId);

        return EnterpriseDashboardVO.builder()
                .orgId(orgId)
                .orgName(orgName)
                .joinStatus(joinStatus)
                .joinStatusName(AdminSupport.joinStatusName(joinStatus))
                .writable(writable)
                .openJobCount(countJobs(orgId))
                .ongoingActivityCount(countActivities(orgId, ACTIVITY_ONGOING))
                .registeringActivityCount(countActivities(orgId, ACTIVITY_REGISTERING))
                .pendingApplicationCount(pendingApplications != null ? pendingApplications : 0)
                .pendingInterviewCount(countPendingInterviews(orgId))
                .materialCount(countMaterials(orgId))
                .build();
    }

    private long countJobs(Long orgId) {
        Long count = jobPostingMapper.selectCount(new LambdaQueryWrapper<JobPosting>()
                .eq(JobPosting::getOrgId, orgId)
                .eq(JobPosting::getStatus, JOB_OPEN));
        return count != null ? count : 0;
    }

    private long countActivities(Long orgId, int status) {
        List<Activity> activities = activityMapper.selectList(new LambdaQueryWrapper<Activity>()
                .eq(Activity::getOrgId, orgId)
                .ne(Activity::getStatus, ActivityStatus.CANCELLED));
        return activityLifecycleService.refreshAll(activities).stream()
                .filter(item -> item.getStatus() != null && item.getStatus() == status)
                .count();
    }

    private long countPendingInterviews(Long orgId) {
        Long count = interviewInvitationMapper.selectCount(new LambdaQueryWrapper<InterviewInvitation>()
                .eq(InterviewInvitation::getOrgId, orgId)
                .eq(InterviewInvitation::getStatus, INVITE_PENDING));
        return count != null ? count : 0;
    }

    private long countMaterials(Long orgId) {
        Long count = orgMaterialMapper.selectCount(new LambdaQueryWrapper<OrgMaterial>()
                .eq(OrgMaterial::getOrgId, orgId)
                .eq(OrgMaterial::getStatus, MATERIAL_ACTIVE));
        return count != null ? count : 0;
    }
}
