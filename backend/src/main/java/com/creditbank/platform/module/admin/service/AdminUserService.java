package com.creditbank.platform.module.admin.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.creditbank.platform.common.BusinessException;
import com.creditbank.platform.common.PageResult;
import com.creditbank.platform.constant.UserRole;
import com.creditbank.platform.entity.SysOrganization;
import com.creditbank.platform.entity.SysUser;
import com.creditbank.platform.mapper.SysOrganizationMapper;
import com.creditbank.platform.mapper.SysUserMapper;
import com.creditbank.platform.module.admin.dto.AdminUserVO;
import com.creditbank.platform.module.admin.dto.CreateEnterpriseUserRequest;
import com.creditbank.platform.module.admin.dto.UpdateUserStatusRequest;
import com.creditbank.platform.security.AuthSupport;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminUserService {

    private final AuthSupport authSupport;
    private final SysUserMapper sysUserMapper;
    private final SysOrganizationMapper sysOrganizationMapper;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public PageResult<AdminUserVO> pageUsers(
            long page, long pageSize, Integer role, Integer status, String keyword) {
        authSupport.requireAdmin();
        AdminSupport.validatePage(page, pageSize);

        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<SysUser>()
                .eq(role != null, SysUser::getRole, role)
                .eq(status != null, SysUser::getStatus, status)
                .orderByDesc(SysUser::getCreateTime);
        if (StringUtils.hasText(keyword)) {
            String kw = keyword.trim();
            wrapper.and(w -> w.like(SysUser::getUsername, kw)
                    .or().like(SysUser::getRealName, kw)
                    .or().like(SysUser::getPhone, kw));
        }

        Page<SysUser> result = sysUserMapper.selectPage(new Page<>(page, pageSize), wrapper);
        Map<Long, String> orgNameMap = loadOrgNameMap(result.getRecords());
        return PageResult.of(
                result.getRecords().stream().map(user -> toVO(user, orgNameMap.get(user.getOrgId()))).toList(),
                result.getTotal(),
                result.getCurrent(),
                result.getSize());
    }

    @Transactional
    public AdminUserVO updateStatus(Long userId, UpdateUserStatusRequest request) {
        authSupport.requireAdmin();
        SysUser user = requireUser(userId);
        if (request.getStatus() != 0 && request.getStatus() != 1) {
            throw new BusinessException(400, "状态无效");
        }
        if (Objects.equals(user.getRole(), UserRole.ADMIN) && request.getStatus() == 0) {
            throw new BusinessException(400, "不能禁用管理员账号");
        }
        user.setStatus(request.getStatus());
        sysUserMapper.updateById(user);
        return toVO(user, loadOrgName(user.getOrgId()));
    }

    @Transactional
    public AdminUserVO createEnterpriseUser(CreateEnterpriseUserRequest request) {
        authSupport.requireAdmin();
        Long exists = sysUserMapper.selectCount(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, request.getUsername()));
        if (exists != null && exists > 0) {
            throw new BusinessException(400, "用户名已存在");
        }

        SysOrganization org = sysOrganizationMapper.selectById(request.getOrgId());
        if (org == null) {
            throw new BusinessException(404, "机构不存在");
        }
        if (org.getJoinStatus() == null || org.getJoinStatus() != AdminSupport.JOIN_APPROVED) {
            throw new BusinessException(400, "机构尚未通过加盟审核");
        }

        SysUser user = new SysUser();
        user.setUsername(request.getUsername().trim());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRealName(request.getRealName().trim());
        user.setPhone(request.getPhone());
        user.setEmail(request.getEmail());
        user.setRole(UserRole.ENTERPRISE);
        user.setOrgId(request.getOrgId());
        user.setStatus(1);
        sysUserMapper.insert(user);
        return toVO(user, org.getName());
    }

    private SysUser requireUser(Long userId) {
        SysUser user = sysUserMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(404, "用户不存在");
        }
        return user;
    }

    private String loadOrgName(Long orgId) {
        if (orgId == null) {
            return null;
        }
        SysOrganization org = sysOrganizationMapper.selectById(orgId);
        return org != null ? org.getName() : null;
    }

    private Map<Long, String> loadOrgNameMap(List<SysUser> users) {
        List<Long> orgIds = users.stream()
                .map(SysUser::getOrgId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        if (orgIds.isEmpty()) {
            return Map.of();
        }
        return sysOrganizationMapper.selectList(new LambdaQueryWrapper<SysOrganization>()
                        .in(SysOrganization::getId, orgIds))
                .stream()
                .collect(Collectors.toMap(SysOrganization::getId, SysOrganization::getName));
    }

    private AdminUserVO toVO(SysUser user, String orgName) {
        return AdminUserVO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .realName(user.getRealName())
                .phone(user.getPhone())
                .email(user.getEmail())
                .role(user.getRole())
                .roleName(UserRole.getName(user.getRole() != null ? user.getRole() : UserRole.STUDENT))
                .orgId(user.getOrgId())
                .orgName(orgName)
                .status(user.getStatus())
                .statusName(AdminSupport.userStatusName(user.getStatus()))
                .createTime(user.getCreateTime())
                .build();
    }
}
