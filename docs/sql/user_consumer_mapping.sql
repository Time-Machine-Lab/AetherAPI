-- =============================================================
-- Consumer & Auth 平台用户与内部 Consumer 的一对一隐式映射表
-- 用于支撑“当前用户 -> 内部 Consumer”的稳定绑定关系
-- docs/sql 仅保存 DDL 与表结构设计，不包含查询、插入、更新等实现 SQL
-- =============================================================

CREATE TABLE IF NOT EXISTS user_consumer_mapping (
    id                    VARCHAR(36)  PRIMARY KEY COMMENT '用户与 Consumer 映射记录唯一标识(UUID)',
    user_id               VARCHAR(64)  NOT NULL COMMENT '平台登录用户唯一标识，由账号体系提供',
    consumer_id           VARCHAR(36)  NOT NULL COMMENT '关联的 Consumer 实体 ID',
    consumer_code         VARCHAR(64)  NOT NULL COMMENT '关联的 Consumer 业务编码快照',
    mapping_status        VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE'
                                      CHECK (mapping_status IN ('ACTIVE', 'INACTIVE')) COMMENT '映射状态：ACTIVE-有效映射，INACTIVE-失效映射',
    created_at            TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '记录创建时间',
    updated_at            TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '记录最近更新时间',
    is_deleted            BOOLEAN      NOT NULL DEFAULT FALSE COMMENT '软删除标记',
    version               BIGINT       NOT NULL DEFAULT 0 COMMENT '乐观锁版本号'
);

COMMENT ON TABLE user_consumer_mapping IS '平台用户与内部 Consumer 的一对一隐式映射表';

COMMENT ON COLUMN user_consumer_mapping.id             IS '用户与 Consumer 映射记录唯一标识(UUID)';
COMMENT ON COLUMN user_consumer_mapping.user_id        IS '平台登录用户唯一标识，由账号体系提供';
COMMENT ON COLUMN user_consumer_mapping.consumer_id    IS '关联的 Consumer 实体 ID';
COMMENT ON COLUMN user_consumer_mapping.consumer_code  IS '关联的 Consumer 业务编码快照';
COMMENT ON COLUMN user_consumer_mapping.mapping_status IS '映射状态：ACTIVE-有效映射，INACTIVE-失效映射';
COMMENT ON COLUMN user_consumer_mapping.created_at     IS '记录创建时间';
COMMENT ON COLUMN user_consumer_mapping.updated_at     IS '记录最近更新时间';
COMMENT ON COLUMN user_consumer_mapping.is_deleted     IS '软删除标记';
COMMENT ON COLUMN user_consumer_mapping.version        IS '乐观锁版本号';

CREATE UNIQUE INDEX IF NOT EXISTS uk_user_consumer_mapping_user_id ON user_consumer_mapping(user_id);
CREATE UNIQUE INDEX IF NOT EXISTS uk_user_consumer_mapping_consumer_id ON user_consumer_mapping(consumer_id);
CREATE INDEX IF NOT EXISTS idx_user_consumer_mapping_status ON user_consumer_mapping(mapping_status);
CREATE INDEX IF NOT EXISTS idx_user_consumer_mapping_deleted ON user_consumer_mapping(is_deleted);
