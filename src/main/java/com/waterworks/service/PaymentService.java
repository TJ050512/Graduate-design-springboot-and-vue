package com.waterworks.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.waterworks.entity.Payment;

/**
 * 缴费记录服务接口
 */
public interface PaymentService extends IService<Payment> {

    /**
     * 分页查询缴费记录列表
     *
     * @param page          页码
     * @param size          每页大小
     * @param userId        用户ID
     * @param paymentNo     缴费单号
     * @param paymentMethod 支付方式
     * @param status        状态
     * @return 缴费记录列表
     */
    Page<Payment> getPaymentPage(Integer page, Integer size, Long userId, String paymentNo, Integer paymentMethod, Integer status);

    /**
     * 创建缴费记录
     *
     * @param payment 缴费信息
     * @return 是否成功
     */
    boolean createPayment(Payment payment);

    /**
     * 支付
     *
     * @param paymentId 缴费ID
     * @return 是否成功
     */
    boolean pay(Long paymentId);
}


