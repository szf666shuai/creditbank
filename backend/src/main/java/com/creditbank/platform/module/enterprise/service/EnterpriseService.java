package com.creditbank.platform.module.enterprise.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.creditbank.platform.common.BusinessException;
import com.creditbank.platform.common.PageResult;
import com.creditbank.platform.constant.OrgType;
import com.creditbank.platform.entity.SysOrganization;
import com.creditbank.platform.mapper.SysOrganizationMapper;
import com.creditbank.platform.module.enterprise.dto.ActivityVO;
import com.creditbank.platform.module.enterprise.dto.JobPostingVO;
import com.creditbank.platform.module.enterprise.dto.OrgMaterialVO;
import com.creditbank.platform.module.enterprise.dto.OrgVO;
import com.creditbank.platform.module.enterprise.entity.Activity;
import com.creditbank.platform.module.enterprise.service.ActivityLifecycleService;
import com.creditbank.platform.module.enterprise.support.ActivityStatus;
import com.creditbank.platform.module.enterprise.entity.JobPosting;
import com.creditbank.platform.module.enterprise.entity.OrgMaterial;
import com.creditbank.platform.module.enterprise.mapper.ActivityMapper;
import com.creditbank.platform.module.enterprise.mapper.JobPostingMapper;
import com.creditbank.platform.module.enterprise.mapper.OrgMaterialMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class EnterpriseService {

    private static final int JOINED = 1;
    private static final int ACTIVE = 1;
    private static final int JOB_OPEN = 1;
    private static final int MATERIAL_ACTIVE = 1;

    private final SysOrganizationMapper orgMapper;
    private final JobPostingMapper jobPostingMapper;
    private final ActivityMapper activityMapper;
    private final OrgMaterialMapper orgMaterialMapper;
    private final ActivityLifecycleService activityLifecycleService;

    public PageResult<OrgVO> pageJoinedOrgs(long page, long pageSize, String name, Integer type) {
        LambdaQueryWrapper<SysOrganization> wrapper = new LambdaQueryWrapper<SysOrganization>()
                .eq(SysOrganization::getJoinStatus, JOINED)
                .eq(SysOrganization::getStatus, ACTIVE)
                .like(StringUtils.hasText(name), SysOrganization::getName, name)
                .eq(type != null, SysOrganization::getType, type)
                .orderByDesc(SysOrganization::getCreateTime);

        Page<SysOrganization> result = orgMapper.selectPage(new Page<>(page, pageSize), wrapper);

        return PageResult.of(
                result.getRecords().stream().map(this::toVO).toList(),
                result.getTotal(),
                result.getCurrent(),
                result.getSize()
        );
    }

    public OrgVO getJoinedOrgDetail(Long id) {
        return toVO(getJoinedOrgOrThrow(id));
    }

    public PageResult<JobPostingVO> pageOrgJobs(Long orgId, long page, long pageSize) {
        getJoinedOrgOrThrow(orgId);

        Page<JobPosting> result = jobPostingMapper.selectPage(
                new Page<>(page, pageSize),
                new LambdaQueryWrapper<JobPosting>()
                        .eq(JobPosting::getOrgId, orgId)
                        .eq(JobPosting::getStatus, JOB_OPEN)
                        .orderByDesc(JobPosting::getCreateTime)
        );

        return PageResult.of(
                result.getRecords().stream().map(this::toJobVO).toList(),
                result.getTotal(),
                result.getCurrent(),
                result.getSize()
        );
    }

    public PageResult<ActivityVO> pageOrgActivities(Long orgId, long page, long pageSize) {
        getJoinedOrgOrThrow(orgId);

        Page<Activity> result = activityMapper.selectPage(
                new Page<>(page, pageSize),
                new LambdaQueryWrapper<Activity>()
                        .eq(Activity::getOrgId, orgId)
                        .in(Activity::getStatus, 1, 2, 3)
                        .orderByDesc(Activity::getStartTime)
        );

        return PageResult.of(
                activityLifecycleService.refreshAll(result.getRecords()).stream()
                        .map(this::toActivityVO).toList(),
                result.getTotal(),
                result.getCurrent(),
                result.getSize()
        );
    }

    public PageResult<OrgMaterialVO> pageOrgMaterials(Long orgId, long page, long pageSize) {
        getJoinedOrgOrThrow(orgId);

        Page<OrgMaterial> result = orgMaterialMapper.selectPage(
                new Page<>(page, pageSize),
                new LambdaQueryWrapper<OrgMaterial>()
                        .eq(OrgMaterial::getOrgId, orgId)
                        .eq(OrgMaterial::getStatus, MATERIAL_ACTIVE)
                        .orderByDesc(OrgMaterial::getCreateTime)
        );

        return PageResult.of(
                result.getRecords().stream().map(this::toMaterialVO).toList(),
                result.getTotal(),
                result.getCurrent(),
                result.getSize()
        );
    }

    private SysOrganization getJoinedOrgOrThrow(Long id) {
        SysOrganization org = orgMapper.selectOne(new LambdaQueryWrapper<SysOrganization>()
                .eq(SysOrganization::getId, id)
                .eq(SysOrganization::getJoinStatus, JOINED)
                .eq(SysOrganization::getStatus, ACTIVE));
        if (org == null) {
            throw new BusinessException(404, "企业不存在或未加盟");
        }
        return org;
    }

    private OrgVO toVO(SysOrganization org) {
        return OrgVO.builder()
                .id(org.getId())
                .name(org.getName())
                .code(org.getCode())
                .type(org.getType())
                .typeName(OrgType.label(org.getType()))
                .logo(org.getLogo())
                .intro(org.getIntro())
                .contact(org.getContact())
                .phone(org.getPhone())
                .email(org.getEmail())
                .address(org.getAddress())
                .website(org.getWebsite())
                .build();
    }

    private JobPostingVO toJobVO(JobPosting job) {
        return JobPostingVO.builder()
                .id(job.getId())
                .title(job.getTitle())
                .description(job.getDescription())
                .requirements(job.getRequirements())
                .salaryRange(job.getSalaryRange())
                .location(job.getLocation())
                .eduRequirement(job.getEduRequirement())
                .status(job.getStatus())
                .statusName(jobStatusLabel(job.getStatus()))
                .createTime(job.getCreateTime())
                .build();
    }

    private ActivityVO toActivityVO(Activity activity) {
        return ActivityVO.builder()
                .id(activity.getId())
                .title(activity.getTitle())
                .description(activity.getDescription())
                .location(activity.getLocation())
                .startTime(activity.getStartTime())
                .endTime(activity.getEndTime())
                .maxParticipants(activity.getMaxParticipants())
                .creditReward(activity.getCreditReward())
                .status(activity.getStatus())
                .statusName(ActivityStatus.label(activity.getStatus()))
                .createTime(activity.getCreateTime())
                .build();
    }

    private OrgMaterialVO toMaterialVO(OrgMaterial material) {
        return OrgMaterialVO.builder()
                .id(material.getId())
                .title(material.getTitle())
                .description(material.getDescription())
                .fileUrl(material.getFileUrl())
                .materialType(material.getMaterialType())
                .materialTypeName(materialTypeLabel(material.getMaterialType()))
                .createTime(material.getCreateTime())
                .build();
    }

    private String jobStatusLabel(Integer status) {
        if (status == null) {
            return "未知";
        }
        return status == JOB_OPEN ? "招聘中" : "已下架";
    }

    private String materialTypeLabel(Integer type) {
        if (type == null) {
            return "其他";
        }
        return switch (type) {
            case 1 -> "文档";
            case 2 -> "视频";
            default -> "其他";
        };
    }
}
