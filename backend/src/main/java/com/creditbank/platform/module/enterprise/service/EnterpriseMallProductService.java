package com.creditbank.platform.module.enterprise.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.creditbank.platform.common.BusinessException;
import com.creditbank.platform.entity.MallCategory;
import com.creditbank.platform.entity.MallProduct;
import com.creditbank.platform.entity.SysUser;
import com.creditbank.platform.mapper.MallCategoryMapper;
import com.creditbank.platform.mapper.MallProductMapper;
import com.creditbank.platform.module.enterprise.dto.EnterpriseMallProductSaveRequest;
import com.creditbank.platform.module.enterprise.dto.EnterpriseMallProductVO;
import com.creditbank.platform.security.AuthSupport;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EnterpriseMallProductService {

    private final AuthSupport authSupport;
    private final MallProductMapper mallProductMapper;
    private final MallCategoryMapper mallCategoryMapper;

    public List<EnterpriseMallProductVO> listMyProducts() {
        SysUser user = authSupport.requireEnterprise();
        return mallProductMapper.selectList(
                        new LambdaQueryWrapper<MallProduct>()
                                .eq(MallProduct::getOrgId, user.getOrgId())
                                .eq(MallProduct::getDeleted, 0)
                                .orderByDesc(MallProduct::getCreateTime)
                )
                .stream()
                .map(this::toVO)
                .toList();
    }

    @Transactional
    public EnterpriseMallProductVO createProduct(EnterpriseMallProductSaveRequest request) {
        SysUser user = authSupport.requireEnterpriseWritable();
        requireCategory(request.getCategoryId());
        MallProduct product = new MallProduct();
        product.setCategoryId(request.getCategoryId());
        product.setOrgId(user.getOrgId());
        product.setPublisherId(user.getId());
        product.setName(request.getName().trim());
        product.setDescription(trim(request.getDescription()));
        product.setCoverUrl(trim(request.getCoverUrl()));
        product.setProductType(request.getProductType());
        product.setRefCourseId(request.getRefCourseId());
        product.setPriceCredit(request.getPriceCredit());
        product.setPriceMoney(request.getPriceMoney() == null ? BigDecimal.ZERO : request.getPriceMoney());
        product.setStock(request.getStock() == null ? 99 : request.getStock());
        product.setStatus(0);
        product.setApprovalStatus(0);
        mallProductMapper.insert(product);
        return toVO(product);
    }

    @Transactional
    public EnterpriseMallProductVO updateProduct(Long id, EnterpriseMallProductSaveRequest request) {
        SysUser user = authSupport.requireEnterpriseWritable();
        MallProduct product = requireOwnedProduct(id, user.getOrgId());
        if (product.getApprovalStatus() != null && product.getApprovalStatus() == 1) {
            throw new BusinessException(400, "已上架商品请下架后再编辑");
        }
        requireCategory(request.getCategoryId());
        product.setCategoryId(request.getCategoryId());
        product.setName(request.getName().trim());
        product.setDescription(trim(request.getDescription()));
        product.setCoverUrl(trim(request.getCoverUrl()));
        product.setProductType(request.getProductType());
        product.setRefCourseId(request.getRefCourseId());
        product.setPriceCredit(request.getPriceCredit());
        product.setPriceMoney(request.getPriceMoney() == null ? BigDecimal.ZERO : request.getPriceMoney());
        product.setStock(request.getStock() == null ? product.getStock() : request.getStock());
        product.setApprovalStatus(0);
        product.setStatus(0);
        mallProductMapper.updateById(product);
        return toVO(product);
    }

    private MallProduct requireOwnedProduct(Long id, Long orgId) {
        MallProduct product = mallProductMapper.selectById(id);
        if (product == null || product.getDeleted() != null && product.getDeleted() == 1) {
            throw new BusinessException(404, "商品不存在");
        }
        if (!orgId.equals(product.getOrgId())) {
            throw new BusinessException(403, "无权操作该商品");
        }
        return product;
    }

    private void requireCategory(Long categoryId) {
        MallCategory category = mallCategoryMapper.selectById(categoryId);
        if (category == null || category.getStatus() == null || category.getStatus() != 1) {
            throw new BusinessException(404, "商品分类不存在");
        }
    }

    private EnterpriseMallProductVO toVO(MallProduct product) {
        EnterpriseMallProductVO vo = new EnterpriseMallProductVO();
        vo.setId(product.getId());
        vo.setCategoryId(product.getCategoryId());
        MallCategory category = mallCategoryMapper.selectById(product.getCategoryId());
        vo.setCategoryName(category == null ? null : category.getName());
        vo.setName(product.getName());
        vo.setDescription(product.getDescription());
        vo.setCoverUrl(product.getCoverUrl());
        vo.setProductType(product.getProductType());
        vo.setProductTypeName(productTypeName(product.getProductType()));
        vo.setRefCourseId(product.getRefCourseId());
        vo.setPriceCredit(product.getPriceCredit());
        vo.setPriceMoney(product.getPriceMoney());
        vo.setStock(product.getStock());
        vo.setStatus(product.getStatus());
        vo.setApprovalStatus(product.getApprovalStatus());
        vo.setApprovalStatusName(approvalStatusName(product.getApprovalStatus()));
        vo.setReviewRemark(product.getReviewRemark());
        vo.setCreateTime(product.getCreateTime());
        return vo;
    }

    private String productTypeName(Integer type) {
        if (type == null) return "未知";
        return switch (type) {
            case 1 -> "实物商品";
            case 2 -> "虚拟商品";
            case 3 -> "课程兑换";
            case 4 -> "服务权益";
            default -> "其他";
        };
    }

    private String approvalStatusName(Integer status) {
        if (status == null) return "待审核";
        return switch (status) {
            case 1 -> "已通过";
            case 2 -> "已驳回";
            default -> "待审核";
        };
    }

    private String trim(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }
}
