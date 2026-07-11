package com.creditbank.platform.module.admin.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class AdminMallProductVO {

    private Long id;
    private Long orgId;
    private String orgName;
    private String name;
    private String description;
    private String coverUrl;
    private Integer productType;
    private String productTypeName;
    private BigDecimal priceCredit;
    private BigDecimal priceMoney;
    private Integer stock;
    private Integer status;
    private Integer approvalStatus;
    private String approvalStatusName;
    private String reviewRemark;
    private LocalDateTime createTime;
}
