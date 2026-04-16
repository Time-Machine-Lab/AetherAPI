-- =============================================================
-- Consumer & Auth 当前用户 API Key 凭证生命周期表
-- 用于支撑一个内部 Consumer 持有多个 API Key 的生命周期治理
-- docs/sql 仅保存 DDL 与表结构设计，不包含查询、插入、更新等实现 SQL
-- =============================================================

CREATE TABLE IF NOT EXISTS api_credential (
    id                    VARCHAR(36)  PRIMARY KEY COMMENT 'API Key 凭证唯一标识(UUID)',
    credential_code       VARCHAR(64)  NOT NULL COMMENT '凭证业务编码，创建后不可变更且全局唯一',
    consumer_id           VARCHAR(36)  NOT NULL COMMENT '所属 Consumer 实体 ID',
    consumer_code         VARCHAR(64)  NOT NULL COMMENT '所属 Consumer 业务编码快照',
    credential_name       VARCHAR(128) NOT NULL COMMENT '凭证展示名称，用于当前用户管理多个 API Key',
    credential_description VARCHAR(512) NULL COMMENT '凭证用途说明，可为空',
    key_prefix            VARCHAR(16)  NOT NULL COMMENT '明文 API Key 前缀快照，用于控制台识别与问题排查',
    masked_key            VARCHAR(32)  NOT NULL COMMENT '掩码后的 API Key，用于后续查询展示，例如 ak_live_****ABCD',
    fingerprint_hash      VARCHAR(128) NOT NULL COMMENT '明文 API Key 的不可逆安全指纹，用于鉴权匹配，系统不持久化完整明文',
    status                VARCHAR(20)  NOT NULL DEFAULT 'ENABLED'
                                      CHECK (status IN ('ENABLED', 'DISABLED', 'REVOKED')) COMMENT '凭证生命周期状态：ENABLED-启用，DISABLED-停用，REVOKED-已吊销',
    expire_at             TIMESTAMP    NULL COMMENT '凭证过期时间，为空表示永不过期',
    last_used_at          TIMESTAMP    NULL COMMENT '最近一次被系统识别使用的时间',
    last_used_channel     VARCHAR(64)  NULL COMMENT '最近一次使用来源通道快照，例如 UNIFIED_ACCESS',
    last_used_result      VARCHAR(32)  NULL COMMENT '最近一次使用结果快照，例如 SUCCESS 或 REJECTED',
    revoked_at            TIMESTAMP    NULL COMMENT '吊销时间，已吊销凭证不可恢复',
    created_at            TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '记录创建时间，也可视为签发时间',
    updated_at            TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '记录最近更新时间',
    is_deleted            BOOLEAN      NOT NULL DEFAULT FALSE COMMENT '软删除标记',
    version               BIGINT       NOT NULL DEFAULT 0 COMMENT '乐观锁版本号'
);

COMMENT ON TABLE api_credential IS '当前用户 API Key 凭证生命周期表';

COMMENT ON COLUMN api_credential.id                     IS 'API Key 凭证唯一标识(UUID)';
COMMENT ON COLUMN api_credential.credential_code        IS '凭证业务编码，创建后不可变更且全局唯一';
COMMENT ON COLUMN api_credential.consumer_id            IS '所属 Consumer 实体 ID';
COMMENT ON COLUMN api_credential.consumer_code          IS '所属 Consumer 业务编码快照';
COMMENT ON COLUMN api_credential.credential_name        IS '凭证展示名称，用于当前用户管理多个 API Key';
COMMENT ON COLUMN api_credential.credential_description IS '凭证用途说明，可为空';
COMMENT ON COLUMN api_credential.key_prefix             IS '明文 API Key 前缀快照，用于控制台识别与问题排查';
COMMENT ON COLUMN api_credential.masked_key             IS '掩码后的 API Key，用于后续查询展示，例如 ak_live_****ABCD';
COMMENT ON COLUMN api_credential.fingerprint_hash       IS '明文 API Key 的不可逆安全指纹，用于鉴权匹配，系统不持久化完整明文';
COMMENT ON COLUMN api_credential.status                 IS '凭证生命周期状态：ENABLED-启用，DISABLED-停用，REVOKED-已吊销';
COMMENT ON COLUMN api_credential.expire_at              IS '凭证过期时间，为空表示永不过期';
COMMENT ON COLUMN api_credential.last_used_at           IS '最近一次被系统识别使用的时间';
COMMENT ON COLUMN api_credential.last_used_channel      IS '最近一次使用来源通道快照，例如 UNIFIED_ACCESS';
COMMENT ON COLUMN api_credential.last_used_result       IS '最近一次使用结果快照，例如 SUCCESS 或 REJECTED';
COMMENT ON COLUMN api_credential.revoked_at             IS '吊销时间，已吊销凭证不可恢复';
COMMENT ON COLUMN api_credential.created_at             IS '记录创建时间，也可视为签发时间';
COMMENT ON COLUMN api_credential.updated_at             IS '记录最近更新时间';
COMMENT ON COLUMN api_credential.is_deleted             IS '软删除标记';
COMMENT ON COLUMN api_credential.version                IS '乐观锁版本号';

CREATE UNIQUE INDEX IF NOT EXISTS uk_api_credential_code ON api_credential(credential_code);
CREATE UNIQUE INDEX IF NOT EXISTS uk_api_credential_fingerprint_hash ON api_credential(fingerprint_hash);
CREATE INDEX IF NOT EXISTS idx_api_credential_consumer_id ON api_credential(consumer_id);
CREATE INDEX IF NOT EXISTS idx_api_credential_consumer_code ON api_credential(consumer_code);
CREATE INDEX IF NOT EXISTS idx_api_credential_status ON api_credential(status);
CREATE INDEX IF NOT EXISTS idx_api_credential_expire_at ON api_credential(expire_at);
CREATE INDEX IF NOT EXISTS idx_api_credential_last_used_at ON api_credential(last_used_at);
CREATE INDEX IF NOT EXISTS idx_api_credential_deleted ON api_credential(is_deleted);
