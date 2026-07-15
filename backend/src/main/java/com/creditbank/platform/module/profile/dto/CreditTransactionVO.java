package com.creditbank.platform.module.profile.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class CreditTransactionVO {

    private Long id;
    private Integer type;
    private String typeName;
    private BigDecimal amount;
    private String bizType;
    private String source;
    private String refType;
    private Long refId;
    private LocalDateTime createTime;
}
