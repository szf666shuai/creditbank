package com.creditbank.platform.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("forum_like")
public class ForumLike {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    /** 1帖子 2回复 */
    private Integer targetType;
    private Long targetId;
    private LocalDateTime createTime;
}
