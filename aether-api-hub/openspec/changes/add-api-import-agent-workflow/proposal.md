## Why

当前仓库已经验证了“离线脚本驱动的 API 批量导入”可以通过现有分类、资产、AI 档案与发布链路完成上架，但这套能力仍停留在开发工具和脚本编排层，后端内并没有一个可持续演进的会话、计划与执行工作流。与此同时，顶层产品文档已经把“外部 API 导入”和“Agent 时代入口”列为核心方向，因此现在需要把这套导入能力内生为后端受控能力，而不是继续依赖工作区脚本和人工串联。

## What Changes

- 新增一个后端内生的 API Import Agent 工作流，支持用户以“导入会话”的方式提交 API 文档来源、补充约束和导入意图，由 Planner 生成结构化导入计划，再由确定性 Executor 执行。
- 新增 owner-scoped 的导入 Agent 会话接口，统一承载会话创建、会话详情读取、追加用户消息、查看计划、确认执行和查询执行结果。
- 新增导入 Agent 的持久化模型，用于保存会话状态、对话轮次、结构化计划快照和执行批次结果，替代当前仅存在于 `docs/api-import-runs/` 的离线产物。
- 明确 Agent 只负责“理解与规划”，不直接写数据库、不直接绕过业务规则；真正的分类创建、资产注册、资产修订、AI 档案绑定和发布仍通过现有确定性应用服务完成。
- 将新增或更新对应的顶层权威文档：
  - `docs/api/api-import-agent.yaml`，映射 `ApiImportAgentController.java`，并要求通过 `tml-docs-spec-generate` 的 API 模板生成。
  - `docs/sql/api_import_agent_session.sql`、`docs/sql/api_import_agent_turn.sql`、`docs/sql/api_import_agent_run.sql`，分别描述导入会话、对话轮次和执行批次表结构，并要求通过 `tml-docs-spec-generate` 的 SQL 模板生成。
- 保持现有 `docs/api/api-asset-management.yaml` 和 `docs/sql/api-asset.sql` 的资产主模型地位不变；Agent 工作流是编排入口，不替代 API Catalog 的主数据与生命周期规则。

## Capabilities

### New Capabilities
- `api-import-agent-session`: 用户可创建、查看和推进导入 Agent 会话，后端保存会话状态、对话轮次与当前计划快照。
- `api-import-agent-execution`: 用户可在确认计划后触发受控执行，后端按步骤记录分类、资产、AI 档案和发布动作的执行结果与失败原因。

### Modified Capabilities
- `catalog-owner-asset-management`: 增补要求，明确导入 Agent 的执行阶段必须复用现有 owner-scoped 资产管理规则与生命周期校验，而不是绕过现有应用服务直接写入资产。

## Impact

- Affected docs:
  - `docs/api/api-import-agent.yaml`（新增，权威接口契约）
  - `docs/sql/api_import_agent_session.sql`（新增，权威表结构）
  - `docs/sql/api_import_agent_turn.sql`（新增，权威表结构）
  - `docs/sql/api_import_agent_run.sql`（新增，权威表结构）
  - `docs/design/aehter-api-hub/` 下与 API Catalog / 导入编排相关的顶层设计文档
- Affected backend modules:
  - `aether-api-hub-adapter`：新增 `ApiImportAgentController` 与 Web delegate
  - `aether-api-hub-service`：新增会话编排、Planner port、Executor service 与结果查询模型
  - `aether-api-hub-domain`：新增导入会话/执行聚合或等价领域模型，明确状态机与审批边界
  - `aether-api-hub-infrastructure`：新增 Planner 适配、会话/轮次/执行批次持久化实现
- External/runtime impact:
  - 需要一个可替换的 Planner 适配边界，以便后续接入 LLM 或规则规划器
  - Discovery、Unified Access 和现有资产工作台接口不应因该 change 改变对外响应语义