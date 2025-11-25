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
 * 水费价格实体类
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("water_price")
public class WaterPrice extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 价格ID
     */
    @TableId(type = IdType.AUTO)
    private Long priceId;

    /**
     * 水表类型(1:家用表 2:商用表 3:工业表)
     */
    private Integer meterType;

    /**
     * 阶梯档位(0:不分档 1:第一档 2:第二档 3:第三档)
     */
    private Integer tier;

    /**
     * 起始用量(立方米)
     */
    private BigDecimal startUsage;

    /**
     * 结束用量(立方米，null表示无上限)
     */
    private BigDecimal endUsage;

    /**
     * 单价(元/立方米)
     */
    private BigDecimal price;

    /**
     * 生效日期
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate effectiveDate;

    /**
     * 失效日期
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate expiryDate;

    /**
     * 状态(0:停用 1:启用)
     */
    private Integer status;
}


