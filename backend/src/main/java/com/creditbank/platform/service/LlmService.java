package com.creditbank.platform.service;

import com.creditbank.platform.common.BusinessException;
import com.creditbank.platform.config.LlmProperties;
import com.creditbank.platform.dto.AgentChatRequest;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.net.http.HttpClient;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class LlmService {

    private static final int MAX_HISTORY = 12;

    private final LlmProperties properties;

    public String chat(String userMessage, List<AgentChatRequest.ChatTurn> history, String context) {
        if (!properties.isEnabled()) {
            throw new BusinessException(503, "AI 助手未启用");
        }
        if (!StringUtils.hasText(properties.getApiKey())) {
            throw new BusinessException(503, "未配置 LLM API Key，请在 application-local.yml 中设置 llm.api-key");
        }

        List<Map<String, String>> messages = new ArrayList<>();
        String system = properties.getSystemPrompt();
        if (StringUtils.hasText(context)) {
            system = system + "\n\n【当前业务上下文】\n" + context.trim();
        }
        messages.add(Map.of("role", "system", "content", system));

        if (history != null) {
            int from = Math.max(0, history.size() - MAX_HISTORY);
            for (int i = from; i < history.size(); i++) {
                AgentChatRequest.ChatTurn turn = history.get(i);
                if (turn == null || !StringUtils.hasText(turn.getContent())) {
                    continue;
                }
                String role = "assistant".equalsIgnoreCase(turn.getRole()) ? "assistant" : "user";
                messages.add(Map.of("role", role, "content", turn.getContent().trim()));
            }
        }
        messages.add(Map.of("role", "user", "content", userMessage.trim()));

        Map<String, Object> body = new HashMap<>();
        body.put("model", properties.getModel());
        body.put("messages", messages);
        body.put("stream", false);
        body.put("temperature", 0.7);

        String url = trimTrailingSlash(properties.getBaseUrl()) + "/chat/completions";
        try {
            JsonNode response = restClient().post()
                    .uri(url)
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("Authorization", "Bearer " + properties.getApiKey())
                    .body(body)
                    .retrieve()
                    .body(JsonNode.class);

            if (response == null) {
                throw new BusinessException(502, "DeepSeek 返回为空");
            }
            JsonNode content = response.path("choices").path(0).path("message").path("content");
            if (content.isMissingNode() || !StringUtils.hasText(content.asText())) {
                log.warn("Unexpected LLM response: {}", response);
                throw new BusinessException(502, "DeepSeek 未返回有效内容");
            }
            return content.asText().trim();
        } catch (RestClientResponseException e) {
            log.error("DeepSeek API error: status={} body={}", e.getStatusCode().value(), e.getResponseBodyAsString());
            throw new BusinessException(502, "DeepSeek 调用失败: " + e.getStatusCode().value());
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("DeepSeek call failed", e);
            throw new BusinessException(502, "DeepSeek 调用异常: " + e.getMessage());
        }
    }

    private RestClient restClient() {
        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(15))
                .build();
        JdkClientHttpRequestFactory factory = new JdkClientHttpRequestFactory(httpClient);
        factory.setReadTimeout(Duration.ofMillis(Math.max(5000, properties.getTimeoutMs())));
        return RestClient.builder().requestFactory(factory).build();
    }

    private static String trimTrailingSlash(String baseUrl) {
        if (baseUrl == null || baseUrl.isBlank()) {
            return "https://api.deepseek.com";
        }
        return baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
    }
}
