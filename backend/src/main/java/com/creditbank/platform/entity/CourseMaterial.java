package com.creditbank.platform.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("course_material")
public class CourseMaterial {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long courseId;
    private String title;
    private String fileType;
    private String fileUrl;
    private Integer sortOrder;
    private LocalDateTime createTime;

    @TableLogic
    private Integer deleted;
}
