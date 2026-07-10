package com.creditbank.platform.module.message.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class MessageVO {

    private Long id;
    private Long fromUserId;
    private String fromUserName;
    private Long toUserId;
    private String toUserName;
    private Integer msgType;
    private String title;
    private String content;
    private Integer readStatus;
    private String refType;
    private Long refId;
    private LocalDateTime createTime;
}
