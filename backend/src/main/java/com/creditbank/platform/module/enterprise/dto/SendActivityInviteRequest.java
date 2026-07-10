package com.creditbank.platform.module.enterprise.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SendActivityInviteRequest {

    @NotNull(message = "活动不能为空")
    private Long activityId;

    @NotNull(message = "受邀学员不能为空")
    private Long toUserId;

    /** 补充说明，写入私信内容 */
    private String remark;
}
