package com.creditbank.platform.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class CourseDanmakuVO {

    private Long id;
    private Long courseId;
    private Long userId;
    private String authorName;
    private String content;
    private BigDecimal videoTimeSeconds;
    private String color;
    private LocalDateTime createTime;
}
