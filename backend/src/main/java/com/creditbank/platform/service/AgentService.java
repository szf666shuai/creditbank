package com.creditbank.platform.service;

import com.creditbank.platform.common.BusinessException;
import com.creditbank.platform.config.LlmProperties;
import com.creditbank.platform.context.UserContext;
import com.creditbank.platform.dto.AgentChatRequest;
import com.creditbank.platform.dto.AgentChatResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class AgentService {

    private final LlmService llmService;
    private final LlmProperties llmProperties;
    private final LearningProfileService learningProfileService;

    public AgentChatResponse chat(AgentChatRequest request) {
        String message = request.getMessage() == null ? "" : request.getMessage().trim();
        if (!StringUtils.hasText(message)) {
            throw new BusinessException("消息不能为空");
        }
        if (message.length() > 2000) {
            throw new BusinessException("消息过长，请控制在 2000 字以内");
        }

        String context = request.getContext();
        Long userId = UserContext.getUserId();
        if (userId != null) {
            String learningCtx = learningProfileService.buildChatContext(userId);
            if (StringUtils.hasText(learningCtx)) {
                context = StringUtils.hasText(context)
                        ? context + "\n\n" + learningCtx
                        : learningCtx;
            }
        }

        String reply = llmService.chat(message, request.getHistory(), context);
        return AgentChatResponse.builder()
                .reply(reply)
                .model(llmProperties.getModel())
                .build();
    }
}
