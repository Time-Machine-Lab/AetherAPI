## Context

现有平台代理档案接口由 `docs/api/platform-proxy-profile.yaml` 映射到 `PlatformProxyProfileController.java`，已包含代理档案管理以及按 `apiCode` 绑定/解绑 API 资产的端点。API Catalog 顶层设计明确资产仍由用户拥有，`api_asset` 只保存可空的 `proxy_profile_id`，代理主机和凭据保存在 `platform_proxy_profile`。

前端要把绑定处的 `apiCode` 手输改为远程搜索下拉框，需要一个管理员视角的资产候选查询。不能复用 `GET /current-user/assets`，因为它只返回当前用户自己的资产；也不能复用 `GET /discovery/assets`，因为它只暴露市场已发布资产，且不应包含绑定运维信息。

## Goals / Non-Goals

**Goals:**

- 在 `PlatformProxyProfileController.java` 下新增平台管理员资产候选搜索能力。
- 查询所有未删除 API 资产，支持按关键字、状态、当前绑定代理档案过滤和分页。
- 响应只返回绑定操作需要的最小字段，并提供当前代理档案绑定摘要。
- 保持现有 owner 资产工作台、市场发现和 Unified Access 语义不变。

**Non-Goals:**

- 不新增、修改或重命名 `api_asset`、`platform_proxy_profile` 表字段。
- 不提供资产编辑、发布、下架、删除等平台管理员批量管理能力。
- 不向普通用户、市场发现接口或 Unified Access 响应暴露代理主机、端口、用户名、密码。
- 不引入专用搜索引擎或复杂排序矩阵。

## Decisions

### 1. 将搜索端点放入 PlatformProxyProfileController

新增 `GET /api/v1/platform/proxy-profiles/asset-binding-candidates`，并在 `docs/api/platform-proxy-profile.yaml` 中更新契约。

原因：

- 搜索能力直接服务“代理档案绑定资产”的管理员工作流，与当前绑定/解绑端点属于同一业务入口。
- `docs/api/` 仍保持一份 YAML 对应一个 Controller，不新增跨 Controller 聚合文档。
- 避免把平台管理员绑定候选查询混入 `ApiAssetController.java` 的当前用户资产工作台语义。

替代方案：新增 `PlatformAssetController.java` 和 `platform-asset.yaml`。暂不采用，因为当前需求只是代理绑定辅助查询，不是完整的平台资产管理面。

### 2. 使用专用查询模型而不是复用 owner 资产列表模型

服务层新增面向平台代理绑定的查询入参和响应模型，例如 `ListPlatformProxyAssetCandidateQuery`、`PlatformProxyAssetCandidateModel`、`PlatformProxyAssetCandidatePageResult`。基础设施层通过专用 query port 从 `api_asset` 读取资产摘要，并按需左连接 `platform_proxy_profile` 获取绑定摘要。

原因：

- 现有 `ApiAssetQueryPort` 明确是 owner workspace 查询，带 `ownerUserId` 约束，不适合跨 owner 管理员搜索。
- 候选响应需要展示 `proxyProfileId/profileCode/profileName`，但不能返回代理连接细节。
- 查询模型可以把“只读候选搜索”与资产生命周期写模型分开。

替代方案：扩展 `ApiAssetQueryPort` 增加可空 owner 参数。暂不采用，因为这会削弱当前用户工作台边界，后续容易被误用为全局资产管理接口。

### 3. 不改 SQL 顶层表结构，先复用现有索引

本变更预计只更新 `docs/api/platform-proxy-profile.yaml`，不更新 `docs/sql/`。查询会使用现有 `uk_api_asset_code`、`idx_api_asset_market_visibility`、`idx_api_asset_proxy_profile_id` 等索引；关键字搜索保持轻量，匹配 `api_code`、`asset_name`、`publisher_display_name`。

原因：

- `api_asset.proxy_profile_id` 与相关索引已经存在。
- 下拉框远程搜索的数据量和分页大小可控，阶段一不需要专门搜索表或全文索引。
- 如果实现或压测证明关键字搜索不足，再通过 `tml-docs-spec-generate` 使用 SQL 模板更新具体表的 `.sql` 文档。

### 4. 管理员权限由现有控制台会话门禁复用

新增端点复用当前平台代理档案工作流的角色判断，允许 `OWNER`、`ADMIN`、`PLATFORM_ADMIN`，非管理员请求返回同类权限错误。

原因：

- 搜索结果包含跨 owner 资产候选与当前代理绑定摘要，必须限定管理员能力会话。
- 与绑定/解绑端点保持一致，前端可以把搜索、绑定、解绑视为同一个受保护工作流。

## Risks / Trade-offs

- [Risk] 关键字搜索在资产量增长后可能慢。-> Mitigation：分页大小限制到 100；先复用现有索引，必要时后续单独更新 `docs/sql/api-asset.sql` 增加查询索引。
- [Risk] 管理员看到跨 owner 资产可能扩大可见面。-> Mitigation：响应只包含绑定所需摘要，不返回上游鉴权、请求模板、代理主机或凭据。
- [Risk] 前端误以为候选列表就是资产管理入口。-> Mitigation：接口命名为 `asset-binding-candidates`，只支持只读搜索，不提供资产生命周期动作。
- [Risk] 静态路径与 `/{profileId}` 路由混淆。-> Mitigation：使用明确静态路径并在 WebMvc 测试中覆盖，确保不会落入 profile detail 路由。
