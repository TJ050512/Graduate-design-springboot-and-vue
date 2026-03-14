package com.waterworks.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.waterworks.annotation.RequireRole;
import com.waterworks.common.PageResult;
import com.waterworks.common.Result;
import com.waterworks.common.ResultCode;
import com.waterworks.entity.Payment;
import com.waterworks.exception.BusinessException;
import com.waterworks.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 缴费记录控制器
 */
@Tag(name = "缴费管理")
@RestController
@RequestMapping("/payment")
public class PaymentController {

    @Autowired
    private PaymentService paymentService;

    @Operation(summary = "分页查询缴费记录列表")
    @GetMapping("/page")
    @RequireRole(roles = {1, 2}, description = "管理员和普通用户可查询缴费记录")
    public Result<PageResult<Payment>> getPaymentPage(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String paymentNo,
            @RequestParam(required = false) Integer paymentMethod,
            @RequestParam(required = false) Integer status,
            HttpServletRequest request) {
        Integer currentUserType = getCurrentUserType(request);
        if (currentUserType == 2) {
            userId = getCurrentUserId(request);
        }
        Page<Payment> paymentPage = paymentService.getPaymentPage(page, size, userId, paymentNo, paymentMethod, status);
        PageResult<Payment> pageResult = PageResult.build(
                paymentPage.getCurrent(),
                paymentPage.getSize(),
                paymentPage.getTotal(),
                paymentPage.getRecords()
        );
        return Result.success(pageResult);
    }

    @Operation(summary = "根据ID查询缴费记录")
    @GetMapping("/{id}")
    @RequireRole(roles = {1, 2}, description = "管理员和普通用户可查询缴费记录详情")
    public Result<Payment> getPaymentById(@PathVariable Long id, HttpServletRequest request) {
        Payment payment = paymentService.getById(id);
        if (payment == null) {
            throw new BusinessException(ResultCode.DATA_NOT_EXIST);
        }
        ensureOwnerOrAdmin(payment.getUserId(), request);
        return Result.success(payment);
    }

    @Operation(summary = "创建缴费记录")
    @PostMapping
    @RequireRole(roles = {1, 2}, description = "管理员和普通用户可创建缴费记录")
    public Result<Boolean> createPayment(@Valid @RequestBody Payment payment, HttpServletRequest request) {
        Integer currentUserType = getCurrentUserType(request);
        if (currentUserType == 2) {
            payment.setUserId(getCurrentUserId(request));
        }
        boolean result = paymentService.createPayment(payment);
        return Result.success(result);
    }

    @Operation(summary = "支付")
    @PutMapping("/pay/{id}")
    @RequireRole(roles = {1, 2}, description = "管理员和普通用户可支付，普通用户仅可支付自己的账单")
    public Result<Boolean> pay(
            @PathVariable Long id,
            @RequestParam(required = false) Integer paymentMethod,
            HttpServletRequest request) {
        Payment payment = paymentService.getById(id);
        if (payment == null) {
            throw new BusinessException(ResultCode.DATA_NOT_EXIST);
        }
        ensureOwnerOrAdmin(payment.getUserId(), request);
        boolean result = paymentService.pay(id, paymentMethod);
        return Result.success(result);
    }

    @Operation(summary = "获取用户缴费统计")
    @GetMapping("/statistics")
    @RequireRole(roles = {1, 2}, description = "管理员和普通用户可查询缴费统计，普通用户仅可查询本人")
    public Result<Map<String, Object>> getUserStatistics(@RequestParam Long userId, HttpServletRequest request) {
        Integer currentUserType = getCurrentUserType(request);
        if (currentUserType == 2) {
            userId = getCurrentUserId(request);
        }
        Map<String, Object> stats = new HashMap<>();
        
        // 查询该用户所有缴费记录
        LambdaQueryWrapper<Payment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Payment::getUserId, userId);
        List<Payment> paymentList = paymentService.list(wrapper);
        
        // 计算已缴金额（状态为已支付）
        BigDecimal paidAmount = paymentList.stream()
                .filter(p -> p.getStatus() != null && p.getStatus() == 1)
                .map(Payment::getAmount)
                .filter(a -> a != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        // 计算待缴金额（状态为待支付）
        BigDecimal unpaidAmount = paymentList.stream()
                .filter(p -> p.getStatus() == null || p.getStatus() == 0)
                .map(Payment::getAmount)
                .filter(a -> a != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        stats.put("totalCount", paymentList.size());
        stats.put("paidAmount", paidAmount);
        stats.put("unpaidAmount", unpaidAmount);
        
        return Result.success(stats);
    }

    @Operation(summary = "发送催缴提醒")
    @PostMapping("/remind/{id}")
    @RequireRole(roles = {1}, description = "仅管理员可发送催缴提醒")
    public Result<Boolean> sendReminder(@PathVariable Long id) {
        boolean result = paymentService.sendReminder(id);
        return Result.success(result);
    }

    @Operation(summary = "批量发送催缴提醒")
    @PostMapping("/remind/batch")
    @RequireRole(roles = {1}, description = "仅管理员可批量发送催缴提醒")
    public Result<Boolean> batchSendReminder(@RequestBody List<Long> paymentIds) {
        boolean result = paymentService.batchSendReminder(paymentIds);
        return Result.success(result);
    }

    @Operation(summary = "获取当前用户的未缴费提醒")
    @GetMapping("/unpaid/reminders")
    @RequireRole(roles = {2}, description = "仅普通用户可查看自己的未缴费提醒")
    public Result<Map<String, Object>> getUnpaidReminders(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        
        // 查询该用户的待缴费记录（需要提醒的）
        LambdaQueryWrapper<Payment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Payment::getUserId, userId);
        wrapper.eq(Payment::getStatus, 0); // 待支付
        wrapper.eq(Payment::getNeedRemind, 1); // 需要提醒
        wrapper.orderByDesc(Payment::getCreateTime);
        List<Payment> unpaidList = paymentService.list(wrapper);
        
        // 计算总欠费金额
        BigDecimal totalUnpaid = unpaidList.stream()
                .map(Payment::getAmount)
                .filter(a -> a != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        Map<String, Object> result = new HashMap<>();
        result.put("count", unpaidList.size());
        result.put("totalAmount", totalUnpaid);
        result.put("list", unpaidList);
        result.put("hasReminder", !unpaidList.isEmpty());
        
        return Result.success(result);
    }

    private void ensureOwnerOrAdmin(Long ownerId, HttpServletRequest request) {
        Integer userType = getCurrentUserType(request);
        if (userType == 1) {
            return;
        }
        Long currentUserId = getCurrentUserId(request);
        if (!currentUserId.equals(ownerId)) {
            throw new BusinessException(ResultCode.NO_PERMISSION);
        }
    }

    private Long getCurrentUserId(HttpServletRequest request) {
        Object userIdObj = request.getAttribute("userId");
        if (userIdObj == null) {
            throw new BusinessException(ResultCode.USER_NOT_LOGIN);
        }
        return Long.valueOf(userIdObj.toString());
    }

    private Integer getCurrentUserType(HttpServletRequest request) {
        Object userTypeObj = request.getAttribute("userType");
        if (userTypeObj == null) {
            throw new BusinessException(ResultCode.USER_NOT_LOGIN);
        }
        return Integer.valueOf(userTypeObj.toString());
    }
}

