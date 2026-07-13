package com.creditbank.platform.module.profile.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class MyJobApplicationVO {

    private Long id;
    private Long jobId;
    private String jobTitle;
    private Long orgId;
    private String orgName;
    private String jobLocation;
    private String salaryRange;
    private String coverMessage;
    private Integer status;
    private String statusName;
    private Boolean jobUnavailable;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
