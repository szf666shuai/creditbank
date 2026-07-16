package com.creditbank.platform.mapper;

import com.creditbank.platform.dto.SearchItemVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface HomeMapper {

    /** 与学习资源列表同源：course 表上架课程，展示完成奖励秩点 */
    @Select("""
            SELECT 'course' AS type, '课程' AS typeName, id, title, description AS summary, cover_url AS coverUrl,
                   CONCAT(IFNULL(credit_reward, 0), ' 秩点') AS extra, create_time AS createTime
            FROM course
            WHERE deleted = 0 AND status = 1
              AND (approval_status IS NULL OR approval_status = 1)
            ORDER BY create_time DESC, id DESC
            LIMIT #{limit}
            """)
    List<SearchItemVO> listFeaturedCourses(@Param("limit") int limit);

    @Select("""
            SELECT 'credit' AS type, id, name AS title, description AS summary, cover_url AS coverUrl,
                   CONCAT(IFNULL(price_credit, 0), ' 秩点') AS extra, create_time AS createTime
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

    /**
     * 微专业 = 学习资源中的技能标签轨道（Java / Spring Boot / 区块链等），
     * 封面取该标签下学时最长的课程，点击后按 tag 进入学习资源。
     */
    @Select("""
            SELECT 'course' AS type,
                   t.name AS typeName,
                   (
                       SELECT c2.id
                       FROM course c2
                       JOIN course_tag ct2 ON ct2.course_id = c2.id
                       WHERE ct2.tag_id = t.id AND c2.deleted = 0 AND c2.status = 1
                       ORDER BY c2.duration_hours DESC, c2.id DESC
                       LIMIT 1
                   ) AS id,
                   CONCAT(t.name, ' 微专业') AS title,
                   CONCAT('学习资源同步 · 共 ', COUNT(DISTINCT c.id), ' 门课程，可按标签筛选学习') AS summary,
                   (
                       SELECT c3.cover_url
                       FROM course c3
                       JOIN course_tag ct3 ON ct3.course_id = c3.id
                       WHERE ct3.tag_id = t.id AND c3.deleted = 0 AND c3.status = 1
                       ORDER BY c3.duration_hours DESC, c3.id DESC
                       LIMIT 1
                   ) AS coverUrl,
                   CONCAT(
                       TRIM(TRAILING '.' FROM TRIM(TRAILING '0' FROM CAST(ROUND(SUM(IFNULL(c.duration_hours, 0)), 0) AS CHAR))),
                       ' 学时'
                   ) AS extra,
                   MAX(c.create_time) AS createTime
            FROM sys_tag t
            JOIN course_tag ct ON ct.tag_id = t.id
            JOIN course c ON c.id = ct.course_id AND c.deleted = 0 AND c.status = 1
            GROUP BY t.id, t.name
            ORDER BY COUNT(DISTINCT c.id) DESC, SUM(IFNULL(c.duration_hours, 0)) DESC, t.name ASC
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
