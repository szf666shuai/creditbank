package com.creditbank.platform.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class PaymentResultVO {

    private Long orderId;
    private String orderNo;
    private String payNo;
    private BigDecimal amountCredit;
    private BigDecimal amountMoney;
    private CreditChangeResult creditChange;
}
