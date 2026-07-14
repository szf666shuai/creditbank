package com.creditbank.platform.module.enterprise.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.creditbank.platform.common.BusinessException;
import com.creditbank.platform.entity.SysTag;
import com.creditbank.platform.entity.SysUser;
import com.creditbank.platform.mapper.SysTagMapper;
import com.creditbank.platform.module.enterprise.dto.JobManageVO;
import com.creditbank.platform.module.enterprise.dto.JobSaveRequest;
import com.creditbank.platform.module.enterprise.dto.TagVO;
import com.creditbank.platform.module.enterprise.entity.JobApplication;
import com.creditbank.platform.module.enterprise.entity.JobPosting;
import com.creditbank.platform.module.enterprise.entity.JobTag;
import com.creditbank.platform.module.enterprise.mapper.JobApplicationMapper;
import com.creditbank.platform.module.enterprise.mapper.JobPostingMapper;
import com.creditbank.platform.module.enterprise.mapper.JobTagMapper;
import com.creditbank.platform.security.AuthSupport;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EnterpriseJobService {

    private static final int JOB_OPEN = 1;
    private static final int JOB_OFFLINE = 0;

    private final AuthSupport authSupport;
    private final SysTagMapper tagMapper;
    private final JobPostingMapper jobPostingMapper;
    private final JobTagMapper jobTagMapper;
    private final JobApplicationMapper jobApplicationMapper;

    public List<TagVO> listSkillTags() {
        return tagMapper.selectList(new LambdaQueryWrapper<SysTag>()
                        .eq(SysTag::getCategory, "skill")
                        .orderByAsc(SysTag::getName))
                .stream()
                .map(tag -> TagVO.builder().id(tag.getId()).name(tag.getName()).build())
                .toList();
    }

    public List<JobManageVO> listMyJobs() {
        SysUser user = authSupport.requireEnterprise();
        List<JobPosting> jobs = jobPostingMapper.selectList(new LambdaQueryWrapper<JobPosting>()
                .eq(JobPosting::getOrgId, user.getOrgId())
                .orderByDesc(JobPosting::getCreateTime));
        return jobs.stream().map(job -> toManageVO(job, loadTags(job.getId()))).toList();
    }

    public JobManageVO getMyJob(Long jobId) {
        SysUser user = authSupport.requireEnterprise();
        JobPosting job = authSupport.requireOrgJob(jobId, user.getOrgId());
        return toManageVO(job, loadTags(job.getId()));
    }

    @Transactional
    public JobManageVO createJob(JobSaveRequest request) {
        SysUser user = authSupport.requireEnterpriseWritable();
        validateTagIds(request.getTagIds());

        JobPosting job = new JobPosting();
        job.setOrgId(user.getOrgId());
        job.setPublisherId(user.getId());
        applyRequest(job, request);
        job.setStatus(JOB_OPEN);
        job.setViewCount(0);
        jobPostingMapper.insert(job);

        saveJobTags(job.getId(), request.getTagIds());
        return toManageVO(job, loadTags(job.getId()));
    }

    @Transactional
    public JobManageVO updateJob(Long jobId, JobSaveRequest request) {
        SysUser user = authSupport.requireEnterpriseWritable();
        validateTagIds(request.getTagIds());
        JobPosting job = authSupport.requireOrgJob(jobId, user.getOrgId());

        applyRequest(job, request);
        jobPostingMapper.updateById(job);
        saveJobTags(job.getId(), request.getTagIds());
        return toManageVO(job, loadTags(job.getId()));
    }

    @Transactional
    public JobManageVO offlineJob(Long jobId) {
        return updateJobStatus(jobId, JOB_OFFLINE);
    }

    @Transactional
    public JobManageVO onlineJob(Long jobId) {
        return updateJobStatus(jobId, JOB_OPEN);
    }

    private JobManageVO updateJobStatus(Long jobId, int status) {
        SysUser user = authSupport.requireEnterpriseWritable();
        JobPosting job = authSupport.requireOrgJob(jobId, user.getOrgId());
        job.setStatus(status);
        jobPostingMapper.updateById(job);
        if (status == JOB_OFFLINE) {
            // 职位下架后清理对应投递记录，避免学员端出现「职位已下架」
            jobApplicationMapper.delete(new LambdaQueryWrapper<JobApplication>()
                    .eq(JobApplication::getJobId, jobId));
        }
        return toManageVO(job, loadTags(job.getId()));
    }

    private void validateTagIds(List<Long> tagIds) {
        if (CollectionUtils.isEmpty(tagIds)) {
            return;
        }
        List<Long> distinctIds = tagIds.stream().filter(Objects::nonNull).distinct().toList();
        if (distinctIds.isEmpty()) {
            return;
        }
        Long count = tagMapper.selectCount(new LambdaQueryWrapper<SysTag>().in(SysTag::getId, distinctIds));
        if (count == null || count != distinctIds.size()) {
            throw new BusinessException(400, "存在无效的技能标签");
        }
    }

    private void applyRequest(JobPosting job, JobSaveRequest request) {
        job.setTitle(request.getTitle().trim());
        job.setDescription(request.getDescription());
        job.setRequirements(request.getRequirements());
        job.setSalaryRange(request.getSalaryRange());
        job.setLocation(request.getLocation());
        job.setEduRequirement(request.getEduRequirement());
    }

    private void saveJobTags(Long jobId, List<Long> tagIds) {
        jobTagMapper.delete(new LambdaQueryWrapper<JobTag>().eq(JobTag::getJobId, jobId));
        if (CollectionUtils.isEmpty(tagIds)) {
            return;
        }
        tagIds.stream().filter(Objects::nonNull).distinct().forEach(tagId -> {
            JobTag jobTag = new JobTag(jobId, tagId);
            jobTagMapper.insert(jobTag);
        });
    }

    private List<TagVO> loadTags(Long jobId) {
        List<JobTag> relations = jobTagMapper.selectList(new LambdaQueryWrapper<JobTag>()
                .eq(JobTag::getJobId, jobId));
        if (relations.isEmpty()) {
            return Collections.emptyList();
        }
        List<Long> tagIds = relations.stream().map(JobTag::getTagId).toList();
        Map<Long, String> tagMap = tagMapper.selectList(new LambdaQueryWrapper<SysTag>()
                        .in(SysTag::getId, tagIds))
                .stream()
                .collect(Collectors.toMap(SysTag::getId, SysTag::getName));
        return tagIds.stream()
                .map(id -> TagVO.builder().id(id).name(tagMap.get(id)).build())
                .filter(tag -> tag.getName() != null)
                .toList();
    }

    private JobManageVO toManageVO(JobPosting job, List<TagVO> tags) {
        return JobManageVO.builder()
                .id(job.getId())
                .orgId(job.getOrgId())
                .title(job.getTitle())
                .description(job.getDescription())
                .requirements(job.getRequirements())
                .salaryRange(job.getSalaryRange())
                .location(job.getLocation())
                .eduRequirement(job.getEduRequirement())
                .status(job.getStatus())
                .statusName(job.getStatus() != null && job.getStatus() == JOB_OPEN ? "招聘中" : "已下架")
                .createTime(job.getCreateTime())
                .updateTime(job.getUpdateTime())
                .tags(tags)
                .build();
    }
}
