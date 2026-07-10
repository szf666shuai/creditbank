package com.creditbank.platform.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.creditbank.platform.dto.LearningResourceVO;
import com.creditbank.platform.entity.Course;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface CourseMapper extends BaseMapper<Course> {

    @Select("""
            SELECT c.id, c.title, c.description, c.cover_url AS coverUrl,
                   c.video_url AS videoUrl, c.video_duration_seconds AS videoDurationSeconds,
                   c.price_credit AS priceCredit, c.price_money AS priceMoney,
                   c.duration_hours AS durationHours, c.credit_reward AS creditReward,
                   o.name AS orgName,
                   GROUP_CONCAT(t.name ORDER BY t.name SEPARATOR ',') AS tags
            FROM course c
            LEFT JOIN sys_organization o ON o.id = c.org_id
            LEFT JOIN course_tag ct ON ct.course_id = c.id
            LEFT JOIN sys_tag t ON t.id = ct.tag_id
            WHERE c.deleted = 0 AND c.status = 1
              AND (#{keyword} IS NULL OR #{keyword} = ''
                   OR c.title LIKE CONCAT('%', #{keyword}, '%')
                   OR c.description LIKE CONCAT('%', #{keyword}, '%')
                   OR t.name LIKE CONCAT('%', #{keyword}, '%'))
              AND (#{tag} IS NULL OR #{tag} = '' OR EXISTS (
                   SELECT 1 FROM course_tag ct2
                   JOIN sys_tag t2 ON t2.id = ct2.tag_id
                   WHERE ct2.course_id = c.id AND t2.name = #{tag}
              ))
            GROUP BY c.id, c.title, c.description, c.cover_url, c.video_url,
                     c.video_duration_seconds, c.price_credit, c.price_money,
                     c.duration_hours, c.credit_reward, o.name, c.create_time
            ORDER BY c.create_time DESC
            """)
    List<LearningResourceVO> listResources(@Param("keyword") String keyword, @Param("tag") String tag);
}
