package com.creditbank.platform.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("learning_achievement")
public class LearningAchievement {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String title;
    private Integer type;
    private Long orgId;
    private Long certificateId;
    private BigDecimal creditValue;
    private String fileUrl;
    private String tags;
    private Integer verifyStatus;
    private String blockchainHash;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
