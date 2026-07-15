package com.creditbank.platform.module.profile.dto;

import com.creditbank.platform.dto.UserInfoVO;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class ProfileDashboardVO {

    private UserInfoVO userInfo;
    private BigDecimal totalEarned;
    private Integer integrityScore;
    private long unreadMessageCount;
}
