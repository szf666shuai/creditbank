package com.creditbank.platform.module.enterprise.support;

import com.creditbank.platform.module.enterprise.entity.Activity;

import java.time.LocalDateTime;
import java.util.Objects;

public final class ActivityStatus {

    public static final int CANCELLED = 0;
    public static final int OPEN = 1;
    public static final int ONGOING = 2;
    public static final int ENDED = 3;

    private ActivityStatus() {
    }

    public static int resolve(Activity activity) {
        return resolve(activity, LocalDateTime.now());
    }

    public static int resolve(Activity activity, LocalDateTime now) {
        if (activity == null) {
            return OPEN;
        }
        if (Objects.equals(activity.getStatus(), CANCELLED)) {
            return CANCELLED;
        }
        LocalDateTime end = activity.getEndTime();
        if (end != null && now.isAfter(end)) {
            return ENDED;
        }
        LocalDateTime start = activity.getStartTime();
        if (start != null && !now.isBefore(start)) {
            return ONGOING;
        }
        return OPEN;
    }

    public static String label(Integer status) {
        if (status == null) {
            return "未知";
        }
        return switch (status) {
            case CANCELLED -> "已取消";
            case OPEN -> "报名中";
            case ONGOING -> "进行中";
            case ENDED -> "已结束";
            default -> "未知";
        };
    }
}
