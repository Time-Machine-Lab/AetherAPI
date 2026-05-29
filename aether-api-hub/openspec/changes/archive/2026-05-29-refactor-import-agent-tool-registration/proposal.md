## Why

当前 Import Agent 的 staged tool-calling 已经能改善漏参问题，但 tool 定义仍然全部聚合在 `OpenAiCompatibleImportAgentPlannerProvider` 内部，表现为同一个 provider 同时承担阶段编排、prompt 组装、HTTP 调用、tool schema 定义和工具选择职责。随着 `extract_import_facts`、`fill_import_slots`、`submit_import_plan` 三段工具链稳定下来，这种集中式实现开始带来两个直接问题：一是新增或调整单个 tool 时必须修改 provider 主类，回归面偏大；二是 tool 元数据和 schema 缺少独立组织方式，不利于后续补充更多 planner tools 或做阶段级校验。

仓库当前已经是 Spring Boot 组件模型，provider 本身也是 Spring Bean。对于“每个 tool 一个类，并能自动注册”的诉求，更合适的方向是基于 Spring Bean 收集和轻量注解元数据建立 registry，而不是继续把 tool schema 内联在 provider 中，或者再引入一套额外的类路径扫描机制。本提案的目标就是把 planner tool 从“provider 内联定义”提升为“独立类声明 + 注解注册 + registry 聚合”的内部架构，同时保持现有会话 API、执行链路和 planner 输出契约不变。

## What Changes

- 为 Import Agent planner 新增内部 tool 声明模型，使每个 planning tool 以独立类承载自身的名称、阶段和 schema 定义。
- 引入基于 Spring Bean 的 tool registry，按 `PlannerStage` 聚合并校验可用 tool，而不是由 provider 手工拼接阶段工具列表。
- 为 planning tool 增加注解式元数据声明，至少覆盖 tool name、stage、order 等稳定属性，便于启动时做去重、排序和阶段映射校验。
- 收敛 `OpenAiCompatibleImportAgentPlannerProvider` 的职责，使其主要负责 staged orchestration、request 组装、LLM 调用和响应解析，不再内联各个 tool 的 schema 细节。
- 保持现有 `extract_import_facts`、`fill_import_slots`、`submit_import_plan` 三阶段行为、现有 feature gate、现有 deterministic reconcile 与现有对外 API / SQL 权威文档不变。

## Capabilities

### Modified Capabilities

- `api-import-agent-session`
  - planner 在 tool-calling 模式下必须通过可注册的 planning tool 集合构建阶段工具列表，并在启动时校验 tool 元数据的完整性与唯一性。
  - planner 的 tool schema 定义必须可被独立演进和测试，而不要求每次修改都进入 provider 主类手工拼装。

## Impact

- Reviewed authority docs:
  - `docs/design/aehter-api-hub/Aether API Hub架构设计文档.md`
  - `openspec/changes/add-api-import-agent-workflow/specs/api-import-agent-session/spec.md`
- Top-level docs impact:
  - 本提案不新增接口，不修改既有 Controller 映射，不调整数据库表结构，因此默认无需更新 `docs/api/` 和 `docs/sql/` 顶层权威文档。
- Affected backend modules:
  - `aether-api-hub-infrastructure`：新增 planning tool 接口、注解、registry，并重构 provider 的 tool 装配路径。
  - `aether-api-hub-infrastructure` tests：补充 tool registry、tool schema 和 provider 编排的回归测试。
- Architecture impact:
  - 继续遵守现有 DDD 分层，tool 注册与 provider 编排仍属于 infrastructure.importagent.planner 内部实现，不把 planner 规则扩散到 adapter 或 service。
- Runtime impact:
  - 不改变现有导入会话接口、执行确认机制和 `aether.import-agent.llm.tool-calling-enabled` feature gate。