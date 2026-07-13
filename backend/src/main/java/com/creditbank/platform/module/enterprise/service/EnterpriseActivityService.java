package com.creditbank.platform.module.enterprise.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.creditbank.platform.common.BusinessException;
import com.creditbank.platform.entity.SysUser;
import com.creditbank.platform.module.enterprise.dto.ActivityManageVO;
import com.creditbank.platform.module.enterprise.dto.ActivitySaveRequest;
import com.creditbank.platform.module.enterprise.entity.Activity;
import com.creditbank.platform.module.enterprise.support.ActivityStatus;
import com.creditbank.platform.module.enterprise.mapper.ActivityMapper;
import com.creditbank.platform.security.AuthSupport;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EnterpriseActivityService {

    private final AuthSupport authSupport;
    private final ActivityMapper activityMapper;
    private final ActivityLifecycleService activityLifecycleService;

    public List<ActivityManageVO> listMyActivities() {
        SysUser user = authSupport.requireEnterprise();
        List<Activity> activities = activityMapper.selectList(new LambdaQueryWrapper<Activity>()
                .eq(Activity::getOrgId, user.getOrgId())
                .orderByDesc(Activity::getStartTime));
        return activityLifecycleService.refreshAll(activities).stream().map(this::toManageVO).toList();
    }

    public ActivityManageVO getMyActivity(Long activityId) {
        SysUser user = authSupport.requireEnterprise();
        Activity activity = activityLifecycleService.refresh(
                authSupport.requireOrgActivity(activityId, user.getOrgId()));
        return toManageVO(activity);
    }

    @Transactional
    public ActivityManageVO createActivity(ActivitySaveRequest request) {
        SysUser user = authSupport.requireEnterpriseWritable();
        validateTimeRange(request);

        Activity activity = new Activity();
        activity.setOrgId(user.getOrgId());
        activity.setPublisherId(user.getId());
        applyRequest(activity, request);
        activity.setStatus(ActivityStatus.OPEN);
        activityMapper.insert(activity);
        return toManageVO(activityLifecycleService.refresh(activity));
    }

    @Transactional
    public ActivityManageVO updateActivity(Long activityId, ActivitySaveRequest request) {
        SysUser user = authSupport.requireEnterpriseWritable();
        validateTimeRange(request);
        Activity activity = activityLifecycleService.refresh(
                authSupport.requireOrgActivity(activityId, user.getOrgId()));
        if (activity.getStatus() != null && activity.getStatus() == ActivityStatus.CANCELLED) {
            throw new BusinessException(400, "已取消的活动不可编辑");
        }

        applyRequest(activity, request);
        activityMapper.updateById(activity);
        return toManageVO(activityLifecycleService.refresh(activity));
    }

    @Transactional
    public ActivityManageVO cancelActivity(Long activityId) {
        SysUser user = authSupport.requireEnterpriseWritable();
        Activity activity = activityLifecycleService.refresh(
                authSupport.requireOrgActivity(activityId, user.getOrgId()));
        if (activity.getStatus() != null && activity.getStatus() == ActivityStatus.CANCELLED) {
            throw new BusinessException(400, "活动已取消");
        }
        activity.setStatus(ActivityStatus.CANCELLED);
        activityMapper.updateById(activity);
        return toManageVO(activity);
    }

    private void validateTimeRange(ActivitySaveRequest request) {
        if (request.getStartTime() != null && request.getEndTime() != null
                && !request.getEndTime().isAfter(request.getStartTime())) {
            throw new BusinessException(400, "结束时间必须晚于开始时间");
        }
    }

    private void applyRequest(Activity activity, ActivitySaveRequest request) {
        activity.setTitle(request.getTitle().trim());
        activity.setDescription(request.getDescription());
        activity.setLocation(request.getLocation());
        activity.setStartTime(request.getStartTime());
        activity.setEndTime(request.getEndTime());
        activity.setMaxParticipants(request.getMaxParticipants());
        activity.setCreditReward(request.getCreditReward() != null ? request.getCreditReward() : BigDecimal.ZERO);
    }

    private ActivityManageVO toManageVO(Activity activity) {
        return ActivityManageVO.builder()
                .id(activity.getId())
                .orgId(activity.getOrgId())
                .title(activity.getTitle())
                .description(activity.getDescription())
                .location(activity.getLocation())
                .startTime(activity.getStartTime())
                .endTime(activity.getEndTime())
                .maxParticipants(activity.getMaxParticipants())
                .creditReward(activity.getCreditReward())
                .status(activity.getStatus())
                .statusName(ActivityStatus.label(activity.getStatus()))
                .createTime(activity.getCreateTime())
                .updateTime(activity.getUpdateTime())
                .build();
    }
}
