package com.creditbank.platform;

import com.creditbank.platform.common.BusinessException;
import com.creditbank.platform.dto.LearningProgressRequest;
import com.creditbank.platform.entity.Course;
import com.creditbank.platform.entity.UserCourse;
import com.creditbank.platform.mapper.CourseMapper;
import com.creditbank.platform.mapper.LearningAchievementMapper;
import com.creditbank.platform.mapper.LearningArchiveMapper;
import com.creditbank.platform.mapper.LearningCertificateMapper;
import com.creditbank.platform.mapper.SysTagMapper;
import com.creditbank.platform.mapper.UserCourseMapper;
import com.creditbank.platform.service.CreditService;
import com.creditbank.platform.service.LearningService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LearningProgressRuleTests {

    @Mock
    private CourseMapper courseMapper;
    @Mock
    private SysTagMapper sysTagMapper;
    @Mock
    private UserCourseMapper userCourseMapper;
    @Mock
    private LearningCertificateMapper certificateMapper;
    @Mock
    private LearningArchiveMapper archiveMapper;
    @Mock
    private LearningAchievementMapper achievementMapper;
    @Mock
    private CreditService creditService;

    @InjectMocks
    private LearningService learningService;

    private Course course;
    private UserCourse learningRecord;

    @BeforeEach
    void setUp() {
        course = new Course();
        course.setId(1L);
        course.setStatus(1);
        course.setVideoUrl("https://example.com/course.mp4");
        course.setVideoDurationSeconds(100);

        learningRecord = new UserCourse();
        learningRecord.setId(1L);
        learningRecord.setUserId(2L);
        learningRecord.setCourseId(1L);
        learningRecord.setProgress(0);
        learningRecord.setWatchedSeconds(0);
        learningRecord.setMaxWatchedPositionSeconds(0);
        learningRecord.setLastPositionSeconds(0);
        learningRecord.setStatus(0);

        when(courseMapper.selectById(1L)).thenReturn(course);
        when(userCourseMapper.selectOne(any())).thenReturn(learningRecord);
    }

    @Test
    void progressIsCalculatedFromFarthestContinuouslyWatchedPosition() {
        LearningProgressRequest request = new LearningProgressRequest();
        request.setWatchedDeltaSeconds(30);
        request.setCurrentTimeSeconds(30);

        UserCourse result = learningService.reportProgress(2L, 1L, request);

        assertEquals(30, result.getWatchedSeconds());
        assertEquals(30, result.getMaxWatchedPositionSeconds());
        assertEquals(30, result.getProgress());
        assertEquals(30, result.getLastPositionSeconds());
    }

    @Test
    void replayingWatchedContentDoesNotIncreaseProgress() {
        learningRecord.setWatchedSeconds(30);
        learningRecord.setMaxWatchedPositionSeconds(30);
        learningRecord.setLastPositionSeconds(10);
        learningRecord.setProgress(30);
        LearningProgressRequest request = new LearningProgressRequest();
        request.setWatchedDeltaSeconds(20);
        request.setCurrentTimeSeconds(30);

        UserCourse result = learningService.reportProgress(2L, 1L, request);

        assertEquals(50, result.getWatchedSeconds());
        assertEquals(30, result.getMaxWatchedPositionSeconds());
        assertEquals(30, result.getProgress());
    }

    @Test
    void unverifiedForwardJumpIsClampedByBackend() {
        learningRecord.setWatchedSeconds(30);
        learningRecord.setMaxWatchedPositionSeconds(30);
        learningRecord.setLastPositionSeconds(30);
        learningRecord.setProgress(30);
        LearningProgressRequest request = new LearningProgressRequest();
        request.setWatchedDeltaSeconds(10);
        request.setCurrentTimeSeconds(90);

        UserCourse result = learningService.reportProgress(2L, 1L, request);

        assertEquals(40, result.getMaxWatchedPositionSeconds());
        assertEquals(40, result.getProgress());
        assertEquals(40, result.getLastPositionSeconds());
    }

    @Test
    void certificateIsRejectedBelowEightyPercent() {
        learningRecord.setWatchedSeconds(79);
        learningRecord.setMaxWatchedPositionSeconds(79);
        learningRecord.setProgress(79);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> learningService.completeCourse(2L, 1L)
        );

        assertEquals(400, exception.getCode());
        assertEquals("课程观看进度需达到 80% 才能颁发合格证，当前为 79%", exception.getMessage());
    }
}
