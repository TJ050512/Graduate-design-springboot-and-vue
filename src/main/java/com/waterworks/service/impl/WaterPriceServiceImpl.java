package com.waterworks.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.waterworks.entity.WaterMeter;
import com.waterworks.entity.WaterPrice;
import com.waterworks.exception.BusinessException;
import com.waterworks.mapper.WaterPriceMapper;
import com.waterworks.service.WaterMeterService;
import com.waterworks.service.WaterPriceService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

/**
 * 水费价格服务实现类
 * 
 * @description 实现阶梯水价计算，支持居民用水阶梯计价和商业/工业用水固定计价
 * 
 * 阶梯计价规则（以居民用水为例）：
 * - 第一档：0-15立方米，单价2.80元/立方米
 * - 第二档：15-25立方米，单价4.00元/立方米
 * - 第三档：25立方米以上，单价6.00元/立方米
 * 
 * 例如：用水30立方米的计算方式
 * = 15 × 2.80 + 10 × 4.00 + 5 × 6.00
 * = 42 + 40 + 30 = 112元
 */
@Slf4j
@Service
public class WaterPriceServiceImpl extends ServiceImpl<WaterPriceMapper, WaterPrice> implements WaterPriceService {

    @Autowired
    @Lazy  // 使用懒加载避免循环依赖
    private WaterMeterService waterMeterService;

    /**
     * 根据水表类型和用水量计算水费（核心方法）
     */
    @Override
    public BigDecimal calculateWaterFee(Integer meterType, BigDecimal usage) {
        if (usage == null || usage.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }

        // 获取该水表类型的有效价格配置
        List<WaterPrice> priceList = getActivePricesByMeterType(meterType);
        
        if (priceList.isEmpty()) {
            log.warn("未找到水表类型{}的价格配置，使用默认价格2.80元/立方米", meterType);
            return usage.multiply(new BigDecimal("2.80")).setScale(2, RoundingMode.HALF_UP);
        }

        // 判断是否为阶梯计价（家用表tier > 0表示阶梯计价）
        boolean isTieredPricing = priceList.stream().anyMatch(p -> p.getTier() != null && p.getTier() > 0);

        if (isTieredPricing) {
            // 阶梯计价（主要用于居民用水）
            return calculateTieredFee(priceList, usage);
        } else {
            // 固定单价（商业用水、工业用水）
            BigDecimal unitPrice = priceList.get(0).getPrice();
            return usage.multiply(unitPrice).setScale(2, RoundingMode.HALF_UP);
        }
    }

    /**
     * 阶梯计价计算
     * 
     * @param priceList 价格配置列表（已按阶梯排序）
     * @param usage     用水量
     * @return 总费用
     */
    private BigDecimal calculateTieredFee(List<WaterPrice> priceList, BigDecimal usage) {
        BigDecimal totalFee = BigDecimal.ZERO;
        BigDecimal remainingUsage = usage;

        log.info("开始阶梯计费，总用水量: {} 立方米", usage);

        for (WaterPrice tier : priceList) {
            if (remainingUsage.compareTo(BigDecimal.ZERO) <= 0) {
                break;
            }

            BigDecimal startUsage = tier.getStartUsage();
            BigDecimal endUsage = tier.getEndUsage();
            BigDecimal unitPrice = tier.getPrice();

            // 计算该档位的用水量区间
            BigDecimal tierCapacity;
            if (endUsage == null) {
                // 最高档位，无上限
                tierCapacity = remainingUsage;
            } else {
                tierCapacity = endUsage.subtract(startUsage);
            }

            // 计算该档位实际计费的用水量
            BigDecimal usageInTier = remainingUsage.min(tierCapacity);
            
            // 计算该档位费用
            BigDecimal tierFee = usageInTier.multiply(unitPrice);
            totalFee = totalFee.add(tierFee);

            log.info("第{}档: {} 立方米 × {} 元/立方米 = {} 元", 
                    tier.getTier(), usageInTier, unitPrice, tierFee.setScale(2, RoundingMode.HALF_UP));

            // 扣除已计算的用水量
            remainingUsage = remainingUsage.subtract(usageInTier);
        }

        BigDecimal result = totalFee.setScale(2, RoundingMode.HALF_UP);
        log.info("阶梯计费完成，总费用: {} 元", result);
        
        return result;
    }

