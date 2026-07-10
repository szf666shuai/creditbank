package com.creditbank.platform.module.enterprise.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("job_posting")
public class JobPosting {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long orgId;
    private Long publisherId;
    private String title;
    private String description;
    private String requirements;
    private String salaryRange;
    private String location;
    private String eduRequirement;
    private Integer status;
    private Integer viewCount;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
