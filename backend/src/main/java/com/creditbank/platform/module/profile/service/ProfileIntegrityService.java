package com.creditbank.platform.module.profile.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.creditbank.platform.common.BusinessException;
import com.creditbank.platform.common.PageResult;
import com.creditbank.platform.entity.IntegrityRecord;
import com.creditbank.platform.entity.IntegrityScore;
import com.creditbank.platform.mapper.IntegrityRecordMapper;
import com.creditbank.platform.mapper.IntegrityScoreMapper;
import com.creditbank.platform.module.profile.dto.IntegrityRecordVO;
import com.creditbank.platform.module.profile.dto.IntegritySummaryVO;
import com.creditbank.platform.security.AuthSupport;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;

@Service
@RequiredArgsConstructor
public class ProfileIntegrityService {

    private static final int EVENT_BONUS = 1;
    private static final int EVENT_PENALTY = 2;
    private static final int DEFAULT_SCORE = 100;

    private final AuthSupport authSupport;
    private final IntegrityScoreMapper integrityScoreMapper;
    private final IntegrityRecordMapper integrityRecordMapper;

    public IntegritySummaryVO getSummary() {
        Long userId = authSupport.requireUserId();
        IntegrityScore score = integrityScoreMapper.selectById(userId);
        int currentScore = score != null && score.getScore() != null ? score.getScore() : DEFAULT_SCORE;
        return IntegritySummaryVO.builder()
                .score(currentScore)
                .levelName(levelName(currentScore))
                .updateTime(score != null ? score.getUpdateTime() : null)
                .build();
    }

    public PageResult<IntegrityRecordVO> pageRecords(
            long page, long pageSize, Integer eventType, LocalDate startDate, LocalDate endDate) {
        Long userId = authSupport.requireUserId();
        if (page < 1 || pageSize < 1 || pageSize > 100) {
            throw new BusinessException(400, "分页参数无效");
        }
        if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
            throw new BusinessException(400, "开始日期不能晚于结束日期");
        }

        LambdaQueryWrapper<IntegrityRecord> wrapper = new LambdaQueryWrapper<IntegrityRecord>()
                .eq(IntegrityRecord::getUserId, userId)
                .eq(eventType != null, IntegrityRecord::getEventType, eventType)
                .orderByDesc(IntegrityRecord::getCreateTime);
        if (startDate != null) {
            wrapper.ge(IntegrityRecord::getCreateTime, startDate.atStartOfDay());
        }
        if (endDate != null) {
            wrapper.le(IntegrityRecord::getCreateTime, endDate.atTime(LocalTime.MAX));
        }

        Page<IntegrityRecord> result = integrityRecordMapper.selectPage(new Page<>(page, pageSize), wrapper);
        return PageResult.of(
                result.getRecords().stream().map(this::toVO).toList(),
                result.getTotal(),
                result.getCurrent(),
                result.getSize());
    }

    private IntegrityRecordVO toVO(IntegrityRecord record) {
        return IntegrityRecordVO.builder()
                .id(record.getId())
                .changeValue(record.getChangeValue())
                .scoreAfter(record.getScoreAfter())
                .eventType(record.getEventType())
                .eventTypeName(eventTypeName(record.getEventType()))
                .reason(record.getReason())
                .refType(record.getRefType())
                .refId(record.getRefId())
                .createTime(record.getCreateTime())
                .build();
    }

    static String eventTypeName(Integer eventType) {
        if (eventType == null) {
            return "未知";
        }
        return switch (eventType) {
            case EVENT_BONUS -> "加分";
            case EVENT_PENALTY -> "扣分";
            default -> "未知";
        };
    }

    static String levelName(int score) {
        if (score >= 90) {
            return "优秀";
        }
        if (score >= 80) {
            return "良好";
        }
        if (score >= 60) {
            return "一般";
        }
        return "较差";
    }
}
