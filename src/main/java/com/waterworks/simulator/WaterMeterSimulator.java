package com.waterworks.simulator;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.waterworks.entity.WaterMeter;
import com.waterworks.mapper.WaterMeterMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalTime;
import java.util.List;
import java.util.Random;

/**
 * 水表读数模拟器
 * 根据水表类型自动模拟用水，更贴近真实场景
 * 
 * 用水参数说明（基于真实数据）：
 * - 家用表(1): 每分钟约 0.0003-0.0008 m³，每天约 0.4-1.2 m³（一个3-4人家庭）
 * - 商用表(2): 每分钟约 0.001-0.003 m³，每天约 1.5-4.5 m³（餐厅、商店等）
 * - 工业表(3): 每分钟约 0.005-0.015 m³，每天约 7-20 m³（小型工厂）
 */
@Slf4j
@Component
public class WaterMeterSimulator {

    @Autowired
    private WaterMeterMapper waterMeterMapper;

    /**
     * 是否启用模拟器（可在配置文件中设置）
     */
    @Value("${waterworks.simulator.enabled:true}")
    private boolean enabled;

    /**
     * 模拟速度倍率（用于演示，可加快用水速度）
     * 默认为1，表示真实速度；设为10则表示10倍速
     */
    @Value("${waterworks.simulator.speed-multiplier:1}")
    private double speedMultiplier;

    private final Random random = new Random();

    /**
     * 家用表基础用水速率（立方米/10秒）
     * 原每分钟0.0005，现在每10秒 = 0.0005/6 ≈ 0.000083
     */
    private static final double RESIDENTIAL_BASE_RATE = 0.000083;
    
    /**
     * 商用表基础用水速率（立方米/10秒）
     * 原每分钟0.002，现在每10秒 = 0.002/6 ≈ 0.00033
     */
    private static final double COMMERCIAL_BASE_RATE = 0.00033;
    
    /**
     * 工业表基础用水速率（立方米/10秒）
     * 原每分钟0.01，现在每10秒 = 0.01/6 ≈ 0.00167
     */
    private static final double INDUSTRIAL_BASE_RATE = 0.00167;

    /**
     * 每10秒执行一次，模拟所有正常状态水表的用水
     * 使用 fixedRate 确保稳定的执行间隔
     * 注：用水量按比例缩小（原来1分钟的量分成6次）
     */
    @Scheduled(fixedRate = 10000) // 每10秒执行一次
    public void simulateWaterUsage() {
        if (!enabled) {
            return;
        }

        try {
            // 查询所有正常状态的水表
            LambdaQueryWrapper<WaterMeter> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(WaterMeter::getStatus, 1); // 状态1为正常
            List<WaterMeter> activeMeters = waterMeterMapper.selectList(queryWrapper);

            if (activeMeters.isEmpty()) {
                return;
            }

            // 获取当前时间因子（模拟不同时间段用水量不同）
            double timeFactor = getTimeFactor();

            int updatedCount = 0;
            for (WaterMeter meter : activeMeters) {
                BigDecimal increment = calculateWaterIncrement(meter.getMeterType(), timeFactor);
                if (increment.compareTo(BigDecimal.ZERO) > 0) {
                    BigDecimal newReading = meter.getCurrentReading().add(increment);
                    // 保留4位小数
                    newReading = newReading.setScale(4, RoundingMode.HALF_UP);
                    
                    meter.setCurrentReading(newReading);
                    waterMeterMapper.updateById(meter);
                    updatedCount++;
                }
            }

            if (updatedCount > 0) {
                log.debug("水表模拟器：已更新 {} 个水表的读数，时间因子: {}", updatedCount, timeFactor);
            }
        } catch (Exception e) {
            log.error("水表模拟器执行异常", e);
        }
    }

