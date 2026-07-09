package com.creditbank.platform.mapper;

import com.creditbank.platform.dto.SearchItemVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SearchMapper {

    @Select("""
            SELECT 'course' AS type, id, title, description AS summary, cover_url AS coverUrl,
                   NULL AS extra, create_time AS createTime
            FROM course
            WHERE deleted = 0 AND status = 1
              AND (title LIKE CONCAT('%', #{keyword}, '%') OR description LIKE CONCAT('%', #{keyword}, '%'))
            ORDER BY create_time DESC
            LIMIT #{limit}
            """)
    List<SearchItemVO> searchCourses(@Param("keyword") String keyword, @Param("limit") int limit);

    @Select("""
            SELECT 'resource' AS type, id, title, description AS summary, file_url AS coverUrl,
                   NULL AS extra, create_time AS createTime
            FROM org_material
            WHERE deleted = 0 AND status = 1
              AND (title LIKE CONCAT('%', #{keyword}, '%') OR description LIKE CONCAT('%', #{keyword}, '%'))
            ORDER BY create_time DESC
            LIMIT #{limit}
            """)
    List<SearchItemVO> searchResources(@Param("keyword") String keyword, @Param("limit") int limit);

    @Select("""
            SELECT 'forum' AS type, id, title, content AS summary, NULL AS extra, create_time AS createTime
            FROM forum_post
            WHERE deleted = 0 AND status = 1
              AND (title LIKE CONCAT('%', #{keyword}, '%') OR content LIKE CONCAT('%', #{keyword}, '%'))
            ORDER BY create_time DESC
            LIMIT #{limit}
            """)
    List<SearchItemVO> searchForum(@Param("keyword") String keyword, @Param("limit") int limit);

    @Select("""
            SELECT 'news' AS type, id, title, content AS summary, source AS extra, publish_time AS createTime
            FROM policy_news
            WHERE deleted = 0 AND status = 1
              AND (title LIKE CONCAT('%', #{keyword}, '%') OR content LIKE CONCAT('%', #{keyword}, '%'))
            ORDER BY publish_time DESC
            LIMIT #{limit}
            """)
    List<SearchItemVO> searchNews(@Param("keyword") String keyword, @Param("limit") int limit);

    @Select("""
            SELECT 'activity' AS type, id, title, description AS summary, location AS extra, create_time AS createTime
            FROM activity
            WHERE deleted = 0 AND status IN (1, 2, 3)
              AND (title LIKE CONCAT('%', #{keyword}, '%') OR description LIKE CONCAT('%', #{keyword}, '%'))
            ORDER BY create_time DESC
            LIMIT #{limit}
            """)
    List<SearchItemVO> searchActivities(@Param("keyword") String keyword, @Param("limit") int limit);

    @Select("""
            SELECT 'job' AS type, id, title, description AS summary, location AS extra, create_time AS createTime
            FROM job_posting
            WHERE deleted = 0 AND status = 1
              AND (title LIKE CONCAT('%', #{keyword}, '%') OR description LIKE CONCAT('%', #{keyword}, '%')
                   OR requirements LIKE CONCAT('%', #{keyword}, '%'))
            ORDER BY create_time DESC
            LIMIT #{limit}
            """)
    List<SearchItemVO> searchJobs(@Param("keyword") String keyword, @Param("limit") int limit);

    @Select("""
            SELECT 'enterprise' AS type, id, name AS title, intro AS summary, code AS extra, create_time AS createTime
            FROM sys_organization
            WHERE deleted = 0 AND status = 1
              AND (name LIKE CONCAT('%', #{keyword}, '%') OR intro LIKE CONCAT('%', #{keyword}, '%'))
            ORDER BY create_time DESC
            LIMIT #{limit}
            """)
    List<SearchItemVO> searchEnterprises(@Param("keyword") String keyword, @Param("limit") int limit);

    @Select("""
            SELECT 'credit' AS type, id, name AS title, description AS summary, cover_url AS coverUrl,
                   NULL AS extra, create_time AS createTime
            FROM mall_product
            WHERE deleted = 0 AND status = 1
              AND (name LIKE CONCAT('%', #{keyword}, '%') OR description LIKE CONCAT('%', #{keyword}, '%'))
            ORDER BY create_time DESC
            LIMIT #{limit}
            """)
    List<SearchItemVO> searchCreditProducts(@Param("keyword") String keyword, @Param("limit") int limit);
}
