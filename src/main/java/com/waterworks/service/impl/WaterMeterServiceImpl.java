package com.waterworks.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.waterworks.common.ResultCode;
import com.waterworks.entity.WaterMeter;
import com.waterworks.exception.BusinessException;
import com.waterworks.mapper.WaterMeterMapper;
import com.waterworks.service.WaterMeterService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 水表服务实现类
 */
@Service
public class WaterMeterServiceImpl extends ServiceImpl<WaterMeterMapper, WaterMeter> implements WaterMeterService {

    @Override
    public Page<WaterMeter> getMeterPage(Integer page, Integer size, String meterNo, Long userId, Integer meterType, Integer status) {
        Page<WaterMeter> meterPage = new Page<>(page, size);
        LambdaQueryWrapper<WaterMeter> wrapper = new LambdaQueryWrapper<>();
        
        if (StrUtil.isNotBlank(meterNo)) {
            wrapper.like(WaterMeter::getMeterNo, meterNo);
        }
        if (userId != null) {
            wrapper.eq(WaterMeter::getUserId, userId);
        }
        if (meterType != null) {
            wrapper.eq(WaterMeter::getMeterType, meterType);
        }
        if (status != null) {
            wrapper.eq(WaterMeter::getStatus, status);
        }
        
        wrapper.orderByDesc(WaterMeter::getCreateTime);
        return this.page(meterPage, wrapper);
    }

    @Override
    public WaterMeter getMeterByNo(String meterNo) {
        LambdaQueryWrapper<WaterMeter> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(WaterMeter::getMeterNo, meterNo);
        return this.getOne(wrapper);
    }

    @Override
    public boolean addMeter(WaterMeter waterMeter) {
        // 检查水表编号是否存在
        WaterMeter existMeter = getMeterByNo(waterMeter.getMeterNo());
        if (existMeter != null) {
            throw new BusinessException(ResultCode.DATA_ALREADY_EXISTED.getCode(), "水表编号已存在");
        }

        return this.save(waterMeter);
    }

    @Override
    public boolean updateMeter(WaterMeter waterMeter) {
        WaterMeter existMeter = this.getById(waterMeter.getMeterId());
        if (existMeter == null) {
            throw new BusinessException(ResultCode.DATA_NOT_EXIST);
        }

        // 如果修改了水表编号，检查是否重复
        if (!existMeter.getMeterNo().equals(waterMeter.getMeterNo())) {
            WaterMeter checkMeter = getMeterByNo(waterMeter.getMeterNo());
            if (checkMeter != null) {
                throw new BusinessException(ResultCode.DATA_ALREADY_EXISTED.getCode(), "水表编号已存在");
            }
        }

        return this.updateById(waterMeter);
    }

    @Override
    public List<WaterMeter> getMetersByUserId(Long userId) {
        LambdaQueryWrapper<WaterMeter> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(WaterMeter::getUserId, userId);
        wrapper.orderByDesc(WaterMeter::getCreateTime);
        return this.list(wrapper);
    }
}


