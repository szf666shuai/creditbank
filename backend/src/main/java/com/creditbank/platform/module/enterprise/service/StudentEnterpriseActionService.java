package com.creditbank.platform.module.enterprise.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.creditbank.platform.common.BusinessException;
import com.creditbank.platform.dto.IntegrityScoreVO;
import com.creditbank.platform.entity.SysOrganization;
import com.creditbank.platform.entity.SysUser;
import com.creditbank.platform.entity.UserResume;
import com.creditbank.platform.mapper.SysOrganizationMapper;
import com.creditbank.platform.mapper.SysUserMapper;
import com.creditbank.platform.module.enterprise.dto.ActivityCheckinResultVO;
import com.creditbank.platform.module.enterprise.dto.ActivityRegisterResultVO;
import com.creditbank.platform.module.enterprise.dto.ApplyJobRequest;
import com.creditbank.platform.module.enterprise.dto.JobApplyResultVO;
import com.creditbank.platform.module.enterprise.dto.OrgParticipationStatusVO;
import com.creditbank.platform.module.enterprise.entity.Activity;
import com.creditbank.platform.module.enterprise.support.ActivityStatus;
import com.creditbank.platform.module.enterprise.entity.ActivityRegistration;
import com.creditbank.platform.module.enterprise.entity.JobApplication;
import com.creditbank.platform.module.enterprise.entity.JobPosting;
import com.creditbank.platform.module.enterprise.mapper.ActivityMapper;
import com.creditbank.platform.module.enterprise.mapper.ActivityRegistrationMapper;
import com.creditbank.platform.module.enterprise.mapper.JobApplicationMapper;
import com.creditbank.platform.module.enterprise.mapper.JobPostingMapper;
import com.creditbank.platform.module.profile.service.ProfileResumeService;
import com.creditbank.platform.security.AuthSupport;
import com.creditbank.platform.service.IntegrityService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StudentEnterpriseActionService {

    private static final int JOB_OPEN = 1;
    private static final int APP_STATUS_APPLIED = 0;
    private static final int ACTIVITY_CANCELLED = ActivityStatus.CANCELLED;
    private static final int ACTIVITY_OPEN = ActivityStatus.OPEN;
    private static final int ACTIVITY_ONGOING = ActivityStatus.ONGOING;
    private static final int ACTIVITY_ENDED = ActivityStatus.ENDED;
    private static final int REG_REGISTERED = 0;
    private static final int REG_CHECKED_IN = 1;

    private final AuthSupport authSupport;
    private final ProfileResumeService profileResumeService;
    private final SysUserMapper userMapper;
    private final SysOrganizationMapper orgMapper;
    private final JobPostingMapper jobPostingMapper;
    private final JobApplicationMapper jobApplicationMapper;
    private final ActivityMapper activityMapper;
    private final ActivityRegistrationMapper activityRegistrationMapper;
    private final IntegrityService integrityService;
    private final ActivityLifecycleService activityLifecycleService;

    public OrgParticipationStatusVO getOrgParticipationStatus(Long orgId) {
        SysUser user = authSupport.requireStudent();
        validateJoinedOrg(orgId);

        List<Long> jobIds = jobPostingMapper.selectList(new LambdaQueryWrapper<JobPosting>()
                        .eq(JobPosting::getOrgId, orgId)
                        .select(JobPosting::getId))
                .stream()
                .map(JobPosting::getId)
                .toList();

        List<Long> activityIds = activityMapper.selectList(new LambdaQueryWrapper<Activity>()
                        .eq(Activity::getOrgId, orgId)
                        .select(Activity::getId))
                .stream()
                .map(Activity::getId)
                .toList();

        List<Long> appliedJobIds = jobIds.isEmpty()
                ? List.of()
                : jobApplicationMapper.selectList(new LambdaQueryWrapper<JobApplication>()
                                .eq(JobApplication::getUserId, user.getId())
                                .in(JobApplication::getJobId, jobIds))
                        .stream()
                        .map(JobApplication::getJobId)
                        .toList();

        List<Long> registeredActivityIds = activityIds.isEmpty()
                ? List.of()
                : activityRegistrationMapper.selectList(new LambdaQueryWrapper<ActivityRegistration>()
                                .eq(ActivityRegistration::getUserId, user.getId())
                                .in(ActivityRegistration::getActivityId, activityIds)
                                .in(ActivityRegistration::getStatus, REG_REGISTERED, REG_CHECKED_IN))
                        .stream()
                        .map(ActivityRegistration::getActivityId)
                        .toList();

        return OrgParticipationStatusVO.builder()
                .appliedJobIds(appliedJobIds)
                .registeredActivityIds(registeredActivityIds)
                .build();
    }

    @Transactional
    public JobApplyResultVO applyJob(Long jobId, ApplyJobRequest request) {
        SysUser user = authSupport.requireStudent();
        JobPosting job = jobPostingMapper.selectById(jobId);
        if (job == null || job.getStatus() == null || job.getStatus() != JOB_OPEN) {
            throw new BusinessException(404, "职位不存在或已下架");
        }
        validateJoinedOrg(job.getOrgId());

        JobApplication existing = jobApplicationMapper.selectOne(new LambdaQueryWrapper<JobApplication>()
                .eq(JobApplication::getJobId, jobId)
                .eq(JobApplication::getUserId, user.getId()));
        if (existing != null) {
            throw new BusinessException(400, "您已投递该职位");
        }

        JobApplication application = new JobApplication();
        application.setJobId(jobId);
        application.setUserId(user.getId());
        application.setResumeId(resolveResumeId(user.getId(), request));
        application.setCoverMessage(StringUtils.hasText(request != null ? request.getCoverMessage() : null)
                ? request.getCoverMessage().trim()
                : null);
        application.setStatus(APP_STATUS_APPLIED);
        jobApplicationMapper.insert(application);

        return JobApplyResultVO.builder()
                .id(application.getId())
                .jobId(jobId)
                .status(application.getStatus())
                .statusName("已投递")
                .createTime(application.getCreateTime())
                .build();
    }

    @Transactional
    public ActivityRegisterResultVO registerActivity(Long activityId) {
        SysUser user = authSupport.requireStudent();
        Activity activity = activityLifecycleService.refreshById(activityId);
        if (activity == null) {
            throw new BusinessException(404, "活动不存在");
        }
        validateJoinedOrg(activity.getOrgId());
        if (activity.getStatus() == null
                || activity.getStatus() == ACTIVITY_CANCELLED
                || activity.getStatus() == ACTIVITY_ENDED
                || (activity.getStatus() != ACTIVITY_OPEN && activity.getStatus() != ACTIVITY_ONGOING)) {
            throw new BusinessException(400, "活动当前不可报名");
        }

        ActivityRegistration existing = activityRegistrationMapper.selectOne(
                new LambdaQueryWrapper<ActivityRegistration>()
                        .eq(ActivityRegistration::getActivityId, activityId)
                        .eq(ActivityRegistration::getUserId, user.getId()));
        if (existing != null && (existing.getStatus() == REG_REGISTERED || existing.getStatus() == REG_CHECKED_IN)) {
            throw new BusinessException(400, "您已报名该活动");
        }

        ensureCapacityAvailable(activity);

        if (existing != null) {
            existing.setStatus(REG_REGISTERED);
            activityRegistrationMapper.updateById(existing);
        } else {
            ActivityRegistration registration = new ActivityRegistration();
            registration.setActivityId(activityId);
            registration.setUserId(user.getId());
            registration.setStatus(REG_REGISTERED);
            activityRegistrationMapper.insert(registration);
        }

        ActivityRegistration saved = activityRegistrationMapper.selectOne(
                new LambdaQueryWrapper<ActivityRegistration>()
                        .eq(ActivityRegistration::getActivityId, activityId)
                        .eq(ActivityRegistration::getUserId, user.getId()));

        return ActivityRegisterResultVO.builder()
                .id(saved != null ? saved.getId() : null)
                .activityId(activityId)
                .status(REG_REGISTERED)
                .statusName("已报名")
                .createTime(saved != null ? saved.getCreateTime() : null)
                .build();
    }

    @Transactional
    public ActivityCheckinResultVO checkInActivity(Long activityId) {
        SysUser user = authSupport.requireStudent();
        Activity activity = activityLifecycleService.refreshById(activityId);
        if (activity == null) {
            throw new BusinessException(404, "活动不存在");
        }
        if (activity.getStatus() == null || activity.getStatus() != ACTIVITY_ONGOING) {
            throw new BusinessException(400, "活动进行中才可签到");
        }
        LocalDateTime now = LocalDateTime.now();
        if (activity.getStartTime() != null && now.isBefore(activity.getStartTime().minusHours(1))) {
            throw new BusinessException(400, "活动尚未开始，暂不可签到");
        }
        if (activity.getEndTime() != null && now.isAfter(activity.getEndTime().plusHours(1))) {
            throw new BusinessException(400, "活动已结束，无法签到");
        }

        ActivityRegistration registration = activityRegistrationMapper.selectOne(
                new LambdaQueryWrapper<ActivityRegistration>()
                        .eq(ActivityRegistration::getActivityId, activityId)
                        .eq(ActivityRegistration::getUserId, user.getId()));
        if (registration == null || registration.getStatus() == null || registration.getStatus() != REG_REGISTERED) {
            throw new BusinessException(400, "请先报名该活动后再签到");
        }
        if (registration.getStatus() == REG_CHECKED_IN) {
            throw new BusinessException(409, "您已签到该活动");
        }

        registration.setStatus(REG_CHECKED_IN);
        registration.setCheckInTime(now);
        activityRegistrationMapper.updateById(registration);

        Boolean integrityGranted = false;
        Integer integrityReward = null;
        String integrityMessage = null;
        try {
            IntegrityScoreVO result = integrityService.applyEvent(user.getId(), 3,
                    "活动签到: " + activity.getTitle(),
                    "activity_registration", registration.getId(), null);
            integrityGranted = true;
            integrityReward = 3;
        } catch (BusinessException ex) {
            integrityMessage = ex.getMessage();
        }

        String message = buildCheckinMessage(integrityGranted, integrityReward, integrityMessage);

        return ActivityCheckinResultVO.builder()
                .registrationId(registration.getId())
                .activityId(activityId)
                .status(REG_CHECKED_IN)
                .statusName("已签到")
                .checkInTime(now)
                .integrityGranted(integrityGranted)
                .integrityReward(integrityReward)
                .integrityMessage(integrityMessage)
                .message(message)
                .build();
    }

    private static String buildCheckinMessage(Boolean integrityGranted, Integer integrityReward, String integrityMessage) {
        if (Boolean.TRUE.equals(integrityGranted) && integrityReward != null) {
            return "签到成功，已获得 " + integrityReward + " 诚信分";
        }
        if (StringUtils.hasText(integrityMessage)) {
            return "签到成功，但诚信分未发放：" + integrityMessage;
        }
        return "签到成功，感谢参与！";
    }

    private Long resolveResumeId(Long userId, ApplyJobRequest request) {
        if (request != null && Boolean.FALSE.equals(request.getAttachResume())) {
            return null;
        }
        if (request != null && request.getResumeId() != null) {
            profileResumeService.getMyResume(request.getResumeId());
            return request.getResumeId();
        }
        UserResume resume = profileResumeService.ensureDefaultResume(userId);
        return resume.getId();
    }

    private void ensureCapacityAvailable(Activity activity) {
        if (activity.getMaxParticipants() == null || activity.getMaxParticipants() <= 0) {
            return;
        }
        Long count = activityRegistrationMapper.selectCount(new LambdaQueryWrapper<ActivityRegistration>()
                .eq(ActivityRegistration::getActivityId, activity.getId())
                .in(ActivityRegistration::getStatus, REG_REGISTERED, REG_CHECKED_IN));
        if (count != null && count >= activity.getMaxParticipants()) {
            throw new BusinessException(400, "活动报名人数已满");
        }
    }

    private void validateJoinedOrg(Long orgId) {
        SysOrganization org = orgMapper.selectOne(new LambdaQueryWrapper<SysOrganization>()
                .eq(SysOrganization::getId, orgId)
                .eq(SysOrganization::getJoinStatus, 1)
                .eq(SysOrganization::getStatus, 1));
        if (org == null) {
            throw new BusinessException(404, "企业不存在或未加盟");
        }
    }
}
