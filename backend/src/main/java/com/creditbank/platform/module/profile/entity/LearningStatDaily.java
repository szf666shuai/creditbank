package com.creditbank.platform.module.profile.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@TableName("learning_stat_daily")
public class LearningStatDaily {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private LocalDate statDate;
    private Integer studyMinutes;
    private Integer coursesCompleted;
    private BigDecimal creditEarned;
}
