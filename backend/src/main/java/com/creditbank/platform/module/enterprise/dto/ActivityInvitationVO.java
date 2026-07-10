package com.creditbank.platform.module.enterprise.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class ActivityInvitationVO {

    private Long id;
    private Long activityId;
    private String activityTitle;
    private Long orgId;
    private String orgName;
    private String location;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private BigDecimal creditReward;
    private Long fromUserId;
    private String fromUserName;
    private Long toUserId;
    private String toUserName;
    private Long messageId;
    private Integer status;
    private String statusName;
    private LocalDateTime createTime;
}
