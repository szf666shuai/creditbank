package com.creditbank.platform.module.admin.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class AdminJobVO {
    private Long id;
    private Long orgId;
    private String orgName;
    private String title;
    private String location;
    private String salaryRange;
    private Integer status;
    private String statusName;
    private Integer viewCount;
    private LocalDateTime createTime;
}
