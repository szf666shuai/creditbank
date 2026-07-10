package com.creditbank.platform.module.profile.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.creditbank.platform.entity.SysOrganization;
import com.creditbank.platform.mapper.SysOrganizationMapper;
import com.creditbank.platform.module.enterprise.entity.JobApplication;
import com.creditbank.platform.module.enterprise.entity.JobPosting;
import com.creditbank.platform.module.enterprise.mapper.JobApplicationMapper;
import com.creditbank.platform.module.enterprise.mapper.JobPostingMapper;
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

    private static final int APP_STATUS_APPLIED = 0;
    private static final int APP_STATUS_VIEWED = 1;
    private static final int APP_STATUS_INTERVIEW = 2;
    private static final int APP_STATUS_HIRED = 3;
    private static final int APP_STATUS_REJECTED = 4;

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
                    return MyJobApplicationVO.builder()
                            .id(app.getId())
                            .jobId(app.getJobId())
                            .jobTitle(job != null ? job.getTitle() : null)
                            .orgId(job != null ? job.getOrgId() : null)
                            .orgName(job != null ? orgNameMap.get(job.getOrgId()) : null)
                            .jobLocation(job != null ? job.getLocation() : null)
                            .salaryRange(job != null ? job.getSalaryRange() : null)
                            .coverMessage(app.getCoverMessage())
                            .status(app.getStatus())
                            .statusName(applicationStatusName(app.getStatus()))
                            .createTime(app.getCreateTime())
                            .updateTime(app.getUpdateTime())
                            .build();
                })
                .toList();
    }

    static String applicationStatusName(Integer status) {
        if (status == null) {
            return "未知";
        }
        return switch (status) {
            case APP_STATUS_APPLIED -> "已投递";
            case APP_STATUS_VIEWED -> "已查看";
            case APP_STATUS_INTERVIEW -> "面试中";
            case APP_STATUS_HIRED -> "录用";
            case APP_STATUS_REJECTED -> "已拒绝";
            default -> "未知";
        };
    }
}
