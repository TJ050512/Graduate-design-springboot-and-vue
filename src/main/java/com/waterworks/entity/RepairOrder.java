package com.waterworks.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 报修工单实体类
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("repair_order")
public class RepairOrder extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 工单ID
     */
    @TableId(type = IdType.AUTO)
    private Long orderId;

    /**
     * 工单编号
     */
    private String orderNo;

    /**
     * 报修用户ID
     */
    private Long userId;

    /**
     * 关联水表ID
     */
    private Long meterId;

    /**
     * 报修类型(1:水表故障 2:漏水 3:水质问题 4:水压异常 5:其他)
     */
    private Integer repairType;

    /**
     * 报修标题
     */
    private String title;

    /**
     * 问题描述
     */
    private String description;

    /**
     * 联系人姓名
     */
    private String contactName;

    /**
     * 联系电话
     */
    private String contactPhone;

    /**
     * 报修地址
     */
    private String address;

    /**
     * 图片路径(多个用逗号分隔)
     */
    private String images;

    /**
     * 状态(0:待处理 1:处理中 2:已完成 3:已取消)
     */
    private Integer status;

    /**
     * 优先级(1:紧急 2:普通 3:低)
     */
    private Integer priority;

    /**
     * 处理人ID
     */
    private Long handlerId;

    /**
     * 开始处理时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime handleTime;

    /**
     * 完成时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime completeTime;

    /**
     * 处理结果
     */
    private String handleResult;

    /**
     * 用户反馈
     */
    private String feedback;

    /**
     * 评分(1-5星)
     */
    private Integer rating;
}
