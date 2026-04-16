## Why

`Consumer & Auth` 领域设计已经明确：用户对外只应感知 `API Key`，而 `Consumer` 应长期作为内部调用身份存在。当前仓库仅有 Catalog 相关的 `docs/api/` 与 `docs/sql/` 权威文档，尚未建立开发者控制台可联调的 API Key 管理契约，因此需要先补齐凭证生命周期与隐式 Consumer 建立这一条用户主链路。

## What Changes

- 新增面向当前登录用户的 API Key 生命周期能力，覆盖 API Key 创建、掩码列表查询、详情查看、停用、启用、吊销与过期信息展示。
- 在凭证签发链路中引入隐式 `Consumer` 自动确保机制，保证“一个用户默认对应一个内部 Consumer，一个 Consumer 可持有多个 API Key”，但不新增任何显式 `Consumer` 业务接口。
- 为开发者控制台补齐联调所需业务接口，并统一返回 TML SDK `Result` 包装，确保前后端可以围绕同一契约联调。
- 明确需要新增 `docs/api/consumer-api-key-management.yaml` 与 `docs/sql/consumer-api-key-management.sql` 作为顶层权威设计产物；后续生成必须使用 `tml-docs-spec-generate` 技能，其中 API 文档使用 API 模板，SQL 文档使用 SQL 模板。
- 约束并发边界：本提案只负责凭证管理与隐式 Consumer 建立，不包含统一接入鉴权校验、调用上下文透传和 API 调用日志查询，这些由独立 change 处理。

## Capabilities

### New Capabilities
- `consumer-auth-key-management`: 定义当前用户 API Key 的创建、查询、停启、吊销，以及隐式 Consumer 绑定规则与开发者控制台联调接口。

### Modified Capabilities
- None.

## Impact

- 受影响文档：[Aether API Hub架构设计文档](D:/Code/Project/Github/AetherAPI/docs/design/aehter-api-hub/Aether API Hub架构设计文档.md)、[Aether API Hub Consumer & Auth领域设计文档](D:/Code/Project/Github/AetherAPI/docs/design/aehter-api-hub/Aether API Hub Consumer & Auth领域设计文档.md)、后续新增的 `docs/api/consumer-api-key-management.yaml` 与 `docs/sql/consumer-api-key-management.sql`。
- 受影响代码：`aether-api-hub-standard` 中与 `Consumer & Auth` 相关的 `domain`、`service`、`adapter`、`api`、`infrastructure` 模块，以及开发者控制台使用的控制器和 DTO。
- 边界冲突提示：当前 `docs/api/` 与 `docs/sql/` 中尚无 Consumer/Auth 权威文档，本提案必须先补顶层文档再进入业务代码；同时不得新增显式 `Consumer` Controller、DTO 或页面流程。
