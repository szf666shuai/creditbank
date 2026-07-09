package com.creditbank.platform.mapper;

import com.creditbank.platform.entity.CreditAccount;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.math.BigDecimal;

@Mapper
public interface CreditAccountExtMapper {

    @Select("SELECT * FROM credit_account WHERE user_id = #{userId} FOR UPDATE")
    CreditAccount selectForUpdate(@Param("userId") Long userId);

    @Update("""
            UPDATE credit_account
            SET balance = #{balance},
                total_earned = #{totalEarned},
                total_spent = #{totalSpent},
                update_time = NOW()
            WHERE user_id = #{userId}
            """)
    int updateBalance(@Param("userId") Long userId,
                      @Param("balance") BigDecimal balance,
                      @Param("totalEarned") BigDecimal totalEarned,
                      @Param("totalSpent") BigDecimal totalSpent);
}
