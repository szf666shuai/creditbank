package com.creditbank.platform.module.profile.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class CreditAccountSummaryVO {

    private BigDecimal totalEarned;
}
