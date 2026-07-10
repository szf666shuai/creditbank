package com.creditbank.platform.module.enterprise.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class JobManageVO {

    private Long id;
    private Long orgId;
    private String title;
    private String description;
    private String requirements;
    private String salaryRange;
    private String location;
    private String eduRequirement;
    private Integer status;
    private String statusName;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private List<TagVO> tags;
}
