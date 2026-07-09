package com.creditbank.platform.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IntegrityScoreVO {
    private Long userId;
    private Integer score;
    private String level;
    private String levelLabel;
    private Double earnMultiplier;
    private Boolean canSpend;
    private Boolean canPost;
    private Boolean canRegisterActivity;
}
