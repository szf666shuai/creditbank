package com.creditbank.platform.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class LearningProgressRequest {

    @NotNull(message = "本次观看时长不能为空")
    @Min(value = 0, message = "本次观看时长不能小于 0")
    @Max(value = 30, message = "单次最多上报 30 秒")
    private Integer watchedDeltaSeconds;

    @NotNull(message = "当前播放位置不能为空")
    @Min(value = 0, message = "当前播放位置不能小于 0")
    private Integer currentTimeSeconds;
}
