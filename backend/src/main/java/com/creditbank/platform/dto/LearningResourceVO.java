package com.creditbank.platform.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class LearningResourceVO {

    private Long id;
    private String title;
    private String description;
    private String coverUrl;
    private String videoUrl;
    private Integer videoDurationSeconds;
    private BigDecimal durationHours;
    private BigDecimal creditReward;
    private Long orgId;
    private String orgName;
    private String tags;
    private BigDecimal creditValue;
    private Integer difficulty;
    private Integer durationMinutes;
    private Integer progress;
    private Integer watchedSeconds;
    private Integer maxWatchedPositionSeconds;
    private Integer lastPositionSeconds;
    private Integer learningStatus;
    private Boolean learned;
    private Long certificateId;
    private String certNo;
}
