package com.creditbank.platform.module.enterprise.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.creditbank.platform.common.BusinessException;
import com.creditbank.platform.entity.SysUser;
import com.creditbank.platform.module.enterprise.dto.MaterialManageVO;
import com.creditbank.platform.module.enterprise.dto.MaterialSaveRequest;
import com.creditbank.platform.module.enterprise.entity.OrgMaterial;
import com.creditbank.platform.module.enterprise.mapper.OrgMaterialMapper;
import com.creditbank.platform.security.AuthSupport;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class EnterpriseMaterialService {

    private static final int STATUS_ACTIVE = 1;
    private static final int STATUS_OFFLINE = 0;

    private final AuthSupport authSupport;
    private final OrgMaterialMapper orgMaterialMapper;

    public List<MaterialManageVO> listMyMaterials() {
        SysUser user = authSupport.requireEnterprise();
        List<OrgMaterial> materials = orgMaterialMapper.selectList(new LambdaQueryWrapper<OrgMaterial>()
                .eq(OrgMaterial::getOrgId, user.getOrgId())
                .orderByDesc(OrgMaterial::getCreateTime));
        return materials.stream().map(this::toVO).toList();
    }

    public MaterialManageVO createMaterial(MaterialSaveRequest request) {
        SysUser user = authSupport.requireEnterpriseWritable();
        validateMaterialType(request.getMaterialType());
        OrgMaterial material = new OrgMaterial();
        material.setOrgId(user.getOrgId());
        material.setPublisherId(user.getId());
        material.setTitle(request.getTitle().trim());
        material.setDescription(trimOrNull(request.getDescription()));
        material.setFileUrl(request.getFileUrl().trim());
        material.setMaterialType(request.getMaterialType());
        material.setStatus(STATUS_ACTIVE);
        orgMaterialMapper.insert(material);
        return toVO(orgMaterialMapper.selectById(material.getId()));
    }

    public MaterialManageVO updateMaterial(Long id, MaterialSaveRequest request) {
        SysUser user = authSupport.requireEnterpriseWritable();
        validateMaterialType(request.getMaterialType());
        OrgMaterial material = requireOwnedMaterial(id, user.getOrgId());
        material.setTitle(request.getTitle().trim());
        material.setDescription(trimOrNull(request.getDescription()));
        material.setFileUrl(request.getFileUrl().trim());
        material.setMaterialType(request.getMaterialType());
        orgMaterialMapper.updateById(material);
        return toVO(material);
    }

    public void offlineMaterial(Long id) {
        SysUser user = authSupport.requireEnterpriseWritable();
        OrgMaterial material = requireOwnedMaterial(id, user.getOrgId());
        material.setStatus(STATUS_OFFLINE);
        orgMaterialMapper.updateById(material);
    }

    public void onlineMaterial(Long id) {
        SysUser user = authSupport.requireEnterpriseWritable();
        OrgMaterial material = requireOwnedMaterial(id, user.getOrgId());
        material.setStatus(STATUS_ACTIVE);
        orgMaterialMapper.updateById(material);
    }

    private OrgMaterial requireOwnedMaterial(Long id, Long orgId) {
        OrgMaterial material = orgMaterialMapper.selectById(id);
        if (material == null || !Objects.equals(material.getOrgId(), orgId)) {
            throw new BusinessException(404, "资料不存在或无权操作");
        }
        return material;
    }

    private void validateMaterialType(Integer type) {
        if (type == null || type < 1 || type > 3) {
            throw new BusinessException(400, "资料类型无效");
        }
    }

    private MaterialManageVO toVO(OrgMaterial material) {
        return MaterialManageVO.builder()
                .id(material.getId())
                .title(material.getTitle())
                .description(material.getDescription())
                .fileUrl(material.getFileUrl())
                .materialType(material.getMaterialType())
                .materialTypeName(materialTypeName(material.getMaterialType()))
                .status(material.getStatus())
                .statusName(material.getStatus() != null && material.getStatus() == STATUS_ACTIVE ? "已发布" : "已下架")
                .createTime(material.getCreateTime())
                .build();
    }

    static String materialTypeName(Integer type) {
        if (type == null) {
            return "其他";
        }
        return switch (type) {
            case 1 -> "文档";
            case 2 -> "视频";
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
