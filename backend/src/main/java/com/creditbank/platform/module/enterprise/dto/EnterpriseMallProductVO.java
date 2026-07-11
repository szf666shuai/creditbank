package com.creditbank.platform.module.enterprise.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class EnterpriseMallProductVO {

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
    private Integer status;
    private Integer approvalStatus;
    private String approvalStatusName;
    private String reviewRemark;
    private LocalDateTime createTime;
}
