## Why

当前 Import Agent 的 planner 已经具备 staged tool-calling、deterministic slot filling 和 tool registry，但整体上仍然是“一个 planner provider 在单个上下文里完成全部推理”。这意味着文档事实抽取、鉴权判断、异步 submit/query 识别、AI profile 推断、缺槽审查和澄清生成仍然竞争同一轮注意力与同一份输出上下文。结果并不是 planner 完全不可用，而是当输入文档较长、字段较多、异步与鉴权规则交织时，仍然容易出现局部遗漏、澄清问题泛化或低确定性字段混入最终计划的情况。

现有架构已经给出了一个自然演进方向：应用层只依赖单一 `ApiImportAgentPlannerPort`，而 infrastructure planner 内部已经有 provider 链和 staged tool-calling。对于这种情况，引入“planner 内部子 agent 协作”比引入一套新的业务级 agent framework 更合适。目标不是改变会话 API 或执行流程，而是把 planner 内部进一步拆成几个窄职责的专业角色，让最终仍然只产出一个 draft import plan，但中间推理更稳定、更可审查、更容易局部测试。

## What Changes

- 为 Import Agent planner 增加内部子 agent 协作编排，使 planner 能把事实抽取、鉴权识别、异步模式识别、计划审查和澄清生成拆给窄职责角色处理。
- 保持应用层仍然只调用单一 `ApiImportAgentPlannerPort`，由 infrastructure planner 在内部完成子 agent 调度、结果合并和最终计划构建。
- 为 planner 增加子 agent 合并规则和审查规则，避免多个内部角色输出冲突或把低确定性信息直接提升为最终 draft plan。
- 将子 agent 设计约束为内部 planner 协作者，而不是独立会话、独立持久化、独立执行的自治运行时框架。
- 保持现有 session / run API、现有 SQL authority docs、现有确认后执行主链路和现有 feature gate 不变。

## Capabilities

### Modified Capabilities

- `api-import-agent-session`
  - planner 在生成当前会话 draft import plan 时可以在内部编排多个窄职责子 agent，但对外仍然必须返回一个 owner-scoped、可版本化的统一计划结果。
  - planner 必须在内部子 agent 输出之间执行合并与审查，优先保留高确定性事实、阻止冲突字段直接进入最终计划，并在必要时回退为 clarification。

## Impact

- Reviewed authority docs:
  - `docs/design/aehter-api-hub/Aether API Hub架构设计文档.md`
  - `docs/api/api-import-agent.yaml`
  - `docs/sql/api_import_agent_session.sql`
  - `docs/sql/api_import_agent_turn.sql`
  - `docs/sql/api_import_agent_run.sql`
- Top-level docs impact:
  - 本提案不新增 Controller 接口，不修改现有 session/run 响应契约，不新增 authority tables，因此默认无需更新 `docs/api/` 和 `docs/sql/` 顶层文档。
- Affected backend modules:
  - `aether-api-hub-infrastructure`：新增 planner internal subagent SPI、orchestrator、merge/review 规则和相应 tests。
  - `aether-api-hub-service`：如需要，仅增加 planner request/result 的内部协作模型，不改变 `ApiImportAgentPlannerPort` 对外接口。
- Architecture impact:
  - 子 agent 仅作为 infrastructure planner 内部协作者存在，不扩散到 adapter 层，不引入新的业务主链路，也不在应用层暴露多 agent 概念。
- Runtime impact:
  - 可能增加 planner 内部调用轮次和 token 成本，但继续保持单一最终计划结果与现有执行确认机制。