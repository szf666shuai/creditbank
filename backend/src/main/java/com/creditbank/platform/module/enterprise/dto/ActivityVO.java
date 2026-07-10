package com.creditbank.platform.module.enterprise.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class ActivityVO {

    private Long id;
    private String title;
    private String description;
    private String location;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer maxParticipants;
    private BigDecimal creditReward;
    private Integer status;
    private String statusName;
    private LocalDateTime createTime;
}
