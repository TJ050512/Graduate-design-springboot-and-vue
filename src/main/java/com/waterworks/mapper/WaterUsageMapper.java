package com.waterworks.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.waterworks.entity.WaterUsage;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用水记录Mapper接口
 */
@Mapper
public interface WaterUsageMapper extends BaseMapper<WaterUsage> {
}


