package com.creditbank.platform.constant;

public final class UserRole {

    public static final int STUDENT = 0;
    public static final int ENTERPRISE = 1;
    public static final int ADMIN = 2;

    private UserRole() {
    }

    public static String getName(int role) {
        return switch (role) {
            case STUDENT -> "学员";
            case ENTERPRISE -> "企业用户";
            case ADMIN -> "系统管理员";
            default -> "未知";
        };
    }
}
