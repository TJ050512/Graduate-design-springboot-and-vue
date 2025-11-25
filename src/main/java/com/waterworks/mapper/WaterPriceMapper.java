package com.waterworks.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.waterworks.entity.WaterPrice;
import org.apache.ibatis.annotations.Mapper;

/**
 * 水费价格Mapper接口
 */
@Mapper
public interface WaterPriceMapper extends BaseMapper<WaterPrice> {
}


