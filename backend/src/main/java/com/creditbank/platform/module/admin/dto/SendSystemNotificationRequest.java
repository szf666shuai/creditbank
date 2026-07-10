package com.creditbank.platform.module.admin.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SendSystemNotificationRequest {
    @NotBlank(message = "标题不能为空")
    private String title;
    @NotBlank(message = "内容不能为空")
    private String content;
    /** all=全体 role=按角色 user=指定用户 */
    @NotBlank(message = "发送范围不能为空")
    private String scope;
    /** scope=role 时必填：0学员 1企业 2管理员 */
    private Integer targetRole;
    /** scope=user 时必填 */
    private Long targetUserId;
}
