-- 水务管理系统数据库初始化脚本

-- 创建数据库
CREATE DATABASE IF NOT EXISTS water_management DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE water_management;

-- 用户表
CREATE TABLE IF NOT EXISTS `sys_user` (
    `user_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '用户ID',
    `username` VARCHAR(50) NOT NULL COMMENT '用户名',
    `password` VARCHAR(100) NOT NULL COMMENT '密码',
    `real_name` VARCHAR(50) COMMENT '真实姓名',
    `gender` TINYINT DEFAULT 1 COMMENT '性别(0:女 1:男)',
    `phone` VARCHAR(20) COMMENT '手机号',
    `email` VARCHAR(100) COMMENT '邮箱',
    `avatar` VARCHAR(255) COMMENT '头像',
    `id_card` VARCHAR(18) COMMENT '身份证号',
    `address` VARCHAR(255) COMMENT '地址',
    `user_type` TINYINT NOT NULL DEFAULT 2 COMMENT '用户类型(1:管理员 2:普通用户 3:抄表员)',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态(0:禁用 1:正常)',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_by` BIGINT COMMENT '创建人',
    `update_by` BIGINT COMMENT '更新人',
    `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除(0:未删除 1:已删除)',
    `remark` VARCHAR(500) COMMENT '备注',
    PRIMARY KEY (`user_id`),
    UNIQUE KEY `uk_username` (`username`),
    KEY `idx_phone` (`phone`),
    KEY `idx_user_type` (`user_type`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 水表表
CREATE TABLE IF NOT EXISTS `water_meter` (
    `meter_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '水表ID',
    `meter_no` VARCHAR(50) NOT NULL COMMENT '水表编号',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `meter_type` TINYINT NOT NULL DEFAULT 1 COMMENT '水表类型(1:家用表 2:商用表 3:工业表)',
    `brand` VARCHAR(50) COMMENT '水表品牌',
    `model` VARCHAR(50) COMMENT '水表型号',
    `install_address` VARCHAR(255) COMMENT '安装地址',
    `install_date` DATE COMMENT '安装日期',
    `initial_reading` DECIMAL(10, 2) DEFAULT 0.00 COMMENT '初始读数',
    `current_reading` DECIMAL(10, 2) DEFAULT 0.00 COMMENT '当前读数',
    `caliber` INT COMMENT '口径(mm)',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '水表状态(0:停用 1:正常 2:故障)',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_by` BIGINT COMMENT '创建人',
    `update_by` BIGINT COMMENT '更新人',
    `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除(0:未删除 1:已删除)',
    `remark` VARCHAR(500) COMMENT '备注',
    PRIMARY KEY (`meter_id`),
    UNIQUE KEY `uk_meter_no` (`meter_no`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_meter_type` (`meter_type`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='水表表';

-- 用水记录表
CREATE TABLE IF NOT EXISTS `water_usage` (
    `usage_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '记录ID',
    `meter_id` BIGINT NOT NULL COMMENT '水表ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `reader_id` BIGINT COMMENT '抄表员ID',
    `read_month` VARCHAR(7) NOT NULL COMMENT '抄表月份(YYYY-MM)',
    `read_date` DATE NOT NULL COMMENT '抄表日期',
    `last_reading` DECIMAL(10, 2) NOT NULL COMMENT '上次读数',
    `current_reading` DECIMAL(10, 2) NOT NULL COMMENT '本次读数',
    `usage` DECIMAL(10, 2) NOT NULL COMMENT '用水量(立方米)',
    `price` DECIMAL(10, 2) NOT NULL COMMENT '水费单价(元/立方米)',
    `amount` DECIMAL(10, 2) NOT NULL COMMENT '应缴金额',
    `status` TINYINT NOT NULL DEFAULT 0 COMMENT '状态(0:待确认 1:已确认 2:已缴费)',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_by` BIGINT COMMENT '创建人',
    `update_by` BIGINT COMMENT '更新人',
    `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除(0:未删除 1:已删除)',
    `remark` VARCHAR(500) COMMENT '备注',
    PRIMARY KEY (`usage_id`),
    KEY `idx_meter_id` (`meter_id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_read_month` (`read_month`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用水记录表';

-- 缴费记录表
CREATE TABLE IF NOT EXISTS `payment` (
    `payment_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '缴费ID',
    `payment_no` VARCHAR(50) NOT NULL COMMENT '缴费单号',
    `usage_id` BIGINT COMMENT '用水记录ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `meter_id` BIGINT NOT NULL COMMENT '水表ID',
    `amount` DECIMAL(10, 2) NOT NULL COMMENT '缴费金额',
    `payment_method` TINYINT NOT NULL COMMENT '支付方式(1:现金 2:微信 3:支付宝 4:银行卡)',
    `payment_time` DATETIME COMMENT '缴费时间',
    `transaction_no` VARCHAR(100) COMMENT '交易流水号',
    `status` TINYINT NOT NULL DEFAULT 0 COMMENT '状态(0:待支付 1:已支付 2:已退款)',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_by` BIGINT COMMENT '创建人',
    `update_by` BIGINT COMMENT '更新人',
    `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除(0:未删除 1:已删除)',
    `remark` VARCHAR(500) COMMENT '备注',
    PRIMARY KEY (`payment_id`),
    UNIQUE KEY `uk_payment_no` (`payment_no`),
    KEY `idx_usage_id` (`usage_id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_status` (`status`),
    KEY `idx_payment_time` (`payment_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='缴费记录表';

-- 水费价格表
CREATE TABLE IF NOT EXISTS `water_price` (
    `price_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '价格ID',
    `meter_type` TINYINT NOT NULL COMMENT '水表类型(1:家用表 2:商用表 3:工业表)',
    `tier` TINYINT NOT NULL DEFAULT 0 COMMENT '阶梯档位(0:不分档 1:第一档 2:第二档 3:第三档)',
    `start_usage` DECIMAL(10, 2) NOT NULL DEFAULT 0.00 COMMENT '起始用量(立方米)',
    `end_usage` DECIMAL(10, 2) COMMENT '结束用量(立方米，null表示无上限)',
    `price` DECIMAL(10, 2) NOT NULL COMMENT '单价(元/立方米)',
    `effective_date` DATE NOT NULL COMMENT '生效日期',
    `expiry_date` DATE COMMENT '失效日期',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态(0:停用 1:启用)',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_by` BIGINT COMMENT '创建人',
    `update_by` BIGINT COMMENT '更新人',
    `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除(0:未删除 1:已删除)',
    `remark` VARCHAR(500) COMMENT '备注',
    PRIMARY KEY (`price_id`),
    KEY `idx_meter_type` (`meter_type`),
    KEY `idx_effective_date` (`effective_date`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='水费价格表';

-- 公告表
CREATE TABLE IF NOT EXISTS `notice` (
    `notice_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '公告ID',
    `title` VARCHAR(200) NOT NULL COMMENT '公告标题',
    `content` TEXT NOT NULL COMMENT '公告内容',
    `notice_type` TINYINT NOT NULL DEFAULT 1 COMMENT '公告类型(1:系统公告 2:停水通知 3:价格调整)',
    `publisher_id` BIGINT COMMENT '发布人ID',
    `status` TINYINT NOT NULL DEFAULT 0 COMMENT '状态(0:草稿 1:已发布 2:已撤回)',
    `is_top` TINYINT NOT NULL DEFAULT 0 COMMENT '是否置顶(0:否 1:是)',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_by` BIGINT COMMENT '创建人',
    `update_by` BIGINT COMMENT '更新人',
    `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除(0:未删除 1:已删除)',
    `remark` VARCHAR(500) COMMENT '备注',
    PRIMARY KEY (`notice_id`),
    KEY `idx_notice_type` (`notice_type`),
    KEY `idx_status` (`status`),
    KEY `idx_is_top` (`is_top`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='公告表';

-- 插入默认管理员账号 (密码: admin123 经过MD5加密)
INSERT INTO `sys_user` (`username`, `password`, `real_name`, `user_type`, `status`, `remark`) 
VALUES ('admin', '0192023a7bbd73250516f069df18b500', '系统管理员', 1, 1, '默认管理员账号');

-- 插入测试用户 (密码: 123456 经过MD5加密)
INSERT INTO `sys_user` (`username`, `password`, `real_name`, `gender`, `phone`, `user_type`, `status`) 
VALUES ('user001', 'e10adc3949ba59abbe56e057f20f883e', '张三', 1, '13800138001', 2, 1);

INSERT INTO `sys_user` (`username`, `password`, `real_name`, `gender`, `phone`, `user_type`, `status`) 
VALUES ('reader001', 'e10adc3949ba59abbe56e057f20f883e', '李四', 1, '13800138002', 3, 1);

-- 插入水费价格配置
INSERT INTO `water_price` (`meter_type`, `tier`, `start_usage`, `end_usage`, `price`, `effective_date`, `status`, `remark`) 
VALUES 
(1, 1, 0.00, 15.00, 2.80, '2024-01-01', 1, '居民用水第一档'),
(1, 2, 15.00, 25.00, 4.00, '2024-01-01', 1, '居民用水第二档'),
(1, 3, 25.00, NULL, 6.00, '2024-01-01', 1, '居民用水第三档'),
(2, 0, 0.00, NULL, 4.50, '2024-01-01', 1, '商业用水'),
(3, 0, 0.00, NULL, 3.50, '2024-01-01', 1, '工业用水');

-- 插入示例公告
INSERT INTO `notice` (`title`, `content`, `notice_type`, `status`, `is_top`) 
VALUES ('欢迎使用水务管理系统', '本系统为水务管理提供全面的信息化解决方案，包括用户管理、水表管理、用水记录、缴费管理等功能。', 1, 1, 1);


