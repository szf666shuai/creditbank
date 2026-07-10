package com.creditbank.platform.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class MallProductVO {

    private Long id;
    private Long categoryId;
    private String categoryName;
    private String name;
    private String description;
    private String coverUrl;
    private Integer productType;
    private String productTypeName;
    private Long refCourseId;
    private BigDecimal priceCredit;
    private BigDecimal priceMoney;
    private Integer stock;
}
