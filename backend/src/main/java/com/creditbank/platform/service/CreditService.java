package com.creditbank.platform.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.creditbank.platform.common.BusinessException;
import com.creditbank.platform.constant.CreditTxnType;
import com.creditbank.platform.constant.IntegrityLevel;
import com.creditbank.platform.dto.CreditAccountVO;
import com.creditbank.platform.dto.CreditChangeResult;
import com.creditbank.platform.dto.CreditEarnRequest;
import com.creditbank.platform.dto.CreditRuleVO;
import com.creditbank.platform.dto.CreditSpendRequest;
import com.creditbank.platform.dto.CreditTransactionVO;
import com.creditbank.platform.entity.CreditAccount;
import com.creditbank.platform.entity.CreditRule;
import com.creditbank.platform.entity.CreditTransaction;
import com.creditbank.platform.entity.IntegrityScore;
import com.creditbank.platform.mapper.CreditAccountExtMapper;
import com.creditbank.platform.mapper.CreditAccountMapper;
import com.creditbank.platform.mapper.CreditRuleMapper;
import com.creditbank.platform.mapper.CreditTransactionMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CreditService {

    /** 规则日次数上限（未在 DB 配置时的默认策略） */
    private static final Map<String, Integer> DEFAULT_DAILY_LIMIT = Map.of(
            "DAILY_CHECKIN", 1,
            "POST_CREATE", 3,
            "REPLY_CREATE", 10,
            "POST_LIKE", 20,
            "COURSE_COMPLETE", 5,
            "ACTIVITY_CHECKIN", 3,
            "REGISTER_BONUS", 1
    );

    /** 规则最低诚信分要求 */
    private static final Map<String, Integer> DEFAULT_MIN_INTEGRITY = Map.of(
            "DAILY_CHECKIN", 60,
            "CHECKIN_STREAK_7", 60,
            "COURSE_COMPLETE", 60,
            "ACTIVITY_CHECKIN", 70,
            "POST_CREATE", 70,
            "REPLY_CREATE", 70,
            "POST_LIKE", 70,
            "ACHIEVE_VERIFY", 80,
            "REGISTER_BONUS", 0
    );

    private final CreditAccountMapper creditAccountMapper;
    private final CreditAccountExtMapper creditAccountExtMapper;
    private final CreditRuleMapper creditRuleMapper;
    private final CreditTransactionMapper creditTransactionMapper;
    private final IntegrityService integrityService;

    public CreditAccount ensureAccount(Long userId) {
        CreditAccount account = creditAccountMapper.selectOne(
                new LambdaQueryWrapper<CreditAccount>().eq(CreditAccount::getUserId, userId)
        );
        if (account == null) {
            account = new CreditAccount();
            account.setUserId(userId);
            account.setBalance(BigDecimal.ZERO);
            account.setTotalEarned(BigDecimal.ZERO);
            account.setTotalSpent(BigDecimal.ZERO);
            creditAccountMapper.insert(account);
        }
        return account;
    }

    public CreditAccountVO getAccount(Long userId) {
        CreditAccount account = ensureAccount(userId);
        IntegrityScore score = integrityService.ensureScore(userId);
        IntegrityLevel level = IntegrityLevel.fromScore(score.getScore());
        return CreditAccountVO.builder()
                .userId(userId)
                .balance(account.getBalance())
                .totalEarned(account.getTotalEarned())
                .totalSpent(account.getTotalSpent())
                .integrityScore(score.getScore())
                .integrityLevel(level.getLabel())
                .earnMultiplier(level.getMultiplier())
                .canSpend(score.getScore() >= IntegrityService.SPEND_THRESHOLD)
                .build();
    }

    public List<CreditRuleVO> listEnabledRules() {
        List<CreditRule> rules = creditRuleMapper.selectList(
                new LambdaQueryWrapper<CreditRule>()
                        .eq(CreditRule::getEnabled, 1)
                        .orderByAsc(CreditRule::getId)
        );
        return rules.stream().map(r -> CreditRuleVO.builder()
                .ruleCode(r.getRuleCode())
                .ruleName(r.getRuleName())
                .amount(r.getAmount())
                .bizType(r.getBizType())
                .description(r.getDescription())
                .dailyLimit(DEFAULT_DAILY_LIMIT.getOrDefault(r.getRuleCode(), null))
                .minIntegrity(DEFAULT_MIN_INTEGRITY.getOrDefault(r.getRuleCode(), 60))
                .build()
        ).toList();
    }

    public List<CreditTransactionVO> listTransactions(Long userId, int limit) {
        int pageLimit = Math.min(Math.max(limit, 1), 100);
        List<CreditTransaction> list = creditTransactionMapper.selectList(
                new LambdaQueryWrapper<CreditTransaction>()
                        .eq(CreditTransaction::getUserId, userId)
                        .orderByDesc(CreditTransaction::getCreateTime)
                        .last("LIMIT " + pageLimit)
        );
        return list.stream().map(this::toTxnVO).toList();
    }

    /**
     * 按规则入账：校验诚信门槛、日上限、幂等，并应用诚信倍率。
     */
    @Transactional
    public CreditChangeResult earnByRule(Long userId, CreditEarnRequest request) {
        CreditRule rule = creditRuleMapper.selectOne(
                new LambdaQueryWrapper<CreditRule>()
                        .eq(CreditRule::getRuleCode, request.getRuleCode())
                        .eq(CreditRule::getEnabled, 1)
        );
        if (rule == null) {
            throw new BusinessException(404, "秩点规则不存在或已停用: " + request.getRuleCode());
        }

        IntegrityScore score = integrityService.ensureScore(userId);
        int minIntegrity = DEFAULT_MIN_INTEGRITY.getOrDefault(rule.getRuleCode(), 60);
        if (score.getScore() < minIntegrity) {
            throw new BusinessException(403, "诚信分不足 " + minIntegrity + "，无法获得该奖励");
        }

        // 幂等：同一业务引用只入账一次
        if (StringUtils.hasText(request.getRefType()) && request.getRefId() != null) {
            long exists = creditTransactionMapper.countByBizRef(
                    userId, rule.getBizType(), request.getRefType(), request.getRefId());
            if (exists > 0) {
                throw new BusinessException(409, "该业务已发放过秩点，请勿重复领取");
            }
        }

        Integer dailyLimit = DEFAULT_DAILY_LIMIT.get(rule.getRuleCode());
        if (dailyLimit != null && dailyLimit > 0) {
            LocalDate today = LocalDate.now();
            long count = creditTransactionMapper.countByUserBizBetween(
                    userId, rule.getBizType(), today.atStartOfDay(), today.plusDays(1).atStartOfDay());
            if (count >= dailyLimit) {
                throw new BusinessException(429, "今日该奖励已达上限（" + dailyLimit + " 次）");
            }
        }

        BigDecimal base = request.getAmountOverride() != null
                ? request.getAmountOverride()
                : rule.getAmount();
        if (base == null || base.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("奖励秩点必须大于 0");
        }

        double multiplier = IntegrityLevel.fromScore(score.getScore()).getMultiplier();
        BigDecimal finalAmount = base.multiply(BigDecimal.valueOf(multiplier))
                .setScale(2, RoundingMode.HALF_UP);

        String source = StringUtils.hasText(request.getSource())
                ? request.getSource()
                : rule.getRuleName() + "（倍率 " + multiplier + "）";

        return applyChange(
                userId,
                finalAmount,
                CreditTxnType.EARN,
                rule.getBizType(),
                source,
                request.getRefType(),
                request.getRefId()
        );
    }

    /**
     * 消耗学分（商城/报名等），需诚信分达标。
     */
    @Transactional
    public CreditChangeResult spend(Long userId, CreditSpendRequest request) {
        integrityService.assertCanSpend(userId);

        if (StringUtils.hasText(request.getRefType()) && request.getRefId() != null) {
            long exists = creditTransactionMapper.countByBizRef(
                    userId, request.getBizType(), request.getRefType(), request.getRefId());
            if (exists > 0) {
                throw new BusinessException(409, "该业务已扣过秩点，请勿重复提交");
            }
        }

        String source = StringUtils.hasText(request.getSource())
                ? request.getSource()
                : "秩点消耗: " + request.getBizType();

        return applyChange(
                userId,
                request.getAmount().negate(),
                CreditTxnType.SPEND,
                request.getBizType(),
                source,
                request.getRefType(),
                request.getRefId()
        );
    }

    /**
     * 退款回滚（正数入账，不走规则倍率）。
     */
    @Transactional
    public CreditChangeResult refund(Long userId, BigDecimal amount, String bizType,
                                     String refType, Long refId, String source) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("退款金额必须大于 0");
        }
        return applyChange(
                userId,
                amount,
                CreditTxnType.CONVERT,
                bizType,
                StringUtils.hasText(source) ? source : "秩点退款",
                refType,
                refId
        );
    }

    private CreditChangeResult applyChange(Long userId, BigDecimal delta, int txnType,
                                           String bizType, String source,
                                           String refType, Long refId) {
        ensureAccount(userId);
        CreditAccount locked = creditAccountExtMapper.selectForUpdate(userId);
        if (locked == null) {
            throw new BusinessException(404, "秩点账户不存在");
        }

        BigDecimal balance = nz(locked.getBalance());
        BigDecimal earned = nz(locked.getTotalEarned());
        BigDecimal spent = nz(locked.getTotalSpent());
        BigDecimal after = balance.add(delta);

        if (after.compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException(400, "秩点余额不足，当前余额 " + balance);
        }

        if (delta.compareTo(BigDecimal.ZERO) > 0) {
            earned = earned.add(delta);
        } else if (delta.compareTo(BigDecimal.ZERO) < 0) {
            spent = spent.add(delta.abs());
        }

        creditAccountExtMapper.updateBalance(userId, after, earned, spent);

        CreditTransaction txn = new CreditTransaction();
        txn.setUserId(userId);
        txn.setType(txnType);
        txn.setAmount(delta);
        txn.setBalanceAfter(after);
        txn.setBizType(bizType);
        txn.setSource(source);
        txn.setRefType(refType);
        txn.setRefId(refId);
        creditTransactionMapper.insert(txn);

        return CreditChangeResult.builder()
                .userId(userId)
                .amount(delta)
                .balanceAfter(after)
                .transactionId(txn.getId())
                .message("ok")
                .build();
    }

    private BigDecimal nz(BigDecimal v) {
        return v == null ? BigDecimal.ZERO : v;
    }

    private CreditTransactionVO toTxnVO(CreditTransaction t) {
        return CreditTransactionVO.builder()
                .id(t.getId())
                .type(t.getType())
                .typeName(typeName(t.getType()))
                .amount(t.getAmount())
                .balanceAfter(t.getBalanceAfter())
                .bizType(t.getBizType())
                .source(t.getSource())
                .refType(t.getRefType())
                .refId(t.getRefId())
                .createTime(t.getCreateTime())
                .build();
    }

    private String typeName(Integer type) {
        if (type == null) return "未知";
        return switch (type) {
            case CreditTxnType.EARN -> "获取";
            case CreditTxnType.CONVERT -> "转换";
            case CreditTxnType.BONUS -> "增长";
            case CreditTxnType.SPEND -> "消耗";
            default -> "未知";
        };
    }
}
