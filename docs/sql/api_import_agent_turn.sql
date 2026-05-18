-- API Import Agent 会话轮次表
-- 说明：
-- 1. api_import_agent_turn 保存用户与 Agent 的多轮对话历史，用于审计和会话重建。
-- 2. 每个用户追加输入后，系统至少应落一条 USER 轮次，并在 Planner 返回后落一条 AGENT 轮次。
-- 3. turn_index 在单个 session 内单调递增，用于稳定展示顺序。

CREATE TABLE IF NOT EXISTS api_import_agent_turn (
    id           VARCHAR(36) PRIMARY KEY COMMENT '轮次主键 UUID',
    session_id   VARCHAR(36) NOT NULL COMMENT '所属导入会话 ID',
    turn_index   INT NOT NULL COMMENT '会话内递增轮次序号，从 1 开始',
    actor_type   VARCHAR(16) NOT NULL COMMENT '轮次发起方：USER / AGENT',
    message_text LONGTEXT NOT NULL COMMENT '轮次消息正文',
    plan_version INT NULL COMMENT '该轮次关联的计划版本号，可为空',
    created_at   TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'
);

COMMENT ON TABLE api_import_agent_turn IS 'API Import Agent 会话轮次表';
COMMENT ON COLUMN api_import_agent_turn.id IS '轮次主键 UUID';
COMMENT ON COLUMN api_import_agent_turn.session_id IS '所属导入会话 ID';
COMMENT ON COLUMN api_import_agent_turn.turn_index IS '会话内递增轮次序号，从 1 开始';
COMMENT ON COLUMN api_import_agent_turn.actor_type IS '轮次发起方：USER / AGENT';
COMMENT ON COLUMN api_import_agent_turn.message_text IS '轮次消息正文';
COMMENT ON COLUMN api_import_agent_turn.plan_version IS '该轮次关联的计划版本号，可为空';
COMMENT ON COLUMN api_import_agent_turn.created_at IS '创建时间';

CREATE UNIQUE INDEX IF NOT EXISTS uk_import_agent_turn_session_index
    ON api_import_agent_turn(session_id, turn_index);
CREATE INDEX IF NOT EXISTS idx_import_agent_turn_session_id
    ON api_import_agent_turn(session_id);