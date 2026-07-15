package com.creditbank.platform.module.admin.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class AdminCourseVO {

    private Long id;
    private Long orgId;
    private String orgName;
    private String title;
    private String description;
    private String coverUrl;
    private List<String> tags;
    private BigDecimal creditValue;
    private Integer duration;
    private Integer difficulty;
    private String difficultyName;
    private Integer status;
    private Integer approvalStatus;
    private String approvalStatusName;
    private String reviewRemark;
    private LocalDateTime createTime;
}