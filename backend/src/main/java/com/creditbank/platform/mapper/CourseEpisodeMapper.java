package com.creditbank.platform.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.creditbank.platform.entity.CourseEpisode;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface CourseEpisodeMapper extends BaseMapper<CourseEpisode> {

    @Select("""
            SELECT id, course_id AS courseId, title, video_url AS videoUrl,
                   video_duration_seconds AS videoDurationSeconds, sort_order AS sortOrder, status
            FROM course_episode
            WHERE deleted = 0 AND status = 1 AND course_id = #{courseId}
            ORDER BY sort_order ASC, id ASC
            """)
    List<CourseEpisode> listByCourseId(@Param("courseId") Long courseId);
}
