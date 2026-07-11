package com.creditbank.platform.module.profile.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.creditbank.platform.common.BusinessException;
import com.creditbank.platform.entity.LearningArchive;
import com.creditbank.platform.entity.UserCourse;
import com.creditbank.platform.mapper.LearningArchiveMapper;
import com.creditbank.platform.mapper.UserCourseMapper;
import com.creditbank.platform.module.profile.dto.LearningStatDailyVO;
import com.creditbank.platform.module.profile.entity.LearningStatDaily;
import com.creditbank.platform.module.profile.mapper.LearningStatDailyMapper;
import com.creditbank.platform.security.AuthSupport;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class ProfileLearningStatsService {

    private static final int DEFAULT_DAYS = 30;
    private static final int ARCHIVE_TYPE_COURSE = 1;
    private static final int ARCHIVE_STATUS_DONE = 1;

    private final AuthSupport authSupport;
    private final LearningStatDailyMapper learningStatDailyMapper;
    private final LearningArchiveMapper learningArchiveMapper;
    private final UserCourseMapper userCourseMapper;

    /** 跨请求累计不足 1 分钟的秒数（单机部署足够；多实例会有轻微误差） */
    private final ConcurrentHashMap<Long, Integer> studySecondRemainder = new ConcurrentHashMap<>();

    public List<LearningStatDailyVO> listDailyStats(LocalDate startDate, LocalDate endDate) {
        Long userId = authSupport.requireUserId();
        LocalDate end = endDate != null ? endDate : LocalDate.now();
        LocalDate start = startDate != null ? startDate : end.minusDays(DEFAULT_DAYS - 1L);

        if (start.isAfter(end)) {
            throw new BusinessException(400, "开始日期不能晚于结束日期");
        }

        Map<LocalDate, MutableDayStat> byDate = new HashMap<>();
        for (LocalDate day = start; !day.isAfter(end); day = day.plusDays(1)) {
            byDate.put(day, new MutableDayStat());
        }

        mergeTrackedDailyStats(userId, start, end, byDate);
        mergeArchiveStats(userId, start, end, byDate);
        mergeCourseWatchStats(userId, start, end, byDate);

        List<LearningStatDailyVO> filled = new ArrayList<>();
        for (LocalDate day = start; !day.isAfter(end); day = day.plusDays(1)) {
            MutableDayStat item = byDate.get(day);
            filled.add(LearningStatDailyVO.builder()
                    .statDate(day)
                    .studyMinutes(item.studyMinutes)
                    .coursesCompleted(item.coursesCompleted)
                    .creditEarned(item.creditEarned)
                    .build());
        }
        return filled;
    }

    private void mergeTrackedDailyStats(Long userId, LocalDate start, LocalDate end,
                                        Map<LocalDate, MutableDayStat> byDate) {
        List<LearningStatDaily> stats = learningStatDailyMapper.selectList(
                new LambdaQueryWrapper<LearningStatDaily>()
                        .eq(LearningStatDaily::getUserId, userId)
                        .ge(LearningStatDaily::getStatDate, start)
                        .le(LearningStatDaily::getStatDate, end));
        for (LearningStatDaily item : stats) {
            MutableDayStat day = byDate.get(item.getStatDate());
            if (day == null) {
                continue;
            }
            day.studyMinutes = Math.max(day.studyMinutes, nz(item.getStudyMinutes()));
            day.coursesCompleted = Math.max(day.coursesCompleted, nz(item.getCoursesCompleted()));
            day.creditEarned = day.creditEarned.max(
                    item.getCreditEarned() == null ? BigDecimal.ZERO : item.getCreditEarned());
        }
    }

    /**
     * 用学习档案补齐完课数与学分（历史完成记录可能早于 daily 写入逻辑）。
     */
    private void mergeArchiveStats(Long userId, LocalDate start, LocalDate end,
                                   Map<LocalDate, MutableDayStat> byDate) {
        List<LearningArchive> archives = learningArchiveMapper.selectList(
                new LambdaQueryWrapper<LearningArchive>()
                        .eq(LearningArchive::getUserId, userId)
                        .eq(LearningArchive::getStatus, ARCHIVE_STATUS_DONE));
        Map<LocalDate, Integer> coursesByDay = new HashMap<>();
        Map<LocalDate, BigDecimal> creditByDay = new HashMap<>();
        for (LearningArchive archive : archives) {
            LocalDate day = resolveArchiveDate(archive);
            if (day == null || day.isBefore(start) || day.isAfter(end)) {
                continue;
            }
            if (archive.getArchiveType() != null && archive.getArchiveType() == ARCHIVE_TYPE_COURSE) {
                coursesByDay.merge(day, 1, Integer::sum);
            }
            BigDecimal credit = archive.getCreditEarned() == null ? BigDecimal.ZERO : archive.getCreditEarned();
            creditByDay.merge(day, credit, BigDecimal::add);
        }
        for (Map.Entry<LocalDate, Integer> entry : coursesByDay.entrySet()) {
            MutableDayStat day = byDate.get(entry.getKey());
            if (day != null) {
                day.coursesCompleted = Math.max(day.coursesCompleted, entry.getValue());
            }
        }
        for (Map.Entry<LocalDate, BigDecimal> entry : creditByDay.entrySet()) {
            MutableDayStat day = byDate.get(entry.getKey());
            if (day != null) {
                day.creditEarned = day.creditEarned.max(entry.getValue());
            }
        }
    }

    /**
     * 用课程观看记录补齐学习时长（按完成日或开始日落在统计区间内）。
     */
    private void mergeCourseWatchStats(Long userId, LocalDate start, LocalDate end,
                                       Map<LocalDate, MutableDayStat> byDate) {
        List<UserCourse> courses = userCourseMapper.selectList(
                new LambdaQueryWrapper<UserCourse>()
                        .eq(UserCourse::getUserId, userId)
                        .gt(UserCourse::getWatchedSeconds, 0));
        Map<LocalDate, Integer> minutesByDay = new HashMap<>();
        for (UserCourse course : courses) {
            LocalDate day = resolveCourseDate(course);
            if (day == null || day.isBefore(start) || day.isAfter(end)) {
                continue;
            }
            int minutes = Math.max(0, nz(course.getWatchedSeconds()) / 60);
            if (minutes > 0) {
                minutesByDay.merge(day, minutes, Integer::sum);
            }
        }
        for (Map.Entry<LocalDate, Integer> entry : minutesByDay.entrySet()) {
            MutableDayStat day = byDate.get(entry.getKey());
            if (day != null) {
                day.studyMinutes = Math.max(day.studyMinutes, entry.getValue());
            }
        }
    }

    private static LocalDate resolveArchiveDate(LearningArchive archive) {
        if (archive.getEndDate() != null) {
            return archive.getEndDate();
        }
        if (archive.getStartDate() != null) {
            return archive.getStartDate();
        }
        if (archive.getCreateTime() != null) {
            return archive.getCreateTime().toLocalDate();
        }
        return null;
    }

    private static LocalDate resolveCourseDate(UserCourse course) {
        if (course.getCompleteTime() != null) {
            return course.getCompleteTime().toLocalDate();
        }
        if (course.getStartTime() != null) {
            return course.getStartTime().toLocalDate();
        }
        return null;
    }

    /**
     * 记录学习时长。进度上报多为几十秒增量，累计满 60 秒再写入分钟。
     */
    @Transactional
    public void recordStudySeconds(Long userId, int deltaSeconds) {
        if (userId == null || deltaSeconds <= 0) {
            return;
        }
        int remainder = studySecondRemainder.getOrDefault(userId, 0) + deltaSeconds;
        int addMinutes = remainder / 60;
        studySecondRemainder.put(userId, remainder % 60);
        if (addMinutes <= 0) {
            return;
        }
        learningStatDailyMapper.upsertAdd(
                userId,
                LocalDate.now(),
                addMinutes,
                0,
                BigDecimal.ZERO);
    }

    /** 课程首次完成时累加完课数与当日学分 */
    @Transactional
    public void recordCourseCompleted(Long userId, BigDecimal creditEarned) {
        if (userId == null) {
            return;
        }
        BigDecimal credit = creditEarned == null ? BigDecimal.ZERO : creditEarned;
        learningStatDailyMapper.upsertAdd(
                userId,
                LocalDate.now(),
                0,
                1,
                credit);
    }

    private static int nz(Integer value) {
        return value == null ? 0 : value;
    }

    private static final class MutableDayStat {
        private int studyMinutes;
        private int coursesCompleted;
        private BigDecimal creditEarned = BigDecimal.ZERO;
    }
}
