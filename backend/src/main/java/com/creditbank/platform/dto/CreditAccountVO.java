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
public class CreditAccountVO {
    private Long userId;
    private BigDecimal balance;
    private BigDecimal totalEarned;
    private BigDecimal totalSpent;
    private Integer integrityScore;
    private String integrityLevel;
    private Double earnMultiplier;
    private Boolean canSpend;
}
