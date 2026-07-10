package com.creditbank.platform.module.profile.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("learning_archive")
public class LearningArchive {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String title;
    private Integer archiveType;
    private Long courseId;
    private Long certificateId;
    private String category;
    private String description;
    private LocalDate startDate;
    private LocalDate endDate;
    private BigDecimal creditEarned;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
