package com.creditbank.platform.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("integrity_score")
public class IntegrityScore {

    @TableId
    private Long userId;
    private Integer score;
    private LocalDateTime updateTime;
}
