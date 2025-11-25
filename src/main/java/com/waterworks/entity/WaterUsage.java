package com.waterworks.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 用水记录实体类
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("water_usage")
public class WaterUsage extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 记录ID
     */
    @TableId(type = IdType.AUTO)
    private Long usageId;

    /**
     * 水表ID
     */
    private Long meterId;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 抄表员ID
     */
    private Long readerId;

    /**
     * 抄表月份
     */
    private String readMonth;

    /**
     * 抄表日期
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate readDate;

    /**
     * 上次读数
     */
    private BigDecimal lastReading;

    /**
     * 本次读数
     */
    private BigDecimal currentReading;

    /**
     * 用水量(立方米)
     */
    private BigDecimal usage;

    /**
     * 水费单价(元/立方米)
     */
    private BigDecimal price;

    /**
     * 应缴金额
     */
    private BigDecimal amount;

    /**
     * 状态(0:待确认 1:已确认 2:已缴费)
     */
    private Integer status;
}


