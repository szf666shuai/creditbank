package com.creditbank.platform.module.profile.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class LearningArchiveVO {

    private Long id;
    private String title;
    private Integer archiveType;
    private String archiveTypeName;
    private Long courseId;
    private Long certificateId;
    private String category;
    private String description;
    private LocalDate startDate;
    private LocalDate endDate;
    private BigDecimal creditEarned;
    private Integer status;
    private String statusName;
    private LocalDateTime createTime;
}
