package com.creditbank.platform.module.enterprise.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class JobApplyResultVO {

    private Long id;
    private Long jobId;
    private Integer status;
    private String statusName;
    private LocalDateTime createTime;
}
