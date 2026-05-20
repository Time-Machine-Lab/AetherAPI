## 背景

当前 Import Agent 的流式接口虽然已经支持 `status`、`message`、`session`、`error` 和 `done` 事件，但对前端来说仍然几乎是黑盒：规划阶段只能看到笼统的 planning / replying 状态，planner provider、subagent orchestrator、review / clarification downgrade 等关键过程只能在后端日志里排查，无法作为用户可见的流式反馈返回给控制台。

这带来两个问题。第一，用户在较长文档或复杂导入任务中无法判断系统正在做什么，只能等待最终回复或最终会话快照。第二，前端即使想展示“代理思考过程”，当前协议也没有结构化事件可消费，只能把所有内容混进最终回复文本。为了解决这个问题，需要把 Import Agent stream 从“纯文本增量 + 单行状态”升级为“安全、结构化的 thinking 事件流 + 最终回复流”，同时明确不暴露原始模型 CoT 或敏感推理细节。

## 变更内容

- 为 Import Agent 的流式会话接口新增结构化 `thinking` 事件，用于返回 planner / subagent / review 阶段的安全思考摘要，而不是仅返回最终助手回复 delta。
- 在后端引入统一的 import-agent stream emitter / 事件模型，使 adapter、service、planner provider、subagent orchestrator 和 reply port 能通过同一通道发出 `status`、`thinking`、`message`、`session`、`error`、`done` 等事件。
- 更新 `docs/api/api-import-agent.yaml` 中 import-agent SSE 契约，记录新的事件类型、thinking 载荷结构和“禁止暴露原始 CoT / 敏感配置”的边界。
- 保持现有 REST 会话 / 执行接口、现有 session / run 持久化模型和最终 `session` 快照事件不变；thinking 事件只增强流式可观测性，不改变会话主事实来源。
- 允许现有未升级的前端通过忽略未知 SSE 事件继续工作，保证 thinking 事件作为增量能力引入，而不是强制破坏性重构。

## 能力变更

### 已修改能力

- `api-import-agent-session`
  - import-agent 的 stream 会话接口在返回最终 reply delta 和会话快照前，必须能够返回结构化的 thinking 事件，用于表达规划阶段、审查阶段和澄清生成阶段的安全过程摘要。
  - stream thinking 事件必须显式禁止暴露原始模型 chain-of-thought、上游密钥、authConfig 明文、完整 prompt、未脱敏 provider payload 或其他敏感内部细节。

## 影响范围

- 已审阅权威文档：
  - `docs/design/aehter-api-hub/Aether API Hub架构设计文档.md`
  - `docs/api/api-import-agent.yaml`
  - `docs/sql/api_import_agent_session.sql`
  - `docs/sql/api_import_agent_turn.sql`
  - `docs/sql/api_import_agent_run.sql`
- 顶层文档影响：
  - 需要更新 `docs/api/api-import-agent.yaml`，因为 stream 事件协议将新增 `thinking` 事件及其载荷约束。
  - 不需要更新 `docs/sql/api_import_agent_*.sql`，因为本次不新增表、不改 session / run 持久化结构。
- 受影响后端模块：
  - `aether-api-hub-adapter`：扩展 SSE 事件输出模型和 delegate 写流逻辑。
  - `aether-api-hub-service`：引入统一 stream emitter，替代当前只支持 `Consumer<String>` 的 reply delta 通道。
  - `aether-api-hub-infrastructure`：planner provider、subagent orchestrator、reply port 接入 thinking 事件发射。
- 运行时影响：
  - thinking 事件会增加少量流式输出，但不引入新的会话 API，也不改变最终会话快照语义。
