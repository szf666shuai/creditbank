package com.creditbank.platform.module.profile.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.creditbank.platform.entity.SysOrganization;
import com.creditbank.platform.mapper.SysOrganizationMapper;
import com.creditbank.platform.module.enterprise.entity.JobApplication;
import com.creditbank.platform.module.enterprise.entity.JobPosting;
import com.creditbank.platform.module.enterprise.mapper.JobApplicationMapper;
import com.creditbank.platform.module.enterprise.mapper.JobPostingMapper;
import com.creditbank.platform.module.enterprise.support.ApplicationStatus;
import com.creditbank.platform.module.profile.dto.MyJobApplicationVO;
import com.creditbank.platform.security.AuthSupport;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProfileApplicationService {

    private final AuthSupport authSupport;
    private final SysOrganizationMapper orgMapper;
    private final JobApplicationMapper jobApplicationMapper;
    private final JobPostingMapper jobPostingMapper;

    public List<MyJobApplicationVO> listMyApplications(Integer status) {
        Long userId = authSupport.requireUserId();

        List<JobApplication> applications = jobApplicationMapper.selectList(
                new LambdaQueryWrapper<JobApplication>()
                        .eq(JobApplication::getUserId, userId)
                        .eq(status != null, JobApplication::getStatus, status)
                        .orderByDesc(JobApplication::getCreateTime));
        if (applications.isEmpty()) {
            return List.of();
        }

        List<Long> jobIds = applications.stream().map(JobApplication::getJobId).distinct().toList();
        Map<Long, JobPosting> jobMap = jobPostingMapper.selectList(
                        new LambdaQueryWrapper<JobPosting>().in(JobPosting::getId, jobIds))
                .stream()
                .collect(Collectors.toMap(JobPosting::getId, job -> job));

        List<Long> orgIds = jobMap.values().stream()
                .map(JobPosting::getOrgId)
                .filter(id -> id != null)
                .distinct()
                .toList();
        Map<Long, String> orgNameMap = orgIds.isEmpty()
                ? Map.of()
                : orgMapper.selectList(new LambdaQueryWrapper<SysOrganization>().in(SysOrganization::getId, orgIds))
                        .stream()
                        .collect(Collectors.toMap(SysOrganization::getId, SysOrganization::getName));

        return applications.stream()
                .map(app -> {
                    JobPosting job = jobMap.get(app.getJobId());
                    boolean jobUnavailable = job == null;
                    return MyJobApplicationVO.builder()
                            .id(app.getId())
                            .jobId(app.getJobId())
                            .jobTitle(jobUnavailable ? ApplicationStatus.UNAVAILABLE_JOB_TITLE : job.getTitle())
                            .orgId(job != null ? job.getOrgId() : null)
                            .orgName(job != null ? orgNameMap.get(job.getOrgId()) : null)
                            .jobLocation(job != null ? job.getLocation() : null)
                            .salaryRange(job != null ? job.getSalaryRange() : null)
                            .coverMessage(app.getCoverMessage())
                            .status(app.getStatus())
                            .statusName(ApplicationStatus.statusName(app.getStatus()))
                            .jobUnavailable(jobUnavailable)
                            .createTime(app.getCreateTime())
                            .updateTime(app.getUpdateTime())
                            .build();
                })
                .toList();
    }
}
