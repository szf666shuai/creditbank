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
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProfileApplicationService {

    private static final int JOB_OPEN = 1;

    private final AuthSupport authSupport;
    private final SysOrganizationMapper orgMapper;
    private final JobApplicationMapper jobApplicationMapper;
    private final JobPostingMapper jobPostingMapper;

    @Transactional
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

        List<Long> staleIds = new ArrayList<>();
        List<JobApplication> activeApps = new ArrayList<>();
        for (JobApplication app : applications) {
            if (isJobUnavailable(jobMap.get(app.getJobId()))) {
                staleIds.add(app.getId());
            } else {
                activeApps.add(app);
            }
        }
        if (!staleIds.isEmpty()) {
            jobApplicationMapper.delete(new LambdaQueryWrapper<JobApplication>()
                    .in(JobApplication::getId, staleIds));
        }
        if (activeApps.isEmpty()) {
            return List.of();
        }

        List<Long> orgIds = activeApps.stream()
                .map(app -> jobMap.get(app.getJobId()))
                .filter(Objects::nonNull)
                .map(JobPosting::getOrgId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        Map<Long, String> orgNameMap = orgIds.isEmpty()
                ? Map.of()
                : orgMapper.selectList(new LambdaQueryWrapper<SysOrganization>().in(SysOrganization::getId, orgIds))
                        .stream()
                        .collect(Collectors.toMap(SysOrganization::getId, SysOrganization::getName));

        return activeApps.stream()
                .map(app -> {
                    JobPosting job = jobMap.get(app.getJobId());
                    return MyJobApplicationVO.builder()
                            .id(app.getId())
                            .jobId(app.getJobId())
                            .jobTitle(job.getTitle())
                            .orgId(job.getOrgId())
                            .orgName(orgNameMap.get(job.getOrgId()))
                            .jobLocation(job.getLocation())
                            .salaryRange(job.getSalaryRange())
                            .coverMessage(app.getCoverMessage())
                            .status(app.getStatus())
                            .statusName(ApplicationStatus.statusName(app.getStatus()))
                            .jobUnavailable(false)
                            .createTime(app.getCreateTime())
                            .updateTime(app.getUpdateTime())
                            .build();
                })
                .toList();
    }

    private boolean isJobUnavailable(JobPosting job) {
        return job == null || job.getStatus() == null || job.getStatus() != JOB_OPEN;
    }
}
