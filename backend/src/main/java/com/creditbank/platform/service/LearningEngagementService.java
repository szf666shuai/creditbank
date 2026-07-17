package com.creditbank.platform.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.creditbank.platform.common.BusinessException;
import com.creditbank.platform.dto.CourseEpisodeVO;
import com.creditbank.platform.dto.IntegrityScoreVO;
import com.creditbank.platform.dto.LearningCheckinVO;
import com.creditbank.platform.dto.LearningProgressRequest;
import com.creditbank.platform.dto.LearningReminderRequest;
import com.creditbank.platform.dto.LearningReminderVO;
import com.creditbank.platform.entity.Course;
import com.creditbank.platform.entity.CourseEpisode;
import com.creditbank.platform.entity.UserCourse;
import com.creditbank.platform.entity.UserEpisodeProgress;
import com.creditbank.platform.entity.UserLearningCheckin;
import com.creditbank.platform.entity.UserLearningReminder;
import com.creditbank.platform.mapper.CourseEpisodeMapper;
import com.creditbank.platform.mapper.CourseMapper;
import com.creditbank.platform.mapper.UserCourseMapper;
import com.creditbank.platform.mapper.UserEpisodeProgressMapper;
import com.creditbank.platform.mapper.UserLearningCheckinMapper;
import com.creditbank.platform.mapper.UserLearningReminderMapper;
import com.creditbank.platform.module.profile.service.ProfileLearningStatsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LearningEngagementService {

    private final CourseMapper courseMapper;
    private final CourseEpisodeMapper courseEpisodeMapper;
    private final UserEpisodeProgressMapper userEpisodeProgressMapper;
    private final UserCourseMapper userCourseMapper;
    private final UserLearningCheckinMapper userLearningCheckinMapper;
    private final UserLearningReminderMapper userLearningReminderMapper;
    private final ProfileLearningStatsService profileLearningStatsService;
    private final IntegrityService integrityService;

    public List<CourseEpisodeVO> listEpisodes(Long userId, Long courseId) {
        requireCourse(courseId);
        List<CourseEpisode> episodes = courseEpisodeMapper.listByCourseId(courseId);
        return episodes.stream().map(episode -> {
            CourseEpisodeVO vo = new CourseEpisodeVO();
            vo.setId(episode.getId());
            vo.setCourseId(episode.getCourseId());
            vo.setTitle(episode.getTitle());
            vo.setVideoUrl(episode.getVideoUrl());
            vo.setVideoDurationSeconds(episode.getVideoDurationSeconds());
            vo.setSortOrder(episode.getSortOrder());
            UserEpisodeProgress progress = userEpisodeProgressMapper.selectOne(
                    new LambdaQueryWrapper<UserEpisodeProgress>()
                            .eq(UserEpisodeProgress::getUserId, userId)
                            .eq(UserEpisodeProgress::getEpisodeId, episode.getId())
            );
            if (progress != null) {
                vo.setProgress(progress.getProgress());
                vo.setMaxWatchedPositionSeconds(progress.getMaxWatchedPositionSeconds());
                vo.setLastPositionSeconds(progress.getLastPositionSeconds());
            } else {
                vo.setProgress(0);
                vo.setMaxWatchedPositionSeconds(0);
                vo.setLastPositionSeconds(0);
            }
            return vo;
        }).toList();
    }

    @Transactional
    public UserCourse reportProgress(Long userId, Long courseId, LearningProgressRequest request) {
        Course course = requireCourse(courseId);
        List<CourseEpisode> episodes = courseEpisodeMapper.listByCourseId(courseId);
        if (episodes.isEmpty()) {
            throw new BusinessException(400, "该课程暂未配置分集内容");
        }
        CourseEpisode episode = resolveEpisode(episodes, request.getEpisodeId());
        int durationSeconds = requireEpisodeDuration(episode);
        UserEpisodeProgress record = userEpisodeProgressMapper.selectOne(
                new LambdaQueryWrapper<UserEpisodeProgress>()
                        .eq(UserEpisodeProgress::getUserId, userId)
                        .eq(UserEpisodeProgress::getEpisodeId, episode.getId())
        );
        if (record == null) {
            record = new UserEpisodeProgress();
            record.setUserId(userId);
            record.setCourseId(courseId);
            record.setEpisodeId(episode.getId());
            record.setProgress(0);
            record.setWatchedSeconds(0);
            record.setMaxWatchedPositionSeconds(0);
            record.setLastPositionSeconds(0);
            record.setStatus(0);
            userEpisodeProgressMapper.insert(record);
        }

        int watchedSeconds = record.getWatchedSeconds() == null ? 0 : record.getWatchedSeconds();
        int maxWatchedPosition = record.getMaxWatchedPositionSeconds() == null ? 0 : record.getMaxWatchedPositionSeconds();
        int watchedDelta = Math.min(Math.max(request.getWatchedDeltaSeconds(), 0), 30);
        int previousWatchedSeconds = watchedSeconds;
        watchedSeconds = Math.min(durationSeconds, watchedSeconds + watchedDelta);
        int acceptedWatchedDelta = Math.max(0, watchedSeconds - previousWatchedSeconds);
        int currentPosition = Math.min(durationSeconds, Math.max(request.getCurrentTimeSeconds(), 0));
        int maximumAllowedPosition = Math.min(durationSeconds, maxWatchedPosition + watchedDelta);
        int acceptedPosition = Math.min(currentPosition, maximumAllowedPosition);
        maxWatchedPosition = Math.max(maxWatchedPosition, acceptedPosition);
        int progress = Math.min(100, (int) Math.floor(maxWatchedPosition * 100.0 / durationSeconds));

        record.setWatchedSeconds(watchedSeconds);
        record.setMaxWatchedPositionSeconds(maxWatchedPosition);
        record.setLastPositionSeconds(acceptedPosition);
        record.setProgress(progress);
        userEpisodeProgressMapper.updateById(record);
        if (acceptedWatchedDelta > 0) {
            profileLearningStatsService.recordStudySeconds(userId, acceptedWatchedDelta);
        }

        return syncCourseProgress(userId, course, episodes);
    }

    public LearningReminderVO getReminder(Long userId, Long courseId) {
        requireCourse(courseId);
        UserLearningReminder reminder = userLearningReminderMapper.selectOne(
                new LambdaQueryWrapper<UserLearningReminder>()
                        .eq(UserLearningReminder::getUserId, userId)
                        .eq(UserLearningReminder::getCourseId, courseId)
        );
        LearningReminderVO vo = new LearningReminderVO();
        vo.setCourseId(courseId);
        if (reminder == null) {
            vo.setEnabled(false);
            vo.setIntervalMinutes(30);
            return vo;
        }
        vo.setEnabled(reminder.getEnabled() != null && reminder.getEnabled() == 1);
        vo.setIntervalMinutes(reminder.getIntervalMinutes());
        return vo;
    }

    @Transactional
    public LearningReminderVO saveReminder(Long userId, Long courseId, LearningReminderRequest request) {
        requireCourse(courseId);
        UserLearningReminder reminder = userLearningReminderMapper.selectOne(
                new LambdaQueryWrapper<UserLearningReminder>()
                        .eq(UserLearningReminder::getUserId, userId)
                        .eq(UserLearningReminder::getCourseId, courseId)
        );
        if (reminder == null) {
            reminder = new UserLearningReminder();
            reminder.setUserId(userId);
            reminder.setCourseId(courseId);
            reminder.setCreateTime(LocalDateTime.now());
        }
        reminder.setEnabled(Boolean.FALSE.equals(request.getEnabled()) ? 0 : 1);
        reminder.setIntervalMinutes(request.getIntervalMinutes() == null ? 30 : request.getIntervalMinutes());
        reminder.setUpdateTime(LocalDateTime.now());
        if (reminder.getId() == null) {
            userLearningReminderMapper.insert(reminder);
        } else {
            userLearningReminderMapper.updateById(reminder);
        }
        return getReminder(userId, courseId);
    }

    public LearningCheckinVO getCheckinStatus(Long userId, Long courseId) {
        requireCourse(courseId);
        boolean checkedInToday = hasCheckinToday(userId, courseId);
        return LearningCheckinVO.builder()
                .courseId(courseId)
                .checkedInToday(checkedInToday)
                .streakDays(countStreakDays(userId, courseId))
                .message(checkedInToday ? "今日已打卡" : "今日尚未打卡")
                .build();
    }

    @Transactional
    public LearningCheckinVO checkin(Long userId, Long courseId) {
        requireCourse(courseId);
        if (hasCheckinToday(userId, courseId)) {
            throw new BusinessException(409, "今日已打卡，明天再来吧");
        }
        UserCourse userCourse = userCourseMapper.selectOne(
                new LambdaQueryWrapper<UserCourse>()
                        .eq(UserCourse::getUserId, userId)
                        .eq(UserCourse::getCourseId, courseId)
        );
        if (userCourse == null) {
            throw new BusinessException(400, "请先开始学习后再打卡");
        }
        UserLearningCheckin checkin = new UserLearningCheckin();
        checkin.setUserId(userId);
        checkin.setCourseId(courseId);
        checkin.setCheckinDate(LocalDate.now());
        checkin.setCreateTime(LocalDateTime.now());
        userLearningCheckinMapper.insert(checkin);

        Integer integrityReward = null;
        try {
            int before = integrityService.getMyScore(userId).getScore();
            IntegrityScoreVO after = integrityService.applyEvent(userId, 1, "课程学习打卡",
                    "learning_checkin", checkin.getId(), null);
            if (after != null && after.getScore() != null && after.getScore() > before) {
                integrityReward = after.getScore() - before;
            }
        } catch (Exception ignored) {
            // 诚信分奖励失败不阻断打卡
        }

        return LearningCheckinVO.builder()
                .courseId(courseId)
                .checkedInToday(true)
                .streakDays(countStreakDays(userId, courseId))
                .integrityReward(integrityReward)
                .message("打卡成功，坚持学习很棒！")
                .build();
    }

    public int totalEpisodeDuration(Long courseId) {
        return courseEpisodeMapper.listByCourseId(courseId).stream()
                .mapToInt(episode -> episode.getVideoDurationSeconds() == null ? 0 : episode.getVideoDurationSeconds())
                .sum();
    }

    private UserCourse syncCourseProgress(Long userId, Course course, List<CourseEpisode> episodes) {
        UserCourse userCourse = userCourseMapper.selectOne(
                new LambdaQueryWrapper<UserCourse>()
                        .eq(UserCourse::getUserId, userId)
                        .eq(UserCourse::getCourseId, course.getId())
        );
        if (userCourse == null) {
            userCourse = new UserCourse();
            userCourse.setUserId(userId);
            userCourse.setCourseId(course.getId());
            userCourse.setStatus(0);
            userCourse.setStartTime(LocalDateTime.now());
            userCourseMapper.insert(userCourse);
        } else if (userCourse.getStartTime() == null) {
            userCourse.setStartTime(LocalDateTime.now());
        }

        int totalDuration = episodes.stream()
                .mapToInt(episode -> episode.getVideoDurationSeconds() == null ? 0 : episode.getVideoDurationSeconds())
                .sum();
        int weightedPosition = 0;
        int watchedSeconds = 0;
        for (CourseEpisode episode : episodes) {
            int episodeDuration = episode.getVideoDurationSeconds() == null ? 0 : episode.getVideoDurationSeconds();
            UserEpisodeProgress progress = userEpisodeProgressMapper.selectOne(
                    new LambdaQueryWrapper<UserEpisodeProgress>()
                            .eq(UserEpisodeProgress::getUserId, userId)
                            .eq(UserEpisodeProgress::getEpisodeId, episode.getId())
            );
            int maxPosition = progress == null || progress.getMaxWatchedPositionSeconds() == null
                    ? 0 : progress.getMaxWatchedPositionSeconds();
            int episodeWatched = progress == null || progress.getWatchedSeconds() == null ? 0 : progress.getWatchedSeconds();
            weightedPosition += maxPosition;
            watchedSeconds += episodeWatched;
        }
        int courseProgress = totalDuration <= 0
                ? 0
                : Math.min(100, (int) Math.floor(weightedPosition * 100.0 / totalDuration));
        userCourse.setProgress(courseProgress);
        userCourse.setWatchedSeconds(watchedSeconds);
        userCourse.setMaxWatchedPositionSeconds(weightedPosition);
        userCourse.setLastPositionSeconds(weightedPosition);
        userCourseMapper.updateById(userCourse);
        return userCourse;
    }

    private CourseEpisode resolveEpisode(List<CourseEpisode> episodes, Long episodeId) {
        if (episodeId == null) {
            return episodes.get(0);
        }
        return episodes.stream()
                .filter(episode -> episode.getId().equals(episodeId))
                .findFirst()
                .orElseThrow(() -> new BusinessException(404, "课程分集不存在"));
    }

    private int requireEpisodeDuration(CourseEpisode episode) {
        if (!StringUtils.hasText(episode.getVideoUrl())
                || episode.getVideoDurationSeconds() == null
                || episode.getVideoDurationSeconds() <= 0) {
            throw new BusinessException(400, "该分集暂未配置可学习的视频");
        }
        return episode.getVideoDurationSeconds();
    }

    private boolean hasCheckinToday(Long userId, Long courseId) {
        return userLearningCheckinMapper.selectCount(
                new LambdaQueryWrapper<UserLearningCheckin>()
                        .eq(UserLearningCheckin::getUserId, userId)
                        .eq(UserLearningCheckin::getCourseId, courseId)
                        .eq(UserLearningCheckin::getCheckinDate, LocalDate.now())
        ) > 0;
    }

    private int countStreakDays(Long userId, Long courseId) {
        int streak = 0;
        LocalDate day = LocalDate.now();
        while (userLearningCheckinMapper.selectCount(
                new LambdaQueryWrapper<UserLearningCheckin>()
                        .eq(UserLearningCheckin::getUserId, userId)
                        .eq(UserLearningCheckin::getCourseId, courseId)
                        .eq(UserLearningCheckin::getCheckinDate, day)
        ) > 0) {
            streak += 1;
            day = day.minusDays(1);
        }
        return streak;
    }

    private Course requireCourse(Long courseId) {
        Course course = courseMapper.selectById(courseId);
        if (course == null || course.getStatus() == null || course.getStatus() != 1) {
            throw new BusinessException(404, "学习资源不存在或已下架");
        }
        return course;
    }
}
