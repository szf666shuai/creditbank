package com.creditbank.platform.module.profile.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class MyActivityRegistrationVO {

    private Long id;
    private Long activityId;
    private String activityTitle;
    private Long orgId;
    private String orgName;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String location;
    private BigDecimal creditReward;
    private Integer activityStatus;
    private String activityStatusName;
    private Integer status;
    private String statusName;
    private LocalDateTime createTime;
}
