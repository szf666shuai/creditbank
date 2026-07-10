package com.creditbank.platform.module.message.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SendMessageRequest {

    @NotNull(message = "接收人不能为空")
    private Long receiverId;

    private String title;

    @NotBlank(message = "消息内容不能为空")
    private String content;

    /** 1普通私信 2面试邀请 3活动邀请 4系统通知，默认1 */
    private Integer msgType;
}
