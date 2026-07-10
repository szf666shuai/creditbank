package com.creditbank.platform.module.admin.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class HandleForumReportRequest {
    @NotNull(message = "处理状态不能为空")
    private Integer status;
    private String handleRemark;
    /** 处理为已处理时是否隐藏被举报内容 */
    private Boolean hideTarget;
}
