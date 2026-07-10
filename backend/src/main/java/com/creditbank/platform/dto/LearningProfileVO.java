package com.creditbank.platform.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@Builder
public class LearningProfileVO {

    private Long userId;
    private String targetJob;
    private String summary;
    private Map<String, Object> profile;
    private LocalDateTime updateTime;
    /** 生成画像时使用的学习事实快照 */
    private LearningSituationVO situation;
}
