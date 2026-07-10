package com.creditbank.platform.module.enterprise.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class MaterialManageVO {

    private Long id;
    private String title;
    private String description;
    private String fileUrl;
    private Integer materialType;
    private String materialTypeName;
    private Integer status;
    private String statusName;
    private LocalDateTime createTime;
}
