package com.creditbank.platform.module.enterprise.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class EnterpriseDashboardVO {

    private Long orgId;
    private String orgName;
    private long openJobCount;
    private long ongoingActivityCount;
    private long registeringActivityCount;
    private long pendingApplicationCount;
    private long pendingInterviewCount;
    private long materialCount;
    private Integer joinStatus;
    private String joinStatusName;
    private boolean writable;
}
