package com.waterworks.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.waterworks.annotation.RequireRole;
import com.waterworks.common.PageResult;
import com.waterworks.common.Result;
import com.waterworks.entity.RepairOrder;
import com.waterworks.service.RepairOrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.Map;

/**
 * 报修工单控制器
 */
@Tag(name = "报修工单管理")
@RestController
@RequestMapping("/repair")
public class RepairOrderController {

    @Autowired
    private RepairOrderService repairOrderService;

    @Operation(summary = "分页查询报修工单列表")
    @GetMapping("/page")
    public Result<PageResult<RepairOrder>> getRepairOrderPage(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String orderNo,
            @RequestParam(required = false) Integer repairType,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Long handlerId) {
        Page<RepairOrder> repairOrderPage = repairOrderService.getRepairOrderPage(page, size, userId, orderNo, repairType, status, handlerId);
        PageResult<RepairOrder> pageResult = PageResult.build(
                repairOrderPage.getCurrent(),
                repairOrderPage.getSize(),
                repairOrderPage.getTotal(),
                repairOrderPage.getRecords()
        );
        return Result.success(pageResult);
    }

    @Operation(summary = "根据ID查询报修工单详情")
    @GetMapping("/{id}")
    public Result<RepairOrder> getRepairOrderById(@PathVariable Long id) {
        RepairOrder repairOrder = repairOrderService.getById(id);
        return Result.success(repairOrder);
    }

    @Operation(summary = "创建报修工单")
    @PostMapping
    public Result<Boolean> createRepairOrder(@Valid @RequestBody RepairOrder repairOrder, HttpServletRequest request) {
        // 如果没有指定用户ID，从当前登录用户获取
        if (repairOrder.getUserId() == null) {
            Long userId = (Long) request.getAttribute("userId");
            repairOrder.setUserId(userId);
        }
        boolean result = repairOrderService.createRepairOrder(repairOrder);
        return Result.success(result);
    }

    @Operation(summary = "更新报修工单")
    @PutMapping
    public Result<Boolean> updateRepairOrder(@Valid @RequestBody RepairOrder repairOrder) {
        boolean result = repairOrderService.updateRepairOrder(repairOrder);
        return Result.success(result);
    }

    @Operation(summary = "删除报修工单")
    @DeleteMapping("/{id}")
    @RequireRole(roles = {1}, description = "仅管理员可删除报修工单")
    public Result<Boolean> deleteRepairOrder(@PathVariable Long id) {
        boolean result = repairOrderService.removeById(id);
        return Result.success(result);
    }

    @Operation(summary = "开始处理工单（接单）")
    @PutMapping("/handle/{id}")
    @RequireRole(roles = {4}, description = "仅维修人员可接单处理")
    public Result<Boolean> handleOrder(@PathVariable Long id, HttpServletRequest request) {
        Long handlerId = (Long) request.getAttribute("userId");
        boolean result = repairOrderService.handleOrder(id, handlerId);
        return Result.success(result);
    }

    @Operation(summary = "完成工单")
    @PutMapping("/complete/{id}")
    @RequireRole(roles = {4}, description = "仅维修人员可完成工单")
    public Result<Boolean> completeOrder(@PathVariable Long id, @RequestBody Map<String, String> params) {
        String handleResult = params.get("handleResult");
        boolean result = repairOrderService.completeOrder(id, handleResult);
        return Result.success(result);
    }

    @Operation(summary = "处理失败，转派工单")
    @PutMapping("/fail/{id}")
    @RequireRole(roles = {4}, description = "仅维修人员可标记工单处理失败")
    public Result<Boolean> failOrder(@PathVariable Long id, @RequestBody Map<String, String> params) {
        String failReason = params.get("failReason");
        boolean result = repairOrderService.failOrder(id, failReason);
        return Result.success(result);
    }

    @Operation(summary = "取消工单")
    @PutMapping("/cancel/{id}")
    public Result<Boolean> cancelOrder(@PathVariable Long id) {
        boolean result = repairOrderService.cancelOrder(id);
        return Result.success(result);
    }

    @Operation(summary = "用户反馈评价")
    @PutMapping("/feedback/{id}")
    public Result<Boolean> feedbackOrder(@PathVariable Long id, @RequestBody Map<String, Object> params) {
        String feedback = (String) params.get("feedback");
        Integer rating = (Integer) params.get("rating");
        boolean result = repairOrderService.feedbackOrder(id, feedback, rating);
        return Result.success(result);
    }
}
