## Context

Import Agent 工作流已经在当前代码中落地，不再是“离线脚本验证后待内生化”的状态。现有实现已经覆盖：

- owner-scoped 的会话、轮次、计划确认与执行批次接口；
- `ApiImportAgentApplicationService` 中的会话编排、确认门禁与同步执行流程；
- `ApiImportAgentController` / `ApiImportAgentWebDelegate` 提供的普通接口和 SSE 流式接口；
- `MybatisApiImportAgentSessionRepository` 与 `MybatisApiImportAgentRunRepository` 提供的会话、轮次、运行批次持久化；
- `OpenAiCompatibleImportAgentPlannerProvider` 与 `OpenAiCompatibleImportAgentReplyPort` 提供的 OpenAI-compatible 规划与回复适配。

当前更准确的设计任务，不是解释“为什么还没有 Import Agent”，而是记录当前实现采用了哪些边界、哪些取舍，以及哪些已知缺口将留给后续 change 处理。

现有顶层文档同时给了两条约束：

- [docs/AetherAPI 项目概念介绍文档.md](docs/AetherAPI%20项目概念介绍文档.md#L80) 已把“外部 API 导入”和“Agent 时代入口”列为产品方向。
- [docs/design/aehter-api-hub/Aether API Hub API Catalog领域设计文档.md](docs/design/aehter-api-hub/Aether%20API%20Hub%20API%20Catalog领域设计文档.md) 已明确 API Catalog 是资产主数据与生命周期核心，Discovery 和 Unified Access 只能消费已发布资产。

因此，这次设计必须把 Agent 放在“会话 + 计划 + 执行编排”层，而不是放进 API Catalog 主模型，更不能绕过现有 owner-scoped 应用服务直接写资产。

## Goals / Non-Goals

**Goals:**

- 记录当前已交付的 owner-scoped 导入 Agent 会话工作流及其模块边界。
- 明确当前实现中 Planner、reply 生成、持久化和确定性执行的职责分层。
- 对齐当前权威接口和权威 SQL 文档与代码实现的关系。
- 明确当前实现尚未覆盖的后续增强点，避免继续沿用实现前假设。

**Non-Goals:**

- 不把 Copilot/Codex 等开发工具运行时直接嵌入后端。
- 不在本 change 中实现通用多 Agent 框架、自治循环、自主外网抓取或自动调试。
- 不改变 Discovery、Unified Access、现有资产工作台 API 的对外语义。
- 不在本 change 中把现有资产写模型迁移到新的 Import Agent 表结构中。
- 不在本 change 中补齐更强的 planner tool schema、多阶段子 agent 或规则型生产 fallback provider。

## Decisions

### Decision 1: 使用独立的 Import Agent Controller，而不是扩展 ApiAssetController

导入 Agent 会话是编排流程，不是资产主数据本身。资产管理接口继续聚焦 owner-scoped 资产 CRUD 与生命周期；导入会话、计划确认、执行批次查询统一进入 `ApiImportAgentController.java`，并由 `docs/api/api-import-agent.yaml` 一对一承载权威契约。

实现说明：当前代码额外提供了 create-session 和 append-turn 的 SSE 变体端点，由 `ApiImportAgentWebDelegate` 负责输出 planning / replying / session / done 事件序列，但并未改变会话的 owner-scoped 语义。

备选方案：直接在 `ApiAssetController` 下追加导入接口。

不采用原因：会把“资产主数据”与“导入编排过程”混在一个控制器里，破坏当前 API Catalog 的边界，也不利于后续会话态扩展。

### Decision 2: Planner 与 Executor 强制分层，Planner 输出结构化计划快照

Planner 的职责是把用户输入、文档来源与补充约束转成结构化计划快照，包括候选分类、资产草稿、AI 档案候选、发布意图以及待确认问题。Executor 只接受结构化计划输入，并按固定步骤调用现有分类与资产应用服务执行。

实现说明：当前主源码通过 `ProviderBackedApiImportAgentPlanner` 装配 Planner port，并由 `OpenAiCompatibleImportAgentPlannerProvider` 提供生产实现。reply 文案生成则由独立的 `ApiImportAgentReplyPort` 负责。Planner 输出会在服务层被规整为 `ImportAgentPlanModel`，并在持久化层直接保存为 `plan_snapshot_json`，而不是保存外部快照引用。

备选方案：让 Agent 直接发起分类/资产写操作。

不采用原因：会把不确定性的 LLM 输出直接暴露给写模型，难以保证 owner 规则、发布校验和失败可恢复性。

### Decision 3: 会话、轮次、执行批次分三张表落地

新增三张权威表：

- `api_import_agent_session`：保存 owner、文档来源、当前状态、当前计划版本和最近一次确认信息。
- `api_import_agent_turn`：保存用户输入、Agent 响应和计划版本推进关系，形成可审计对话历史。
- `api_import_agent_run`：保存执行批次、步骤结果、失败原因、受影响资产清单和最终摘要。

备选方案：只用一张大表存 JSON。

不采用原因：会让会话态、对话历史和执行批次混在一起，后续查询、重试和审计都会变得脆弱。

实现说明：当前会话表保存的是完整 `plan_snapshot_json`，轮次表保存 turn index / actor / message / plan version，执行批次表保存 step results、affected api codes 和 failure reason。当前代码没有额外引入富领域聚合来持有这些状态，而是在 service model 与 persistence adapter 中维持状态机与投影。

### Decision 4: 执行必须经过显式确认门禁

Planner 可以多轮更新计划，但一旦计划涉及创建分类、注册资产、绑定 AI 档案或发布，系统必须要求当前用户显式确认该计划版本后才允许创建执行批次。未确认计划只能停留在会话态。

备选方案：Planner 生成计划后立即自动执行。

不采用原因：外部 API 文档质量参差不齐，自动执行会放大误导入和错误发布风险。

实现说明：当前执行流程在 `ApiImportAgentApplicationService.startRun(...)` 内同步完成，并先写入 RUNNING 批次，再根据步骤结果落为 SUCCEEDED、PARTIALLY_FAILED 或 FAILED；不存在后台任务队列和自动补偿事务。

### Decision 5: Executor 复用现有 Catalog/Category 应用服务，导入结果只做编排投影

导入执行中的 `ensure-category`、`register-asset`、`revise-asset`、`attach-ai-profile`、`publish-asset` 等步骤必须复用现有确定性服务能力；Import Agent 自身只记录步骤状态、计划版本和结果摘要，不再重新实现资产规则。

备选方案：为 Import Agent 单独实现一套写库逻辑。

不采用原因：会复制 API Catalog 的业务规则，并引入与现有 owner-scoped 资产工作台不一致的行为。

实现说明：当前执行步骤固定复用 `CategoryUseCase` 与 `ApiAssetUseCase`，按 ensure-category、register/revise、attach-ai-profile、publish 顺序推进。Import Agent run 仅记录编排结果，并不引入平行的资产写入通路。

## Risks / Trade-offs

- [LLM 计划不稳定] → 当前通过结构化计划快照、后置校验和显式确认门禁截断不确定性，但 tool-calling schema 仍然偏弱，缺失字段仍可能回落为 clarificationQuestions。
- [没有生产级 fallback planner] → 当前主源码只有 OpenAI-compatible provider；若没有 provider 命中，`ProviderBackedApiImportAgentPlanner` 会直接失败。
- [会话与执行状态过多] → 当前实现限定为单用户、单会话版本推进，不做跨会话协作和并行执行编排。
- [执行步骤跨多个应用服务，失败恢复复杂] → 当前通过 `api_import_agent_run` 保留 step-level history，并支持 PARTIALLY_FAILED，但不做自动补偿事务。

## Current Known Gaps

- `OpenAiCompatibleImportAgentPlannerProvider` 当前只有单个 `submit_import_plan` tool，tool schema 主要是 properties 描述，缺少更强的 required / conditional constraints。
- `application-dev.yml` 和 `application-prod.yml` 中的 `tool-calling-enabled` 默认值均为 `false`，因此当前生产路径并不默认依赖 tool calling。
- 测试目录中存在规则型 planner 测试痕迹，但当前主源码并未交付第二个生产级 planner provider。
- 会话规划阶段尚未内建“自动补槽 / 二次提取 / 子 agent 分阶段抽取”能力，这些能力将由后续 change 单独补强。