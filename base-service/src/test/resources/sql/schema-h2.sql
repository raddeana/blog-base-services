-- H2 (MODE=MySQL) compatible schema for integration tests.
-- Mirrors db/init/01_schema.sql, minus MySQL-only syntax (ENGINE / inline KEY /
-- BIGINT UNSIGNED / ON UPDATE). update_time is filled by MetaObjectHandler.

CREATE TABLE base_tag
(
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    name        VARCHAR(32)  NOT NULL,
    status      TINYINT      NOT NULL DEFAULT 1,
    sort        INT          NOT NULL DEFAULT 0,
    remark      VARCHAR(255),
    create_time TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted     TINYINT      NOT NULL DEFAULT 0
);
CREATE INDEX idx_tag_status_sort ON base_tag (status, sort);
CREATE INDEX idx_tag_name ON base_tag (name);

CREATE TABLE base_notification
(
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id     BIGINT       NOT NULL,
    title       VARCHAR(128) NOT NULL,
    content     VARCHAR(1024) NOT NULL,
    type        TINYINT      NOT NULL DEFAULT 1,
    is_read     TINYINT      NOT NULL DEFAULT 0,
    read_time   TIMESTAMP,
    create_time TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted     TINYINT      NOT NULL DEFAULT 0
);
CREATE INDEX idx_notification_user_read ON base_notification (user_id, is_read);
CREATE INDEX idx_notification_user_created ON base_notification (user_id, create_time);

CREATE TABLE base_message_board
(
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    parent_id   BIGINT,
    nickname    VARCHAR(32)  NOT NULL,
    email       VARCHAR(64),
    content     VARCHAR(1000) NOT NULL,
    ip          VARCHAR(45)  NOT NULL,
    status      TINYINT      NOT NULL DEFAULT 0,
    is_hidden   TINYINT      NOT NULL DEFAULT 0,
    is_admin    TINYINT      NOT NULL DEFAULT 0,
    create_time TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted     TINYINT      NOT NULL DEFAULT 0
);
CREATE INDEX idx_message_parent ON base_message_board (parent_id);
CREATE INDEX idx_message_status_created ON base_message_board (status, create_time);

CREATE TABLE base_region
(
    id          BIGINT PRIMARY KEY,
    parent_id   BIGINT       NOT NULL DEFAULT 0,
    code        VARCHAR(12)  NOT NULL,
    name        VARCHAR(64)  NOT NULL,
    level       TINYINT      NOT NULL,
    sort        INT          NOT NULL DEFAULT 0,
    create_time TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted     TINYINT      NOT NULL DEFAULT 0
);
CREATE UNIQUE INDEX uk_region_code ON base_region (code);
CREATE INDEX idx_region_parent ON base_region (parent_id);
