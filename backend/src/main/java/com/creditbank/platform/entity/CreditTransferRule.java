package com.creditbank.platform.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("credit_transfer_rule")
public class CreditTransferRule {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long orgId;
    private Integer sourceType;
    private Long sourceCourseId;
    private String sourceTags;
    private Integer targetType;
    private Long targetCourseId;
    private Long targetCertificateId;
    private Long targetAchievementId;
    private Long targetOrgId;
    private BigDecimal creditRatio;
    private String description;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}