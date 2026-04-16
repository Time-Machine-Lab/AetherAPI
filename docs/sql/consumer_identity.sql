-- =============================================================
-- Consumer & Auth 内部调用主体 Consumer 主数据表
-- 用于维护内部 Consumer 的唯一标识、类型与可用状态
-- docs/sql 仅保存 DDL 与表结构设计，不包含查询、插入、更新等实现 SQL
-- =============================================================

CREATE TABLE IF NOT EXISTS consumer_identity (
    id                    VARCHAR(36)  PRIMARY KEY COMMENT 'Consumer 实体唯一标识(UUID)',
    consumer_code         VARCHAR(64)  NOT NULL COMMENT 'Consumer 业务编码，创建后不可变更且全局唯一',
    consumer_name         VARCHAR(128) NOT NULL COMMENT 'Consumer 展示名称，用于控制台与日志展示快照',
    consumer_type         VARCHAR(32)  NOT NULL DEFAULT 'USER_ACCOUNT'
                                      CHECK (consumer_type IN ('USER_ACCOUNT')) COMMENT 'Consumer 类型，一期固定为 USER_ACCOUNT',
    status                VARCHAR(20)  NOT NULL DEFAULT 'ENABLED'
                                      CHECK (status IN ('ENABLED', 'DISABLED')) COMMENT 'Consumer 状态：ENABLED-可用，DISABLED-停用',
    created_at            TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '记录创建时间',
    updated_at            TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '记录最近更新时间',
    is_deleted            BOOLEAN      NOT NULL DEFAULT FALSE COMMENT '软删除标记',
    version               BIGINT       NOT NULL DEFAULT 0 COMMENT '乐观锁版本号'
);

COMMENT ON TABLE consumer_identity IS '内部调用主体 Consumer 主数据表';

COMMENT ON COLUMN consumer_identity.id            IS 'Consumer 实体唯一标识(UUID)';
COMMENT ON COLUMN consumer_identity.consumer_code IS 'Consumer 业务编码，创建后不可变更且全局唯一';
COMMENT ON COLUMN consumer_identity.consumer_name IS 'Consumer 展示名称，用于控制台与日志展示快照';
COMMENT ON COLUMN consumer_identity.consumer_type IS 'Consumer 类型，一期固定为 USER_ACCOUNT';
COMMENT ON COLUMN consumer_identity.status        IS 'Consumer 状态：ENABLED-可用，DISABLED-停用';
COMMENT ON COLUMN consumer_identity.created_at    IS '记录创建时间';
COMMENT ON COLUMN consumer_identity.updated_at    IS '记录最近更新时间';
COMMENT ON COLUMN consumer_identity.is_deleted    IS '软删除标记';
COMMENT ON COLUMN consumer_identity.version       IS '乐观锁版本号';

CREATE UNIQUE INDEX IF NOT EXISTS uk_consumer_identity_code ON consumer_identity(consumer_code);
CREATE INDEX IF NOT EXISTS idx_consumer_identity_status ON consumer_identity(status);
CREATE INDEX IF NOT EXISTS idx_consumer_identity_deleted ON consumer_identity(is_deleted);
