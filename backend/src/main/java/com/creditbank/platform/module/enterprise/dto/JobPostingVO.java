package com.creditbank.platform.module.enterprise.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class JobPostingVO {

    private Long id;
    private String title;
    private String description;
    private String requirements;
    private String salaryRange;
    private String location;
    private String eduRequirement;
    private Integer status;
    private String statusName;
    private LocalDateTime createTime;
}
