## 1. 契约与范围确认

- [x] 1.1 阅读 `docs/spec/` 后端开发规范、`docs/design/aehter-api-hub/Aether API Hub架构设计文档.md`、`docs/api/api-import-agent.yaml` 以及本 change 的 `proposal.md` / `design.md` / `specs/api-import-agent-session/spec.md`，确认本次只增强 Import Agent 现有流式契约，不新增 controller endpoint。
- [x] 1.2 更新 `docs/api/api-import-agent.yaml` 中 `ApiImportAgentController` 对应的 SSE 契约，新增 `thinking` 事件定义、载荷字段和示例，并明确最终 `session` 事件仍是会话真相源。
- [x] 1.3 确认本次不新增表、不修改 `api_import_agent_session` / `api_import_agent_turn` / `api_import_agent_run` 结构，因此不更新 `docs/sql/`。

## 2. 后端流式事件模型

- [x] 2.1 在 service 边界引入统一的 Import Agent 流式事件模型，覆盖 `status`、`thinking`、`message`、`session`、`error`、`done` 事件，并为 `thinking` 定义 `stage`、`title`、`summary`、可选 `detail`、顺序或时间字段。
- [x] 2.2 用统一 stream emitter 替换当前只适合 reply delta 的 `Consumer<String>` 通道，使 create-session 与 append-turn 主链路都能发出结构化事件。
- [x] 2.3 保持现有 `message` delta、最终 `session` 快照、`error` 和 `done` 行为不变，确保未识别 `thinking` 的旧前端仍可按旧协议消费流。

## 3. Adapter SSE 输出

- [x] 3.1 重构 `ApiImportAgentWebDelegate` 的 SSE 写出逻辑，让 delegate 只负责把统一事件模型序列化为对应 SSE event。
- [x] 3.2 为 `thinking` 事件补充 JSON 序列化测试，覆盖字段命名、事件顺序和与现有事件并存的场景。
- [x] 3.3 回归 create-session 与 append-turn 两条 stream endpoint，确认状态事件、回复增量、最终会话快照和结束事件的输出语义未变。

## 4. Planner / Subagent / Reply 接入

- [x] 4.1 在 `OpenAiCompatibleImportAgentPlannerProvider` 的稳定阶段边界发出 `thinking` 摘要，包括开始规划、提取事实、填充草案、提交计划、fallback planning 和完成规划。
- [x] 4.2 在 `ImportAgentPlannerSubagentOrchestrator` 中接入 subagent 开始 / 完成、merge、review、clarification downgrade 等关键节点的 `thinking` 摘要。
- [x] 4.3 保持 `OpenAiCompatibleImportAgentReplyPort` 只生产用户可见的最终 reply delta；可增加回复开始的 `status` 或 `thinking` 提示，但不得把 thinking 文本混入 assistant message。

## 5. 内容边界与回归验证

- [x] 5.1 为 thinking 事件增加统一内容边界检查，禁止输出 raw CoT、完整 prompt、provider 原始 payload、异常堆栈正文、`authConfig` 明文或用户提交的密钥值。
- [x] 5.2 增加 service / infrastructure 测试，证明 planner、subagent 和 reply 链路发出的 thinking 事件是安全摘要，不影响最终 plan / session 结果。
- [x] 5.3 使用 Java 17 运行相关 adapter、service、infrastructure Maven 测试，并记录任何超出本次 change 范围的环境问题。
- [x] 5.4 在 `aether-api-hub` 下运行 `openspec validate add-import-agent-thinking-stream --strict`。
