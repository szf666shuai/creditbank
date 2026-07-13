package com.creditbank.platform.module.enterprise.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class InterviewInvitationVO {

    private Long id;
    private Long jobId;
    private String jobTitle;
    private Long orgId;
    private String orgName;
    private Long fromUserId;
    private String fromUserName;
    private Long toUserId;
    private String toUserName;
    private Long applicationId;
    private Long messageId;
    private Integer status;
    private String statusName;
    private LocalDateTime inviteTime;
    private String location;
    private Integer interviewMode;
    private String interviewModeName;
    private String roomId;
    private Integer applicationStatus;
    private String applicationStatusName;
    private Boolean canJoinVideo;
    private LocalDateTime createTime;
}
