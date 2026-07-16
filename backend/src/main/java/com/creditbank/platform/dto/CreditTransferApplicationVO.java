package com.creditbank.platform.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class CreditTransferApplicationVO {

    private Long id;
    private Long userId;
    private String userName;
    private Integer sourceType;
    private String sourceTypeName;
    private Long sourceCourseId;
    private String sourceCourseName;
    private Long sourceAchievementId;
    private String sourceAchievementTitle;
    private Long sourceOrgId;
    private String sourceOrgName;
    private BigDecimal sourceCredit;
    private Integer targetType;
    private String targetTypeName;
    private Long targetCourseId;
    private String targetCourseName;
    private Long targetCertificateId;
    private String targetCertificateName;
    private Long targetAchievementId;
    private String targetAchievementTitle;
    private Long targetOrgId;
    private String targetOrgName;
    private String applyReason;
    private String aiSuggestion;
    private String aiReason;
    private Boolean aiLlmUsed;
    private LocalDateTime aiScreenTime;
    private Integer status;
    private String statusName;
    private Long reviewerId;
    private String reviewerName;
    private String reviewComment;
    private BigDecimal actualCredit;
    private LocalDateTime applyTime;
    private LocalDateTime reviewTime;
}