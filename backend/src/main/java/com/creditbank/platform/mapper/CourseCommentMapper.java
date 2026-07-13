package com.creditbank.platform.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.creditbank.platform.dto.CourseCommentVO;
import com.creditbank.platform.entity.CourseComment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface CourseCommentMapper extends BaseMapper<CourseComment> {

    @Select("""
            SELECT cc.id, cc.course_id AS courseId, cc.user_id AS userId, cc.parent_id AS parentId,
                   COALESCE(NULLIF(u.real_name, ''), u.username) AS authorName,
                   u.avatar,
                   cc.content, cc.like_count AS likeCount,
                   CASE WHEN #{userId} IS NULL THEN FALSE
                        WHEN ccl.id IS NOT NULL THEN TRUE
                        ELSE FALSE END AS liked,
                   cc.create_time AS createTime
            FROM course_comment cc
            JOIN sys_user u ON u.id = cc.user_id
            LEFT JOIN course_comment_like ccl
                   ON ccl.comment_id = cc.id AND ccl.user_id = #{userId}
            WHERE cc.deleted = 0 AND cc.course_id = #{courseId}
            ORDER BY cc.create_time ASC
            LIMIT #{limit}
            """)
    List<CourseCommentVO> listByCourseId(@Param("courseId") Long courseId,
                                         @Param("userId") Long userId,
                                         @Param("limit") int limit);
}
