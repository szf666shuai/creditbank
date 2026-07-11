package com.creditbank.platform.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
public class LearningReminderRequest {

    private Boolean enabled = true;

    @Min(5)
    @Max(180)
    private Integer intervalMinutes = 30;
}
