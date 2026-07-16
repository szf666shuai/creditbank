package com.creditbank.platform.module.enterprise.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class EnterpriseCourseVO {

    private Long id;
    private String title;
    private String description;
    private String coverUrl;
    private BigDecimal creditValue;
    private BigDecimal creditReward;
    private String tags;
    private String videoUrl;
    private Integer videoDurationSeconds;
    private Integer status;
    private String statusName;
    private Integer approvalStatus;
    private String approvalStatusName;
    private String reviewRemark;
    private Integer difficulty;
    private String difficultyName;
    private Integer durationMinutes;
    private Integer duration;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}