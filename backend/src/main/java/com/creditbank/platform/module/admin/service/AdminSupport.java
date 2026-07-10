package com.creditbank.platform.module.admin.service;

import com.creditbank.platform.common.BusinessException;

public final class AdminSupport {

    public static final int JOIN_PENDING = 0;
    public static final int JOIN_APPROVED = 1;
    public static final int JOIN_EXITED = 2;

    public static final int REPORT_PENDING = 0;
    public static final int REPORT_HANDLED = 1;
    public static final int REPORT_REJECTED = 2;

    public static final int TARGET_POST = 1;
    public static final int TARGET_REPLY = 2;

    public static final int MSG_TYPE_SYSTEM = 4;

    private AdminSupport() {
    }

    public static void validatePage(long page, long pageSize) {
        if (page < 1 || pageSize < 1 || pageSize > 100) {
            throw new BusinessException(400, "分页参数无效");
        }
    }

    public static String joinStatusName(Integer status) {
        if (status == null) {
            return "未知";
        }
        return switch (status) {
            case JOIN_PENDING -> "待审核";
            case JOIN_APPROVED -> "已加盟";
            case JOIN_EXITED -> "已退出";
            default -> "未知";
        };
    }

    public static String orgStatusName(Integer status) {
        if (status == null || status == 1) {
            return "正常";
        }
        return status == 0 ? "停用" : "未知";
    }

    public static String userStatusName(Integer status) {
        if (status == null || status == 1) {
            return "正常";
        }
        return status == 0 ? "禁用" : "未知";
    }

    public static String reportStatusName(Integer status) {
        if (status == null) {
            return "未知";
        }
        return switch (status) {
            case REPORT_PENDING -> "待处理";
            case REPORT_HANDLED -> "已处理";
            case REPORT_REJECTED -> "已驳回";
            default -> "未知";
        };
    }

    public static String targetTypeName(Integer type) {
        if (type == null) {
            return "未知";
        }
        return type == TARGET_POST ? "帖子" : type == TARGET_REPLY ? "回复" : "未知";
    }

    public static String jobStatusName(Integer status) {
        if (status == null || status == 1) {
            return "招聘中";
        }
        return status == 0 ? "已下架" : "未知";
    }

    public static String activityStatusName(Integer status) {
        if (status == null || status == 1) {
            return "进行中";
        }
        return status == 0 ? "已下架" : "未知";
    }
}
