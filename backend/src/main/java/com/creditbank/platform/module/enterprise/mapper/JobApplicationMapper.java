package com.creditbank.platform.module.enterprise.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.creditbank.platform.module.enterprise.entity.JobApplication;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface JobApplicationMapper extends BaseMapper<JobApplication> {

    @Select("""
            SELECT COUNT(*)
            FROM job_application ja
            INNER JOIN job_posting jp ON ja.job_id = jp.id AND jp.deleted = 0
            WHERE jp.org_id = #{orgId} AND ja.status = 0
            """)
    Long countPendingByOrgId(@Param("orgId") Long orgId);
}
