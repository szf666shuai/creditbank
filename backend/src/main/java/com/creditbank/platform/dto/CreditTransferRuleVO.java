package com.creditbank.platform.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class CreditTransferRuleVO {

    private Long id;
    private Long orgId;
    private String orgName;
    private Integer sourceType;
    private String sourceTypeName;
    private Long sourceCourseId;
    private String sourceCourseName;
    private String sourceTags;
    private Integer targetType;
    private String targetTypeName;
    private Long targetCourseId;
    private String targetCourseName;
    private Long targetCertificateId;
    private String targetCertificateName;
    private Long targetAchievementId;
    private String targetAchievementTitle;
    private Long targetOrgId;
    private BigDecimal creditRatio;
    private String description;
    private Integer status;
    private String statusName;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}