    /**
     * 根据水表ID计算水费
     */
    @Override
    public BigDecimal calculateWaterFeeByMeterId(Long meterId, BigDecimal usage) {
        WaterMeter waterMeter = waterMeterService.getById(meterId);
        if (waterMeter == null) {
            throw new BusinessException("水表不存在");
        }
        return calculateWaterFee(waterMeter.getMeterType(), usage);
    }

    /**
     * 获取指定水表类型的当前有效价格配置
     */
    @Override
    public List<WaterPrice> getActivePricesByMeterType(Integer meterType) {
        LocalDate today = LocalDate.now();
        
        LambdaQueryWrapper<WaterPrice> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(WaterPrice::getMeterType, meterType)
               .eq(WaterPrice::getStatus, 1)  // 启用状态
               .le(WaterPrice::getEffectiveDate, today)  // 已生效
               .and(w -> w.isNull(WaterPrice::getExpiryDate)  // 未过期
                         .or()
                         .ge(WaterPrice::getExpiryDate, today))
               .orderByAsc(WaterPrice::getTier);  // 按阶梯档位排序
        
        return this.list(wrapper);
    }

    /**
     * 获取基础单价（第一档价格）
     */
    @Override
    public BigDecimal getBasePriceByMeterType(Integer meterType) {
        List<WaterPrice> priceList = getActivePricesByMeterType(meterType);
        if (priceList.isEmpty()) {
            return new BigDecimal("2.80");  // 默认价格
        }
        return priceList.get(0).getPrice();
    }

    /**
     * 获取计费明细描述（用于账单展示）
     */
    @Override
    public String getFeeCalculationDetail(Integer meterType, BigDecimal usage) {
        if (usage == null || usage.compareTo(BigDecimal.ZERO) <= 0) {
            return "用水量为0，无需缴费";
        }

        List<WaterPrice> priceList = getActivePricesByMeterType(meterType);
        if (priceList.isEmpty()) {
            return String.format("%.2f立方米 × 2.80元/立方米 = %.2f元", 
                    usage, usage.multiply(new BigDecimal("2.80")));
        }

        // 判断是否为阶梯计价
        boolean isTieredPricing = priceList.stream().anyMatch(p -> p.getTier() != null && p.getTier() > 0);

        if (!isTieredPricing) {
            // 固定单价
            BigDecimal unitPrice = priceList.get(0).getPrice();
            BigDecimal total = usage.multiply(unitPrice).setScale(2, RoundingMode.HALF_UP);
            return String.format("%.2f立方米 × %.2f元/立方米 = %.2f元", usage, unitPrice, total);
        }

        // 阶梯计价明细
        StringBuilder detail = new StringBuilder();
        BigDecimal remainingUsage = usage;
        BigDecimal totalFee = BigDecimal.ZERO;

        for (WaterPrice tier : priceList) {
            if (remainingUsage.compareTo(BigDecimal.ZERO) <= 0) {
                break;
            }

            BigDecimal startUsage = tier.getStartUsage();
            BigDecimal endUsage = tier.getEndUsage();
            BigDecimal unitPrice = tier.getPrice();

            BigDecimal tierCapacity;
            if (endUsage == null) {
                tierCapacity = remainingUsage;
            } else {
                tierCapacity = endUsage.subtract(startUsage);
            }

            BigDecimal usageInTier = remainingUsage.min(tierCapacity);
            BigDecimal tierFee = usageInTier.multiply(unitPrice).setScale(2, RoundingMode.HALF_UP);
            totalFee = totalFee.add(tierFee);

            if (detail.length() > 0) {
                detail.append(" + ");
            }
            detail.append(String.format("%.2f×%.2f", usageInTier, unitPrice));

            remainingUsage = remainingUsage.subtract(usageInTier);
        }

        detail.append(String.format(" = %.2f元", totalFee.setScale(2, RoundingMode.HALF_UP)));
        
        return detail.toString();
    }
}
