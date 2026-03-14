package com.waterworks.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.waterworks.annotation.RequireRole;
import com.waterworks.common.PageResult;
import com.waterworks.common.Result;
import com.waterworks.common.ResultCode;
import com.waterworks.entity.RepairOrder;
import com.waterworks.exception.BusinessException;
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
    @RequireRole(roles = {1, 2, 4}, description = "管理员、普通用户、维修人员可查询工单，普通用户仅可查询本人")
    public Result<PageResult<RepairOrder>> getRepairOrderPage(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String orderNo,
            @RequestParam(required = false) Integer repairType,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Long handlerId,
            HttpServletRequest request) {
        Integer currentUserType = getCurrentUserType(request);
        if (currentUserType == 2) {
            userId = getCurrentUserId(request);
        }
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
    @RequireRole(roles = {1, 2, 4}, description = "管理员、普通用户、维修人员可查询工单详情，普通用户仅可查询本人")
    public Result<RepairOrder> getRepairOrderById(@PathVariable Long id, HttpServletRequest request) {
        RepairOrder repairOrder = repairOrderService.getById(id);
        if (repairOrder == null) {
            throw new BusinessException(ResultCode.DATA_NOT_EXIST);
        }
        Integer currentUserType = getCurrentUserType(request);
        if (currentUserType == 2 && !getCurrentUserId(request).equals(repairOrder.getUserId())) {
            throw new BusinessException(ResultCode.NO_PERMISSION);
        }
        return Result.success(repairOrder);
    }

    @Operation(summary = "创建报修工单")
    @PostMapping
    @RequireRole(roles = {2}, description = "仅普通用户可创建报修工单")
    public Result<Boolean> createRepairOrder(@Valid @RequestBody RepairOrder repairOrder, HttpServletRequest request) {
        repairOrder.setUserId(getCurrentUserId(request));
        boolean result = repairOrderService.createRepairOrder(repairOrder);
        return Result.success(result);
    }

    @Operation(summary = "更新报修工单")
    @PutMapping
    @RequireRole(roles = {1}, description = "仅管理员可更新报修工单")
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
    @RequireRole(roles = {1}, description = "仅管理员可取消工单")
    public Result<Boolean> cancelOrder(@PathVariable Long id) {
        boolean result = repairOrderService.cancelOrder(id);
        return Result.success(result);
    }

    @Operation(summary = "用户反馈评价")
    @PutMapping("/feedback/{id}")
    @RequireRole(roles = {2}, description = "仅普通用户可评价自己的工单")
    public Result<Boolean> feedbackOrder(@PathVariable Long id, @RequestBody Map<String, Object> params, HttpServletRequest request) {
        RepairOrder repairOrder = repairOrderService.getById(id);
        if (repairOrder == null) {
            throw new BusinessException(ResultCode.DATA_NOT_EXIST);
        }
        if (!getCurrentUserId(request).equals(repairOrder.getUserId())) {
            throw new BusinessException(ResultCode.NO_PERMISSION);
        }
        String feedback = (String) params.get("feedback");
        Integer rating = (Integer) params.get("rating");
        boolean result = repairOrderService.feedbackOrder(id, feedback, rating);
        return Result.success(result);
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
