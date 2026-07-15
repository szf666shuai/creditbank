package com.creditbank.platform.module.admin.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.creditbank.platform.common.BusinessException;
import com.creditbank.platform.common.PageResult;
import com.creditbank.platform.entity.Course;
import com.creditbank.platform.entity.CreditTransaction;
import com.creditbank.platform.entity.IntegrityRecord;
import com.creditbank.platform.entity.SysOrganization;
import com.creditbank.platform.entity.SysUser;
import com.creditbank.platform.mapper.CourseMapper;
import com.creditbank.platform.mapper.CreditTransactionMapper;
import com.creditbank.platform.mapper.IntegrityRecordMapper;
import com.creditbank.platform.mapper.SysOrganizationMapper;
import com.creditbank.platform.mapper.SysUserMapper;
import com.creditbank.platform.module.admin.dto.AdminActivityVO;
import com.creditbank.platform.module.admin.dto.AdminCourseVO;
import com.creditbank.platform.module.admin.dto.AdminCreditTransactionVO;
import com.creditbank.platform.module.admin.dto.AdminIntegrityRecordVO;
import com.creditbank.platform.module.admin.dto.AdminJobVO;
import com.creditbank.platform.module.admin.dto.AdminProductApprovalRequest;
import com.creditbank.platform.module.admin.dto.UpdateContentStatusRequest;
import com.creditbank.platform.module.enterprise.service.ActivityLifecycleService;
import com.creditbank.platform.module.enterprise.support.ActivityStatus;
import com.creditbank.platform.module.enterprise.entity.Activity;
import com.creditbank.platform.module.enterprise.entity.JobApplication;
import com.creditbank.platform.module.enterprise.entity.JobPosting;
import com.creditbank.platform.module.enterprise.mapper.ActivityMapper;
import com.creditbank.platform.module.enterprise.mapper.JobApplicationMapper;
import com.creditbank.platform.module.enterprise.mapper.JobPostingMapper;
import com.creditbank.platform.security.AuthSupport;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminOversightService {

    private final AuthSupport authSupport;
    private final JobPostingMapper jobPostingMapper;
    private final JobApplicationMapper jobApplicationMapper;
    private final ActivityMapper activityMapper;
    private final IntegrityRecordMapper integrityRecordMapper;
    private final CreditTransactionMapper creditTransactionMapper;
    private final SysOrganizationMapper sysOrganizationMapper;
    private final SysUserMapper sysUserMapper;
    private final CourseMapper courseMapper;
    private final ActivityLifecycleService activityLifecycleService;

    public PageResult<AdminJobVO> pageJobs(long page, long pageSize, Integer status, String keyword) {
        authSupport.requireAdmin();
        AdminSupport.validatePage(page, pageSize);

        LambdaQueryWrapper<JobPosting> wrapper = new LambdaQueryWrapper<JobPosting>()
                .eq(status != null, JobPosting::getStatus, status)
                .orderByDesc(JobPosting::getCreateTime);
        if (StringUtils.hasText(keyword)) {
            wrapper.like(JobPosting::getTitle, keyword.trim());
        }

        Page<JobPosting> result = jobPostingMapper.selectPage(new Page<>(page, pageSize), wrapper);
        Map<Long, String> orgNameMap = loadOrgNameMap(result.getRecords().stream().map(JobPosting::getOrgId).toList());
        return PageResult.of(
                result.getRecords().stream().map(job -> toJobVO(job, orgNameMap.get(job.getOrgId()))).toList(),
                result.getTotal(),
                result.getCurrent(),
                result.getSize());
    }

    public PageResult<AdminCourseVO> pageCourses(long page, long pageSize, Integer approvalStatus, String keyword) {
        authSupport.requireAdmin();
        AdminSupport.validatePage(page, pageSize);
        LambdaQueryWrapper<Course> wrapper = new LambdaQueryWrapper<Course>()
                .eq(Course::getDeleted, 0)
                .eq(approvalStatus != null, Course::getApprovalStatus, approvalStatus)
                .orderByDesc(Course::getCreateTime);
        if (StringUtils.hasText(keyword)) {
            wrapper.like(Course::getTitle, keyword.trim());
        }
        Page<Course> result = courseMapper.selectPage(new Page<>(page, pageSize), wrapper);
        Map<Long, String> orgNameMap = loadOrgNameMap(result.getRecords().stream().map(Course::getOrgId).toList());
        return PageResult.of(
                result.getRecords().stream().map(course -> toCourseVO(course, orgNameMap.get(course.getOrgId()))).toList(),
                result.getTotal(),
                result.getCurrent(),
                result.getSize());
    }

    @Transactional
    public AdminCourseVO reviewCourse(Long courseId, AdminProductApprovalRequest request) {
        SysUser admin = authSupport.requireAdmin();
        Course course = courseMapper.selectById(courseId);
        if (course == null || course.getDeleted() != null && course.getDeleted() == 1) {
            throw new BusinessException(404, "课程不存在");
        }
        if (request.getApprovalStatus() != 1 && request.getApprovalStatus() != 2) {
            throw new BusinessException(400, "审核状态无效");
        }
        course.setApprovalStatus(request.getApprovalStatus());
        course.setReviewRemark(request.getReviewRemark());
        course.setReviewedBy(admin.getId());
        course.setReviewedAt(LocalDateTime.now());
        course.setStatus(request.getApprovalStatus() == 1 ? 1 : 0);
        courseMapper.updateById(course);
        return toCourseVO(course, loadOrgName(course.getOrgId()));
    }

    @Transactional
    public AdminJobVO updateJobStatus(Long jobId, UpdateContentStatusRequest request) {
        authSupport.requireAdmin();
        JobPosting job = jobPostingMapper.selectById(jobId);
        if (job == null) {
            throw new BusinessException(404, "职位不存在");
        }
        if (request.getStatus() != 0 && request.getStatus() != 1) {
            throw new BusinessException(400, "状态无效");
        }
        job.setStatus(request.getStatus());
        jobPostingMapper.updateById(job);
        if (request.getStatus() == 0) {
            jobApplicationMapper.delete(new LambdaQueryWrapper<JobApplication>()
                    .eq(JobApplication::getJobId, jobId));
        }
        return toJobVO(job, loadOrgName(job.getOrgId()));
    }

    public PageResult<AdminActivityVO> pageActivities(long page, long pageSize, Integer status, String keyword) {
        authSupport.requireAdmin();
        AdminSupport.validatePage(page, pageSize);

        LambdaQueryWrapper<Activity> wrapper = new LambdaQueryWrapper<Activity>()
                .eq(status != null, Activity::getStatus, status)
                .orderByDesc(Activity::getCreateTime);
        if (StringUtils.hasText(keyword)) {
            wrapper.like(Activity::getTitle, keyword.trim());
        }

        Page<Activity> result = activityMapper.selectPage(new Page<>(page, pageSize), wrapper);
        List<Activity> refreshed = activityLifecycleService.refreshAll(result.getRecords());
        Map<Long, String> orgNameMap = loadOrgNameMap(refreshed.stream().map(Activity::getOrgId).toList());
        return PageResult.of(
                refreshed.stream().map(item -> toActivityVO(item, orgNameMap.get(item.getOrgId()))).toList(),
                result.getTotal(),
                result.getCurrent(),
                result.getSize());
    }

    @Transactional
    public AdminActivityVO updateActivityStatus(Long activityId, UpdateContentStatusRequest request) {
        authSupport.requireAdmin();
        Activity activity = activityMapper.selectById(activityId);
        if (activity == null) {
            throw new BusinessException(404, "活动不存在");
        }
        if (request.getStatus() != 0 && request.getStatus() != 1) {
            throw new BusinessException(400, "状态无效");
        }
        if (request.getStatus() == 0) {
            activity.setStatus(ActivityStatus.CANCELLED);
        } else {
            activity.setStatus(ActivityStatus.OPEN);
            activityLifecycleService.refresh(activity);
        }
        activityMapper.updateById(activity);
        return toActivityVO(activity, loadOrgName(activity.getOrgId()));
    }

    public PageResult<AdminIntegrityRecordVO> pageIntegrityRecords(
            long page, long pageSize, Integer eventType, Long userId) {
        authSupport.requireAdmin();
        AdminSupport.validatePage(page, pageSize);

        Page<IntegrityRecord> result = integrityRecordMapper.selectPage(
                new Page<>(page, pageSize),
                new LambdaQueryWrapper<IntegrityRecord>()
                        .eq(eventType != null, IntegrityRecord::getEventType, eventType)
                        .eq(userId != null, IntegrityRecord::getUserId, userId)
                        .orderByDesc(IntegrityRecord::getCreateTime));

        Map<Long, String> userNameMap = loadUserNameMap(result.getRecords().stream()
                .map(IntegrityRecord::getUserId).toList());
        return PageResult.of(
                result.getRecords().stream().map(record -> toIntegrityVO(record, userNameMap)).toList(),
                result.getTotal(),
                result.getCurrent(),
                result.getSize());
    }

    public PageResult<AdminCreditTransactionVO> pageCreditTransactions(
            long page, long pageSize, Integer type, Long userId) {
        authSupport.requireAdmin();
        AdminSupport.validatePage(page, pageSize);

        Page<CreditTransaction> result = creditTransactionMapper.selectPage(
                new Page<>(page, pageSize),
                new LambdaQueryWrapper<CreditTransaction>()
                        .eq(type != null, CreditTransaction::getType, type)
                        .eq(userId != null, CreditTransaction::getUserId, userId)
                        .orderByDesc(CreditTransaction::getCreateTime));

        Map<Long, String> userNameMap = loadUserNameMap(result.getRecords().stream()
                .map(CreditTransaction::getUserId).toList());
        return PageResult.of(
                result.getRecords().stream().map(tx -> toCreditVO(tx, userNameMap)).toList(),
                result.getTotal(),
                result.getCurrent(),
                result.getSize());
    }

    private String loadOrgName(Long orgId) {
        if (orgId == null) {
            return null;
        }
        SysOrganization org = sysOrganizationMapper.selectById(orgId);
        return org != null ? org.getName() : null;
    }

    private Map<Long, String> loadOrgNameMap(List<Long> orgIds) {
        List<Long> ids = orgIds.stream().filter(Objects::nonNull).distinct().toList();
        if (ids.isEmpty()) {
            return Map.of();
        }
        return sysOrganizationMapper.selectList(new LambdaQueryWrapper<SysOrganization>().in(SysOrganization::getId, ids))
                .stream()
                .collect(Collectors.toMap(SysOrganization::getId, SysOrganization::getName));
    }

    private Map<Long, String> loadUserNameMap(List<Long> userIds) {
        List<Long> ids = userIds.stream().filter(Objects::nonNull).distinct().toList();
        if (ids.isEmpty()) {
            return Map.of();
        }
        return sysUserMapper.selectList(new LambdaQueryWrapper<SysUser>().in(SysUser::getId, ids))
                .stream()
                .collect(Collectors.toMap(
                        SysUser::getId,
                        user -> StringUtils.hasText(user.getRealName()) ? user.getRealName() : user.getUsername()));
    }

    private AdminJobVO toJobVO(JobPosting job, String orgName) {
        return AdminJobVO.builder()
                .id(job.getId())
                .orgId(job.getOrgId())
                .orgName(orgName)
                .title(job.getTitle())
                .location(job.getLocation())
                .salaryRange(job.getSalaryRange())
                .status(job.getStatus())
                .statusName(AdminSupport.jobStatusName(job.getStatus()))
                .viewCount(job.getViewCount())
                .createTime(job.getCreateTime())
                .build();
    }

    private AdminActivityVO toActivityVO(Activity activity, String orgName) {
        return AdminActivityVO.builder()
                .id(activity.getId())
                .orgId(activity.getOrgId())
                .orgName(orgName)
                .title(activity.getTitle())
                .location(activity.getLocation())
                .startTime(activity.getStartTime())
                .endTime(activity.getEndTime())
                .status(activity.getStatus())
                .statusName(ActivityStatus.label(activity.getStatus()))
                .createTime(activity.getCreateTime())
                .build();
    }

    private AdminIntegrityRecordVO toIntegrityVO(IntegrityRecord record, Map<Long, String> userNameMap) {
        return AdminIntegrityRecordVO.builder()
                .id(record.getId())
                .userId(record.getUserId())
                .userName(userNameMap.get(record.getUserId()))
                .changeValue(record.getChangeValue())
                .scoreAfter(record.getScoreAfter())
                .eventType(record.getEventType())
                .eventTypeName(integrityEventTypeName(record.getEventType()))
                .reason(record.getReason())
                .refType(record.getRefType())
                .refId(record.getRefId())
                .createTime(record.getCreateTime())
                .build();
    }

    private AdminCreditTransactionVO toCreditVO(CreditTransaction tx, Map<Long, String> userNameMap) {
        return AdminCreditTransactionVO.builder()
                .id(tx.getId())
                .userId(tx.getUserId())
                .userName(userNameMap.get(tx.getUserId()))
                .type(tx.getType())
                .typeName(creditTypeName(tx.getType()))
                .amount(tx.getAmount())
                .bizType(tx.getBizType())
                .source(tx.getSource())
                .createTime(tx.getCreateTime())
                .build();
    }

    private AdminCourseVO toCourseVO(Course course, String orgName) {
        return AdminCourseVO.builder()
                .id(course.getId())
                .orgId(course.getOrgId())
                .orgName(orgName)
                .title(course.getTitle())
                .description(course.getDescription())
                .coverUrl(course.getCoverUrl())
                .tags(course.getTags() != null ? List.of(course.getTags().split(",")) : null)
                .creditValue(course.getCreditValue())
                .duration(course.getDurationMinutes())
                .difficulty(course.getDifficulty())
                .difficultyName(difficultyName(course.getDifficulty()))
                .status(course.getStatus())
                .approvalStatus(course.getApprovalStatus())
                .approvalStatusName(approvalStatusName(course.getApprovalStatus()))
                .reviewRemark(course.getReviewRemark())
                .createTime(course.getCreateTime())
                .build();
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

    private static String integrityEventTypeName(Integer eventType) {
        if (eventType == null) {
            return "未知";
        }
        return switch (eventType) {
            case 1 -> "加分";
            case 2 -> "扣分";
            default -> "未知";
        };
    }

    private static String creditTypeName(Integer type) {
        if (type == null) {
            return "未知";
        }
        return switch (type) {
            case 1 -> "获取";
            case 2 -> "转换";
            case 3 -> "增长";
            case 4 -> "消耗";
            default -> "未知";
        };
    }
}
