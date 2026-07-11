package com.creditbank.platform.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("course_comment")
public class CourseComment {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long courseId;
    private Long userId;
    private Long parentId;
    private String content;
    private Integer likeCount;
    private LocalDateTime createTime;

    @TableLogic
    private Integer deleted;
}
