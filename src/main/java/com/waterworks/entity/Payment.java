package com.waterworks.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 缴费记录实体类
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("payment")
public class Payment extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 缴费ID
     */
    @TableId(type = IdType.AUTO)
    private Long paymentId;

    /**
     * 缴费单号
     */
    private String paymentNo;

    /**
     * 用水记录ID
     */
    private Long usageId;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 水表ID
     */
    private Long meterId;

    /**
     * 缴费金额
     */
    private BigDecimal amount;

    /**
     * 支付方式(1:现金 2:微信 3:支付宝 4:银行卡)
     */
    private Integer paymentMethod;

    /**
     * 缴费时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime paymentTime;

    /**
     * 交易流水号
     */
    private String transactionNo;

    /**
     * 状态(0:待支付 1:已支付 2:已退款)
     */
    private Integer status;
}


