package com.creditbank.platform.module.enterprise.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class ActivitySaveRequest {

    @NotBlank(message = "活动名称不能为空")
    private String title;

    private String description;
    private String location;

    @NotNull(message = "开始时间不能为空")
    private LocalDateTime startTime;

    @NotNull(message = "结束时间不能为空")
    private LocalDateTime endTime;

    private Integer maxParticipants;
    private BigDecimal creditReward;
}
