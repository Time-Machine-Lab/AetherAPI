## 背景

Import Agent 目前把字段级追问当成普通聊天文本处理，再依赖 LLM 从用户的编号回答里推断这些答案应该写回哪个可执行计划字段。这个机制导致控制台里出现了糟糕体验：用户已经回答了问题，但下一轮仍然被重复追问，因为答案没有被确定性吸收到计划里。

本变更把“碰巧包含缺失字段的聊天文本”升级为“结构化、可定位、按 owner 约束的计划澄清契约”，同时保留现有 session、confirm、run 工作流。

## 变更内容

- 为 Import Agent 计划增加结构化澄清模型，使每个缺失项都有稳定 id、目标计划路径、字段 key、输入类型、可选项和展示标签。
- 保留 `clarificationQuestions` 作为兼容旧客户端的文本摘要，但新客户端优先使用 `clarificationItems`。
- 扩展 append-turn 契约，允许客户端在自由文本 `message` 之外，或不带自由文本地提交结构化 `clarificationAnswers`。
- 在调用 planner 前确定性应用结构化澄清答案，避免再让 LLM 解析编号回复。
- 增加匿名资产计划的 planner 保护：当之前的匿名资产可以通过位置或目标路径匹配时，答案必须更新该资产，而不是遗留一个空的旧资产。
- 将强化后的 staged tool-calling 路径设为已配置 Import Agent 环境的默认路径，同时保留 provider feature flag 作为紧急回滚开关。
- 与现有 `add-import-agent-thinking-stream` 变更协同：thinking 事件可以报告进度和已接受字段摘要，但最终 session 状态仍以服务端计划快照为准。
- 实现前使用 `tml-docs-spec-generate` 的 API 文档模板更新权威契约 `docs/api/api-import-agent.yaml`。
- 预计不需要数据库表变更；现有 `api_import_agent_session.plan_snapshot_json` 可以承载新增的计划字段。

## 能力

### 新增能力

- `api-import-agent-guided-clarification`：Import Agent 计划的结构化澄清项和确定性答案应用。

### 修改能力

- 暂无已归档 spec 需要修改。本变更会与进行中的 Import Agent 变更协同，但由于 `api-import-agent-session` 尚未归档到 `openspec/specs/`，因此使用独立能力描述。

## 影响范围

- 权威文档：更新 `docs/api/api-import-agent.yaml` 中的 `ImportAgentPlanResp`、`AppendImportAgentTurnReq`、SSE 示例，以及新的澄清答案/澄清项 schema。不计划更新 `docs/sql/`。
- 后端 adapter：`ApiImportAgentController` 与 `ApiImportAgentWebDelegate` 的请求/响应映射。
- 服务层：`ApiImportAgentApplicationService`、`AppendImportAgentTurnCommand`、`ImportAgentPlanModel` 和 planner 请求/结果模型。
- 基础设施 planner：`ImportAgentPlannerJsonSupport`、解析器、合并器、校验器、结构化 tool schema 默认配置和 subagent review 行为。
- 持久化：现有 JSON 快照序列化/反序列化必须保留新增的澄清项字段。
