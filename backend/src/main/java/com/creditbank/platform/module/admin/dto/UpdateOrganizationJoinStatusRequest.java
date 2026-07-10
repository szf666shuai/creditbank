package com.creditbank.platform.module.admin.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateOrganizationJoinStatusRequest {
    @NotNull(message = "加盟状态不能为空")
    private Integer joinStatus;
}
