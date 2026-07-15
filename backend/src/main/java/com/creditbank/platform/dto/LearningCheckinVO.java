package com.creditbank.platform.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class LearningCheckinVO {

    private Long courseId;
    private Boolean checkedInToday;
    private Integer streakDays;
    private Integer integrityReward;
    private String message;
}
