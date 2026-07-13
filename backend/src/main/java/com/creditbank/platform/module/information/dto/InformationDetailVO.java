package com.creditbank.platform.module.information.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class InformationDetailVO {

    private String type;
    private Long id;
    private Long orgId;
    private String orgName;
    private String title;
    private String content;
    private String description;
    private String requirements;
    private String salaryRange;
    private String location;
    private String eduRequirement;
    private Integer maxParticipants;
    private BigDecimal creditReward;
    private String source;
    private String author;
    private String coverUrl;
    private Integer status;
    private String statusName;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private LocalDateTime publishTime;
    private LocalDateTime createTime;
}
