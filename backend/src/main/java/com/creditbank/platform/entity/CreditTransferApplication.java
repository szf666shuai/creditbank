package com.creditbank.platform.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("credit_transfer_application")
public class CreditTransferApplication {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;
    private Integer sourceType;
    private Long sourceCourseId;
    private Long sourceAchievementId;
    private Long sourceOrgId;
    private BigDecimal sourceCredit;
    private Integer targetType;
    private Long targetCourseId;
    private Long targetCertificateId;
    private Long targetAchievementId;
    private Long targetOrgId;
    private String applyReason;
    private Integer status;
    private Long reviewerId;
    private String reviewComment;
    private BigDecimal actualCredit;
    private LocalDateTime applyTime;
    private LocalDateTime reviewTime;
}