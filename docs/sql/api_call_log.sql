-- =============================================================
-- Observability 调用日志事实表
-- 用于沉淀 Unified Access 每次完成调用后的最小平台调用事实
-- docs/sql 仅保留 DDL 与表结构设计，不包含查询、插入、更新等实现 SQL
-- =============================================================

CREATE TABLE IF NOT EXISTS api_call_log (
    id                    VARCHAR(36)  PRIMARY KEY COMMENT '调用日志唯一标识(UUID)',
    consumer_id           VARCHAR(36)  NULL COMMENT '调用主体 Consumer 实体 ID 快照，未解析主体时允许为空',
    consumer_code         VARCHAR(64)  NULL COMMENT '调用主体 Consumer 业务编码快照，未解析主体时允许为空',
    consumer_name         VARCHAR(128) NULL COMMENT '调用主体显示名称快照',
    consumer_type         VARCHAR(32)  NULL COMMENT '调用主体类型快照，例如 USER_ACCOUNT',
    credential_id         VARCHAR(36)  NULL COMMENT '本次调用使用的 API 凭证 ID 快照',
    credential_code       VARCHAR(64)  NULL COMMENT '本次调用使用的 API 凭证业务编码快照',
    credential_status     VARCHAR(20)  NULL COMMENT '本次调用时的凭证状态快照',
    access_channel        VARCHAR(64)  NOT NULL COMMENT '接入通道快照，例如 UNIFIED_ACCESS',
    target_api_id         VARCHAR(36)  NULL COMMENT '目标 API 资源实体 ID 快照，目标未解析时允许为空',
    target_api_code       VARCHAR(64)  NULL COMMENT '目标 API 业务编码快照，允许记录请求中的原始 API code',
    target_api_name       VARCHAR(128) NULL COMMENT '目标 API 名称快照',
    target_api_type       VARCHAR(32)  NULL COMMENT '目标 API 类型快照，例如 STANDARD_API 或 AI_API',
    request_method        VARCHAR(16)  NOT NULL COMMENT '本次调用的 HTTP 请求方法快照',
    invocation_time       TIMESTAMP    NOT NULL COMMENT '平台判定本次调用开始的时间点',
    duration_ms           BIGINT       NOT NULL COMMENT '本次调用耗时(毫秒)',
    result_type           VARCHAR(32)  NOT NULL COMMENT '统一调用结果分类，例如 SUCCESS、UPSTREAM_FAILURE、INVALID_CREDENTIAL',
    success               BOOLEAN      NOT NULL COMMENT '是否为成功调用',
    http_status_code      INT          NULL COMMENT '本次调用返回或映射的 HTTP 状态码',
    error_code            VARCHAR(64)  NULL COMMENT '失败场景的稳定错误码快照',
    error_type            VARCHAR(64)  NULL COMMENT '失败场景的错误类型快照',
    error_summary         VARCHAR(512) NULL COMMENT '失败场景的错误摘要，禁止持久化完整原始请求或响应体',
    ai_provider           VARCHAR(64)  NULL COMMENT 'AI 调用扩展字段：模型供应商快照',
    ai_model              VARCHAR(128) NULL COMMENT 'AI 调用扩展字段：模型标识快照',
    ai_streaming          BOOLEAN      NULL COMMENT 'AI 调用扩展字段：是否支持或使用流式输出',
    ai_usage_snapshot     VARCHAR(512) NULL COMMENT 'AI 调用扩展字段：轻量用量摘要预留位',
    ai_billing_reserved   VARCHAR(512) NULL COMMENT 'AI 调用扩展字段：计费与成本预留摘要位',
    created_at            TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '记录创建时间',
    updated_at            TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '记录最近更新时间',
    is_deleted            BOOLEAN      NOT NULL DEFAULT FALSE COMMENT '软删除标记',
    version               BIGINT       NOT NULL DEFAULT 0 COMMENT '乐观锁版本号'
);

COMMENT ON TABLE api_call_log IS 'Observability 调用日志事实表';

