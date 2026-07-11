package com.creditbank.platform.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("user_learning_reminder")
public class UserLearningReminder {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Long courseId;
    private Integer enabled;
    private Integer intervalMinutes;
    private LocalDateTime lastRemindAt;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
