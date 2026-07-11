package com.creditbank.platform.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.creditbank.platform.dto.CourseDanmakuVO;
import com.creditbank.platform.entity.CourseDanmaku;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface CourseDanmakuMapper extends BaseMapper<CourseDanmaku> {

    @Select("""
            SELECT cd.id, cd.course_id AS courseId, cd.user_id AS userId,
                   COALESCE(NULLIF(u.real_name, ''), u.username) AS authorName,
                   cd.content, cd.video_time_seconds AS videoTimeSeconds,
                   cd.color, cd.create_time AS createTime
            FROM course_danmaku cd
            JOIN sys_user u ON u.id = cd.user_id
            WHERE cd.deleted = 0 AND cd.course_id = #{courseId}
            ORDER BY cd.video_time_seconds ASC, cd.id ASC
            """)
    List<CourseDanmakuVO> listByCourseId(@Param("courseId") Long courseId);
}
