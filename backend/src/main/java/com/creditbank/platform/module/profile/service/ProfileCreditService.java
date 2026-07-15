package com.creditbank.platform.module.profile.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.creditbank.platform.common.BusinessException;
import com.creditbank.platform.common.PageResult;
import com.creditbank.platform.entity.CreditAccount;
import com.creditbank.platform.entity.CreditTransaction;
import com.creditbank.platform.mapper.CreditAccountMapper;
import com.creditbank.platform.mapper.CreditTransactionMapper;
import com.creditbank.platform.module.profile.dto.CreditAccountSummaryVO;
import com.creditbank.platform.module.profile.dto.CreditTransactionVO;
import com.creditbank.platform.security.AuthSupport;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;

@Service
@RequiredArgsConstructor
public class ProfileCreditService {

    private static final int TYPE_EARN = 1;
    private static final int TYPE_CONVERT = 2;
    private static final int TYPE_GROWTH = 3;

    private final AuthSupport authSupport;
    private final CreditAccountMapper creditAccountMapper;
    private final CreditTransactionMapper creditTransactionMapper;

    public CreditAccountSummaryVO getAccountSummary() {
        Long userId = authSupport.requireUserId();
        CreditAccount account = creditAccountMapper.selectOne(new LambdaQueryWrapper<CreditAccount>()
                .eq(CreditAccount::getUserId, userId));
        if (account == null) {
            return CreditAccountSummaryVO.builder()
                    .totalEarned(java.math.BigDecimal.ZERO)
                    .build();
        }
        return CreditAccountSummaryVO.builder()
                .totalEarned(account.getTotalEarned())
                .build();
    }

    public PageResult<CreditTransactionVO> pageTransactions(
            long page, long pageSize, Integer type, LocalDate startDate, LocalDate endDate) {
        Long userId = authSupport.requireUserId();
        if (page < 1 || pageSize < 1 || pageSize > 100) {
            throw new BusinessException(400, "分页参数无效");
        }
        if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
            throw new BusinessException(400, "开始日期不能晚于结束日期");
        }

        LambdaQueryWrapper<CreditTransaction> wrapper = new LambdaQueryWrapper<CreditTransaction>()
                .eq(CreditTransaction::getUserId, userId)
                .eq(type != null, CreditTransaction::getType, type)
                .orderByDesc(CreditTransaction::getCreateTime);
        if (startDate != null) {
            wrapper.ge(CreditTransaction::getCreateTime, startDate.atStartOfDay());
        }
        if (endDate != null) {
            wrapper.le(CreditTransaction::getCreateTime, endDate.atTime(LocalTime.MAX));
        }

        Page<CreditTransaction> result = creditTransactionMapper.selectPage(new Page<>(page, pageSize), wrapper);
        return PageResult.of(
                result.getRecords().stream().map(this::toVO).toList(),
                result.getTotal(),
                result.getCurrent(),
                result.getSize());
    }

    private CreditTransactionVO toVO(CreditTransaction tx) {
        return CreditTransactionVO.builder()
                .id(tx.getId())
                .type(tx.getType())
                .typeName(typeName(tx.getType()))
                .amount(tx.getAmount())
                .bizType(tx.getBizType())
                .source(tx.getSource())
                .refType(tx.getRefType())
                .refId(tx.getRefId())
                .createTime(tx.getCreateTime())
                .build();
    }

    static String typeName(Integer type) {
        if (type == null) {
            return "未知";
        }
        return switch (type) {
            case TYPE_EARN -> "获取";
            case TYPE_CONVERT -> "转换";
            case TYPE_GROWTH -> "增长";
            default -> "未知";
        };
    }
}
