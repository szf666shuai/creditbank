package com.creditbank.platform.module.enterprise.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SendInterviewInviteRequest {

    /** 从投递记录发起时必填 */
    private Long applicationId;

    /** 无投递记录时，需同时提供 jobId 与 toUserId */
    private Long jobId;
    private Long toUserId;

    @NotNull(message = "面试时间不能为空")
    private LocalDateTime inviteTime;

    private String location;

    /** 0现场 1视频，默认视频 */
    private Integer interviewMode;

    /** 补充说明，写入私信内容 */
    private String remark;
}
