package com.waterworks.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.waterworks.entity.Payment;
import org.apache.ibatis.annotations.Mapper;

/**
 * 缴费记录Mapper接口
 */
@Mapper
public interface PaymentMapper extends BaseMapper<Payment> {
}


