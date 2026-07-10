package com.creditbank.platform.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("credit_transaction")
public class CreditTransaction {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    /** 1获取 2转换 3增长 4消耗 */
    private Integer type;
    private BigDecimal amount;
    private BigDecimal balanceAfter;
    private String bizType;
    private String source;
    private String refType;
    private Long refId;
    private LocalDateTime createTime;
}
