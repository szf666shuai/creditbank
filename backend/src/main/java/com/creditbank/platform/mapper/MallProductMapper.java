package com.creditbank.platform.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.creditbank.platform.dto.MallProductVO;
import com.creditbank.platform.entity.MallProduct;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface MallProductMapper extends BaseMapper<MallProduct> {

    @Select("""
            SELECT p.id, p.category_id AS categoryId, c.name AS categoryName, p.name, p.description,
                   p.cover_url AS coverUrl, p.product_type AS productType, p.ref_course_id AS refCourseId,
                   p.price_credit AS priceCredit, p.price_money AS priceMoney, p.stock
            FROM mall_product p
            JOIN mall_category c ON c.id = p.category_id
            WHERE p.deleted = 0 AND p.status = 1 AND p.approval_status = 1 AND c.status = 1
              AND (#{categoryId} IS NULL OR p.category_id = #{categoryId})
              AND (#{keyword} IS NULL OR #{keyword} = ''
                   OR p.name LIKE CONCAT('%', #{keyword}, '%')
                   OR p.description LIKE CONCAT('%', #{keyword}, '%'))
            ORDER BY c.sort_order ASC, p.create_time DESC
            """)
    List<MallProductVO> listProducts(@Param("categoryId") Long categoryId, @Param("keyword") String keyword);

    @Select("""
            SELECT p.id, p.category_id AS categoryId, c.name AS categoryName, p.name, p.description,
                   p.cover_url AS coverUrl, p.product_type AS productType, p.ref_course_id AS refCourseId,
                   p.price_credit AS priceCredit, p.price_money AS priceMoney, p.stock
            FROM mall_product p
            JOIN mall_category c ON c.id = p.category_id
            WHERE p.id = #{productId} AND p.deleted = 0 AND p.status = 1 AND c.status = 1
            """)
    MallProductVO getProduct(@Param("productId") Long productId);

    @Update("""
            UPDATE mall_product
            SET stock = stock - #{quantity}
            WHERE id = #{productId} AND deleted = 0 AND status = 1
              AND stock IS NOT NULL AND stock >= #{quantity}
            """)
    int decreaseStock(@Param("productId") Long productId, @Param("quantity") Integer quantity);
}
