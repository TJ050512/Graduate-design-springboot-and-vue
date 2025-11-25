package com.waterworks.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.waterworks.common.PageResult;
import com.waterworks.common.Result;
import com.waterworks.entity.WaterUsage;
import com.waterworks.service.WaterUsageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

/**
 * 用水记录控制器
 */
@Tag(name = "用水记录管理")
@RestController
@RequestMapping("/waterUsage")
public class WaterUsageController {

    @Autowired
    private WaterUsageService waterUsageService;

    @Operation(summary = "分页查询用水记录列表")
    @GetMapping("/page")
    public Result<PageResult<WaterUsage>> getUsagePage(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Long meterId,
            @RequestParam(required = false) String readMonth,
            @RequestParam(required = false) Integer status) {
        Page<WaterUsage> usagePage = waterUsageService.getUsagePage(page, size, userId, meterId, readMonth, status);
        PageResult<WaterUsage> pageResult = PageResult.build(
                usagePage.getCurrent(),
                usagePage.getSize(),
                usagePage.getTotal(),
                usagePage.getRecords()
        );
        return Result.success(pageResult);
    }

    @Operation(summary = "根据ID查询用水记录")
    @GetMapping("/{id}")
    public Result<WaterUsage> getUsageById(@PathVariable Long id) {
        WaterUsage waterUsage = waterUsageService.getById(id);
        return Result.success(waterUsage);
    }

    @Operation(summary = "添加用水记录")
    @PostMapping
    public Result<Boolean> addUsage(@Valid @RequestBody WaterUsage waterUsage) {
        boolean result = waterUsageService.addUsage(waterUsage);
        return Result.success(result);
    }

    @Operation(summary = "更新用水记录")
    @PutMapping
    public Result<Boolean> updateUsage(@Valid @RequestBody WaterUsage waterUsage) {
        boolean result = waterUsageService.updateUsage(waterUsage);
        return Result.success(result);
    }

    @Operation(summary = "确认用水记录")
    @PutMapping("/confirm/{id}")
    public Result<Boolean> confirmUsage(@PathVariable Long id) {
        boolean result = waterUsageService.confirmUsage(id);
        return Result.success(result);
    }

    @Operation(summary = "删除用水记录")
    @DeleteMapping("/{id}")
    public Result<Boolean> deleteUsage(@PathVariable Long id) {
        boolean result = waterUsageService.removeById(id);
        return Result.success(result);
    }
}


