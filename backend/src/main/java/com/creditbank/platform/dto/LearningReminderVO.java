package com.creditbank.platform.dto;

import lombok.Data;

@Data
public class LearningReminderVO {

    private Long courseId;
    private Boolean enabled;
    private Integer intervalMinutes;
}
