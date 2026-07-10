package com.creditbank.platform.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class AgentChatRequest {

    @NotBlank(message = "消息不能为空")
    private String message;

    /** 可选历史，按时间正序；每项 role 为 user / assistant */
    private List<ChatTurn> history = new ArrayList<>();

    /** 可选业务上下文（如搜索关键词与结果摘要），会注入到系统提示中 */
    private String context;

    @Data
    public static class ChatTurn {
        private String role;
        private String content;
    }
}
