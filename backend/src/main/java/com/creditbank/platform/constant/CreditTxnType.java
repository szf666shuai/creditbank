package com.creditbank.platform.constant;

/**
 * 学分流水类型：1获取 2转换 3增长 4消耗
 */
public final class CreditTxnType {

    public static final int EARN = 1;
    public static final int CONVERT = 2;
    public static final int BONUS = 3;
    public static final int SPEND = 4;

    private CreditTxnType() {
    }
}
