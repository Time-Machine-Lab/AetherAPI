## 为什么

部分上游服务要求调用时携带稳定的非鉴权请求头，例如功能开关、beta 版本选择、租户提示或异步提交开关。当前 API 资产只建模了 `authConfig`，资产所有者和 Import Agent 计划无法表达这些请求头，或只能把它们塞进鉴权配置，导致 Unified Access 转发边界含混且脆弱。

## 变更内容

- 为 API 资产增加 owner-scoped 的 `upstreamRequestHeaders` 配置，用于 Unified Access 转发时发送固定上游请求头。
- 在 `api_asset` 上以 nullable JSON text 快照保存请求头配置，条目结构为 `name` / `value`，不使用自由文本。
- 通过现有所有者资产管理 API 契约 `docs/api/api-asset-management.yaml` 暴露 `upstreamRequestHeaders`，该契约一一映射到 `ApiAssetController.java`。
- Unified Access 出站请求在调用方请求头过滤之后、现有上游鉴权之前/并行阶段应用配置请求头，同时拒绝或忽略平台、hop-by-hop、authorization、content type、host、content length 等不安全请求头名。
- 扩展 Import Agent 计划，使 `assetPlans[].upstreamRequestHeaders` 成为独立结构化字段，与 `authConfig` 分离，并参与引导式澄清和确认执行。
- Discovery 与市场公开面不得暴露 owner-only 上游请求头。
- 异步任务查询的请求头继承保持显式设计边界：提交请求头不会自动发送到任务查询端点，除非后续显式建模任务查询请求头行为。

## 能力

### 新增能力

- `unified-access-upstream-forwarding`：Unified Access 在目标解析后转发资产所有者配置且经过清洗的上游请求头。
- `api-import-agent-upstream-headers`：Import Agent 可以抽取、澄清、展示、持久化并执行上游请求头计划，而不重载 `authConfig`。

### 修改能力

- `catalog-owner-asset-management`：owner-scoped API 资产创建、修订、清空和详情流程接受并返回 nullable `upstreamRequestHeaders` 配置。

## 影响

- 权威文档：
  - 更新 `docs/sql/api-asset.sql`，为 `api_asset` 增加 nullable 上游请求头列。
  - 更新 `docs/api/api-asset-management.yaml`，为 `ApiAssetController.java` 的请求和响应 schema 增加 `upstreamRequestHeaders`。
  - 更新 `docs/api/api-import-agent.yaml`，为 `ApiImportAgentController.java` 增加 `assetPlans[].upstreamRequestHeaders` 以及引导式客户端所需的澄清示例。
  - 更新 `docs/design/aehter-api-hub/` 下相关设计文档，说明上游鉴权、固定上游请求头与调用方透传请求头之间的边界。
  - `docs/sql/` 和 `docs/api/` 权威更新必须在应用代码变更前使用 `tml-docs-spec-generate` 的 SQL/API 模板产出。
- 后端代码：
  - 扩展 catalog 领域模型、命令、DTO、转换器、持久化实体、查询记录和测试。
  - 扩展 `TargetApiSnapshotModel` 与 Unified Access 下游转发，应用清洗后的配置请求头且不泄漏平台内部头。
  - 扩展 Import Agent 模型、planner schema、JSON 序列化、边界校验、澄清处理、执行映射和安全流式摘要。
- 数据库：
  - 在 `api_asset` 上新增一个 nullable JSON text 列。
- 前端协同：
  - `aether-console` 需要配套提案，用于编辑 owner headers 并渲染 Import Agent 计划 headers。
