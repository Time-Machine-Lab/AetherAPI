## Why

当前 Import Agent 已经具备会话、规划、确认和执行闭环，但规划阶段的 tool calling 仍然偏弱：`OpenAiCompatibleImportAgentPlannerProvider` 只有一个 `submit_import_plan` 工具，schema 主要是字段描述和少量 enum，缺少 required、conditional constraints 与 staged extraction。结果是模型时常会漏掉 `authConfig`、`asyncTaskConfig.queryUrlTemplate`、`aiProfile.model` 等关键参数，随后只能依赖后置校验回退为 clarificationQuestions。

仓库里的 `.codex/skills/batch-import-api-skill/SKILL.md` 已经证明，更稳定的导入体验依赖“前置结构化输入、显式字段命名、authConfig 纯字符串格式、异步查询并入 asyncTaskConfig”这些规则，而不是把所有理解、抽取和装配都压缩在一次自由生成里。因此，需要单独发起一个 change，补强 Import Agent 的 tool-calling 约束和自动补槽能力。

## What Changes

- 补强 Import Agent planner 的 tool-calling 合同，把当前单个松散 `submit_import_plan` 工具升级为更强约束的结构化规划工具链。
- 为 planner 增加 staged extraction / slot filling / final submit 的内部编排，减少一次性生成完整计划导致的漏字段问题。
- 将 planner 的字段约束和格式规范与 `.codex/skills/batch-import-api-skill/SKILL.md` 对齐，特别是 `authConfig`、`asyncTaskConfig`、`requestJsonSchema`、`responseJsonSchema`、`aiProfile` 等关键字段。
- 在不改变现有 `docs/api/api-import-agent.yaml` 和 `docs/sql/api_import_agent_*.sql` 对外契约的前提下，提升规划阶段的确定性和缺失参数恢复能力。

## Planned Tool Set

- `extract_import_facts`
  - 从 `documentSummary`、`latestUserMessage`、`currentPlan` 中提取高确定性事实。
  - 输出候选资产、候选鉴权、异步提交/查询关系、AI profile 线索和 schema 快照线索。
- `fill_import_slots`
  - 只针对缺失槽位工作，把 `currentPlan`、最近 turns 和 extraction facts 结合起来补齐字段。
  - 目标是减少重复 clarification，而不是重新生成整份计划。
- `submit_import_plan`
  - 只接收经过抽取和补槽后的最终结构化计划。
  - 必须满足更强 schema 约束，尤其是 publish、auth、AI profile、async-task 相关条件约束。

## Expected Deliverables

- 一份明确的 planner tool 列表和各 tool 的职责边界。
- 一份可直接转成 JSON Schema 的字段约束矩阵，包括 `required`、`enum`、`additionalProperties: false` 和条件规则。
- 一份 slot-filling 顺序草案，规定系统在什么情况下应先自动补齐、在什么情况下才向用户发 clarificationQuestions。

## Capabilities

### Modified Capabilities
- `api-import-agent-session`: 增补要求，明确 planner 在 tool-calling 模式下必须以更强 schema 约束提交结构化计划，并在向用户发起 clarificationQuestions 之前优先执行内部补槽与格式归一。

## Impact

- Reviewed authority docs:
  - `docs/api/api-import-agent.yaml`
  - `docs/api/api-asset-management.yaml`
  - `docs/sql/api_import_agent_session.sql`
  - `docs/sql/api_import_agent_turn.sql`
  - `docs/sql/api_import_agent_run.sql`
- Top-level docs impact:
  - 当前提案默认不引入新的 Controller 接口和新的表结构；如后续设计确认不修改公共响应字段，则无需新增或修改 `docs/api/` 与 `docs/sql/` 顶层权威文档。
- Affected backend modules:
  - `aether-api-hub-service`：补充 planner orchestration 所需的中间模型或 slot-filling 协调逻辑
  - `aether-api-hub-infrastructure`：重构 `OpenAiCompatibleImportAgentPlannerProvider`、增强 tool schema、增加 staged extraction 与 planner tests
- External/runtime impact:
  - 继续沿用 `aether.import-agent.llm.tool-calling-enabled` 作为 feature gate
  - 默认保持现有 session / run API 不变；本次重点是提升 planner 输出完整性和可恢复性，而不是扩展外部接口