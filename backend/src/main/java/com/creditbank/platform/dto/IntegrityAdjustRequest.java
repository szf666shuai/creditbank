package com.creditbank.platform.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class IntegrityAdjustRequest {

    @NotNull(message = "变动分值不能为空")
    private Integer changeValue;

    @NotBlank(message = "原因不能为空")
    private String reason;

    private String refType;

    private Long refId;
}