    /**
     * 根据水表类型和时间因子计算用水增量
     * 
     * @param meterType 水表类型（1:家用 2:商用 3:工业）
     * @param timeFactor 时间因子（模拟不同时间段用水差异）
     * @return 用水增量（立方米）
     */
    private BigDecimal calculateWaterIncrement(Integer meterType, double timeFactor) {
        if (meterType == null) {
            meterType = 1; // 默认家用
        }

        double baseRate;
        double variationRange; // 随机波动范围

        switch (meterType) {
            case 1: // 家用表
                baseRate = RESIDENTIAL_BASE_RATE;
                variationRange = 0.6; // ±60%波动，模拟家庭用水不规律
                break;
            case 2: // 商用表
                baseRate = COMMERCIAL_BASE_RATE;
                variationRange = 0.5; // ±50%波动
                break;
            case 3: // 工业表
                baseRate = INDUSTRIAL_BASE_RATE;
                variationRange = 0.3; // ±30%波动，工业用水相对稳定
                break;
            default:
                baseRate = RESIDENTIAL_BASE_RATE;
                variationRange = 0.6;
        }

        // 计算随机波动因子（0.4 到 1.6 之间，以variationRange=0.6为例）
        double randomFactor = 1 + (random.nextDouble() * 2 - 1) * variationRange;
        
        // 最终用水量 = 基础速率 * 时间因子 * 随机因子 * 速度倍率
        double finalRate = baseRate * timeFactor * randomFactor * speedMultiplier;
        
        // 有小概率（5%）完全不用水（模拟用户外出等情况）
        if (meterType == 1 && random.nextDouble() < 0.05) {
            finalRate = 0;
        }

        return BigDecimal.valueOf(finalRate).setScale(6, RoundingMode.HALF_UP);
    }

    /**
     * 获取时间因子，模拟不同时间段的用水规律
     * 
     * 家庭用水规律：
     * - 早高峰(6:00-9:00): 用水量较大，洗漱、做早餐
     * - 中午(11:00-13:00): 做饭用水
     * - 晚高峰(17:00-21:00): 用水量最大，做饭、洗澡、洗衣
     * - 深夜(23:00-6:00): 用水量很少
     * 
     * @return 时间因子（0.1 到 1.5 之间）
     */
    private double getTimeFactor() {
        LocalTime now = LocalTime.now();
        int hour = now.getHour();

        if (hour >= 6 && hour < 9) {
            // 早高峰
            return 1.2;
        } else if (hour >= 9 && hour < 11) {
            // 上午
            return 0.6;
        } else if (hour >= 11 && hour < 13) {
            // 午餐时间
            return 1.0;
        } else if (hour >= 13 && hour < 17) {
            // 下午
            return 0.5;
        } else if (hour >= 17 && hour < 21) {
            // 晚高峰
            return 1.5;
        } else if (hour >= 21 && hour < 23) {
            // 晚间
            return 0.8;
        } else {
            // 深夜（23:00-6:00）
            return 0.1;
        }
    }

    /**
     * 获取模拟器状态信息（供API调用）
     */
    public String getSimulatorStatus() {
        StringBuilder sb = new StringBuilder();
        sb.append("水表模拟器状态：").append(enabled ? "已启用" : "已禁用").append("\n");
        sb.append("速度倍率：").append(speedMultiplier).append("x\n");
        sb.append("当前时间因子：").append(String.format("%.2f", getTimeFactor())).append("\n");
        sb.append("\n基础用水速率（立方米/分钟）：\n");
        sb.append("- 家用表：").append(RESIDENTIAL_BASE_RATE).append("\n");
        sb.append("- 商用表：").append(COMMERCIAL_BASE_RATE).append("\n");
        sb.append("- 工业表：").append(INDUSTRIAL_BASE_RATE).append("\n");
        return sb.toString();
    }

    /**
     * 手动设置模拟器启用状态
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        log.info("水表模拟器状态已更改为：{}", enabled ? "启用" : "禁用");
    }

    /**
     * 手动设置速度倍率
     */
    public void setSpeedMultiplier(double multiplier) {
        if (multiplier > 0 && multiplier <= 100) {
            this.speedMultiplier = multiplier;
            log.info("水表模拟器速度倍率已设置为：{}x", multiplier);
        }
    }

    public boolean isEnabled() {
        return enabled;
    }

    public double getSpeedMultiplier() {
        return speedMultiplier;
    }
}
