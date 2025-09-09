#!/bin/bash
# MySQL数据库初始化脚本

# 创建服装生产管理系统数据库
CREATE DATABASE IF NOT EXISTS garment_production 
CHARACTER SET utf8mb4 
COLLATE utf8mb4_unicode_ci;

# 使用数据库
USE garment_production;

# 创建租户表
CREATE TABLE IF NOT EXISTS `tenants` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '租户ID',
  `tenant_code` varchar(32) NOT NULL COMMENT '租户编码',
  `company_name` varchar(100) NOT NULL COMMENT '企业名称',
  `company_type` enum('factory','workshop','trading') DEFAULT 'factory' COMMENT '企业类型',
  `contact_person` varchar(50) NOT NULL COMMENT '联系人',
  `contact_phone` varchar(20) NOT NULL COMMENT '联系电话',
  `contact_email` varchar(100) DEFAULT NULL COMMENT '联系邮箱',
  `address` varchar(200) DEFAULT NULL COMMENT '企业地址',
  `business_license` varchar(50) DEFAULT NULL COMMENT '营业执照号',
  `tax_number` varchar(50) DEFAULT NULL COMMENT '税号',
  `logo_url` varchar(255) DEFAULT NULL COMMENT '企业Logo',
  `subscription_plan` enum('trial','basic','standard','premium') DEFAULT 'trial' COMMENT '订阅套餐',
  `subscription_start` date DEFAULT NULL COMMENT '订阅开始日期',
  `subscription_end` date DEFAULT NULL COMMENT '订阅结束日期',
  `max_users` int(11) DEFAULT 10 COMMENT '最大用户数',
  `max_storage` bigint(20) DEFAULT 1073741824 COMMENT '最大存储空间(字节)',
  `status` enum('pending','active','suspended','expired') DEFAULT 'pending' COMMENT '状态',
  `created_by` bigint(20) NOT NULL COMMENT '创建人ID',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint(1) DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_tenant_code` (`tenant_code`),
  KEY `idx_status` (`status`),
  KEY `idx_subscription_plan` (`subscription_plan`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='租户表';

# 创建用户表
CREATE TABLE IF NOT EXISTS `users` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `username` varchar(50) DEFAULT NULL COMMENT '用户名',
  `phone` varchar(20) NOT NULL COMMENT '手机号',
  `password` varchar(255) DEFAULT NULL COMMENT '密码',
  `name` varchar(50) NOT NULL COMMENT '姓名',
  `avatar` varchar(255) DEFAULT NULL COMMENT '头像',
  `gender` enum('male','female','unknown') DEFAULT 'unknown' COMMENT '性别',
  `birth_date` date DEFAULT NULL COMMENT '出生日期',
  `wechat_openid` varchar(64) DEFAULT NULL COMMENT '微信OpenID',
  `wechat_unionid` varchar(64) DEFAULT NULL COMMENT '微信UnionID',
  `current_tenant_id` bigint(20) DEFAULT NULL COMMENT '当前租户ID',
  `is_system_admin` tinyint(1) DEFAULT 0 COMMENT '是否系统管理员',
  `last_login_time` datetime DEFAULT NULL COMMENT '最后登录时间',
  `last_login_ip` varchar(50) DEFAULT NULL COMMENT '最后登录IP',
  `status` enum('active','disabled','locked') DEFAULT 'active' COMMENT '状态',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint(1) DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_phone` (`phone`),
  UNIQUE KEY `uk_username` (`username`),
  UNIQUE KEY `uk_wechat_openid` (`wechat_openid`),
  KEY `idx_current_tenant` (`current_tenant_id`),
  KEY `idx_wechat_unionid` (`wechat_unionid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

# 创建租户用户关联表
CREATE TABLE IF NOT EXISTS `tenant_users` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint(20) NOT NULL COMMENT '租户ID',
  `user_id` bigint(20) NOT NULL COMMENT '用户ID',
  `role` enum('owner','admin','manager','supervisor','worker') NOT NULL COMMENT '租户内角色',
  `employee_no` varchar(50) DEFAULT NULL COMMENT '员工工号',
  `department` varchar(50) DEFAULT NULL COMMENT '部门',
  `position` varchar(50) DEFAULT NULL COMMENT '职位',
  `workshop_id` bigint(20) DEFAULT NULL COMMENT '车间ID',
  `permissions` json DEFAULT NULL COMMENT '权限配置',
  `status` enum('pending','active','suspended') DEFAULT 'pending' COMMENT '状态',
  `invited_by` bigint(20) DEFAULT NULL COMMENT '邀请人ID',
  `invited_at` datetime DEFAULT NULL COMMENT '邀请时间',
  `joined_at` datetime DEFAULT NULL COMMENT '加入时间',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint(1) DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_tenant_user` (`tenant_id`, `user_id`),
  UNIQUE KEY `uk_tenant_employee_no` (`tenant_id`, `employee_no`),
  KEY `idx_role` (`role`),
  KEY `idx_status` (`status`),
  KEY `idx_workshop` (`workshop_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='租户用户关联表';

# 创建邀请记录表
CREATE TABLE IF NOT EXISTS `invitations` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint(20) NOT NULL COMMENT '租户ID',
  `inviter_id` bigint(20) NOT NULL COMMENT '邀请人ID',
  `phone` varchar(20) NOT NULL COMMENT '被邀请人手机号',
  `email` varchar(100) DEFAULT NULL COMMENT '被邀请人邮箱',
  `name` varchar(50) NOT NULL COMMENT '被邀请人姓名',
  `role` enum('admin','manager','supervisor','worker') NOT NULL COMMENT '邀请角色',
  `employee_no` varchar(50) DEFAULT NULL COMMENT '员工工号',
  `department` varchar(50) DEFAULT NULL COMMENT '部门',
  `position` varchar(50) DEFAULT NULL COMMENT '职位',
  `workshop_id` bigint(20) DEFAULT NULL COMMENT '车间ID',
  `invitation_code` varchar(32) NOT NULL COMMENT '邀请码',
  `message` text DEFAULT NULL COMMENT '邀请消息',
  `status` enum('pending','accepted','rejected','expired') DEFAULT 'pending' COMMENT '状态',
  `expires_at` datetime NOT NULL COMMENT '过期时间',
  `accepted_at` datetime DEFAULT NULL COMMENT '接受时间',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_invitation_code` (`invitation_code`),
  KEY `idx_tenant_phone` (`tenant_id`, `phone`),
  KEY `idx_status` (`status`),
  KEY `idx_expires_at` (`expires_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='邀请记录表';

# 插入初始数据
INSERT INTO `tenants` (`tenant_code`, `company_name`, `company_type`, `contact_person`, `contact_phone`, `status`, `created_by`, `max_users`) 
VALUES ('DEMO001', '演示服装厂', 'factory', '张总', '13800138000', 'active', 1, 50);

INSERT INTO `users` (`username`, `phone`, `password`, `name`, `current_tenant_id`, `is_system_admin`, `status`) 
VALUES ('admin', '13800138000', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEILC', '系统管理员', 1, 1, 'active');

INSERT INTO `tenant_users` (`tenant_id`, `user_id`, `role`, `employee_no`, `status`, `joined_at`) 
VALUES (1, 1, 'owner', 'ADMIN001', 'active', NOW());

# 创建车间表
CREATE TABLE IF NOT EXISTS `workshops` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint(20) NOT NULL COMMENT '租户ID',
  `workshop_code` varchar(50) NOT NULL COMMENT '车间编码',
  `workshop_name` varchar(100) NOT NULL COMMENT '车间名称',
  `manager_id` bigint(20) DEFAULT NULL COMMENT '车间主管ID',
  `description` text DEFAULT NULL COMMENT '车间描述',
  `sort_order` int(11) DEFAULT 0 COMMENT '排序',
  `status` enum('active','inactive') DEFAULT 'active',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint(1) DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_tenant_workshop_code` (`tenant_id`, `workshop_code`),
  KEY `idx_manager` (`manager_id`),
  KEY `idx_sort_order` (`sort_order`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='车间表';

# 创建客户表
CREATE TABLE IF NOT EXISTS `customers` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint(20) NOT NULL COMMENT '租户ID',
  `customer_code` varchar(50) NOT NULL COMMENT '客户编码',
  `customer_name` varchar(100) NOT NULL COMMENT '客户名称',
  `contact_person` varchar(50) DEFAULT NULL COMMENT '联系人',
  `contact_phone` varchar(20) DEFAULT NULL COMMENT '联系电话',
  `contact_email` varchar(100) DEFAULT NULL COMMENT '联系邮箱',
  `address` varchar(200) DEFAULT NULL COMMENT '客户地址',
  `status` enum('active','inactive') DEFAULT 'active',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint(1) DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_tenant_customer_code` (`tenant_id`, `customer_code`),
  KEY `idx_customer_name` (`customer_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='客户表';

# 创建款式表
CREATE TABLE IF NOT EXISTS `styles` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint(20) NOT NULL COMMENT '租户ID',
  `style_code` varchar(50) NOT NULL COMMENT '款号',
  `style_name` varchar(100) NOT NULL COMMENT '款式名称',
  `customer_id` bigint(20) DEFAULT NULL COMMENT '客户ID',
  `season` varchar(20) DEFAULT NULL COMMENT '季节',
  `category` varchar(50) DEFAULT NULL COMMENT '分类',
  `colors` json DEFAULT NULL COMMENT '颜色列表',
  `sizes` json DEFAULT NULL COMMENT '尺码列表',
  `size_ratio` json DEFAULT NULL COMMENT '默认尺码比例',
  `unit_price` decimal(10,2) DEFAULT NULL COMMENT '单价',
  `description` text DEFAULT NULL COMMENT '款式描述',
  `image_urls` json DEFAULT NULL COMMENT '款式图片',
  `tech_spec` text DEFAULT NULL COMMENT '工艺说明',
  `status` enum('active','inactive') DEFAULT 'active',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint(1) DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_tenant_style_code` (`tenant_id`, `style_code`),
  KEY `idx_customer` (`customer_id`),
  KEY `idx_season` (`season`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='款式表';

# 创建工序表
CREATE TABLE IF NOT EXISTS `processes` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint(20) NOT NULL COMMENT '租户ID',
  `process_code` varchar(50) NOT NULL COMMENT '工序编码',
  `process_name` varchar(100) NOT NULL COMMENT '工序名称',
  `category` varchar(50) DEFAULT NULL COMMENT '工序分类',
  `unit` varchar(10) DEFAULT '件' COMMENT '计量单位',
  `default_price` decimal(10,4) DEFAULT NULL COMMENT '默认工价',
  `difficulty_level` enum('easy','medium','hard') DEFAULT 'medium' COMMENT '难度等级',
  `quality_standard` text DEFAULT NULL COMMENT '质量标准',
  `sort_order` int(11) DEFAULT 0 COMMENT '排序',
  `status` enum('active','inactive') DEFAULT 'active',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint(1) DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_tenant_process_code` (`tenant_id`, `process_code`),
  KEY `idx_category` (`category`),
  KEY `idx_sort_order` (`sort_order`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='工序表';

# 创建工价模板表
CREATE TABLE IF NOT EXISTS `process_prices` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint(20) NOT NULL COMMENT '租户ID',
  `style_id` bigint(20) DEFAULT NULL COMMENT '款式ID（为空表示通用）',
  `process_id` bigint(20) NOT NULL COMMENT '工序ID',
  `workshop_id` bigint(20) DEFAULT NULL COMMENT '车间ID（为空表示通用）',
  `price` decimal(10,4) NOT NULL COMMENT '工价',
  `price_type` enum('per_piece','per_hour','fixed') DEFAULT 'per_piece' COMMENT '计价方式',
  `effective_from` date NOT NULL COMMENT '生效日期',
  `effective_to` date DEFAULT NULL COMMENT '失效日期',
  `version` int(11) DEFAULT 1 COMMENT '版本号',
  `remark` varchar(200) DEFAULT NULL COMMENT '备注',
  `status` enum('active','inactive') DEFAULT 'active',
  `created_by` bigint(20) NOT NULL COMMENT '创建人',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint(1) DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  KEY `idx_style_process` (`style_id`, `process_id`),
  KEY `idx_process_workshop` (`process_id`, `workshop_id`),
  KEY `idx_effective_date` (`effective_from`, `effective_to`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='工价模板表';

# 创建裁床订单表
CREATE TABLE IF NOT EXISTS `cut_orders` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint(20) NOT NULL COMMENT '租户ID',
  `order_no` varchar(50) NOT NULL COMMENT '裁床单号',
  `style_id` bigint(20) NOT NULL COMMENT '款式ID',
  `color` varchar(50) NOT NULL COMMENT '颜色',
  `bed_no` varchar(50) DEFAULT NULL COMMENT '床次号',
  `total_layers` int(11) NOT NULL COMMENT '总层数',
  `cutting_type` enum('average','segment') DEFAULT 'average' COMMENT '裁切类型：平均裁/分层段裁',
  `size_ratio` json NOT NULL COMMENT '尺码比例',
  `segment_plan` json DEFAULT NULL COMMENT '分段方案（分层段裁时使用）',
  `bundle_rules` json NOT NULL COMMENT '分包规则',
  `total_quantity` int(11) NOT NULL COMMENT '总数量',
  `bundle_count` int(11) DEFAULT 0 COMMENT '包数',
  `cutting_date` date DEFAULT NULL COMMENT '裁剪日期',
  `delivery_date` date DEFAULT NULL COMMENT '交期',
  `priority` enum('low','medium','high','urgent') DEFAULT 'medium' COMMENT '优先级',
  `remark` text DEFAULT NULL COMMENT '备注',
  `status` enum('draft','confirmed','cutting','completed','cancelled') DEFAULT 'draft',
  `created_by` bigint(20) NOT NULL COMMENT '创建人',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint(1) DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_tenant_order_no` (`tenant_id`, `order_no`),
  KEY `idx_style` (`style_id`),
  KEY `idx_status` (`status`),
  KEY `idx_cutting_date` (`cutting_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='裁床订单表';

# 创建包表
CREATE TABLE IF NOT EXISTS `bundles` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint(20) NOT NULL COMMENT '租户ID',
  `cut_order_id` bigint(20) NOT NULL COMMENT '裁床订单ID',
  `bundle_no` varchar(50) NOT NULL COMMENT '包号',
  `size` varchar(10) NOT NULL COMMENT '尺码',
  `color` varchar(50) NOT NULL COMMENT '颜色',
  `quantity` int(11) NOT NULL COMMENT '数量',
  `layer_from` int(11) DEFAULT NULL COMMENT '起始层',
  `layer_to` int(11) DEFAULT NULL COMMENT '结束层',
  `segment_tag` varchar(20) DEFAULT NULL COMMENT '层段标签（HIGH/MID/LOW）',
  `qr_code` varchar(255) NOT NULL COMMENT '二维码内容',
  `barcode` varchar(100) DEFAULT NULL COMMENT '条形码',
  `status` enum('pending','in_work','completed','returned','shipped') DEFAULT 'pending',
  `current_process_id` bigint(20) DEFAULT NULL COMMENT '当前工序ID',
  `current_worker_id` bigint(20) DEFAULT NULL COMMENT '当前工人ID',
  `progress` json DEFAULT NULL COMMENT '工序进度记录',
  `completion_rate` decimal(5,2) DEFAULT 0.00 COMMENT '完成率',
  `quality_grade` enum('A','B','C','D') DEFAULT NULL COMMENT '质量等级',
  `defect_count` int(11) DEFAULT 0 COMMENT '疵点数量',
  `printed_at` datetime DEFAULT NULL COMMENT '打印时间',
  `started_at` datetime DEFAULT NULL COMMENT '开始生产时间',
  `completed_at` datetime DEFAULT NULL COMMENT '完成时间',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint(1) DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_tenant_bundle_no` (`tenant_id`, `bundle_no`),
  UNIQUE KEY `uk_qr_code` (`qr_code`),
  KEY `idx_cut_order` (`cut_order_id`),
  KEY `idx_status` (`status`),
  KEY `idx_current_worker` (`current_worker_id`),
  KEY `idx_current_process` (`current_process_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='包表';

# 创建生产流水表
CREATE TABLE IF NOT EXISTS `production_flows` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint(20) NOT NULL COMMENT '租户ID',
  `bundle_id` bigint(20) NOT NULL COMMENT '包ID',
  `process_id` bigint(20) NOT NULL COMMENT '工序ID',
  `worker_id` bigint(20) NOT NULL COMMENT '工人ID',
  `action` enum('take','submit','return','repair','cancel') NOT NULL COMMENT '操作类型',
  `quantity_ok` int(11) DEFAULT 0 COMMENT '合格数量',
  `quantity_ng` int(11) DEFAULT 0 COMMENT '不良数量',
  `unit_price` decimal(10,4) DEFAULT NULL COMMENT '单价',
  `amount` decimal(10,2) DEFAULT NULL COMMENT '金额',
  `work_hours` decimal(8,2) DEFAULT NULL COMMENT '工时',
  `efficiency` decimal(8,2) DEFAULT NULL COMMENT '效率',
  `quality_score` decimal(5,2) DEFAULT NULL COMMENT '质量评分',
  `scan_time` datetime NOT NULL COMMENT '扫码时间',
  `location` varchar(100) DEFAULT NULL COMMENT '操作地点',
  `device_info` varchar(200) DEFAULT NULL COMMENT '设备信息',
  `remark` text DEFAULT NULL COMMENT '备注',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_tenant_bundle` (`tenant_id`, `bundle_id`),
  KEY `idx_worker_time` (`worker_id`, `scan_time`),
  KEY `idx_process` (`process_id`),
  KEY `idx_action` (`action`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='生产流水表';

# 创建计件工资记录表
CREATE TABLE IF NOT EXISTS `piecework_records` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint(20) NOT NULL COMMENT '租户ID',
  `worker_id` bigint(20) NOT NULL COMMENT '工人ID',
  `bundle_id` bigint(20) NOT NULL COMMENT '包ID',
  `process_id` bigint(20) NOT NULL COMMENT '工序ID',
  `flow_id` bigint(20) NOT NULL COMMENT '生产流水ID',
  `quantity` int(11) NOT NULL COMMENT '计件数量',
  `unit_price` decimal(10,4) NOT NULL COMMENT '单价',
  `amount` decimal(10,2) NOT NULL COMMENT '计件金额',
  `bonus` decimal(10,2) DEFAULT 0.00 COMMENT '奖金',
  `penalty` decimal(10,2) DEFAULT 0.00 COMMENT '扣款',
  `final_amount` decimal(10,2) NOT NULL COMMENT '最终金额',
  `period_id` bigint(20) DEFAULT NULL COMMENT '结算期ID',
  `settlement_status` enum('pending','settled','locked') DEFAULT 'pending' COMMENT '结算状态',
  `work_date` date NOT NULL COMMENT '工作日期',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_worker_period` (`worker_id`, `period_id`),
  KEY `idx_tenant_date` (`tenant_id`, `work_date`),
  KEY `idx_bundle_process` (`bundle_id`, `process_id`),
  KEY `idx_flow` (`flow_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='计件工资记录表';

# 创建结算期表
CREATE TABLE IF NOT EXISTS `payroll_periods` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint(20) NOT NULL COMMENT '租户ID',
  `period_name` varchar(50) NOT NULL COMMENT '结算期名称',
  `from_date` date NOT NULL COMMENT '开始日期',
  `to_date` date NOT NULL COMMENT '结束日期',
  `total_amount` decimal(15,2) DEFAULT 0.00 COMMENT '总金额',
  `worker_count` int(11) DEFAULT 0 COMMENT '工人数量',
  `record_count` int(11) DEFAULT 0 COMMENT '记录数量',
  `status` enum('draft','confirmed','locked','paid') DEFAULT 'draft' COMMENT '状态',
  `locked_by` bigint(20) DEFAULT NULL COMMENT '锁定人',
  `locked_at` datetime DEFAULT NULL COMMENT '锁定时间',
  `created_by` bigint(20) NOT NULL COMMENT '创建人',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_tenant_period` (`tenant_id`, `period_name`),
  KEY `idx_date_range` (`from_date`, `to_date`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='结算期表';

# 创建返修记录表
CREATE TABLE IF NOT EXISTS `repair_records` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint(20) NOT NULL COMMENT '租户ID',
  `bundle_id` bigint(20) NOT NULL COMMENT '包ID',
  `process_id` bigint(20) NOT NULL COMMENT '返修工序ID',
  `defect_process_id` bigint(20) DEFAULT NULL COMMENT '出现问题的工序ID',
  `worker_id` bigint(20) NOT NULL COMMENT '返修工人ID',
  `defect_worker_id` bigint(20) DEFAULT NULL COMMENT '造成问题的工人ID',
  `reason_code` varchar(50) NOT NULL COMMENT '返修原因代码',
  `reason_desc` varchar(200) NOT NULL COMMENT '返修原因描述',
  `quantity` int(11) NOT NULL COMMENT '返修数量',
  `severity` enum('low','medium','high','critical') DEFAULT 'medium' COMMENT '严重程度',
  `repair_price` decimal(10,4) DEFAULT NULL COMMENT '返修工价',
  `repair_amount` decimal(10,2) DEFAULT NULL COMMENT '返修金额',
  `penalty_amount` decimal(10,2) DEFAULT NULL COMMENT '质量扣款',
  `images` json DEFAULT NULL COMMENT '问题图片',
  `repair_method` text DEFAULT NULL COMMENT '返修方法',
  `status` enum('pending','in_repair','completed','cancelled') DEFAULT 'pending',
  `reported_by` bigint(20) NOT NULL COMMENT '报告人',
  `reported_at` datetime NOT NULL COMMENT '报告时间',
  `completed_at` datetime DEFAULT NULL COMMENT '完成时间',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_bundle` (`bundle_id`),
  KEY `idx_worker` (`worker_id`),
  KEY `idx_defect_worker` (`defect_worker_id`),
  KEY `idx_status` (`status`),
  KEY `idx_reason` (`reason_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='返修记录表';

# 创建打印任务表
CREATE TABLE IF NOT EXISTS `print_tasks` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint(20) NOT NULL COMMENT '租户ID',
  `cut_order_id` bigint(20) NOT NULL COMMENT '裁床订单ID',
  `task_name` varchar(100) NOT NULL COMMENT '打印任务名称',
  `bundle_ids` json NOT NULL COMMENT '包ID列表',
  `template_id` bigint(20) NOT NULL COMMENT '模板ID',
  `printer_type` enum('58mm','80mm') DEFAULT '80mm' COMMENT '打印机类型',
  `total_count` int(11) NOT NULL COMMENT '总打印数量',
  `printed_count` int(11) DEFAULT 0 COMMENT '已打印数量',
  `failed_count` int(11) DEFAULT 0 COMMENT '失败数量',
  `status` enum('pending','printing','completed','failed','cancelled') DEFAULT 'pending',
  `print_settings` json DEFAULT NULL COMMENT '打印设置',
  `error_message` text DEFAULT NULL COMMENT '错误信息',
  `started_at` datetime DEFAULT NULL COMMENT '开始时间',
  `completed_at` datetime DEFAULT NULL COMMENT '完成时间',
  `created_by` bigint(20) NOT NULL COMMENT '创建人',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_cut_order` (`cut_order_id`),
  KEY `idx_status` (`status`),
  KEY `idx_created_by` (`created_by`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='打印任务表';

# 插入初始数据
INSERT INTO `workshops` (`tenant_id`, `workshop_code`, `workshop_name`, `description`, `sort_order`) VALUES
(1, 'WS001', '一车间', '主要负责基础工序', 1),
(1, 'WS002', '二车间', '主要负责精细工序', 2),
(1, 'WS003', '三车间', '主要负责后整工序', 3);

INSERT INTO `customers` (`tenant_id`, `customer_code`, `customer_name`, `contact_person`, `contact_phone`) VALUES
(1, 'CUST001', '优衣库', '张经理', '13800001111'),
(1, 'CUST002', 'ZARA', '李经理', '13800002222'),
(1, 'CUST003', 'H&M', '王经理', '13800003333');

INSERT INTO `processes` (`tenant_id`, `process_code`, `process_name`, `category`, `default_price`, `sort_order`) VALUES
(1, 'P001', '裁剪', '裁剪工序', 0.50, 1),
(1, 'P002', '缝合', '缝制工序', 1.20, 2),
(1, 'P003', '锁边', '缝制工序', 0.80, 3),
(1, 'P004', '熨烫', '后整工序', 0.60, 4),
(1, 'P005', '检验', '质检工序', 0.40, 5),
(1, 'P006', '包装', '包装工序', 0.30, 6);

INSERT INTO `styles` (`tenant_id`, `style_code`, `style_name`, `customer_id`, `season`, `colors`, `sizes`, `size_ratio`) VALUES
(1, 'ST001', '经典T恤', 1, '夏季', '["白色", "黑色", "灰色"]', '["S", "M", "L", "XL"]', '{"S": 20, "M": 30, "L": 30, "XL": 20}'),
(1, 'ST002', '休闲裤', 2, '全季', '["黑色", "深蓝", "卡其"]', '["28", "30", "32", "34", "36"]', '{"28": 15, "30": 25, "32": 30, "34": 20, "36": 10}');

# 创建订阅记录表
CREATE TABLE IF NOT EXISTS `subscriptions` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint(20) NOT NULL COMMENT '租户ID',
  `plan_type` enum('trial','basic','standard','premium') NOT NULL COMMENT '套餐类型',
  `duration` int(11) NOT NULL COMMENT '订阅时长（月）',
  `original_amount` decimal(10,2) DEFAULT 0.00 COMMENT '原价',
  `discount_amount` decimal(10,2) DEFAULT 0.00 COMMENT '折扣金额',
  `final_amount` decimal(10,2) NOT NULL COMMENT '最终金额',
  `payment_method` varchar(20) DEFAULT NULL COMMENT '支付方式',
  `payment_status` enum('pending','paid','cancelled','refunded') DEFAULT 'pending' COMMENT '支付状态',
  `payment_order_no` varchar(50) UNIQUE NOT NULL COMMENT '支付订单号',
  `coupon_code` varchar(50) DEFAULT NULL COMMENT '优惠券代码',
  `status` enum('pending','active','expired','cancelled') DEFAULT 'pending' COMMENT '订阅状态',
  `start_date` datetime DEFAULT NULL COMMENT '开始日期',
  `end_date` datetime DEFAULT NULL COMMENT '结束日期',
  `paid_at` datetime DEFAULT NULL COMMENT '支付时间',
  `remark` varchar(200) DEFAULT NULL COMMENT '备注',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint(1) DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_payment_order_no` (`payment_order_no`),
  KEY `idx_tenant_id` (`tenant_id`),
  KEY `idx_status` (`status`),
  KEY `idx_plan_type` (`plan_type`),
  KEY `idx_end_date` (`end_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订阅记录表';

-- 应用用户与最小权限授权（确保 Flyway 可读取 performance_schema）
-- 注意：此段在容器首次初始化时执行；已存在用户将通过 IF NOT EXISTS/ALTER USER 保持幂等
CREATE USER IF NOT EXISTS 'garment_user'@'%' IDENTIFIED BY 'garment_pass_2024';
CREATE USER IF NOT EXISTS 'garment_user'@'localhost' IDENTIFIED BY 'garment_pass_2024';

-- 兼容部分驱动对 mysql_native_password 的要求
ALTER USER 'garment_user'@'%' IDENTIFIED WITH mysql_native_password BY 'garment_pass_2024';
ALTER USER 'garment_user'@'localhost' IDENTIFIED WITH mysql_native_password BY 'garment_pass_2024';

-- 业务库权限
GRANT ALL PRIVILEGES ON garment_production.* TO 'garment_user'@'%';
GRANT ALL PRIVILEGES ON garment_production.* TO 'garment_user'@'localhost';

-- 只读权限：供 Flyway 等读取 performance_schema
GRANT SELECT ON performance_schema.* TO 'garment_user'@'%';
GRANT SELECT ON performance_schema.* TO 'garment_user'@'localhost';

FLUSH PRIVILEGES;