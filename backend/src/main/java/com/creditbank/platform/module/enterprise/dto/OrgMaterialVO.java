package com.creditbank.platform.module.enterprise.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class OrgMaterialVO {

    private Long id;
    private String title;
    private String description;
    private String fileUrl;
    private Integer materialType;
    private String materialTypeName;
    private LocalDateTime createTime;
}
