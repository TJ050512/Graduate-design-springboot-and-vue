-- ============================================
-- 第二阶段数据库升级脚本
-- ============================================

USE water_management;

-- 1. 为 sys_user 表添加 salt 字段（密码加盐安全改造）
ALTER TABLE `sys_user` ADD COLUMN `salt` VARCHAR(32) COMMENT '密码盐值' AFTER `password`;

-- 2. 创建报修工单表
CREATE TABLE IF NOT EXISTS `repair_order` (
    `order_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '工单ID',
    `order_no` VARCHAR(50) NOT NULL COMMENT '工单编号',
    `user_id` BIGINT NOT NULL COMMENT '报修用户ID',
    `meter_id` BIGINT COMMENT '关联水表ID',
    `repair_type` TINYINT NOT NULL DEFAULT 1 COMMENT '报修类型(1:水表故障 2:漏水 3:水质问题 4:水压异常 5:其他)',
    `title` VARCHAR(100) NOT NULL COMMENT '报修标题',
    `description` TEXT NOT NULL COMMENT '问题描述',
    `contact_name` VARCHAR(50) COMMENT '联系人姓名',
    `contact_phone` VARCHAR(20) COMMENT '联系电话',
    `address` VARCHAR(255) COMMENT '报修地址',
    `images` VARCHAR(500) COMMENT '图片路径(多个用逗号分隔)',
    `status` TINYINT NOT NULL DEFAULT 0 COMMENT '状态(0:待处理 1:处理中 2:已完成 3:已取消)',
    `priority` TINYINT NOT NULL DEFAULT 2 COMMENT '优先级(1:紧急 2:普通 3:低)',
    `handler_id` BIGINT COMMENT '处理人ID',
    `handle_time` DATETIME COMMENT '开始处理时间',
    `complete_time` DATETIME COMMENT '完成时间',
    `handle_result` TEXT COMMENT '处理结果',
    `feedback` TEXT COMMENT '用户反馈',
    `rating` TINYINT COMMENT '评分(1-5星)',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_by` BIGINT COMMENT '创建人',
    `update_by` BIGINT COMMENT '更新人',
    `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除(0:未删除 1:已删除)',
    `remark` VARCHAR(500) COMMENT '备注',
    PRIMARY KEY (`order_id`),
    UNIQUE KEY `uk_order_no` (`order_no`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_meter_id` (`meter_id`),
    KEY `idx_status` (`status`),
    KEY `idx_repair_type` (`repair_type`),
    KEY `idx_handler_id` (`handler_id`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='报修工单表';

-- 完成提示
SELECT '升级完成！已添加 salt 字段和 repair_order 表' AS message;
