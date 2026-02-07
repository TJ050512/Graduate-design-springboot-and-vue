package com.waterworks.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.waterworks.annotation.RequireRole;
import com.waterworks.common.Result;
import com.waterworks.entity.MeterReadTask;
import com.waterworks.entity.WaterMeter;
import com.waterworks.entity.WaterUsage;
import com.waterworks.mapper.MeterReadTaskMapper;
import com.waterworks.service.WaterMeterService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 抄表任务控制器
 */
@Slf4j
@Tag(name = "抄表任务管理")
@RestController
@RequestMapping("/meterReadTask")
public class MeterReadTaskController {

    @Autowired
    private MeterReadTaskMapper meterReadTaskMapper;

    @Autowired
    private WaterMeterService waterMeterService;

    @Operation(summary = "获取需要抄表的水表列表（读数有变化的水表）")
    @GetMapping("/needRead")
    @RequireRole(roles = {1}, description = "仅管理员可查看需要抄表的水表")
    public Result<List<Map<String, Object>>> getNeedReadMeters() {
        // 查询所有正常状态的水表
        LambdaQueryWrapper<WaterMeter> meterWrapper = new LambdaQueryWrapper<>();
        meterWrapper.eq(WaterMeter::getStatus, 1);
        List<WaterMeter> meters = waterMeterService.list(meterWrapper);

        List<Map<String, Object>> result = new ArrayList<>();
        for (WaterMeter meter : meters) {
            // 查找该水表最后一条已完成的抄表任务或用水记录的读数
            LambdaQueryWrapper<MeterReadTask> taskWrapper = new LambdaQueryWrapper<>();
            taskWrapper.eq(MeterReadTask::getMeterId, meter.getMeterId());
            taskWrapper.orderByDesc(MeterReadTask::getCreateTime);
            taskWrapper.last("LIMIT 1");
            MeterReadTask lastTask = meterReadTaskMapper.selectOne(taskWrapper);

            // 上次记录的读数（如果没有抄表记录，就用初始读数）
            java.math.BigDecimal lastRecordedReading = lastTask != null 
                    ? lastTask.getCurrentReading() 
                    : meter.getInitialReading();

            // 当前读数与上次记录的读数差值
            java.math.BigDecimal diff = meter.getCurrentReading().subtract(lastRecordedReading);

            // 有变化的水表才需要抄表
            if (diff.compareTo(java.math.BigDecimal.ZERO) > 0) {
                Map<String, Object> item = new HashMap<>();
                item.put("meterId", meter.getMeterId());
                item.put("meterNo", meter.getMeterNo());
                item.put("meterType", meter.getMeterType());
                item.put("userId", meter.getUserId());
                item.put("installAddress", meter.getInstallAddress());
                item.put("lastRecordedReading", lastRecordedReading);
                item.put("currentReading", meter.getCurrentReading());
                item.put("diff", diff);
                // 检查是否已有待抄表任务
                LambdaQueryWrapper<MeterReadTask> pendingWrapper = new LambdaQueryWrapper<>();
                pendingWrapper.eq(MeterReadTask::getMeterId, meter.getMeterId());
                pendingWrapper.eq(MeterReadTask::getStatus, 0); // 待抄表
                Long pendingCount = meterReadTaskMapper.selectCount(pendingWrapper);
                item.put("hasPendingTask", pendingCount > 0);
                result.add(item);
            }
        }

        return Result.success(result);
    }

