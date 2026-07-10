package com.creditbank.platform.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
@Builder
public class LearningSituationVO {

    private Long userId;
    private String username;
    private String realName;
    private Integer role;
    private BigDecimal creditBalance;
    private BigDecimal creditEarned;
    private BigDecimal creditSpent;
    private Integer integrityScore;
    private List<Map<String, Object>> courses;
    private List<Map<String, Object>> archives;
    private List<Map<String, Object>> achievements;
    private List<Map<String, Object>> targetTags;
    private List<Map<String, Object>> forumPosts;
    private List<Map<String, Object>> recentCredits;
}
