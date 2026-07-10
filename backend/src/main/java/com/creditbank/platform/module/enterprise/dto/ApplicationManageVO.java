package com.creditbank.platform.module.enterprise.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ApplicationManageVO {

    private Long id;
    private Long jobId;
    private String jobTitle;
    private Long userId;
    private String applicantName;
    private String coverMessage;
    private Integer status;
    private String statusName;
    private Boolean hasPendingInvite;
    private LocalDateTime createTime;
}
