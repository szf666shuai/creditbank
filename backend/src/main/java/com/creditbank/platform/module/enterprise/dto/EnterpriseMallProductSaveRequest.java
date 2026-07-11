package com.creditbank.platform.module.enterprise.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class EnterpriseMallProductSaveRequest {

    @NotNull(message = "分类不能为空")
    private Long categoryId;

    @NotBlank(message = "商品名称不能为空")
    private String name;

    private String description;
    private String coverUrl;

    @NotNull(message = "商品类型不能为空")
    private Integer productType;

    private Long refCourseId;

    @NotNull(message = "秩点价格不能为空")
    @DecimalMin(value = "0", message = "秩点价格不能小于0")
    private BigDecimal priceCredit;

    @DecimalMin(value = "0", message = "现金价格不能小于0")
    private BigDecimal priceMoney;

    private Integer stock;
}