    @Operation(summary = "通知抄表员抄表（创建抄表任务）")
    @PostMapping("/notify")
    @RequireRole(roles = {1}, description = "仅管理员可发送抄表通知")
    public Result<String> notifyMeterRead(@RequestBody List<Long> meterIds) {
        if (meterIds == null || meterIds.isEmpty()) {
            return Result.error("请选择需要抄表的水表");
        }

        int count = 0;
        for (Long meterId : meterIds) {
            WaterMeter meter = waterMeterService.getById(meterId);
            if (meter == null || meter.getStatus() != 1) continue;

            // 检查是否已有未完成的任务
            LambdaQueryWrapper<MeterReadTask> pendingWrapper = new LambdaQueryWrapper<>();
            pendingWrapper.eq(MeterReadTask::getMeterId, meterId);
            pendingWrapper.eq(MeterReadTask::getStatus, 0);
            Long pendingCount = meterReadTaskMapper.selectCount(pendingWrapper);
            if (pendingCount > 0) continue;

            // 获取上次记录的读数
            LambdaQueryWrapper<MeterReadTask> lastWrapper = new LambdaQueryWrapper<>();
            lastWrapper.eq(MeterReadTask::getMeterId, meterId);
            lastWrapper.orderByDesc(MeterReadTask::getCreateTime);
            lastWrapper.last("LIMIT 1");
            MeterReadTask lastTask = meterReadTaskMapper.selectOne(lastWrapper);

            java.math.BigDecimal lastReading = lastTask != null 
                    ? lastTask.getCurrentReading() 
                    : meter.getInitialReading();

            // 创建抄表任务
            MeterReadTask task = new MeterReadTask();
            task.setMeterId(meterId);
            task.setUserId(meter.getUserId());
            task.setLastReading(lastReading);
            task.setCurrentReading(meter.getCurrentReading());
            task.setStatus(0);
            task.setNotifiedAt(LocalDateTime.now());
            meterReadTaskMapper.insert(task);
            count++;

            log.info("创建抄表任务 - 水表: {}, 上次读数: {}, 当前读数: {}", 
                    meter.getMeterNo(), lastReading, meter.getCurrentReading());
        }

        return Result.success("已成功创建 " + count + " 个抄表任务");
    }

    @Operation(summary = "批量通知所有需要抄表的水表")
    @PostMapping("/notifyAll")
    @RequireRole(roles = {1}, description = "仅管理员可批量发送抄表通知")
    public Result<String> notifyAllMeterRead() {
        // 获取所有需要抄表的水表
        Result<List<Map<String, Object>>> needReadResult = getNeedReadMeters();
        List<Map<String, Object>> needReadList = needReadResult.getData();
        
        if (needReadList == null || needReadList.isEmpty()) {
            return Result.success("当前没有需要抄表的水表");
        }

        List<Long> meterIds = needReadList.stream()
                .filter(item -> !(Boolean) item.get("hasPendingTask"))
                .map(item -> ((Number) item.get("meterId")).longValue())
                .collect(Collectors.toList());

        if (meterIds.isEmpty()) {
            return Result.success("所有水表都已有待处理的抄表任务");
        }

        return notifyMeterRead(meterIds);
    }

    @Operation(summary = "获取抄表员的待处理任务列表")
    @GetMapping("/pending")
    @RequireRole(roles = {3}, description = "抄表员可查看待处理任务")
    public Result<List<MeterReadTask>> getPendingTasks() {
        LambdaQueryWrapper<MeterReadTask> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MeterReadTask::getStatus, 0);
        wrapper.orderByDesc(MeterReadTask::getNotifiedAt);
        List<MeterReadTask> tasks = meterReadTaskMapper.selectList(wrapper);
        return Result.success(tasks);
    }

    @Operation(summary = "获取抄表员的待处理任务数量")
    @GetMapping("/pending/count")
    @RequireRole(roles = {3}, description = "抄表员可查看待处理数量")
    public Result<Long> getPendingTaskCount() {
        LambdaQueryWrapper<MeterReadTask> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MeterReadTask::getStatus, 0);
        Long count = meterReadTaskMapper.selectCount(wrapper);
        return Result.success(count);
    }

    @Operation(summary = "完成抄表任务（抄表员提交用水记录后调用）")
    @PutMapping("/complete/{taskId}")
    @RequireRole(roles = {3}, description = "抄表员可完成任务")
    public Result<Boolean> completeTask(@PathVariable Long taskId, @RequestParam(required = false) Long usageId) {
        MeterReadTask task = meterReadTaskMapper.selectById(taskId);
        if (task == null) {
            return Result.error("任务不存在");
        }
        task.setStatus(1);
        task.setCompletedAt(LocalDateTime.now());
        if (usageId != null) {
            task.setUsageId(usageId);
        }
        meterReadTaskMapper.updateById(task);
        return Result.success(true);
    }
}
