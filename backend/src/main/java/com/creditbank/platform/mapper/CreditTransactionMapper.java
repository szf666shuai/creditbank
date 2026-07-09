package com.creditbank.platform.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.creditbank.platform.entity.CreditTransaction;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Mapper
public interface CreditTransactionMapper extends BaseMapper<CreditTransaction> {

    @Select("""
            SELECT COUNT(1) FROM credit_transaction
            WHERE user_id = #{userId}
              AND biz_type = #{bizType}
              AND create_time >= #{start}
              AND create_time < #{end}
            """)
    long countByUserBizBetween(@Param("userId") Long userId,
                               @Param("bizType") String bizType,
                               @Param("start") LocalDateTime start,
                               @Param("end") LocalDateTime end);

    @Select("""
            SELECT IFNULL(SUM(amount), 0) FROM credit_transaction
            WHERE user_id = #{userId}
              AND biz_type = #{bizType}
              AND create_time >= #{start}
              AND create_time < #{end}
              AND amount > 0
            """)
    BigDecimal sumEarnByUserBizBetween(@Param("userId") Long userId,
                                       @Param("bizType") String bizType,
                                       @Param("start") LocalDateTime start,
                                       @Param("end") LocalDateTime end);

    @Select("""
            SELECT COUNT(1) FROM credit_transaction
            WHERE user_id = #{userId}
              AND biz_type = #{bizType}
              AND ref_type = #{refType}
              AND ref_id = #{refId}
            """)
    long countByBizRef(@Param("userId") Long userId,
                       @Param("bizType") String bizType,
                       @Param("refType") String refType,
                       @Param("refId") Long refId);
}
