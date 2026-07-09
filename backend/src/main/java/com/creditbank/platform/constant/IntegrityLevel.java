package com.creditbank.platform.constant;

/**
 * 诚信等级与倍率
 */
public enum IntegrityLevel {

    EXCELLENT(90, 100, "优秀", 1.20),
    GOOD(80, 89, "良好", 1.00),
    NORMAL(60, 79, "一般", 0.80),
    RISK(0, 59, "风险", 0.50);

    private final int min;
    private final int max;
    private final String label;
    private final double multiplier;

    IntegrityLevel(int min, int max, String label, double multiplier) {
        this.min = min;
        this.max = max;
        this.label = label;
        this.multiplier = multiplier;
    }

    public int getMin() {
        return min;
    }

    public int getMax() {
        return max;
    }

    public String getLabel() {
        return label;
    }

    public double getMultiplier() {
        return multiplier;
    }

    public static IntegrityLevel fromScore(int score) {
        int s = Math.max(0, Math.min(100, score));
        for (IntegrityLevel level : values()) {
            if (s >= level.min && s <= level.max) {
                return level;
            }
        }
        return RISK;
    }
}
