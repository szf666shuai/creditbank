package com.creditbank.platform.module.enterprise.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.creditbank.platform.module.enterprise.entity.Activity;
import com.creditbank.platform.module.enterprise.mapper.ActivityMapper;
import com.creditbank.platform.module.enterprise.support.ActivityStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class ActivityLifecycleService {

    private final ActivityMapper activityMapper;

    @Transactional
    public Activity refresh(Activity activity) {
        if (activity == null) {
            return null;
        }
        if (Objects.equals(activity.getStatus(), ActivityStatus.CANCELLED)) {
            return activity;
        }
        int effective = ActivityStatus.resolve(activity);
        if (!Objects.equals(activity.getStatus(), effective)) {
            activity.setStatus(effective);
            activityMapper.updateById(activity);
        }
        return activity;
    }

    public Activity refreshById(Long activityId) {
        if (activityId == null) {
            return null;
        }
        return refresh(activityMapper.selectById(activityId));
    }

    public List<Activity> refreshAll(List<Activity> activities) {
        if (activities == null || activities.isEmpty()) {
            return activities;
        }
        return activities.stream().map(this::refresh).toList();
    }

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void syncAllOnStartup() {
        List<Activity> activities = activityMapper.selectList(
                new LambdaQueryWrapper<Activity>()
                        .ne(Activity::getStatus, ActivityStatus.CANCELLED));
        int changed = 0;
        for (Activity activity : activities) {
            int before = activity.getStatus() == null ? -1 : activity.getStatus();
            refresh(activity);
            int after = activity.getStatus() == null ? -1 : activity.getStatus();
            if (before != after) {
                changed++;
            }
        }
        if (changed > 0) {
            log.info("活动状态已按时间自动同步 {} 条", changed);
        }
    }
}
