package com.creditbank.platform.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.creditbank.platform.entity.MallOrderItem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface MallOrderItemMapper extends BaseMapper<MallOrderItem> {

    @Select("""
            SELECT DISTINCT p.ref_course_id
            FROM mall_order_item item
            JOIN mall_order o ON o.id = item.order_id AND o.pay_status = 1
            JOIN mall_product p ON p.id = item.product_id
            WHERE o.user_id = #{userId} AND p.product_type = 3 AND p.ref_course_id IS NOT NULL
            """)
    List<Long> findPurchasedCourseIds(@Param("userId") Long userId);
}
