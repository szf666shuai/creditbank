package com.creditbank.platform.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CreditSpendRequest {

    @NotNull(message = "扣减金额不能为空")
    @DecimalMin(value = "0.01", message = "扣减金额必须大于 0")
    private BigDecimal amount;

    @NotBlank(message = "业务类型不能为空")
    private String bizType;

    private String refType;

    private Long refId;

    private String source;
}
