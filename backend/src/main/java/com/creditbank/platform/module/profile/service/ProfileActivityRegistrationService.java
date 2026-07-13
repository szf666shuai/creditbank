package com.creditbank.platform.module.profile.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.creditbank.platform.entity.SysOrganization;
import com.creditbank.platform.entity.SysUser;
import com.creditbank.platform.mapper.SysOrganizationMapper;
import com.creditbank.platform.module.enterprise.service.ActivityLifecycleService;
import com.creditbank.platform.module.enterprise.support.ActivityStatus;
import com.creditbank.platform.module.enterprise.entity.Activity;
import com.creditbank.platform.module.enterprise.entity.ActivityRegistration;
import com.creditbank.platform.module.enterprise.mapper.ActivityMapper;
import com.creditbank.platform.module.enterprise.mapper.ActivityRegistrationMapper;
import com.creditbank.platform.module.profile.dto.MyActivityRegistrationVO;
import com.creditbank.platform.security.AuthSupport;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProfileActivityRegistrationService {

    private static final int REG_REGISTERED = 0;
    private static final int REG_CHECKED_IN = 1;
    private static final int REG_CANCELLED = 2;

    private final AuthSupport authSupport;
    private final ActivityRegistrationMapper activityRegistrationMapper;
    private final ActivityMapper activityMapper;
    private final SysOrganizationMapper orgMapper;
    private final ActivityLifecycleService activityLifecycleService;

    public List<MyActivityRegistrationVO> listMyRegistrations() {
        SysUser user = authSupport.requireStudent();
        List<ActivityRegistration> registrations = activityRegistrationMapper.selectList(
                new LambdaQueryWrapper<ActivityRegistration>()
                        .eq(ActivityRegistration::getUserId, user.getId())
                        .in(ActivityRegistration::getStatus, REG_REGISTERED, REG_CHECKED_IN)
                        .orderByDesc(ActivityRegistration::getCreateTime));
        if (registrations.isEmpty()) {
            return List.of();
        }

        List<Long> activityIds = registrations.stream()
                .map(ActivityRegistration::getActivityId)
                .distinct()
                .toList();
        Map<Long, Activity> activityMap = activityLifecycleService.refreshAll(
                        activityMapper.selectList(
                                new LambdaQueryWrapper<Activity>().in(Activity::getId, activityIds)))
                .stream()
                .collect(Collectors.toMap(Activity::getId, Function.identity()));

        List<Long> orgIds = activityMap.values().stream()
                .map(Activity::getOrgId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        Map<Long, String> orgNameMap = orgIds.isEmpty()
                ? Map.of()
                : orgMapper.selectList(new LambdaQueryWrapper<SysOrganization>().in(SysOrganization::getId, orgIds))
                        .stream()
                        .collect(Collectors.toMap(SysOrganization::getId, SysOrganization::getName));

        return registrations.stream()
                .map(item -> toVO(item, activityMap.get(item.getActivityId()), orgNameMap))
                .filter(Objects::nonNull)
                .toList();
    }

    private MyActivityRegistrationVO toVO(
            ActivityRegistration registration, Activity activity, Map<Long, String> orgNameMap) {
        if (activity == null) {
            return null;
        }
        return MyActivityRegistrationVO.builder()
                .id(registration.getId())
                .activityId(activity.getId())
                .activityTitle(activity.getTitle())
                .orgId(activity.getOrgId())
                .orgName(orgNameMap.get(activity.getOrgId()))
                .startTime(activity.getStartTime())
                .endTime(activity.getEndTime())
                .location(activity.getLocation())
                .creditReward(activity.getCreditReward())
                .activityStatus(activity.getStatus())
                .activityStatusName(ActivityStatus.label(activity.getStatus()))
                .status(registration.getStatus())
                .statusName(registrationStatusName(registration.getStatus()))
                .createTime(registration.getCreateTime())
                .build();
    }

    static String registrationStatusName(Integer status) {
        if (status == null) {
            return "未知";
        }
        return switch (status) {
            case REG_REGISTERED -> "已报名";
            case REG_CHECKED_IN -> "已签到";
            case REG_CANCELLED -> "已取消";
            default -> "未知";
        };
    }
}
