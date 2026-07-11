package com.creditbank.platform.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class CoursePurchaseResult {
    private Long courseId;
    private BigDecimal paidCredit;
    private BigDecimal balanceAfter;
    private Boolean purchased;
}
