package com.creditbank.platform.module.enterprise.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class InterviewRtcCredentialsVO {

    private Long invitationId;
    private Long sdkAppId;
    private String userId;
    private String userSig;
    private String roomId;
}
