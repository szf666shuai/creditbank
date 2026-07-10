package com.creditbank.platform.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
public class LearningCompleteRequest {

    @Min(value = 0, message = "学习进度不能小于 0")
    @Max(value = 100, message = "学习进度不能大于 100")
    private Integer progress = 100;
}
