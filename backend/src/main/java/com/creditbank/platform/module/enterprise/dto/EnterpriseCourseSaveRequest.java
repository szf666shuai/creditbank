package com.creditbank.platform.module.enterprise.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class EnterpriseCourseSaveRequest {

    @NotBlank(message = "课程名称不能为空")
    private String title;

    private String description;
    private String coverUrl;

    @DecimalMin(value = "0", message = "学分值不能小于0")
    private BigDecimal creditValue;

    private String tags;
    private String videoUrl;
    private Integer videoDurationSeconds;
    private Integer difficulty;
    private Integer duration;
}