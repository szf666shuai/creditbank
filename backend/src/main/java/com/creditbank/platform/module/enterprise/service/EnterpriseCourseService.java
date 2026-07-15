package com.creditbank.platform.module.enterprise.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.creditbank.platform.common.BusinessException;
import com.creditbank.platform.entity.Course;
import com.creditbank.platform.entity.SysUser;
import com.creditbank.platform.mapper.CourseMapper;
import com.creditbank.platform.module.enterprise.dto.EnterpriseCourseSaveRequest;
import com.creditbank.platform.module.enterprise.dto.EnterpriseCourseVO;
import com.creditbank.platform.security.AuthSupport;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EnterpriseCourseService {

    private final AuthSupport authSupport;
    private final CourseMapper courseMapper;

    public List<EnterpriseCourseVO> listMyCourses() {
        SysUser user = authSupport.requireEnterprise();
        return courseMapper.selectList(
                        new LambdaQueryWrapper<Course>()
                                .eq(Course::getOrgId, user.getOrgId())
                                .eq(Course::getDeleted, 0)
                                .orderByDesc(Course::getCreateTime)
                )
                .stream()
                .map(this::toVO)
                .toList();
    }

    @Transactional
    public EnterpriseCourseVO createCourse(EnterpriseCourseSaveRequest request) {
        SysUser user = authSupport.requireEnterpriseWritable();
        Course course = new Course();
        course.setOrgId(user.getOrgId());
        course.setTitle(request.getTitle().trim());
        course.setDescription(trim(request.getDescription()));
        course.setCoverUrl(trim(request.getCoverUrl()));
        course.setCreditValue(request.getCreditValue() == null ? BigDecimal.valueOf(3.00) : request.getCreditValue());
        course.setDifficulty(request.getDifficulty() == null ? 1 : request.getDifficulty());
        course.setDurationMinutes(request.getDuration() == null ? 60 : request.getDuration());
        course.setTags(trim(request.getTags()));
        course.setVideoUrl(trim(request.getVideoUrl()));
        course.setVideoDurationSeconds(request.getVideoDurationSeconds());
        course.setStatus(0);
        course.setApprovalStatus(0);
        course.setDeleted(0);
        courseMapper.insert(course);
        return toVO(course);
    }

    @Transactional
    public EnterpriseCourseVO updateCourse(Long id, EnterpriseCourseSaveRequest request) {
        SysUser user = authSupport.requireEnterpriseWritable();
        Course course = requireOwnedCourse(id, user.getOrgId());
        course.setTitle(request.getTitle().trim());
        course.setDescription(trim(request.getDescription()));
        course.setCoverUrl(trim(request.getCoverUrl()));
        course.setCreditValue(request.getCreditValue() == null ? course.getCreditValue() : request.getCreditValue());
        course.setDifficulty(request.getDifficulty() == null ? course.getDifficulty() : request.getDifficulty());
        course.setDurationMinutes(request.getDuration() == null ? course.getDurationMinutes() : request.getDuration());
        course.setTags(trim(request.getTags()));
        course.setVideoUrl(trim(request.getVideoUrl()));
        course.setVideoDurationSeconds(request.getVideoDurationSeconds());
        // 编辑后重新提交审核
        course.setApprovalStatus(0);
        course.setReviewRemark(null);
        course.setStatus(0);
        courseMapper.updateById(course);
        return toVO(course);
    }

    @Transactional
    public void deleteCourse(Long id) {
        SysUser user = authSupport.requireEnterpriseWritable();
        Course course = requireOwnedCourse(id, user.getOrgId());
        course.setDeleted(1);
        course.setStatus(0);
        courseMapper.updateById(course);
    }

    private Course requireOwnedCourse(Long id, Long orgId) {
        Course course = courseMapper.selectById(id);
        if (course == null || course.getDeleted() != null && course.getDeleted() == 1) {
            throw new BusinessException(404, "课程不存在");
        }
        if (!orgId.equals(course.getOrgId())) {
            throw new BusinessException(403, "无权操作该课程");
        }
        return course;
    }

    private EnterpriseCourseVO toVO(Course course) {
        EnterpriseCourseVO vo = new EnterpriseCourseVO();
        vo.setId(course.getId());
        vo.setTitle(course.getTitle());
        vo.setDescription(course.getDescription());
        vo.setCoverUrl(course.getCoverUrl());
        vo.setCreditValue(course.getCreditValue());
        vo.setDuration(course.getDurationMinutes());
        vo.setDifficulty(course.getDifficulty());
        vo.setDifficultyName(difficultyName(course.getDifficulty()));
        vo.setTags(course.getTags());
        vo.setVideoUrl(course.getVideoUrl());
        vo.setVideoDurationSeconds(course.getVideoDurationSeconds());
        vo.setStatus(course.getStatus());
        vo.setStatusName(course.getStatus() != null && course.getStatus() == 1 ? "已上架" : "已下架");
        vo.setApprovalStatus(course.getApprovalStatus());
        vo.setApprovalStatusName(approvalStatusName(course.getApprovalStatus()));
        vo.setReviewRemark(course.getReviewRemark());
        vo.setCreateTime(course.getCreateTime());
        vo.setUpdateTime(course.getUpdateTime());
        return vo;
    }

    private static String difficultyName(Integer difficulty) {
        if (difficulty == null) return "入门";
        return switch (difficulty) {
            case 1 -> "入门";
            case 2 -> "初级";
            case 3 -> "中级";
            case 4 -> "高级";
            default -> "入门";
        };
    }

    private static String approvalStatusName(Integer status) {
        if (status == null) return "待审核";
        return switch (status) {
            case 1 -> "已通过";
            case 2 -> "已驳回";
            default -> "待审核";
        };
    }

    private String trim(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }
}