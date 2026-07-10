package com.creditbank.platform.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("sys_tag")
public class SysTag {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private String category;
    private LocalDateTime createTime;
}
