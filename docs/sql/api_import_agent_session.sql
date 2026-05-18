-- API Import Agent 会话表
-- 说明：
-- 1. api_import_agent_session 保存当前用户导入会话主状态、当前计划快照和确认门禁信息。
-- 2. Planner 输出仅作为草稿计划快照保存在本表，不直接写入 API Catalog 主数据。
-- 3. 会话是 owner-scoped 资源，任意读取或执行都必须以 owner_user_id 做访问控制。

CREATE TABLE IF NOT EXISTS api_import_agent_session (
    id                     VARCHAR(36) PRIMARY KEY COMMENT '导入会话主键 UUID',
    owner_user_id          VARCHAR(64) NOT NULL COMMENT '会话所有者用户 ID',
    status                 VARCHAR(32) NOT NULL COMMENT '会话状态：WAITING_FOR_PLAN / WAITING_FOR_CONFIRMATION / WAITING_FOR_CLARIFICATION / CONFIRMED / EXECUTING / COMPLETED / FAILED',
    document_source        VARCHAR(1024) NULL COMMENT '导入文档来源，例如 URL、文件标识或来源说明',
    document_summary       TEXT NULL COMMENT '用户提供的文档摘要或预解析内容',
    import_intent          TEXT NOT NULL COMMENT '用户导入意图、补充约束或目标说明',
    publisher_display_name VARCHAR(128) NULL COMMENT '执行导入时使用的发布者展示名快照',
    current_plan_version   INT NULL COMMENT '当前最新计划版本号',
    confirmed_plan_version INT NULL COMMENT '最近一次显式确认的计划版本号',
    plan_snapshot_json     LONGTEXT NULL COMMENT '当前结构化导入计划快照 JSON',
    latest_run_id          VARCHAR(36) NULL COMMENT '最近一次执行批次 ID，可为空',
    latest_confirmed_at    TIMESTAMP NULL COMMENT '最近一次计划确认时间',
    created_at             TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at             TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
    version                BIGINT NOT NULL DEFAULT 0 COMMENT '乐观锁版本号'
);

COMMENT ON TABLE api_import_agent_session IS 'API Import Agent 会话主表';
COMMENT ON COLUMN api_import_agent_session.id IS '导入会话主键 UUID';
COMMENT ON COLUMN api_import_agent_session.owner_user_id IS '会话所有者用户 ID';
COMMENT ON COLUMN api_import_agent_session.status IS '会话状态：WAITING_FOR_PLAN / WAITING_FOR_CONFIRMATION / WAITING_FOR_CLARIFICATION / CONFIRMED / EXECUTING / COMPLETED / FAILED';
COMMENT ON COLUMN api_import_agent_session.document_source IS '导入文档来源，例如 URL、文件标识或来源说明';
COMMENT ON COLUMN api_import_agent_session.document_summary IS '用户提供的文档摘要或预解析内容';
COMMENT ON COLUMN api_import_agent_session.import_intent IS '用户导入意图、补充约束或目标说明';
COMMENT ON COLUMN api_import_agent_session.publisher_display_name IS '执行导入时使用的发布者展示名快照';
COMMENT ON COLUMN api_import_agent_session.current_plan_version IS '当前最新计划版本号';
COMMENT ON COLUMN api_import_agent_session.confirmed_plan_version IS '最近一次显式确认的计划版本号';
COMMENT ON COLUMN api_import_agent_session.plan_snapshot_json IS '当前结构化导入计划快照 JSON';
COMMENT ON COLUMN api_import_agent_session.latest_run_id IS '最近一次执行批次 ID，可为空';
COMMENT ON COLUMN api_import_agent_session.latest_confirmed_at IS '最近一次计划确认时间';
COMMENT ON COLUMN api_import_agent_session.created_at IS '创建时间';
COMMENT ON COLUMN api_import_agent_session.updated_at IS '更新时间';
COMMENT ON COLUMN api_import_agent_session.version IS '乐观锁版本号';

CREATE INDEX IF NOT EXISTS idx_import_agent_session_owner ON api_import_agent_session(owner_user_id);
CREATE INDEX IF NOT EXISTS idx_import_agent_session_status ON api_import_agent_session(status);
CREATE INDEX IF NOT EXISTS idx_import_agent_session_latest_run_id ON api_import_agent_session(latest_run_id);