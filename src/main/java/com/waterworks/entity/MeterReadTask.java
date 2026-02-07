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
 * 抄表任务实体类
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("meter_read_task")
public class MeterReadTask extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 任务ID
     */
    @TableId(type = IdType.AUTO)
    private Long taskId;

    /**
     * 水表ID
     */
    private Long meterId;

    /**
     * 用户ID(水表所属用户)
     */
    private Long userId;

    /**
     * 上次抄表读数
     */
    private BigDecimal lastReading;

    /**
     * 当前水表读数(发通知时)
     */
    private BigDecimal currentReading;

    /**
     * 状态(0:待抄表 1:已完成)
     */
    private Integer status;

    /**
     * 通知时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime notifiedAt;

    /**
     * 完成时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime completedAt;

    /**
     * 关联的用水记录ID(完成后)
     */
    private Long usageId;
}
