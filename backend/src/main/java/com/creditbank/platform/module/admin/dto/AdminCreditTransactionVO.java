package com.creditbank.platform.module.admin.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class AdminCreditTransactionVO {
    private Long id;
    private Long userId;
    private String userName;
    private Integer type;
    private String typeName;
    private BigDecimal amount;
    private BigDecimal balanceAfter;
    private String bizType;
    private String source;
    private LocalDateTime createTime;
}
