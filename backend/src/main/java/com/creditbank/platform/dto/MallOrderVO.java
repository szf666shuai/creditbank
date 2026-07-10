package com.creditbank.platform.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class MallOrderVO {

    private Long id;
    private String orderNo;
    private BigDecimal totalCredit;
    private BigDecimal totalMoney;
    private Integer payMethod;
    private String payMethodName;
    private Integer payStatus;
    private String payStatusName;
    private LocalDateTime payTime;
    private String remark;
    private LocalDateTime createTime;
    private List<MallOrderItemVO> items;
}
