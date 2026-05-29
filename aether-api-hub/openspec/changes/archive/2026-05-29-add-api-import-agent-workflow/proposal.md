## Why

当前后端代码已经把 API Import Agent 落地为 owner-scoped 的会话、规划与执行工作流。本变更文档需要反映已经交付的实现状态，而不是继续停留在“离线脚本尚未内生化”的前置语境里。

当前实现的核心价值是：把原先依赖工作区脚本和人工串联的导入过程收敛为平台内受控流程，同时保持 API Catalog 的主数据和生命周期规则不被绕过。与此同时，当前实现也暴露出新的运行时边界，例如 Planner 依赖 OpenAI-compatible provider、tool calling 为可选增强且默认关闭，这些都应在变更记录中如实体现。

## What Changes

- 已新增一个后端内生的 API Import Agent 工作流，允许当前用户创建导入会话、追加多轮消息、确认计划版本并启动执行批次。
- 已新增 owner-scoped 的导入 Agent HTTP 接口与对应权威契约 `docs/api/api-import-agent.yaml`，包括普通会话接口和流式会话接口。
- 已新增导入 Agent 的会话、轮次和执行批次持久化模型，并通过 `docs/sql/api_import_agent_session.sql`、`docs/sql/api_import_agent_turn.sql`、`docs/sql/api_import_agent_run.sql` 对齐权威表结构。
- 已实现 Planner/Executor 分层：Planner 只产生结构化计划草稿与澄清问题，Executor 仅在显式确认后复用现有分类与资产应用服务完成确定性写操作。
- 已实现 OpenAI-compatible Planner provider 与 streamed reply port，并通过 `aether.import-agent.llm.*` 和 `aether.import-agent.stream.*` 配置项控制启用状态、超时、模型和 tool-calling 开关。
- 已保持 `docs/api/api-asset-management.yaml` 和相关资产主模型权威地位不变；Import Agent 仍是编排入口，而不是 API Catalog 的平行写路径。

## Capabilities

### New Capabilities
- `api-import-agent-session`: 用户可创建、查看和推进导入 Agent 会话，后端保存会话状态、对话轮次与当前计划快照。
- `api-import-agent-execution`: 用户可在确认计划后触发受控执行，后端按步骤记录分类、资产、AI 档案和发布动作的执行结果与失败原因。

### Modified Capabilities
- `catalog-owner-asset-management`: 增补要求，明确导入 Agent 的执行阶段必须复用现有 owner-scoped 资产管理规则与生命周期校验，而不是绕过现有应用服务直接写入资产。

## Impact

- Affected docs:
  - `docs/api/api-import-agent.yaml`（已新增，权威接口契约）
  - `docs/sql/api_import_agent_session.sql`（已新增，权威表结构）
  - `docs/sql/api_import_agent_turn.sql`（已新增，权威表结构）
  - `docs/sql/api_import_agent_run.sql`（已新增，权威表结构）
  - `docs/design/aehter-api-hub/` 下与 API Catalog / 导入编排相关的顶层设计文档
- Affected backend modules:
  - `aether-api-hub-adapter`：已新增 `ApiImportAgentController` 与 Web delegate
  - `aether-api-hub-service`：已新增会话编排、Planner port、Executor service 与结果查询模型
  - `aether-api-hub-domain`：当前仅引入 `ImportAgentDomainException` 等导入域边界异常，核心会话与运行态模型仍主要位于 service / infrastructure 层
  - `aether-api-hub-infrastructure`：已新增 Planner 适配、streamed reply 适配、会话/轮次/执行批次持久化实现
- External/runtime impact:
  - 当前运行时通过 `aether.import-agent.llm.*` 配置接入 OpenAI-compatible Planner provider；tool calling 为可选增强，`application-dev.yml` 与 `application-prod.yml` 中默认关闭
  - 当前主源码中未提供第二个生产级 Planner provider；当没有 provider 命中时，ProviderBacked planner 会直接失败
  - Discovery、Unified Access 和现有资产工作台接口未因该 change 改变对外响应语义