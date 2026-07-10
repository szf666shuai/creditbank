package com.creditbank.platform.module.enterprise.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("org_material")
public class OrgMaterial {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long orgId;
    private Long publisherId;
    private String title;
    private String description;
    private String fileUrl;
    private Integer materialType;
    private Integer status;
    private LocalDateTime createTime;

    @TableLogic
    private Integer deleted;
}
