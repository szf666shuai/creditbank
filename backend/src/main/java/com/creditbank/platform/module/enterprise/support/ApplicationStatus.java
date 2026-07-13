package com.creditbank.platform.module.enterprise.support;

public final class ApplicationStatus {

    public static final int APPLIED = 0;
    public static final int VIEWED = 1;
    public static final int INTERVIEW = 2;
    public static final int HIRED = 3;
    public static final int REJECTED = 4;
    public static final int INTERVIEW_CANCELLED = 5;

    public static final String UNAVAILABLE_JOB_TITLE = "职位已下架";

    private ApplicationStatus() {
    }

    public static String statusName(Integer status) {
        if (status == null) {
            return "未知";
        }
        return switch (status) {
            case APPLIED -> "已投递";
            case VIEWED -> "已查看";
            case INTERVIEW -> "面试中";
            case HIRED -> "录用";
            case REJECTED -> "已拒绝";
            case INTERVIEW_CANCELLED -> "面试取消";
            default -> "未知";
        };
    }

    public static boolean isTerminal(Integer status) {
        return status != null && (status == HIRED || status == REJECTED);
    }

    public static boolean canEnterInterview(Integer status) {
        return status != null && status == INTERVIEW;
    }
}
