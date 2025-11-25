package com.waterworks.service.impl;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.waterworks.common.ResultCode;
import com.waterworks.entity.Payment;
import com.waterworks.entity.WaterUsage;
import com.waterworks.exception.BusinessException;
import com.waterworks.mapper.PaymentMapper;
import com.waterworks.service.PaymentService;
import com.waterworks.service.WaterUsageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 缴费记录服务实现类
 */
@Service
public class PaymentServiceImpl extends ServiceImpl<PaymentMapper, Payment> implements PaymentService {

    @Autowired
    private WaterUsageService waterUsageService;

    @Override
    public Page<Payment> getPaymentPage(Integer page, Integer size, Long userId, String paymentNo, Integer paymentMethod, Integer status) {
        Page<Payment> paymentPage = new Page<>(page, size);
        LambdaQueryWrapper<Payment> wrapper = new LambdaQueryWrapper<>();
        
        if (userId != null) {
            wrapper.eq(Payment::getUserId, userId);
        }
        if (StrUtil.isNotBlank(paymentNo)) {
            wrapper.like(Payment::getPaymentNo, paymentNo);
        }
        if (paymentMethod != null) {
            wrapper.eq(Payment::getPaymentMethod, paymentMethod);
        }
        if (status != null) {
            wrapper.eq(Payment::getStatus, status);
        }
        
        wrapper.orderByDesc(Payment::getCreateTime);
        return this.page(paymentPage, wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean createPayment(Payment payment) {
        // 如果提供了用水记录ID，从用水记录中获取信息
        if (payment.getUsageId() != null) {
            WaterUsage waterUsage = waterUsageService.getById(payment.getUsageId());
            if (waterUsage == null) {
                throw new BusinessException(ResultCode.DATA_NOT_EXIST.getCode(), "用水记录不存在");
            }
            
            // 检查该用水记录是否已经创建过缴费单
            LambdaQueryWrapper<Payment> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Payment::getUsageId, payment.getUsageId());
            wrapper.ne(Payment::getStatus, 2); // 排除已退款的
            long count = this.count(wrapper);
            if (count > 0) {
                throw new BusinessException("该用水记录已创建缴费单");
            }
            
            // 从用水记录中填充信息
            payment.setUserId(waterUsage.getUserId());
            payment.setMeterId(waterUsage.getMeterId());
            payment.setAmount(waterUsage.getAmount());
        }
        
        // 生成缴费单号
        payment.setPaymentNo("PAY" + IdUtil.getSnowflakeNextIdStr());
        payment.setStatus(0);
        
        return this.save(payment);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean pay(Long paymentId) {
        Payment payment = this.getById(paymentId);
        if (payment == null) {
            throw new BusinessException(ResultCode.DATA_NOT_EXIST);
        }

        if (payment.getStatus() == 1) {
            throw new BusinessException("该订单已支付");
        }

        // 更新支付状态
        payment.setStatus(1);
        payment.setPaymentTime(LocalDateTime.now());
        payment.setTransactionNo(IdUtil.getSnowflakeNextIdStr());
        boolean result = this.updateById(payment);

        // 更新用水记录状态
        if (result && payment.getUsageId() != null) {
            WaterUsage waterUsage = waterUsageService.getById(payment.getUsageId());
            if (waterUsage != null) {
                waterUsage.setStatus(2);
                waterUsageService.updateById(waterUsage);
            }
        }

        return result;
    }
}


