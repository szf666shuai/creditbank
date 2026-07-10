package com.creditbank.platform.module.profile.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class IntegritySummaryVO {

    private Integer score;
    private String levelName;
    private LocalDateTime updateTime;
}
