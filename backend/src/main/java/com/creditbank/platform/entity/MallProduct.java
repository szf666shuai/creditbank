package com.creditbank.platform.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("mall_product")
public class MallProduct {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long categoryId;
    private Long orgId;
    private Long publisherId;
    private String name;
    private String description;
    private String coverUrl;
    private Integer productType;
    private Long refCourseId;
    private BigDecimal priceCredit;
    private BigDecimal priceMoney;
    private Integer stock;
    private Integer status;
    private Integer approvalStatus;
    private String reviewRemark;
    private Long reviewedBy;
    private LocalDateTime reviewedAt;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
