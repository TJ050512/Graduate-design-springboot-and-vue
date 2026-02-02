package com.waterworks.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.waterworks.entity.WaterPrice;

import java.math.BigDecimal;
import java.util.List;

/**
 * 水费价格服务接口
 * 
 * @description 提供阶梯水价计算功能，支持不同水表类型的差异化定价
 */
public interface WaterPriceService extends IService<WaterPrice> {

    /**
     * 根据水表类型和用水量计算水费
     * 
     * @param meterType 水表类型(1:家用表 2:商用表 3:工业表)
     * @param usage     用水量(立方米)
     * @return 应缴水费金额
     */
    BigDecimal calculateWaterFee(Integer meterType, BigDecimal usage);

    /**
     * 根据水表ID和用水量计算水费
     * 
     * @param meterId 水表ID
     * @param usage   用水量(立方米)
     * @return 应缴水费金额
     */
    BigDecimal calculateWaterFeeByMeterId(Long meterId, BigDecimal usage);

    /**
     * 获取指定水表类型的当前有效价格配置
     * 
     * @param meterType 水表类型
     * @return 价格配置列表(按阶梯排序)
     */
    List<WaterPrice> getActivePricesByMeterType(Integer meterType);

    /**
     * 获取水表类型的基础单价(第一档或固定价格)
     * 
     * @param meterType 水表类型
     * @return 基础单价
     */
    BigDecimal getBasePriceByMeterType(Integer meterType);

    /**
     * 获取计费明细(用于账单展示)
     * 
     * @param meterType 水表类型
     * @param usage     用水量
     * @return 计费明细描述
     */
    String getFeeCalculationDetail(Integer meterType, BigDecimal usage);
}
