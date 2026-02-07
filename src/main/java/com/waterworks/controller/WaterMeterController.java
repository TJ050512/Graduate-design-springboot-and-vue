package com.waterworks.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.waterworks.annotation.RequireRole;
import com.waterworks.common.PageResult;
import com.waterworks.common.Result;
import com.waterworks.entity.WaterMeter;
import com.waterworks.service.WaterMeterService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

/**
 * 水表控制器
 */
@Tag(name = "水表管理")
@RestController
@RequestMapping("/waterMeter")
public class WaterMeterController {

    @Autowired
    private WaterMeterService waterMeterService;

    @Operation(summary = "分页查询水表列表")
    @GetMapping("/page")
    @RequireRole(roles = {1, 3}, description = "管理员和抄表员可查询水表列表")
    public Result<PageResult<WaterMeter>> getMeterPage(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String meterNo,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Integer meterType,
            @RequestParam(required = false) Integer status) {
        Page<WaterMeter> meterPage = waterMeterService.getMeterPage(page, size, meterNo, userId, meterType, status);
        PageResult<WaterMeter> pageResult = PageResult.build(
                meterPage.getCurrent(),
                meterPage.getSize(),
                meterPage.getTotal(),
                meterPage.getRecords()
        );
        return Result.success(pageResult);
    }

    @Operation(summary = "根据ID查询水表")
    @GetMapping("/{id}")
    public Result<WaterMeter> getMeterById(@PathVariable Long id) {
        WaterMeter waterMeter = waterMeterService.getById(id);
        return Result.success(waterMeter);
    }

    @Operation(summary = "查询当前用户的水表列表")
    @GetMapping("/my")
    public Result<List<WaterMeter>> getMyMeters(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        List<WaterMeter> meters = waterMeterService.getMetersByUserId(userId);
        return Result.success(meters);
    }

    @Operation(summary = "添加水表")
    @PostMapping
    @RequireRole(roles = {1}, description = "仅管理员可添加水表")
    public Result<Boolean> addMeter(@Valid @RequestBody WaterMeter waterMeter) {
        boolean result = waterMeterService.addMeter(waterMeter);
        return Result.success(result);
    }

    @Operation(summary = "更新水表信息（不包含当前读数，读数由系统自动更新）")
    @PutMapping
    @RequireRole(roles = {1}, description = "仅管理员可更新水表基本信息")
    public Result<Boolean> updateMeter(@Valid @RequestBody WaterMeter waterMeter) {
        // 不允许通过此接口修改当前读数，当前读数由水表模拟器自动更新
        // 获取原水表数据，保留当前读数不变
        WaterMeter existingMeter = waterMeterService.getById(waterMeter.getMeterId());
        if (existingMeter != null) {
            waterMeter.setCurrentReading(existingMeter.getCurrentReading());
        }
        boolean result = waterMeterService.updateMeter(waterMeter);
        return Result.success(result);
    }

    @Operation(summary = "删除水表")
    @DeleteMapping("/{id}")
    @RequireRole(roles = {1}, description = "仅管理员可删除水表")
    public Result<Boolean> deleteMeter(@PathVariable Long id) {
        boolean result = waterMeterService.removeById(id);
        return Result.success(result);
    }
}


