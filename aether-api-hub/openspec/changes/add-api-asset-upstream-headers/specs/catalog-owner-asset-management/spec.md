## ADDED Requirements

### Requirement: 所有者资产管理 MUST 维护上游请求头

系统 MUST 允许当前认证资产所有者为自有 API 资产创建、修订、清空和读取 nullable 上游请求头配置。该行为的权威文件是 `docs/api/api-asset-management.yaml`（对应 `ApiAssetController.java`）和现有 `docs/sql/api-asset.sql`（对应 `api_asset` 表）。

#### Scenario: 创建带上游请求头的资产

- **WHEN** 当前认证用户提交包含 `upstreamRequestHeaders` 的创建资产请求
- **THEN** 系统将这些请求头条目保存到新建的草稿 API 资产上

#### Scenario: 修订上游请求头

- **WHEN** 资产所有者使用新的 `upstreamRequestHeaders` 列表修订自有资产
- **THEN** 系统持久化该资产修订后的请求头配置

#### Scenario: 清空上游请求头

- **WHEN** 资产所有者使用 null 或空上游请求头配置修订自有资产
- **THEN** 系统按照文档化 API 契约，将该资产请求头配置保存为对应的 null 或空状态

#### Scenario: 在所有者详情中返回上游请求头

- **WHEN** 当前认证用户请求自有 API 资产详情
- **THEN** owner-scoped 资产响应包含 nullable `upstreamRequestHeaders`

#### Scenario: 拒绝不安全请求头名

- **WHEN** 资产所有者尝试保存平台、传输、鉴权或内容语义保留的请求头名
- **THEN** 系统以客户端可见的校验失败拒绝该配置，并且不持久化不安全请求头

#### Scenario: 代码实现前更新权威文档

- **WHEN** 项目实现 API 资产上游请求头存储能力
- **THEN** 必须先更新 `docs/api/api-asset-management.yaml` 和 `docs/sql/api-asset.sql`，再实现后端代码
