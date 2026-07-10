package com.creditbank.platform.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("user_learning_profile")
public class UserLearningProfile {

    @TableId
    private Long userId;
    private String targetJob;
    private String summary;
    private String profileJson;
    private LocalDateTime updateTime;
}
