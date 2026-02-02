package com.waterworks.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.waterworks.annotation.RequireRole;
import com.waterworks.common.PageResult;
import com.waterworks.common.Result;
import com.waterworks.entity.Payment;
import com.waterworks.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

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
            @RequestParam(required = false) Integer status) {
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
    public Result<Payment> getPaymentById(@PathVariable Long id) {
        Payment payment = paymentService.getById(id);
        return Result.success(payment);
    }

    @Operation(summary = "创建缴费记录")
    @PostMapping
    @RequireRole(roles = {1}, description = "仅管理员可创建缴费记录")
    public Result<Boolean> createPayment(@Valid @RequestBody Payment payment) {
        boolean result = paymentService.createPayment(payment);
        return Result.success(result);
    }

    @Operation(summary = "支付")
    @PutMapping("/pay/{id}")
    public Result<Boolean> pay(@PathVariable Long id) {
        boolean result = paymentService.pay(id);
        return Result.success(result);
    }
}


