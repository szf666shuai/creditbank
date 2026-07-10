package com.creditbank.platform.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class MallOrderItemVO {

    private Long id;
    private Long productId;
    private String productName;
    private Integer productType;
    private Long refCourseId;
    private Integer quantity;
    private BigDecimal priceCredit;
    private BigDecimal priceMoney;
    private String redemptionCode;
}
