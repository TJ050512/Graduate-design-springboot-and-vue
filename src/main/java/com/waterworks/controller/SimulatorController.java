package com.waterworks.controller;

import com.waterworks.annotation.RequireRole;
import com.waterworks.common.Result;
import com.waterworks.simulator.WaterMeterSimulator;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 水表模拟器控制器
 * 提供模拟器状态查询和控制接口
 */
@Tag(name = "水表模拟器")
@RestController
@RequestMapping("/simulator")
public class SimulatorController {

    @Autowired
    private WaterMeterSimulator waterMeterSimulator;

    @Operation(summary = "获取模拟器状态")
    @GetMapping("/status")
    @RequireRole(roles = {1}, description = "仅管理员可查看模拟器状态")
    public Result<Map<String, Object>> getStatus() {
        Map<String, Object> status = new HashMap<>();
        status.put("enabled", waterMeterSimulator.isEnabled());
        status.put("speedMultiplier", waterMeterSimulator.getSpeedMultiplier());
        status.put("description", waterMeterSimulator.getSimulatorStatus());
        return Result.success(status);
    }

    @Operation(summary = "启用/禁用模拟器")
    @PutMapping("/toggle")
    @RequireRole(roles = {1}, description = "仅管理员可控制模拟器")
    public Result<String> toggleSimulator(@RequestParam boolean enabled) {
        waterMeterSimulator.setEnabled(enabled);
        return Result.success(enabled ? "模拟器已启用" : "模拟器已禁用");
    }

    @Operation(summary = "设置模拟速度倍率")
    @PutMapping("/speed")
    @RequireRole(roles = {1}, description = "仅管理员可设置速度倍率")
    public Result<String> setSpeedMultiplier(@RequestParam double multiplier) {
        if (multiplier <= 0 || multiplier > 100) {
            return Result.error("速度倍率必须在 0-100 之间");
        }
        waterMeterSimulator.setSpeedMultiplier(multiplier);
        return Result.success("速度倍率已设置为 " + multiplier + "x");
    }

    @Operation(summary = "立即执行一次模拟（手动触发）")
    @PostMapping("/trigger")
    @RequireRole(roles = {1}, description = "仅管理员可手动触发模拟")
    public Result<String> triggerSimulation() {
        waterMeterSimulator.simulateWaterUsage();
        return Result.success("模拟已执行，请刷新查看最新读数");
    }
}
