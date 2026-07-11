package com.creditbank.platform.module.enterprise.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ActivityCheckinResultVO {

    private Long registrationId;
    private Long activityId;
    private Integer status;
    private String statusName;
    private LocalDateTime checkInTime;
    private String message;
}
