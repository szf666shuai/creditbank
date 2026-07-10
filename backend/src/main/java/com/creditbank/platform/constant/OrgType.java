package com.creditbank.platform.constant;

public final class OrgType {

    public static final int UNIVERSITY = 1;
    public static final int TRAINING = 2;
    public static final int ENTERPRISE = 3;
    public static final int OTHER = 4;

    private OrgType() {
    }

    public static String label(Integer type) {
        if (type == null) {
            return "未知";
        }
        return switch (type) {
            case UNIVERSITY -> "高校";
            case TRAINING -> "培训机构";
            case ENTERPRISE -> "企业";
            case OTHER -> "其他";
            default -> "未知";
        };
    }
}
