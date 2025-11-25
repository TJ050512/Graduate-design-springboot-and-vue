package com.waterworks.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.waterworks.common.ResultCode;
import com.waterworks.entity.WaterMeter;
import com.waterworks.entity.WaterUsage;
import com.waterworks.exception.BusinessException;
import com.waterworks.mapper.WaterUsageMapper;
import com.waterworks.service.WaterMeterService;
import com.waterworks.service.WaterUsageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

/**
 * 用水记录服务实现类
 */
@Service
public class WaterUsageServiceImpl extends ServiceImpl<WaterUsageMapper, WaterUsage> implements WaterUsageService {

    @Autowired
    private WaterMeterService waterMeterService;

    @Override
    public Page<WaterUsage> getUsagePage(Integer page, Integer size, Long userId, Long meterId, String readMonth, Integer status) {
        Page<WaterUsage> usagePage = new Page<>(page, size);
        LambdaQueryWrapper<WaterUsage> wrapper = new LambdaQueryWrapper<>();
        
        if (userId != null) {
            wrapper.eq(WaterUsage::getUserId, userId);
        }
        if (meterId != null) {
            wrapper.eq(WaterUsage::getMeterId, meterId);
        }
        if (StrUtil.isNotBlank(readMonth)) {
            wrapper.eq(WaterUsage::getReadMonth, readMonth);
        }
        if (status != null) {
            wrapper.eq(WaterUsage::getStatus, status);
        }
        
        wrapper.orderByDesc(WaterUsage::getReadDate);
        return this.page(usagePage, wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean addUsage(WaterUsage waterUsage) {
        // 计算用水量
        BigDecimal usage = waterUsage.getCurrentReading().subtract(waterUsage.getLastReading());
        if (usage.compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException("本次读数不能小于上次读数");
        }
        waterUsage.setUsage(usage);

        // 计算应缴金额
        BigDecimal amount = usage.multiply(waterUsage.getPrice());
        waterUsage.setAmount(amount);

        // 保存记录
        boolean result = this.save(waterUsage);

        // 更新水表当前读数
        if (result) {
            WaterMeter waterMeter = waterMeterService.getById(waterUsage.getMeterId());
            if (waterMeter != null) {
                waterMeter.setCurrentReading(waterUsage.getCurrentReading());
                waterMeterService.updateById(waterMeter);
            }
        }

        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateUsage(WaterUsage waterUsage) {
        WaterUsage existUsage = this.getById(waterUsage.getUsageId());
        if (existUsage == null) {
            throw new BusinessException(ResultCode.DATA_NOT_EXIST);
        }

        // 计算用水量
        BigDecimal usage = waterUsage.getCurrentReading().subtract(waterUsage.getLastReading());
        if (usage.compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException("本次读数不能小于上次读数");
        }
        waterUsage.setUsage(usage);

        // 计算应缴金额
        BigDecimal amount = usage.multiply(waterUsage.getPrice());
        waterUsage.setAmount(amount);

        // 更新记录
        boolean result = this.updateById(waterUsage);

        // 更新水表当前读数
        if (result) {
            WaterMeter waterMeter = waterMeterService.getById(waterUsage.getMeterId());
            if (waterMeter != null) {
                waterMeter.setCurrentReading(waterUsage.getCurrentReading());
                waterMeterService.updateById(waterMeter);
            }
        }

        return result;
    }

    @Override
    public boolean confirmUsage(Long usageId) {
        WaterUsage waterUsage = this.getById(usageId);
        if (waterUsage == null) {
            throw new BusinessException(ResultCode.DATA_NOT_EXIST);
        }

        waterUsage.setStatus(1);
        return this.updateById(waterUsage);
    }
}


