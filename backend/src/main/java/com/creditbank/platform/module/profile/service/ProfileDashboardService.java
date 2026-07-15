package com.creditbank.platform.module.profile.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.creditbank.platform.dto.UserInfoVO;
import com.creditbank.platform.entity.CreditAccount;
import com.creditbank.platform.entity.IntegrityScore;
import com.creditbank.platform.mapper.CreditAccountMapper;
import com.creditbank.platform.mapper.IntegrityScoreMapper;
import com.creditbank.platform.module.profile.dto.ProfileDashboardVO;
import com.creditbank.platform.module.message.service.MessageService;
import com.creditbank.platform.security.AuthSupport;
import com.creditbank.platform.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class ProfileDashboardService {

    private final AuthSupport authSupport;
    private final AuthService authService;
    private final MessageService messageService;
    private final CreditAccountMapper creditAccountMapper;
    private final IntegrityScoreMapper integrityScoreMapper;

    public ProfileDashboardVO getDashboard() {
        Long userId = authSupport.requireUserId();
        UserInfoVO userInfo = authService.getUserInfo(userId);

        CreditAccount account = creditAccountMapper.selectOne(new LambdaQueryWrapper<CreditAccount>()
                .eq(CreditAccount::getUserId, userId));
        IntegrityScore integrity = integrityScoreMapper.selectById(userId);

        Long unreadCount = messageService.getUnreadCount();

        return ProfileDashboardVO.builder()
                .userInfo(userInfo)
                .totalEarned(account != null ? account.getTotalEarned() : BigDecimal.ZERO)
                .integrityScore(integrity != null ? integrity.getScore() : null)
                .unreadMessageCount(unreadCount)
                .build();
    }
}
