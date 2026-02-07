package com.waterworks.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.waterworks.common.ResultCode;
import com.waterworks.entity.Payment;
import com.waterworks.entity.WaterMeter;
import com.waterworks.entity.WaterUsage;
import com.waterworks.exception.BusinessException;
import com.waterworks.mapper.PaymentMapper;
import com.waterworks.mapper.WaterUsageMapper;
import com.waterworks.service.WaterMeterService;
import com.waterworks.service.WaterPriceService;
import com.waterworks.service.WaterUsageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

/**
 * 用水记录服务实现类
 * 
 * @description 管理用水记录，支持阶梯水价计算
 */
@Slf4j
@Service
public class WaterUsageServiceImpl extends ServiceImpl<WaterUsageMapper, WaterUsage> implements WaterUsageService {

    @Autowired
    private WaterMeterService waterMeterService;

    @Autowired
    private WaterPriceService waterPriceService;

    @Autowired
    private PaymentMapper paymentMapper;

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
        // 获取水表信息
        WaterMeter waterMeter = waterMeterService.getById(waterUsage.getMeterId());
        if (waterMeter == null) {
            throw new BusinessException("水表不存在");
        }

        // 【改进】如果没有提供当前读数，自动使用水表的当前读数（由模拟器自动更新）
        if (waterUsage.getCurrentReading() == null) {
            waterUsage.setCurrentReading(waterMeter.getCurrentReading());
        }

        // 计算用水量
        BigDecimal usage = waterUsage.getCurrentReading().subtract(waterUsage.getLastReading());
        if (usage.compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException("本次读数不能小于上次读数");
        }
        waterUsage.setUsage(usage);

        // 【核心改进】使用阶梯水价计算应缴金额
        BigDecimal amount = waterPriceService.calculateWaterFee(waterMeter.getMeterType(), usage);
        waterUsage.setAmount(amount);
        
        // 设置基础单价（用于显示，取第一档价格）
        BigDecimal basePrice = waterPriceService.getBasePriceByMeterType(waterMeter.getMeterType());
        waterUsage.setPrice(basePrice);

        log.info("用水记录计费 - 水表: {}, 类型: {}, 用水量: {}m³, 应缴金额: {}元", 
                waterMeter.getMeterNo(), getMeterTypeName(waterMeter.getMeterType()), usage, amount);

        // 保存记录（不再更新水表读数，读数由模拟器自动管理）
        return this.save(waterUsage);
    }

    /**
     * 获取水表类型名称
     */
    private String getMeterTypeName(Integer meterType) {
        switch (meterType) {
            case 1: return "家用表";
            case 2: return "商用表";
            case 3: return "工业表";
            default: return "未知";
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateUsage(WaterUsage waterUsage) {
        WaterUsage existUsage = this.getById(waterUsage.getUsageId());
        if (existUsage == null) {
            throw new BusinessException(ResultCode.DATA_NOT_EXIST);
        }

        // 获取水表信息
        WaterMeter waterMeter = waterMeterService.getById(waterUsage.getMeterId());
        if (waterMeter == null) {
            throw new BusinessException("水表不存在");
        }

        // 计算用水量
        BigDecimal usage = waterUsage.getCurrentReading().subtract(waterUsage.getLastReading());
        if (usage.compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException("本次读数不能小于上次读数");
        }
        waterUsage.setUsage(usage);

        // 【核心改进】使用阶梯水价计算应缴金额
        BigDecimal amount = waterPriceService.calculateWaterFee(waterMeter.getMeterType(), usage);
        waterUsage.setAmount(amount);
        
        // 设置基础单价
        BigDecimal basePrice = waterPriceService.getBasePriceByMeterType(waterMeter.getMeterType());
        waterUsage.setPrice(basePrice);

        log.info("用水记录更新计费 - 水表: {}, 类型: {}, 用水量: {}m³, 应缴金额: {}元", 
                waterMeter.getMeterNo(), getMeterTypeName(waterMeter.getMeterType()), usage, amount);

        // 更新记录（不再更新水表读数，读数由模拟器自动管理）
        boolean result = this.updateById(waterUsage);

        // 同步更新关联的待支付缴费记录金额
        if (result) {
            LambdaQueryWrapper<Payment> paymentWrapper = new LambdaQueryWrapper<>();
            paymentWrapper.eq(Payment::getUsageId, waterUsage.getUsageId());
            paymentWrapper.eq(Payment::getStatus, 0); // 只更新待支付的记录
            Payment payment = paymentMapper.selectOne(paymentWrapper);
            if (payment != null) {
                payment.setAmount(amount);
                paymentMapper.updateById(payment);
                log.info("同步更新缴费记录金额 - 缴费单号: {}, 新金额: {}元", payment.getPaymentNo(), amount);
            }
        }

        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean confirmUsage(Long usageId) {
        WaterUsage waterUsage = this.getById(usageId);
        if (waterUsage == null) {
            throw new BusinessException(ResultCode.DATA_NOT_EXIST);
        }

        // 更新用水记录状态为已确认
        waterUsage.setStatus(1);
        boolean result = this.updateById(waterUsage);

        // 自动创建缴费单
        if (result) {
            // 检查是否已存在缴费单
            LambdaQueryWrapper<Payment> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Payment::getUsageId, usageId);
            wrapper.ne(Payment::getStatus, 2); // 排除已退款的
            Payment existPayment = paymentMapper.selectOne(wrapper);
            
            if (existPayment == null) {
                // 创建新的缴费单
                Payment payment = new Payment();
                payment.setUsageId(usageId);
                payment.setUserId(waterUsage.getUserId());
                payment.setMeterId(waterUsage.getMeterId());
                payment.setAmount(waterUsage.getAmount());
                payment.setPaymentNo("PAY" + cn.hutool.core.util.IdUtil.getSnowflakeNextIdStr());
                payment.setPaymentMethod(2); // 默认微信支付
                payment.setStatus(0); // 待支付
                paymentMapper.insert(payment);
                log.info("自动创建缴费单 - 用水记录ID: {}, 缴费单号: {}, 金额: {}元", 
                        usageId, payment.getPaymentNo(), payment.getAmount());
            }
        }

        return result;
    }
}


