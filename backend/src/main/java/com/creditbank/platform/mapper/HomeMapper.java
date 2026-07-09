package com.creditbank.platform.mapper;

import com.creditbank.platform.dto.SearchItemVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface HomeMapper {

    @Select("""
            SELECT 'course' AS type, id, title, description AS summary, cover_url AS coverUrl,
                   CONCAT(IFNULL(price_credit, 0), ' 学分') AS extra, create_time AS createTime
            FROM course
            WHERE deleted = 0 AND status = 1
            ORDER BY create_time DESC
            LIMIT #{limit}
            """)
    List<SearchItemVO> listFeaturedCourses(@Param("limit") int limit);

    @Select("""
            SELECT 'credit' AS type, id, name AS title, description AS summary, cover_url AS coverUrl,
                   CONCAT(IFNULL(price_credit, 0), ' 学分') AS extra, create_time AS createTime
            FROM mall_product
            WHERE deleted = 0 AND status = 1
            ORDER BY create_time DESC
            LIMIT #{limit}
            """)
    List<SearchItemVO> listHotProducts(@Param("limit") int limit);

    @Select("""
            SELECT 'activity' AS type, id, title, description AS summary, NULL AS coverUrl,
                   location AS extra, start_time AS createTime
            FROM activity
            WHERE deleted = 0 AND status IN (1, 2, 3)
            ORDER BY start_time ASC
            LIMIT #{limit}
            """)
    List<SearchItemVO> listHotActivities(@Param("limit") int limit);

    @Select("""
            SELECT DISTINCT 'course' AS type, c.id, c.title, c.description AS summary, c.cover_url AS coverUrl,
                   CONCAT(IFNULL(c.duration_hours, 0), ' 学时') AS extra, c.create_time AS createTime
            FROM course c
            LEFT JOIN course_tag ct ON c.id = ct.course_id
            LEFT JOIN sys_tag t ON ct.tag_id = t.id
            WHERE c.deleted = 0 AND c.status = 1
              AND (c.title LIKE '%微专业%' OR t.name LIKE '%微专业%' OR IFNULL(c.duration_hours, 0) >= 24)
            ORDER BY c.create_time DESC
            LIMIT #{limit}
            """)
    List<SearchItemVO> listMicroMajors(@Param("limit") int limit);

    @Select("""
            SELECT 'news' AS type, id, title, content AS summary, source AS extra, publish_time AS createTime
            FROM policy_news
            WHERE deleted = 0 AND status = 1
            ORDER BY publish_time DESC
            LIMIT #{limit}
            """)
    List<SearchItemVO> listHotNews(@Param("limit") int limit);

    @Select("""
            SELECT 'job' AS type, id, title, description AS summary, NULL AS coverUrl,
                   CONCAT(IFNULL(location, ''), ' · ', IFNULL(salary_range, '')) AS extra,
                   create_time AS createTime
            FROM job_posting
            WHERE deleted = 0 AND status = 1
            ORDER BY create_time DESC
            LIMIT #{limit}
            """)
    List<SearchItemVO> listJobs(@Param("limit") int limit);

    @Select("""
            SELECT 'forum' AS type, id, title, content AS summary, NULL AS coverUrl,
                   CONCAT(IFNULL(reply_count, 0), ' 回复') AS extra, create_time AS createTime
            FROM forum_post
            WHERE deleted = 0 AND status = 1
            ORDER BY is_top DESC, (IFNULL(reply_count, 0) + IFNULL(like_count, 0)) DESC, create_time DESC
            LIMIT #{limit}
            """)
    List<SearchItemVO> listHotForumPosts(@Param("limit") int limit);

    @Select("""
            SELECT 'partner' AS type, id, name AS title, intro AS summary, logo AS coverUrl,
                   code AS extra, create_time AS createTime
            FROM sys_organization
            WHERE deleted = 0 AND status = 1 AND join_status = 1
            ORDER BY create_time ASC
            LIMIT #{limit}
            """)
    List<SearchItemVO> listPartners(@Param("limit") int limit);
}
