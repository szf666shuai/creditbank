package com.creditbank.platform.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("course")
public class Course {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long orgId;
    private Long publisherId;
    private String title;
    private String description;
    private String coverUrl;
    private String videoUrl;
    private Integer videoDurationSeconds;
    private BigDecimal durationHours;
    private BigDecimal creditReward;
    private BigDecimal creditValue;
    private String tags;
    private Integer difficulty;
    private Integer durationMinutes;
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
