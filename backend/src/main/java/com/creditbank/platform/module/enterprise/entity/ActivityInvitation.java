package com.creditbank.platform.module.enterprise.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("activity_invitation")
public class ActivityInvitation {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long activityId;
    private Long fromUserId;
    private Long toUserId;
    private Long messageId;
    private Integer status;
    private LocalDateTime createTime;
}
