package com.creditbank.platform.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("forum_report")
public class ForumReport {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    /** 1帖子 2回复 */
    private Integer targetType;
    private Long targetId;
    private String reason;
    /** 0待处理 1已处理 2驳回 */
    private Integer status;
    private String handleRemark;
    private LocalDateTime createTime;
}
