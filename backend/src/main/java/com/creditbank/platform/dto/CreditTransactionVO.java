package com.creditbank.platform.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreditTransactionVO {
    private Long id;
    private Integer type;
    private String typeName;
    private BigDecimal amount;
    private BigDecimal balanceAfter;
    private String bizType;
    private String source;
    private String refType;
    private Long refId;
    private LocalDateTime createTime;
}
