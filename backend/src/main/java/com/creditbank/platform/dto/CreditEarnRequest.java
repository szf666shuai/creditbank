package com.creditbank.platform.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CreditEarnRequest {

    @NotBlank(message = "规则编码不能为空")
    private String ruleCode;

    private String refType;

    private Long refId;

    /** 可选：覆盖规则默认金额（如课程自定义奖励） */
    @DecimalMin(value = "0.01", message = "金额必须大于 0")
    private BigDecimal amountOverride;

    private String source;
}
