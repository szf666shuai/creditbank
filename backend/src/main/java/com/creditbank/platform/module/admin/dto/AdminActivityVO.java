package com.creditbank.platform.module.admin.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class AdminActivityVO {
    private Long id;
    private Long orgId;
    private String orgName;
    private String title;
    private String location;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer status;
    private String statusName;
    private LocalDateTime createTime;
}
