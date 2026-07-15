package com.creditbank.platform.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreditTransferApplyRequest {

    @NotNull(message = "源类型不能为空")
    private Integer sourceType;

    private Long sourceCourseId;

    private Long sourceAchievementId;

    @NotNull(message = "目标组织ID不能为空")
    private Long targetOrgId;

    private Integer targetType;

    private Long targetCourseId;
    private Long targetCertificateId;
    private Long targetAchievementId;

    private String applyReason;
}