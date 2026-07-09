package com.creditbank.platform.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreditRuleVO {
    private String ruleCode;
    private String ruleName;
    private BigDecimal amount;
    private String bizType;
    private String description;
    private Integer dailyLimit;
    private Integer minIntegrity;
}
