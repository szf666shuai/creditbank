package com.creditbank.platform.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("course_episode")
public class CourseEpisode {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long courseId;
    private String title;
    private String videoUrl;
    private Integer videoDurationSeconds;
    private Integer sortOrder;
    private Integer status;
    private LocalDateTime createTime;

    @TableLogic
    private Integer deleted;
}
