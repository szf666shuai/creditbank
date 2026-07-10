package com.creditbank.platform.module.admin.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AdminDashboardStatsVO {
    private long totalUsers;
    private long studentCount;
    private long enterpriseCount;
    private long adminCount;
    private long totalOrganizations;
    private long pendingOrganizations;
    private long activeOrganizations;
    private long pendingReports;
    private long totalJobs;
    private long activeJobs;
    private long totalActivities;
    private long activeActivities;
    private long unreadMessages;
}
