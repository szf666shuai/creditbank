package com.creditbank.platform.module.admin.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.creditbank.platform.common.BusinessException;
import com.creditbank.platform.common.PageResult;
import com.creditbank.platform.constant.OrgType;
import com.creditbank.platform.entity.SysOrganization;
import com.creditbank.platform.mapper.SysOrganizationMapper;
import com.creditbank.platform.module.admin.dto.AdminOrganizationVO;
import com.creditbank.platform.module.admin.dto.UpdateOrganizationJoinStatusRequest;
import com.creditbank.platform.module.admin.dto.UpdateOrganizationStatusRequest;
import com.creditbank.platform.security.AuthSupport;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminOrganizationService {

    private final AuthSupport authSupport;
    private final SysOrganizationMapper sysOrganizationMapper;

    public PageResult<AdminOrganizationVO> pageOrganizations(
            long page, long pageSize, Integer joinStatus, Integer status, String keyword) {
        authSupport.requireAdmin();
        AdminSupport.validatePage(page, pageSize);

        LambdaQueryWrapper<SysOrganization> wrapper = new LambdaQueryWrapper<SysOrganization>()
                .eq(joinStatus != null, SysOrganization::getJoinStatus, joinStatus)
                .eq(status != null, SysOrganization::getStatus, status)
                .orderByDesc(SysOrganization::getCreateTime);
        if (keyword != null && !keyword.isBlank()) {
            String kw = keyword.trim();
            wrapper.and(w -> w.like(SysOrganization::getName, kw).or().like(SysOrganization::getCode, kw));
        }

        Page<SysOrganization> result = sysOrganizationMapper.selectPage(new Page<>(page, pageSize), wrapper);
        return PageResult.of(
                result.getRecords().stream().map(this::toVO).toList(),
                result.getTotal(),
                result.getCurrent(),
                result.getSize());
    }

    @Transactional
    public AdminOrganizationVO updateJoinStatus(Long orgId, UpdateOrganizationJoinStatusRequest request) {
        authSupport.requireAdmin();
        SysOrganization org = requireOrg(orgId);
        Integer joinStatus = request.getJoinStatus();
        if (joinStatus != AdminSupport.JOIN_PENDING
                && joinStatus != AdminSupport.JOIN_APPROVED
                && joinStatus != AdminSupport.JOIN_EXITED) {
            throw new BusinessException(400, "加盟状态无效");
        }
        org.setJoinStatus(joinStatus);
        if (joinStatus == AdminSupport.JOIN_APPROVED) {
            org.setStatus(1);
        }
        sysOrganizationMapper.updateById(org);
        return toVO(org);
    }

    @Transactional
    public AdminOrganizationVO updateStatus(Long orgId, UpdateOrganizationStatusRequest request) {
        authSupport.requireAdmin();
        SysOrganization org = requireOrg(orgId);
        if (request.getStatus() != 0 && request.getStatus() != 1) {
            throw new BusinessException(400, "状态无效");
        }
        org.setStatus(request.getStatus());
        sysOrganizationMapper.updateById(org);
        return toVO(org);
    }

    private SysOrganization requireOrg(Long orgId) {
        SysOrganization org = sysOrganizationMapper.selectById(orgId);
        if (org == null) {
            throw new BusinessException(404, "机构不存在");
        }
        return org;
    }

    private AdminOrganizationVO toVO(SysOrganization org) {
        return AdminOrganizationVO.builder()
                .id(org.getId())
                .name(org.getName())
                .code(org.getCode())
                .type(org.getType())
                .typeName(OrgType.label(org.getType()))
                .contact(org.getContact())
                .phone(org.getPhone())
                .email(org.getEmail())
                .joinStatus(org.getJoinStatus())
                .joinStatusName(AdminSupport.joinStatusName(org.getJoinStatus()))
                .status(org.getStatus())
                .statusName(AdminSupport.orgStatusName(org.getStatus()))
                .createTime(org.getCreateTime())
                .build();
    }
}
