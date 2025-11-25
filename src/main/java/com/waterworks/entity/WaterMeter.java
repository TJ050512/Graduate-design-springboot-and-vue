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
 * 水表实体类
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("water_meter")
public class WaterMeter extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 水表ID
     */
    @TableId(type = IdType.AUTO)
    private Long meterId;

    /**
     * 水表编号
     */
    private String meterNo;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 水表类型(1:家用表 2:商用表 3:工业表)
     */
    private Integer meterType;

    /**
     * 水表品牌
     */
    private String brand;

    /**
     * 水表型号
     */
    private String model;

    /**
     * 安装地址
     */
    private String installAddress;

    /**
     * 安装日期
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate installDate;

    /**
     * 初始读数
     */
    private BigDecimal initialReading;

    /**
     * 当前读数
     */
    private BigDecimal currentReading;

    /**
     * 口径(mm)
     */
    private Integer caliber;

    /**
     * 水表状态(0:停用 1:正常 2:故障)
     */
    private Integer status;
}


