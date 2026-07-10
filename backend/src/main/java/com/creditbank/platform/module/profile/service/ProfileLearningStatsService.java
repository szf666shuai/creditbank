package com.creditbank.platform.module.profile.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.creditbank.platform.common.BusinessException;
import com.creditbank.platform.module.profile.dto.LearningStatDailyVO;
import com.creditbank.platform.module.profile.entity.LearningStatDaily;
import com.creditbank.platform.module.profile.mapper.LearningStatDailyMapper;
import com.creditbank.platform.security.AuthSupport;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProfileLearningStatsService {

    private static final int DEFAULT_DAYS = 30;

    private final AuthSupport authSupport;
    private final LearningStatDailyMapper learningStatDailyMapper;

    public List<LearningStatDailyVO> listDailyStats(LocalDate startDate, LocalDate endDate) {
        Long userId = authSupport.requireUserId();
        LocalDate end = endDate != null ? endDate : LocalDate.now();
        LocalDate start = startDate != null ? startDate : end.minusDays(DEFAULT_DAYS - 1L);

        if (start.isAfter(end)) {
            throw new BusinessException(400, "开始日期不能晚于结束日期");
        }

        List<LearningStatDaily> stats = learningStatDailyMapper.selectList(
                new LambdaQueryWrapper<LearningStatDaily>()
                        .eq(LearningStatDaily::getUserId, userId)
                        .ge(LearningStatDaily::getStatDate, start)
                        .le(LearningStatDaily::getStatDate, end)
                        .orderByAsc(LearningStatDaily::getStatDate));

        return stats.stream()
                .map(item -> LearningStatDailyVO.builder()
                        .statDate(item.getStatDate())
                        .studyMinutes(item.getStudyMinutes())
                        .coursesCompleted(item.getCoursesCompleted())
                        .creditEarned(item.getCreditEarned())
                        .build())
                .toList();
    }
}
