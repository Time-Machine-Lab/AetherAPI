## Context

当前仓库已经通过离线脚本和工作区技能验证了一条可用的外部 API 导入链路：先确保分类，再创建资产草稿、修订资产、绑定 AI 档案，最后发布。`docs/api-import-runs/` 证明这条链路是可执行的，但现状仍存在三类缺口：

- 后端没有导入会话、对话轮次和执行批次的权威模型，导入过程无法在平台内持续追踪。
- “理解文档并生成导入计划”的 AI 编排目前在后端内不存在，用户只能依赖开发工具或离线脚本。
- `docs/api/` 与 `docs/sql/` 还没有对应 Import Agent 的权威契约，因此无法在后端模块边界内稳定演进该能力。

现有顶层文档同时给了两条约束：

- [docs/AetherAPI 项目概念介绍文档.md](docs/AetherAPI%20项目概念介绍文档.md#L80) 已把“外部 API 导入”和“Agent 时代入口”列为产品方向。
- [docs/design/aehter-api-hub/Aether API Hub API Catalog领域设计文档.md](docs/design/aehter-api-hub/Aether%20API%20Hub%20API%20Catalog领域设计文档.md) 已明确 API Catalog 是资产主数据与生命周期核心，Discovery 和 Unified Access 只能消费已发布资产。

因此，这次设计必须把 Agent 放在“会话 + 计划 + 执行编排”层，而不是放进 API Catalog 主模型，更不能绕过现有 owner-scoped 应用服务直接写资产。

## Goals / Non-Goals

**Goals:**

- 提供一个后端内生的 owner-scoped 导入 Agent 会话工作流，用于接收用户输入、保存上下文并生成结构化导入计划。
- 让 Planner 只负责理解、拆解和补全导入计划，真正的分类/资产变更继续通过确定性业务服务完成。
- 为会话、轮次和执行批次建立平台内持久化模型，替代仅存在于离线 Markdown/JSON 中的运行产物。
- 通过单一控制器契约与逐表 SQL 权威文档，把 Agent 能力纳入现有 OpenSpec 与文档治理体系。

**Non-Goals:**

- 不把 Copilot/Codex 等开发工具运行时直接嵌入后端。
- 不在本 change 中实现通用多 Agent 框架、自治循环、自主外网抓取或自动调试。
- 不改变 Discovery、Unified Access、现有资产工作台 API 的对外语义。
- 不在本 change 中把现有资产写模型迁移到新的 Import Agent 表结构中。

## Decisions

### Decision 1: 使用独立的 Import Agent Controller，而不是扩展 ApiAssetController

导入 Agent 会话是编排流程，不是资产主数据本身。资产管理接口继续聚焦 owner-scoped 资产 CRUD 与生命周期；导入会话、计划确认、执行批次查询统一进入 `ApiImportAgentController.java`，并由 `docs/api/api-import-agent.yaml` 一对一承载权威契约。

备选方案：直接在 `ApiAssetController` 下追加导入接口。

不采用原因：会把“资产主数据”与“导入编排过程”混在一个控制器里，破坏当前 API Catalog 的边界，也不利于后续会话态扩展。

### Decision 2: Planner 与 Executor 强制分层，Planner 输出结构化计划快照

Planner 的职责是把用户输入、文档来源与补充约束转成结构化计划快照，包括候选分类、资产草稿、AI 档案候选、发布意图以及待确认问题。Executor 只接受结构化计划输入，并按固定步骤调用现有分类与资产应用服务执行。

备选方案：让 Agent 直接发起分类/资产写操作。

不采用原因：会把不确定性的 LLM 输出直接暴露给写模型，难以保证 owner 规则、发布校验和失败可恢复性。

### Decision 3: 会话、轮次、执行批次分三张表落地

新增三张权威表：

- `api_import_agent_session`：保存 owner、文档来源、当前状态、当前计划版本和最近一次确认信息。
- `api_import_agent_turn`：保存用户输入、Agent 响应和计划版本推进关系，形成可审计对话历史。
- `api_import_agent_run`：保存执行批次、步骤结果、失败原因、受影响资产清单和最终摘要。

备选方案：只用一张大表存 JSON。

不采用原因：会让会话态、对话历史和执行批次混在一起，后续查询、重试和审计都会变得脆弱。

### Decision 4: 执行必须经过显式确认门禁

Planner 可以多轮更新计划，但一旦计划涉及创建分类、注册资产、绑定 AI 档案或发布，系统必须要求当前用户显式确认该计划版本后才允许创建执行批次。未确认计划只能停留在会话态。

备选方案：Planner 生成计划后立即自动执行。

不采用原因：外部 API 文档质量参差不齐，自动执行会放大误导入和错误发布风险。

### Decision 5: Executor 复用现有 Catalog/Category 应用服务，导入结果只做编排投影

导入执行中的 `ensure-category`、`register-asset`、`revise-asset`、`attach-ai-profile`、`publish-asset` 等步骤必须复用现有确定性服务能力；Import Agent 自身只记录步骤状态、计划版本和结果摘要，不再重新实现资产规则。

备选方案：为 Import Agent 单独实现一套写库逻辑。

不采用原因：会复制 API Catalog 的业务规则，并引入与现有 owner-scoped 资产工作台不一致的行为。

## Risks / Trade-offs

- [LLM 计划不稳定] → 使用结构化计划快照和显式确认门禁，把不确定性截断在 Planner 阶段。
- [会话与执行状态过多，初期实现复杂度上升] → 先限定为单用户、单会话版本推进，不做跨会话协作和并行执行编排。
- [导入文档来源格式差异大] → 本期只保证 Planner 能接收“文档摘要 / 文档链接 / 预解析结构”三类输入，不把所有抓取与 OCR 问题一并纳入。
- [执行步骤跨多个应用服务，失败恢复复杂] → `api_import_agent_run` 记录逐步状态与失败原因，先支持“人工修正后重新生成新 run”，不做自动补偿事务。

## Migration Plan

1. 先用 `tml-docs-spec-generate` 生成 `docs/api/api-import-agent.yaml` 与三份逐表 SQL 权威文档。
2. 再在 adapter/service/domain/infrastructure 中引入 Import Agent 会话、Planner port、Executor service 与持久化实现。
3. 在 apply 阶段补充回归测试，确认 Discovery、Unified Access 与现有资产工作台响应不受影响。
4. 若后续 Planner provider 不可用，可暂时退回“仅保留会话和手工提交结构化计划”的降级模式，不影响 Executor 与历史数据。

## Open Questions

- Planner provider 首期是接已有 AI 聚合能力，还是先提供一个规则/Mock planner 作为基础实现。
- 会话中是否允许上传完整文档原文，还是只保存来源链接与提取摘要，避免存储过大文本。
- 执行批次是否需要在首期暴露“逐步骤重试”，还是只支持整批重新执行。