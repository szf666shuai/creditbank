package com.creditbank.platform.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.creditbank.platform.entity.UserLearningProfile;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface UserLearningProfileMapper extends BaseMapper<UserLearningProfile> {

    @Select("""
            SELECT u.id AS userId, u.username, u.real_name AS realName, u.role,
                   ca.total_earned AS creditEarned,
                   COALESCE(ins.score, 100) AS integrityScore
            FROM sys_user u
            LEFT JOIN credit_account ca ON ca.user_id = u.id
            LEFT JOIN integrity_score ins ON ins.user_id = u.id
            WHERE u.id = #{userId} AND u.deleted = 0
            """)
    Map<String, Object> selectUserBasics(@Param("userId") Long userId);

    @Select("""
            SELECT c.title AS courseTitle, uc.progress, uc.status, uc.paid_credit AS paidCredit,
                   GROUP_CONCAT(DISTINCT t.name ORDER BY t.name SEPARATOR ',') AS tags
            FROM user_course uc
            JOIN course c ON c.id = uc.course_id AND c.deleted = 0
            LEFT JOIN course_tag ct ON ct.course_id = c.id
            LEFT JOIN sys_tag t ON t.id = ct.tag_id
            WHERE uc.user_id = #{userId}
            GROUP BY uc.id, c.title, uc.progress, uc.status, uc.paid_credit
            ORDER BY uc.status ASC, uc.progress DESC
            """)
    List<Map<String, Object>> selectCourseProgress(@Param("userId") Long userId);

    @Select("""
            SELECT title, archive_type AS archiveType, category, description,
                   credit_earned AS creditEarned, status, start_date AS startDate, end_date AS endDate
            FROM learning_archive
            WHERE user_id = #{userId} AND deleted = 0
            ORDER BY create_time DESC
            LIMIT #{limit}
            """)
    List<Map<String, Object>> selectArchives(@Param("userId") Long userId, @Param("limit") int limit);

    @Select("""
            SELECT title, type, credit_value AS creditValue, verify_status AS verifyStatus
            FROM learning_achievement
            WHERE user_id = #{userId} AND deleted = 0
            ORDER BY create_time DESC
            LIMIT #{limit}
            """)
    List<Map<String, Object>> selectAchievements(@Param("userId") Long userId, @Param("limit") int limit);

    @Select("""
            SELECT t.name AS tagName, ut.source
            FROM user_target_tag ut
            JOIN sys_tag t ON t.id = ut.tag_id
            WHERE ut.user_id = #{userId}
            ORDER BY t.name
            """)
    List<Map<String, Object>> selectTargetTags(@Param("userId") Long userId);

    @Select("""
            SELECT title, LEFT(content, 80) AS contentPreview, create_time AS createTime
            FROM forum_post
            WHERE user_id = #{userId} AND deleted = 0 AND status = 1
            ORDER BY create_time DESC
            LIMIT #{limit}
            """)
    List<Map<String, Object>> selectForumPosts(@Param("userId") Long userId, @Param("limit") int limit);

    @Select("""
            SELECT type, amount, biz_type AS bizType, source, create_time AS createTime
            FROM credit_transaction
            WHERE user_id = #{userId}
            ORDER BY create_time DESC
            LIMIT #{limit}
            """)
    List<Map<String, Object>> selectCreditTxns(@Param("userId") Long userId, @Param("limit") int limit);
}
