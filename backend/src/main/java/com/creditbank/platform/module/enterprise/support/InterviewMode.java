package com.creditbank.platform.module.enterprise.support;

public final class InterviewMode {

    public static final int ONSITE = 0;
    public static final int VIDEO = 1;

    private InterviewMode() {
    }

    public static String modeName(Integer mode) {
        if (mode == null) {
            return "现场面试";
        }
        return mode == VIDEO ? "视频面试" : "现场面试";
    }
}
