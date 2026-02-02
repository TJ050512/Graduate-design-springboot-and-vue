package com.waterworks.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.waterworks.annotation.RequireRole;
import com.waterworks.common.Result;
import com.waterworks.entity.Payment;
import com.waterworks.entity.User;
import com.waterworks.entity.WaterMeter;
import com.waterworks.entity.WaterUsage;
import com.waterworks.service.PaymentService;
import com.waterworks.service.UserService;
import com.waterworks.service.WaterMeterService;
import com.waterworks.service.WaterUsageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * 数据统计控制器
 * 
 * @description 提供Dashboard数据可视化所需的统计接口
 */
@Slf4j
@Tag(name = "数据统计")
@RestController
@RequestMapping("/statistics")
public class StatisticsController {

    @Autowired
    private UserService userService;

    @Autowired
    private WaterMeterService waterMeterService;

    @Autowired
    private WaterUsageService waterUsageService;

    @Autowired
    private PaymentService paymentService;

    /**
     * 获取Dashboard概览统计数据
     */
    @Operation(summary = "获取概览统计数据")
    @GetMapping("/overview")
    public Result<Map<String, Object>> getOverview() {
        Map<String, Object> result = new HashMap<>();

        // 统计用户数量
        long userCount = userService.count();
        result.put("userCount", userCount);

        // 统计水表数量
        long meterCount = waterMeterService.count();
        result.put("meterCount", meterCount);

        // 统计本月用水记录数
        String currentMonth = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM"));
        LambdaQueryWrapper<WaterUsage> usageWrapper = new LambdaQueryWrapper<>();
        usageWrapper.eq(WaterUsage::getReadMonth, currentMonth);
        long monthUsageCount = waterUsageService.count(usageWrapper);
        result.put("monthUsageCount", monthUsageCount);

        // 统计待缴费金额
        LambdaQueryWrapper<Payment> paymentWrapper = new LambdaQueryWrapper<>();
        paymentWrapper.eq(Payment::getStatus, 0);  // 待支付
        List<Payment> pendingPayments = paymentService.list(paymentWrapper);
        BigDecimal pendingAmount = pendingPayments.stream()
                .map(Payment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        result.put("pendingAmount", pendingAmount);

        // 统计本月已缴费金额
        paymentWrapper = new LambdaQueryWrapper<>();
        paymentWrapper.eq(Payment::getStatus, 1);  // 已支付
        List<Payment> paidPayments = paymentService.list(paymentWrapper);
        BigDecimal paidAmount = paidPayments.stream()
                .map(Payment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        result.put("paidAmount", paidAmount);

        return Result.success(result);
    }

    /**
     * 获取近6个月用水量趋势数据
     */
    @Operation(summary = "获取用水量趋势数据")
    @GetMapping("/usage-trend")
    @RequireRole(roles = {1}, description = "仅管理员可查看用水趋势")
    public Result<Map<String, Object>> getUsageTrend() {
        Map<String, Object> result = new HashMap<>();
        
        List<String> months = new ArrayList<>();
        List<BigDecimal> usageData = new ArrayList<>();
        List<BigDecimal> amountData = new ArrayList<>();

        // 获取近6个月的数据
        LocalDate now = LocalDate.now();
        for (int i = 5; i >= 0; i--) {
            LocalDate date = now.minusMonths(i);
            String month = date.format(DateTimeFormatter.ofPattern("yyyy-MM"));
            months.add(date.format(DateTimeFormatter.ofPattern("M月")));

            // 查询该月的用水记录
            LambdaQueryWrapper<WaterUsage> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(WaterUsage::getReadMonth, month);
            List<WaterUsage> usageList = waterUsageService.list(wrapper);

            // 计算总用水量
            BigDecimal totalUsage = usageList.stream()
                    .map(WaterUsage::getUsage)
                    .reduce(BigDecimal.ZERO, BigDecimal::add)
                    .setScale(2, RoundingMode.HALF_UP);
            usageData.add(totalUsage);

            // 计算总金额
            BigDecimal totalAmount = usageList.stream()
                    .map(WaterUsage::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add)
                    .setScale(2, RoundingMode.HALF_UP);
            amountData.add(totalAmount);
        }

        result.put("months", months);
        result.put("usageData", usageData);
        result.put("amountData", amountData);

        return Result.success(result);
    }

    /**
     * 获取用户类型分布数据
     */
    @Operation(summary = "获取用户类型分布")
    @GetMapping("/user-distribution")
    @RequireRole(roles = {1}, description = "仅管理员可查看用户分布")
    public Result<List<Map<String, Object>>> getUserDistribution() {
        List<Map<String, Object>> result = new ArrayList<>();

        // 统计管理员
        LambdaQueryWrapper<User> adminWrapper = new LambdaQueryWrapper<>();
        adminWrapper.eq(User::getUserType, 1);
        long adminCount = userService.count(adminWrapper);
        result.add(createDistributionItem("管理员", adminCount));

        // 统计普通用户
        LambdaQueryWrapper<User> userWrapper = new LambdaQueryWrapper<>();
        userWrapper.eq(User::getUserType, 2);
        long normalUserCount = userService.count(userWrapper);
        result.add(createDistributionItem("普通用户", normalUserCount));

        // 统计抄表员
        LambdaQueryWrapper<User> readerWrapper = new LambdaQueryWrapper<>();
        readerWrapper.eq(User::getUserType, 3);
        long readerCount = userService.count(readerWrapper);
        result.add(createDistributionItem("抄表员", readerCount));

        return Result.success(result);
    }

    /**
     * 获取水表类型分布数据
     */
    @Operation(summary = "获取水表类型分布")
    @GetMapping("/meter-distribution")
    @RequireRole(roles = {1, 3}, description = "管理员和抄表员可查看水表分布")
    public Result<List<Map<String, Object>>> getMeterDistribution() {
        List<Map<String, Object>> result = new ArrayList<>();

        // 统计家用表
        LambdaQueryWrapper<WaterMeter> homeWrapper = new LambdaQueryWrapper<>();
        homeWrapper.eq(WaterMeter::getMeterType, 1);
        long homeCount = waterMeterService.count(homeWrapper);
        result.add(createDistributionItem("家用表", homeCount));

        // 统计商用表
        LambdaQueryWrapper<WaterMeter> commercialWrapper = new LambdaQueryWrapper<>();
        commercialWrapper.eq(WaterMeter::getMeterType, 2);
        long commercialCount = waterMeterService.count(commercialWrapper);
        result.add(createDistributionItem("商用表", commercialCount));

        // 统计工业表
        LambdaQueryWrapper<WaterMeter> industrialWrapper = new LambdaQueryWrapper<>();
        industrialWrapper.eq(WaterMeter::getMeterType, 3);
        long industrialCount = waterMeterService.count(industrialWrapper);
        result.add(createDistributionItem("工业表", industrialCount));

        return Result.success(result);
    }

    /**
     * 获取缴费状态统计
     */
    @Operation(summary = "获取缴费状态统计")
    @GetMapping("/payment-status")
    @RequireRole(roles = {1}, description = "仅管理员可查看缴费统计")
    public Result<List<Map<String, Object>>> getPaymentStatus() {
        List<Map<String, Object>> result = new ArrayList<>();

        // 待支付
        LambdaQueryWrapper<Payment> pendingWrapper = new LambdaQueryWrapper<>();
        pendingWrapper.eq(Payment::getStatus, 0);
        long pendingCount = paymentService.count(pendingWrapper);
        result.add(createDistributionItem("待支付", pendingCount));

        // 已支付
        LambdaQueryWrapper<Payment> paidWrapper = new LambdaQueryWrapper<>();
        paidWrapper.eq(Payment::getStatus, 1);
        long paidCount = paymentService.count(paidWrapper);
        result.add(createDistributionItem("已支付", paidCount));

        // 已退款
        LambdaQueryWrapper<Payment> refundWrapper = new LambdaQueryWrapper<>();
        refundWrapper.eq(Payment::getStatus, 2);
        long refundCount = paymentService.count(refundWrapper);
        result.add(createDistributionItem("已退款", refundCount));

        return Result.success(result);
    }

    /**
     * 创建分布数据项
     */
    private Map<String, Object> createDistributionItem(String name, long value) {
        Map<String, Object> item = new HashMap<>();
        item.put("name", name);
        item.put("value", value);
        return item;
    }
}
