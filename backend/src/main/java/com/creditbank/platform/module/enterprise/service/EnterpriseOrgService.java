package com.creditbank.platform.module.enterprise.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.creditbank.platform.common.BusinessException;
import com.creditbank.platform.entity.SysOrganization;
import com.creditbank.platform.entity.SysUser;
import com.creditbank.platform.mapper.SysOrganizationMapper;
import com.creditbank.platform.module.enterprise.dto.OrgVO;
import com.creditbank.platform.module.enterprise.dto.UpdateMyOrgRequest;
import com.creditbank.platform.security.AuthSupport;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class EnterpriseOrgService {

    private final AuthSupport authSupport;
    private final SysOrganizationMapper orgMapper;

    public OrgVO getMyOrg() {
        SysUser user = authSupport.requireEnterprise();
        return toVO(requireOwnedOrg(user.getOrgId()));
    }

    public OrgVO updateMyOrg(UpdateMyOrgRequest request) {
        SysUser user = authSupport.requireEnterprise();
        authSupport.requireOrganizationNotDisabled(user.getOrgId());
        SysOrganization org = requireOwnedOrg(user.getOrgId());
        org.setName(request.getName().trim());
        org.setIntro(trimOrNull(request.getIntro()));
        org.setContact(trimOrNull(request.getContact()));
        org.setPhone(trimOrNull(request.getPhone()));
        org.setEmail(trimOrNull(request.getEmail()));
        org.setAddress(trimOrNull(request.getAddress()));
        org.setWebsite(trimOrNull(request.getWebsite()));
        org.setLogo(trimOrNull(request.getLogo()));
        orgMapper.updateById(org);
        return toVO(org);
    }

    private SysOrganization requireOwnedOrg(Long orgId) {
        if (orgId == null) {
            throw new BusinessException(403, "当前账号未绑定企业机构");
        }
        SysOrganization org = orgMapper.selectById(orgId);
        if (org == null) {
            throw new BusinessException(404, "企业机构不存在");
        }
        return org;
    }

    private OrgVO toVO(SysOrganization org) {
        return OrgVO.builder()
                .id(org.getId())
                .name(org.getName())
                .code(org.getCode())
                .type(org.getType())
                .typeName(orgTypeName(org.getType()))
                .logo(org.getLogo())
                .intro(org.getIntro())
                .contact(org.getContact())
                .phone(org.getPhone())
                .email(org.getEmail())
                .address(org.getAddress())
                .website(org.getWebsite())
                .build();
    }

    static String orgTypeName(Integer type) {
        if (type == null) {
            return "其他";
        }
        return switch (type) {
            case 1 -> "高校";
            case 2 -> "培训机构";
            case 3 -> "企业";
            case 4 -> "政府机构";
            default -> "其他";
        };
    }

    private static String trimOrNull(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return value.trim();
    }
}
