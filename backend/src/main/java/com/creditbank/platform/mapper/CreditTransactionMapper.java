package com.creditbank.platform.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.creditbank.platform.entity.CreditTransaction;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CreditTransactionMapper extends BaseMapper<CreditTransaction> {
}
