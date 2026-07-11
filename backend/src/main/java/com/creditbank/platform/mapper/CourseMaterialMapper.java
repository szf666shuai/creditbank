package com.creditbank.platform.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.creditbank.platform.dto.CourseMaterialVO;
import com.creditbank.platform.entity.CourseMaterial;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface CourseMaterialMapper extends BaseMapper<CourseMaterial> {

    @Select("""
            SELECT id, course_id AS courseId, title, file_type AS fileType,
                   file_url AS fileUrl, sort_order AS sortOrder
            FROM course_material
            WHERE deleted = 0 AND course_id = #{courseId}
            ORDER BY sort_order ASC, id ASC
            """)
    List<CourseMaterialVO> listByCourseId(@Param("courseId") Long courseId);
}
