-- =====================================================
-- blog-base-services schema
-- Database: blog_base (utf8mb4)
-- =====================================================

CREATE DATABASE IF NOT EXISTS `blog_base`
    DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;

USE `blog_base`;

-- -----------------------------------------------------
-- 1. tags
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `base_tag`
(
    `id`          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
    `name`        VARCHAR(32)     NOT NULL COMMENT '标签名',
    `status`      TINYINT         NOT NULL DEFAULT 1 COMMENT '状态：1启用 0禁用',
    `sort`        INT             NOT NULL DEFAULT 0 COMMENT '排序，越小越前',
    `remark`      VARCHAR(255)             DEFAULT NULL COMMENT '备注',
    `create_time` DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`     TINYINT         NOT NULL DEFAULT 0 COMMENT '逻辑删除：0未删 1已删',
    PRIMARY KEY (`id`),
    KEY `idx_status_sort` (`status`, `sort`),
    KEY `idx_name` (`name`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='标签';

-- -----------------------------------------------------
-- 2. notifications (in-site notifications)
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `base_notification`
(
    `id`          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
    `user_id`     BIGINT UNSIGNED NOT NULL COMMENT '接收人用户ID（博客用户体系）',
    `title`       VARCHAR(128)    NOT NULL COMMENT '标题',
    `content`     VARCHAR(1024)   NOT NULL COMMENT '内容',
    `type`        TINYINT         NOT NULL DEFAULT 1 COMMENT '类型：1系统通知 2评论 3点赞',
    `is_read`     TINYINT         NOT NULL DEFAULT 0 COMMENT '是否已读：0未读 1已读',
    `read_time`   DATETIME                 DEFAULT NULL COMMENT '阅读时间',
    `create_time` DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`     TINYINT         NOT NULL DEFAULT 0 COMMENT '逻辑删除：0未删 1已删',
    PRIMARY KEY (`id`),
    KEY `idx_user_read` (`user_id`, `is_read`),
    KEY `idx_user_created` (`user_id`, `create_time`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='站内通知';

-- -----------------------------------------------------
-- 3. message board (visitor messages)
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `base_message_board`
(
    `id`          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
    `parent_id`   BIGINT UNSIGNED          DEFAULT NULL COMMENT '回复的根留言ID，NULL=顶层留言',
    `nickname`    VARCHAR(32)     NOT NULL COMMENT '访客昵称',
    `email`       VARCHAR(64)              DEFAULT NULL COMMENT '访客邮箱',
    `content`     VARCHAR(1000)   NOT NULL COMMENT '留言内容',
    `ip`          VARCHAR(45)     NOT NULL COMMENT '留言IP（IPv4/IPv6）',
    `status`      TINYINT         NOT NULL DEFAULT 0 COMMENT '审核状态：0待审 1通过 2驳回',
    `is_hidden`   TINYINT         NOT NULL DEFAULT 0 COMMENT '是否隐藏：1隐藏',
    `is_admin`    TINYINT         NOT NULL DEFAULT 0 COMMENT '是否管理员回复：1是',
    `create_time` DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`     TINYINT         NOT NULL DEFAULT 0 COMMENT '逻辑删除：0未删 1已删',
    PRIMARY KEY (`id`),
    KEY `idx_parent` (`parent_id`),
    KEY `idx_status_created` (`status`, `create_time`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='访客留言板';

-- -----------------------------------------------------
-- 4. regions (China administrative divisions, 3 levels)
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `base_region`
(
    `id`          BIGINT UNSIGNED NOT NULL COMMENT '主键（种子数据显式赋值 1..N）',
    `parent_id`   BIGINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '父级ID，0=省级根',
    `code`        VARCHAR(12)    NOT NULL COMMENT '行政区划码（GB/T 2260；直筒子市下辖区县为9位扩展码）',
    `name`        VARCHAR(64)     NOT NULL COMMENT '名称',
    `level`       TINYINT         NOT NULL COMMENT '层级：1省 2市 3区县',
    `sort`        INT             NOT NULL DEFAULT 0 COMMENT '排序（按数据源顺序）',
    `create_time` DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`     TINYINT         NOT NULL DEFAULT 0 COMMENT '逻辑删除：0未删 1已删',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_code` (`code`),
    KEY `idx_parent` (`parent_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='中国行政区划（省/市/区县）';
