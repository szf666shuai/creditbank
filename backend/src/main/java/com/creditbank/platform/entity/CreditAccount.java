package com.creditbank.platform.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("credit_account")
public class CreditAccount {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private BigDecimal totalEarned;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
