package com.creditbank.platform.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.creditbank.platform.entity.CreditAccount;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CreditAccountMapper extends BaseMapper<CreditAccount> {
}
