## Why

平台代理档案绑定资产时，当前只能由前端手工输入 `apiCode`，容易输错，也无法让管理员确认目标资产名称、状态和当前代理绑定情况。现有 `current-user/assets` 与 `discovery/assets` 都不是平台管理员全量资产候选来源，因此需要新增一个受管理员权限保护的资产搜索接口。

## What Changes

- 在 `docs/api/platform-proxy-profile.yaml` 中新增一个映射到 `PlatformProxyProfileController.java` 的管理员资产搜索端点，用于代理绑定前查询资产候选。
- 搜索结果返回最小可操作字段：`apiCode`、资产名称、资产类型、状态、所有者快照、当前绑定的代理档案摘要和时间字段。
- 查询参数支持 `keyword`、`status`、`boundProfileId`、`page`、`size`，用于前端实现远程搜索下拉框。
- 不新增表结构；接口读取现有 `api_asset.proxy_profile_id` 与 `platform_proxy_profile`，不暴露代理主机、端口、用户名或密码。
- 不改变现有 `current-user/assets`、`discovery/assets`、Unified Access 调用解析和资产 owner 工作台语义。

## Capabilities

### New Capabilities

- `platform-proxy-asset-search`: 平台管理员可在平台代理档案绑定工作流中分页搜索 API 资产候选，并查看当前代理绑定摘要。

### Modified Capabilities

- 无。

## Impact

- 权威 API 文档：更新 `docs/api/platform-proxy-profile.yaml`，继续保持一份 YAML 对应 `PlatformProxyProfileController.java`。
- 权威 SQL 文档：预计不需要修改；若实现证明缺少必要索引，再通过 `tml-docs-spec-generate` 使用 SQL 模板更新 `docs/sql/api-asset.sql`。
- 后端代码：`PlatformProxyProfileController`、对应应用服务、查询 DTO/响应 DTO、Catalog 查询端口与 MyBatis 查询适配器。
- 前端影响：后续 `aether-console` 可基于该接口把代理绑定处的 `apiCode` 手输改为远程搜索下拉框。
- 安全边界：仅管理员能力会话可访问；响应不得泄漏上游鉴权配置、代理连接信息或代理凭据。
