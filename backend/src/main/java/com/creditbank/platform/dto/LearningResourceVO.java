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
    private BigDecimal priceCredit;
    private BigDecimal priceMoney;
    private BigDecimal durationHours;
    private BigDecimal creditReward;
    private String orgName;
    private String tags;
    private Integer progress;
    private Integer watchedSeconds;
    private Integer maxWatchedPositionSeconds;
    private Integer lastPositionSeconds;
    private Integer learningStatus;
    private Boolean purchased;
    private Boolean paid;
    private Boolean learned;
    private Long purchaseProductId;
    private Long certificateId;
    private String certNo;
}
