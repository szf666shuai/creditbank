package com.creditbank.platform.module.profile.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class LearningAchievementVO {

    private Long id;
    private String title;
    private Integer type;
    private String typeName;
    private Long orgId;
    private String orgName;
    private Long certificateId;
    private BigDecimal creditValue;
    private String fileUrl;
    private Integer verifyStatus;
    private String verifyStatusName;
    private String blockchainHash;
    private LocalDateTime createTime;
}