COMMENT ON COLUMN api_call_log.id                  IS '调用日志唯一标识(UUID)';
COMMENT ON COLUMN api_call_log.consumer_id         IS '调用主体 Consumer 实体 ID 快照，未解析主体时允许为空';
COMMENT ON COLUMN api_call_log.consumer_code       IS '调用主体 Consumer 业务编码快照，未解析主体时允许为空';
COMMENT ON COLUMN api_call_log.consumer_name       IS '调用主体显示名称快照';
COMMENT ON COLUMN api_call_log.consumer_type       IS '调用主体类型快照，例如 USER_ACCOUNT';
COMMENT ON COLUMN api_call_log.credential_id       IS '本次调用使用的 API 凭证 ID 快照';
COMMENT ON COLUMN api_call_log.credential_code     IS '本次调用使用的 API 凭证业务编码快照';
COMMENT ON COLUMN api_call_log.credential_status   IS '本次调用时的凭证状态快照';
COMMENT ON COLUMN api_call_log.access_channel      IS '接入通道快照，例如 UNIFIED_ACCESS';
COMMENT ON COLUMN api_call_log.target_api_id       IS '目标 API 资源实体 ID 快照，目标未解析时允许为空';
COMMENT ON COLUMN api_call_log.target_api_code     IS '目标 API 业务编码快照，允许记录请求中的原始 API code';
COMMENT ON COLUMN api_call_log.target_api_name     IS '目标 API 名称快照';
COMMENT ON COLUMN api_call_log.target_api_type     IS '目标 API 类型快照，例如 STANDARD_API 或 AI_API';
COMMENT ON COLUMN api_call_log.request_method      IS '本次调用的 HTTP 请求方法快照';
COMMENT ON COLUMN api_call_log.invocation_time     IS '平台判定本次调用开始的时间点';
COMMENT ON COLUMN api_call_log.duration_ms         IS '本次调用耗时(毫秒)';
COMMENT ON COLUMN api_call_log.result_type         IS '统一调用结果分类，例如 SUCCESS、UPSTREAM_FAILURE、INVALID_CREDENTIAL';
COMMENT ON COLUMN api_call_log.success             IS '是否为成功调用';
COMMENT ON COLUMN api_call_log.http_status_code    IS '本次调用返回或映射的 HTTP 状态码';
COMMENT ON COLUMN api_call_log.error_code          IS '失败场景的稳定错误码快照';
COMMENT ON COLUMN api_call_log.error_type          IS '失败场景的错误类型快照';
COMMENT ON COLUMN api_call_log.error_summary       IS '失败场景的错误摘要，禁止持久化完整原始请求或响应体';
COMMENT ON COLUMN api_call_log.ai_provider         IS 'AI 调用扩展字段：模型供应商快照';
COMMENT ON COLUMN api_call_log.ai_model            IS 'AI 调用扩展字段：模型标识快照';
COMMENT ON COLUMN api_call_log.ai_streaming        IS 'AI 调用扩展字段：是否支持或使用流式输出';
COMMENT ON COLUMN api_call_log.ai_usage_snapshot   IS 'AI 调用扩展字段：轻量用量摘要预留位';
COMMENT ON COLUMN api_call_log.ai_billing_reserved IS 'AI 调用扩展字段：计费与成本预留摘要位';
COMMENT ON COLUMN api_call_log.created_at          IS '记录创建时间';
COMMENT ON COLUMN api_call_log.updated_at          IS '记录最近更新时间';
COMMENT ON COLUMN api_call_log.is_deleted          IS '软删除标记';
COMMENT ON COLUMN api_call_log.version             IS '乐观锁版本号';

CREATE INDEX IF NOT EXISTS idx_api_call_log_consumer_id ON api_call_log(consumer_id);
CREATE INDEX IF NOT EXISTS idx_api_call_log_consumer_code ON api_call_log(consumer_code);
CREATE INDEX IF NOT EXISTS idx_api_call_log_target_api_code ON api_call_log(target_api_code);
CREATE INDEX IF NOT EXISTS idx_api_call_log_invocation_time ON api_call_log(invocation_time);
CREATE INDEX IF NOT EXISTS idx_api_call_log_result_type ON api_call_log(result_type);
CREATE INDEX IF NOT EXISTS idx_api_call_log_success ON api_call_log(success);
CREATE INDEX IF NOT EXISTS idx_api_call_log_access_channel ON api_call_log(access_channel);
CREATE INDEX IF NOT EXISTS idx_api_call_log_deleted ON api_call_log(is_deleted);
