package com.creditbank.platform.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.creditbank.platform.common.BusinessException;
import com.creditbank.platform.constant.IntegrityLevel;
import com.creditbank.platform.dto.IntegrityAdjustRequest;
import com.creditbank.platform.dto.IntegrityRecordVO;
import com.creditbank.platform.dto.IntegrityScoreVO;
import com.creditbank.platform.entity.IntegrityRecord;
import com.creditbank.platform.entity.IntegrityScore;
import com.creditbank.platform.mapper.IntegrityRecordMapper;
import com.creditbank.platform.mapper.IntegrityScoreMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class IntegrityService {

    public static final int DEFAULT_SCORE = 100;
    public static final int MIN_SCORE = 0;
    public static final int MAX_SCORE = 100;
    /** 低于此分禁止商城兑换 / 活动报名 */
    public static final int SPEND_THRESHOLD = 60;
    /** 低于此分限制发帖（先审后发由业务侧处理，此处仅标记） */
    public static final int POST_THRESHOLD = 60;

    private final IntegrityScoreMapper integrityScoreMapper;
    private final IntegrityRecordMapper integrityRecordMapper;

    public IntegrityScore ensureScore(Long userId) {
        IntegrityScore score = integrityScoreMapper.selectById(userId);
        if (score == null) {
            score = new IntegrityScore();
            score.setUserId(userId);
            score.setScore(DEFAULT_SCORE);
            integrityScoreMapper.insert(score);
        }
        return score;
    }

    public IntegrityScoreVO getMyScore(Long userId) {
        IntegrityScore score = ensureScore(userId);
        IntegrityLevel level = IntegrityLevel.fromScore(score.getScore());
        return IntegrityScoreVO.builder()
                .userId(userId)
                .score(score.getScore())
                .level(level.name())
                .levelLabel(level.getLabel())
                .earnMultiplier(level.getMultiplier())
                .canSpend(score.getScore() >= SPEND_THRESHOLD)
                .canPost(score.getScore() >= POST_THRESHOLD)
                .canRegisterActivity(score.getScore() >= SPEND_THRESHOLD)
                .build();
    }

    public double getEarnMultiplier(Long userId) {
        IntegrityScore score = ensureScore(userId);
        return IntegrityLevel.fromScore(score.getScore()).getMultiplier();
    }

    public void assertCanSpend(Long userId) {
        IntegrityScore score = ensureScore(userId);
        if (score.getScore() < SPEND_THRESHOLD) {
            throw new BusinessException(403, "诚信分不足 " + SPEND_THRESHOLD + "，暂不可兑换或报名");
        }
    }

    public List<IntegrityRecordVO> listRecords(Long userId, int limit) {
        int pageLimit = Math.min(Math.max(limit, 1), 100);
        List<IntegrityRecord> records = integrityRecordMapper.selectList(
                new LambdaQueryWrapper<IntegrityRecord>()
                        .eq(IntegrityRecord::getUserId, userId)
                        .orderByDesc(IntegrityRecord::getCreateTime)
                        .last("LIMIT " + pageLimit)
        );
        return records.stream().map(this::toVO).toList();
    }

    @Transactional
    public IntegrityScoreVO adjust(Long userId, IntegrityAdjustRequest request, Long operatorId) {
        if (request.getChangeValue() == 0) {
            throw new BusinessException("变动分值不能为 0");
        }
        IntegrityScore score = ensureScore(userId);
        int before = score.getScore();
        int after = clamp(before + request.getChangeValue());
        int actualChange = after - before;
        if (actualChange == 0) {
            throw new BusinessException("分数已达边界，无法继续调整");
        }

        score.setScore(after);
        integrityScoreMapper.updateById(score);

        IntegrityRecord record = new IntegrityRecord();
        record.setUserId(userId);
        record.setChangeValue(actualChange);
        record.setScoreAfter(after);
        record.setEventType(actualChange > 0 ? 1 : 2);
        record.setReason(request.getReason());
        record.setRefType(request.getRefType());
        record.setRefId(request.getRefId());
        record.setOperatorId(operatorId);
        integrityRecordMapper.insert(record);

        return getMyScore(userId);
    }

    /** 按预设事件加减分（供业务模块调用） */
    @Transactional
    public IntegrityScoreVO applyEvent(Long userId, int changeValue, String reason,
                                       String refType, Long refId, Long operatorId) {
        IntegrityAdjustRequest req = new IntegrityAdjustRequest();
        req.setChangeValue(changeValue);
        req.setReason(reason);
        req.setRefType(refType);
        req.setRefId(refId);
        return adjust(userId, req, operatorId);
    }

    private int clamp(int score) {
        return Math.max(MIN_SCORE, Math.min(MAX_SCORE, score));
    }

    private IntegrityRecordVO toVO(IntegrityRecord r) {
        return IntegrityRecordVO.builder()
                .id(r.getId())
                .changeValue(r.getChangeValue())
                .scoreAfter(r.getScoreAfter())
                .eventType(r.getEventType())
                .eventTypeName(r.getEventType() != null && r.getEventType() == 1 ? "加分" : "扣分")
                .reason(r.getReason())
                .refType(r.getRefType())
                .refId(r.getRefId())
                .createTime(r.getCreateTime())
                .build();
    }
}
