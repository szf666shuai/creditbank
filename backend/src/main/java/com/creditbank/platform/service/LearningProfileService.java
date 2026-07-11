package com.creditbank.platform.service;

import com.creditbank.platform.common.BusinessException;
import com.creditbank.platform.dto.LearningProfileVO;
import com.creditbank.platform.dto.LearningSituationVO;
import com.creditbank.platform.entity.UserLearningProfile;
import com.creditbank.platform.mapper.UserLearningProfileMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class LearningProfileService {

    private static final String GENERATE_PROMPT = """
            你是星秩存册平台的学习画像分析师。请严格依据【当前业务上下文】中的学习事实，为该登录用户生成学习画像。
            要求：
            1. 不要编造上下文中不存在的课程、成绩或经历。
            2. 用简洁中文。
            3. 只输出一个 JSON 对象，不要 Markdown 代码块，字段如下：
            {
              "targetJob": "建议的心仪职位，字符串",
              "summary": "80-160字的学习画像摘要",
              "stage": "学习阶段，如入门/进阶/求职冲刺",
              "skills": ["已掌握或正在学习的技能"],
              "strengths": ["优势，2-4条"],
              "gaps": ["待补齐短板，2-4条"],
              "suggestions": ["下一步行动建议，3条以内"]
            }
            """;

    private final UserLearningProfileMapper profileMapper;
    private final LlmService llmService;
    private final ObjectMapper objectMapper;

    public LearningSituationVO loadSituation(Long userId) {
        Map<String, Object> basics = profileMapper.selectUserBasics(userId);
        if (basics == null || basics.isEmpty()) {
            throw new BusinessException(404, "用户不存在");
        }
        return LearningSituationVO.builder()
                .userId(userId)
                .username(asString(basics.get("username")))
                .realName(asString(basics.get("realName")))
                .role(asInt(basics.get("role")))
                .creditBalance(asDecimal(basics.get("creditBalance")))
                .creditEarned(asDecimal(basics.get("creditEarned")))
                .creditSpent(asDecimal(basics.get("creditSpent")))
                .integrityScore(asInt(basics.get("integrityScore")))
                .courses(nullToEmpty(profileMapper.selectCourseProgress(userId)))
                .archives(nullToEmpty(profileMapper.selectArchives(userId, 8)))
                .achievements(nullToEmpty(profileMapper.selectAchievements(userId, 8)))
                .targetTags(nullToEmpty(profileMapper.selectTargetTags(userId)))
                .forumPosts(nullToEmpty(profileMapper.selectForumPosts(userId, 5)))
                .recentCredits(nullToEmpty(profileMapper.selectCreditTxns(userId, 8)))
                .build();
    }

    public LearningProfileVO getProfile(Long userId) {
        UserLearningProfile saved = profileMapper.selectById(userId);
        LearningSituationVO situation = loadSituation(userId);
        if (saved == null) {
            return LearningProfileVO.builder()
                    .userId(userId)
                    .situation(situation)
                    .build();
        }
        return LearningProfileVO.builder()
                .userId(userId)
                .targetJob(saved.getTargetJob())
                .summary(saved.getSummary())
                .profile(parseProfileJson(saved.getProfileJson()))
                .updateTime(saved.getUpdateTime())
                .situation(situation)
                .build();
    }

    @Transactional
    public LearningProfileVO generate(Long userId) {
        LearningSituationVO situation = loadSituation(userId);
        String context = buildContext(situation);
        String raw = llmService.chat(GENERATE_PROMPT, List.of(), context);
        Map<String, Object> profile = parseLlmJson(raw);

        String summary = asString(profile.get("summary"));
        if (!StringUtils.hasText(summary)) {
            summary = raw.length() > 300 ? raw.substring(0, 300) + "…" : raw;
        }
        String targetJob = asString(profile.get("targetJob"));
        if (!StringUtils.hasText(targetJob) && situation.getTargetTags() != null && !situation.getTargetTags().isEmpty()) {
            targetJob = "基于目标技能的相关岗位";
        }

        String json;
        try {
            json = objectMapper.writeValueAsString(profile);
        } catch (Exception e) {
            json = "{}";
        }

        UserLearningProfile entity = profileMapper.selectById(userId);
        if (entity == null) {
            entity = new UserLearningProfile();
            entity.setUserId(userId);
            entity.setTargetJob(targetJob);
            entity.setSummary(summary);
            entity.setProfileJson(json);
            entity.setUpdateTime(LocalDateTime.now());
            profileMapper.insert(entity);
        } else {
            entity.setTargetJob(targetJob);
            entity.setSummary(summary);
            entity.setProfileJson(json);
            entity.setUpdateTime(LocalDateTime.now());
            profileMapper.updateById(entity);
        }

        return LearningProfileVO.builder()
                .userId(userId)
                .targetJob(entity.getTargetJob())
                .summary(entity.getSummary())
                .profile(profile)
                .updateTime(entity.getUpdateTime())
                .situation(situation)
                .build();
    }

    /** 供对话助手注入的精简上下文 */
    public String buildChatContext(Long userId) {
        try {
            LearningProfileVO profile = getProfile(userId);
            StringBuilder sb = new StringBuilder();
            sb.append("登录用户学习情况：\n");
            LearningSituationVO s = profile.getSituation();
            if (s != null) {
                sb.append("- 用户：").append(s.getRealName()).append("（").append(s.getUsername()).append("）\n");
                sb.append("- 秩点余额：").append(s.getCreditBalance())
                        .append("，累计获得：").append(s.getCreditEarned()).append('\n');
                sb.append("- 诚信分：").append(s.getIntegrityScore()).append('\n');
                if (s.getCourses() != null && !s.getCourses().isEmpty()) {
                    sb.append("- 课程进度：\n");
                    for (Map<String, Object> c : s.getCourses()) {
                        sb.append("  · ").append(c.get("courseTitle"))
                                .append(" 进度").append(c.get("progress")).append('%')
                                .append(" 状态").append(courseStatusLabel(c.get("status")))
                                .append('\n');
                    }
                }
                if (s.getTargetTags() != null && !s.getTargetTags().isEmpty()) {
                    sb.append("- 目标技能：");
                    List<String> tags = new ArrayList<>();
                    for (Map<String, Object> t : s.getTargetTags()) {
                        tags.add(asString(t.get("tagName")));
                    }
                    sb.append(String.join("、", tags)).append('\n');
                }
            }
            if (StringUtils.hasText(profile.getSummary())) {
                sb.append("- 已生成画像摘要：").append(profile.getSummary()).append('\n');
            }
            if (StringUtils.hasText(profile.getTargetJob())) {
                sb.append("- 心仪职位：").append(profile.getTargetJob()).append('\n');
            }
            return sb.toString();
        } catch (Exception e) {
            log.warn("buildChatContext failed for user {}: {}", userId, e.getMessage());
            return "";
        }
    }

    private String buildContext(LearningSituationVO s) {
        try {
            return "场景：为当前登录用户生成学习画像\n学习事实JSON：\n"
                    + objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(s);
        } catch (Exception e) {
            throw new BusinessException(500, "构建学习上下文失败");
        }
    }

    private Map<String, Object> parseLlmJson(String raw) {
        String text = raw == null ? "" : raw.trim();
        if (text.startsWith("```")) {
            int start = text.indexOf('{');
            int end = text.lastIndexOf('}');
            if (start >= 0 && end > start) {
                text = text.substring(start, end + 1);
            }
        }
        try {
            return objectMapper.readValue(text, new TypeReference<LinkedHashMap<String, Object>>() {});
        } catch (Exception e) {
            log.warn("LLM profile JSON parse failed, fallback. raw={}", raw);
            Map<String, Object> fallback = new LinkedHashMap<>();
            fallback.put("summary", raw);
            fallback.put("skills", List.of());
            fallback.put("strengths", List.of());
            fallback.put("gaps", List.of());
            fallback.put("suggestions", List.of());
            return fallback;
        }
    }

    private Map<String, Object> parseProfileJson(String json) {
        if (!StringUtils.hasText(json)) {
            return Map.of();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<LinkedHashMap<String, Object>>() {});
        } catch (Exception e) {
            return Map.of("raw", json);
        }
    }

    private static List<Map<String, Object>> nullToEmpty(List<Map<String, Object>> list) {
        return list == null ? List.of() : list;
    }

    private static String asString(Object v) {
        return v == null ? null : String.valueOf(v);
    }

    private static Integer asInt(Object v) {
        if (v == null) return null;
        if (v instanceof Number n) return n.intValue();
        try {
            return Integer.parseInt(String.valueOf(v));
        } catch (Exception e) {
            return null;
        }
    }

    private static BigDecimal asDecimal(Object v) {
        if (v == null) return BigDecimal.ZERO;
        if (v instanceof BigDecimal b) return b;
        if (v instanceof Number n) return BigDecimal.valueOf(n.doubleValue());
        try {
            return new BigDecimal(String.valueOf(v));
        } catch (Exception e) {
            return BigDecimal.ZERO;
        }
    }

    private static String courseStatusLabel(Object status) {
        Integer s = asInt(status);
        if (s == null) return "未知";
        return switch (s) {
            case 1 -> "已完成";
            case 2 -> "已退课";
            default -> "学习中";
        };
    }
}
