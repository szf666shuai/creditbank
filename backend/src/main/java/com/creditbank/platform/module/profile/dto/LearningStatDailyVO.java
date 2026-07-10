package com.creditbank.platform.module.profile.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
public class LearningStatDailyVO {

    private LocalDate statDate;
    private Integer studyMinutes;
    private Integer coursesCompleted;
    private BigDecimal creditEarned;
}
