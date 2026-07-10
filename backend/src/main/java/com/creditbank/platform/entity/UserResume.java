package com.creditbank.platform.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("user_resume")
public class UserResume {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String title;
    private String contentJson;
    private String fileUrl;
    private Integer isDefault;
    private Integer version;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
