package com.creditbank.platform.module.admin.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class AdminForumReportVO {
    private Long id;
    private Long userId;
    private String reporterName;
    private Integer targetType;
    private String targetTypeName;
    private Long targetId;
    private String targetSummary;
    private String reason;
    private Integer status;
    private String statusName;
    private String handleRemark;
    private LocalDateTime createTime;
}
