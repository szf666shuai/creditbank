package com.creditbank.platform.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CourseDanmakuCreateRequest {

    @NotBlank(message = "弹幕内容不能为空")
    @Size(max = 100, message = "弹幕内容不能超过100字")
    private String content;

    @DecimalMin(value = "0", message = "弹幕时间点不能小于0")
    @DecimalMax(value = "86400", message = "弹幕时间点超出范围")
    private BigDecimal videoTimeSeconds;

    @Size(max = 16, message = "弹幕颜色格式不正确")
    private String color;
}
