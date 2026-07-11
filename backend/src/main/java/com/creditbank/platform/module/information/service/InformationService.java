package com.creditbank.platform.module.information.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.creditbank.platform.common.BusinessException;
import com.creditbank.platform.common.PageResult;
import com.creditbank.platform.entity.PolicyNews;
import com.creditbank.platform.entity.SysOrganization;
import com.creditbank.platform.mapper.PolicyNewsMapper;
import com.creditbank.platform.mapper.SysOrganizationMapper;
import com.creditbank.platform.module.enterprise.entity.Activity;
import com.creditbank.platform.module.enterprise.entity.JobPosting;
import com.creditbank.platform.module.enterprise.mapper.ActivityMapper;
import com.creditbank.platform.module.enterprise.mapper.JobPostingMapper;
import com.creditbank.platform.module.information.dto.InformationDetailVO;
import com.creditbank.platform.module.information.dto.InformationItemVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InformationService {

    private static final String TYPE_JOB = "job";
    private static final String TYPE_ACTIVITY = "activity";
    private static final String TYPE_POLICY = "policy";

    private final JobPostingMapper jobPostingMapper;
    private final ActivityMapper activityMapper;
    private final PolicyNewsMapper policyNewsMapper;
    private final SysOrganizationMapper organizationMapper;

    public PageResult<InformationItemVO> page(String type, long page, long pageSize, String keyword) {
        validateType(type);
        validatePage(page, pageSize);
        return switch (type) {
            case TYPE_JOB -> pageJobs(page, pageSize, keyword);
            case TYPE_ACTIVITY -> pageActivities(page, pageSize, keyword);
            case TYPE_POLICY -> pagePolicies(page, pageSize, keyword);
            default -> throw new BusinessException(400, "资讯类型无效");
        };
    }

    public InformationDetailVO getDetail(String type, Long id) {
        validateType(type);
        return switch (type) {
            case TYPE_JOB -> getJobDetail(id);
            case TYPE_ACTIVITY -> getActivityDetail(id);
            case TYPE_POLICY -> getPolicyDetail(id);
            default -> throw new BusinessException(400, "资讯类型无效");
        };
    }

    private PageResult<InformationItemVO> pageJobs(long page, long pageSize, String keyword) {
        Page<JobPosting> result = jobPostingMapper.selectPage(new Page<>(page, pageSize),
                new LambdaQueryWrapper<JobPosting>()
                        .eq(JobPosting::getStatus, 1)
                        .and(StringUtils.hasText(keyword), w -> w
                                .like(JobPosting::getTitle, keyword)
                                .or()
                                .like(JobPosting::getDescription, keyword)
                                .or()
                                .like(JobPosting::getRequirements, keyword))
                        .orderByDesc(JobPosting::getCreateTime));
        Map<Long, String> orgNames = loadOrgNames(result.getRecords().stream().map(JobPosting::getOrgId).distinct().toList());
        return PageResult.of(
                result.getRecords().stream().map(job -> InformationItemVO.builder()
                        .type(TYPE_JOB)
                        .id(job.getId())
                        .orgId(job.getOrgId())
                        .orgName(orgNames.get(job.getOrgId()))
                        .title(job.getTitle())
                        .summary(summary(job.getDescription()))
                        .location(job.getLocation())
                        .tag(job.getSalaryRange())
                        .status(job.getStatus())
                        .statusName("招聘中")
                        .createTime(job.getCreateTime())
                        .build()).toList(),
                result.getTotal(),
                result.getCurrent(),
                result.getSize());
    }

    private PageResult<InformationItemVO> pageActivities(long page, long pageSize, String keyword) {
        Page<Activity> result = activityMapper.selectPage(new Page<>(page, pageSize),
                new LambdaQueryWrapper<Activity>()
                        .in(Activity::getStatus, 1, 2, 3)
                        .and(StringUtils.hasText(keyword), w -> w
                                .like(Activity::getTitle, keyword)
                                .or()
                                .like(Activity::getDescription, keyword))
                        .orderByDesc(Activity::getStartTime));
        Map<Long, String> orgNames = loadOrgNames(result.getRecords().stream().map(Activity::getOrgId).distinct().toList());
        return PageResult.of(
                result.getRecords().stream().map(activity -> InformationItemVO.builder()
                        .type(TYPE_ACTIVITY)
                        .id(activity.getId())
                        .orgId(activity.getOrgId())
                        .orgName(orgNames.get(activity.getOrgId()))
                        .title(activity.getTitle())
                        .summary(summary(activity.getDescription()))
                        .location(activity.getLocation())
                        .tag(activity.getCreditReward() == null ? null : "奖励 " + activity.getCreditReward() + " 学分")
                        .status(activity.getStatus())
                        .statusName(activityStatusLabel(activity.getStatus()))
                        .startTime(activity.getStartTime())
                        .createTime(activity.getCreateTime())
                        .build()).toList(),
                result.getTotal(),
                result.getCurrent(),
                result.getSize());
    }

    private PageResult<InformationItemVO> pagePolicies(long page, long pageSize, String keyword) {
        Page<PolicyNews> result = policyNewsMapper.selectPage(new Page<>(page, pageSize),
                new LambdaQueryWrapper<PolicyNews>()
                        .eq(PolicyNews::getStatus, 1)
                        .and(StringUtils.hasText(keyword), w -> w
                                .like(PolicyNews::getTitle, keyword)
                                .or()
                                .like(PolicyNews::getContent, keyword)
                                .or()
                                .like(PolicyNews::getSource, keyword))
                        .orderByDesc(PolicyNews::getPublishTime)
                        .orderByDesc(PolicyNews::getCreateTime));
        return PageResult.of(
                result.getRecords().stream().map(policy -> InformationItemVO.builder()
                        .type(TYPE_POLICY)
                        .id(policy.getId())
                        .title(policy.getTitle())
                        .summary(summary(policy.getContent()))
                        .tag(policy.getSource())
                        .status(policy.getStatus())
                        .statusName("已发布")
                        .publishTime(policy.getPublishTime())
                        .createTime(policy.getCreateTime())
                        .build()).toList(),
                result.getTotal(),
                result.getCurrent(),
                result.getSize());
    }

    private InformationDetailVO getJobDetail(Long id) {
        JobPosting job = jobPostingMapper.selectOne(new LambdaQueryWrapper<JobPosting>()
                .eq(JobPosting::getId, id)
                .eq(JobPosting::getStatus, 1));
        if (job == null) {
            throw new BusinessException(404, "招聘信息不存在");
        }
        String orgName = loadOrgNames(List.of(job.getOrgId())).get(job.getOrgId());
        return InformationDetailVO.builder()
                .type(TYPE_JOB)
                .id(job.getId())
                .orgId(job.getOrgId())
                .orgName(orgName)
                .title(job.getTitle())
                .description(job.getDescription())
                .requirements(job.getRequirements())
                .salaryRange(job.getSalaryRange())
                .location(job.getLocation())
                .eduRequirement(job.getEduRequirement())
                .status(job.getStatus())
                .statusName("招聘中")
                .createTime(job.getCreateTime())
                .build();
    }

    private InformationDetailVO getActivityDetail(Long id) {
        Activity activity = activityMapper.selectOne(new LambdaQueryWrapper<Activity>()
                .eq(Activity::getId, id)
                .in(Activity::getStatus, 1, 2, 3));
        if (activity == null) {
            throw new BusinessException(404, "活动信息不存在");
        }
        String orgName = loadOrgNames(List.of(activity.getOrgId())).get(activity.getOrgId());
        return InformationDetailVO.builder()
                .type(TYPE_ACTIVITY)
                .id(activity.getId())
                .orgId(activity.getOrgId())
                .orgName(orgName)
                .title(activity.getTitle())
                .description(activity.getDescription())
                .location(activity.getLocation())
                .maxParticipants(activity.getMaxParticipants())
                .creditReward(activity.getCreditReward())
                .status(activity.getStatus())
                .statusName(activityStatusLabel(activity.getStatus()))
                .startTime(activity.getStartTime())
                .endTime(activity.getEndTime())
                .createTime(activity.getCreateTime())
                .build();
    }

    private InformationDetailVO getPolicyDetail(Long id) {
        PolicyNews policy = policyNewsMapper.selectOne(new LambdaQueryWrapper<PolicyNews>()
                .eq(PolicyNews::getId, id)
                .eq(PolicyNews::getStatus, 1));
        if (policy == null) {
            throw new BusinessException(404, "政策资讯不存在");
        }
        return InformationDetailVO.builder()
                .type(TYPE_POLICY)
                .id(policy.getId())
                .title(policy.getTitle())
                .content(policy.getContent())
                .source(policy.getSource())
                .author(policy.getAuthor())
                .coverUrl(policy.getCoverUrl())
                .status(policy.getStatus())
                .statusName("已发布")
                .publishTime(policy.getPublishTime())
                .createTime(policy.getCreateTime())
                .build();
    }

    private Map<Long, String> loadOrgNames(List<Long> orgIds) {
        if (orgIds.isEmpty()) {
            return Map.of();
        }
        return organizationMapper.selectList(new LambdaQueryWrapper<SysOrganization>().in(SysOrganization::getId, orgIds))
                .stream()
                .collect(Collectors.toMap(SysOrganization::getId, SysOrganization::getName));
    }

    private String summary(String value) {
        if (!StringUtils.hasText(value)) {
            return "";
        }
        String text = value.trim();
        return text.length() > 120 ? text.substring(0, 120) + "..." : text;
    }

    private String activityStatusLabel(Integer status) {
        if (status == null) {
            return "未知";
        }
        return switch (status) {
            case 1 -> "报名中";
            case 2 -> "进行中";
            case 3 -> "已结束";
            default -> "未知";
        };
    }

    private void validateType(String type) {
        if (!TYPE_JOB.equals(type) && !TYPE_ACTIVITY.equals(type) && !TYPE_POLICY.equals(type)) {
            throw new BusinessException(400, "资讯类型无效");
        }
    }

    private void validatePage(long page, long pageSize) {
        if (page < 1 || pageSize < 1 || pageSize > 100) {
            throw new BusinessException(400, "分页参数无效");
        }
    }
}
