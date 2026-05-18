-- API Import Agent 执行批次表
-- 说明：
-- 1. api_import_agent_run 保存显式确认后触发的导入执行批次状态与步骤结果。
-- 2. Executor 只能复用既有分类和资产应用服务完成真实写操作，本表仅记录编排投影和审计结果。
-- 3. step_results_json 必须保留已完成步骤，即使后续步骤失败也不能把整批执行折叠成不可解释的 opaque error。

CREATE TABLE IF NOT EXISTS api_import_agent_run (
    id                    VARCHAR(36) PRIMARY KEY COMMENT '执行批次主键 UUID',
    session_id            VARCHAR(36) NOT NULL COMMENT '所属导入会话 ID',
    owner_user_id         VARCHAR(64) NOT NULL COMMENT '执行批次所有者用户 ID',
    plan_version          INT NOT NULL COMMENT '本次执行对应的已确认计划版本号',
    status                VARCHAR(32) NOT NULL COMMENT '执行状态：RUNNING / SUCCEEDED / PARTIALLY_FAILED / FAILED',
    summary               TEXT NULL COMMENT '执行结果摘要',
    failure_reason        TEXT NULL COMMENT '执行失败或部分失败原因',
    affected_api_codes    LONGTEXT NULL COMMENT '受影响 API Code 列表 JSON 数组',
    step_results_json     LONGTEXT NULL COMMENT '逐步骤执行结果 JSON 数组',
    created_at            TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at            TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
    version               BIGINT NOT NULL DEFAULT 0 COMMENT '乐观锁版本号'
);

COMMENT ON TABLE api_import_agent_run IS 'API Import Agent 执行批次表';
COMMENT ON COLUMN api_import_agent_run.id IS '执行批次主键 UUID';
COMMENT ON COLUMN api_import_agent_run.session_id IS '所属导入会话 ID';
COMMENT ON COLUMN api_import_agent_run.owner_user_id IS '执行批次所有者用户 ID';
COMMENT ON COLUMN api_import_agent_run.plan_version IS '本次执行对应的已确认计划版本号';
COMMENT ON COLUMN api_import_agent_run.status IS '执行状态：RUNNING / SUCCEEDED / PARTIALLY_FAILED / FAILED';
COMMENT ON COLUMN api_import_agent_run.summary IS '执行结果摘要';
COMMENT ON COLUMN api_import_agent_run.failure_reason IS '执行失败或部分失败原因';
COMMENT ON COLUMN api_import_agent_run.affected_api_codes IS '受影响 API Code 列表 JSON 数组';
COMMENT ON COLUMN api_import_agent_run.step_results_json IS '逐步骤执行结果 JSON 数组';
COMMENT ON COLUMN api_import_agent_run.created_at IS '创建时间';
COMMENT ON COLUMN api_import_agent_run.updated_at IS '更新时间';
COMMENT ON COLUMN api_import_agent_run.version IS '乐观锁版本号';

CREATE INDEX IF NOT EXISTS idx_import_agent_run_session_id ON api_import_agent_run(session_id);
CREATE INDEX IF NOT EXISTS idx_import_agent_run_owner_user_id ON api_import_agent_run(owner_user_id);
CREATE INDEX IF NOT EXISTS idx_import_agent_run_status ON api_import_agent_run(status);