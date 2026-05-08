## 1. 顶层契约

- [x] 1.1 阅读 `docs/spec/` 后端开发规范、`docs/design/aehter-api-hub/Aether API Hub API Catalog领域设计文档.md`、`docs/api/platform-proxy-profile.yaml`、`docs/sql/api-asset.sql` 和 `docs/sql/platform_proxy_profile.sql`，确认资产搜索只读边界与代理绑定边界。
- [x] 1.2 使用 `tml-docs-spec-generate` 的 API 文档模板更新 `docs/api/platform-proxy-profile.yaml`，新增 `GET /platform/proxy-profiles/asset-binding-candidates`，并保持该 YAML 一对一映射 `PlatformProxyProfileController.java`。
- [x] 1.3 确认本变更不需要修改 `docs/sql/`；若实现阶段发现必须新增索引，必须先使用 `tml-docs-spec-generate` 的 SQL 文档模板更新对应表文件，不得直接改代码绕过顶层文档。

## 2. API 与服务模型

- [x] 2.1 新增资产候选搜索请求/响应 DTO，覆盖 `keyword`、`status`、`boundProfileId`、`page`、`size` 查询参数和分页响应结构。
- [x] 2.2 新增服务层查询模型与结果模型，例如 `ListPlatformProxyAssetCandidateQuery`、`PlatformProxyAssetCandidateModel`、`PlatformProxyAssetCandidatePageResult`。
- [x] 2.3 扩展 `PlatformProxyProfileUseCase` 与 `PlatformProxyProfileApplicationService`，实现管理员角色校验、参数归一化、分页边界和候选搜索编排。

## 3. 查询端口与基础设施

- [x] 3.1 新增专用查询端口，避免复用 owner-scoped 的 `ApiAssetQueryPort` 作为跨 owner 管理员搜索入口。
- [x] 3.2 在基础设施层实现基于 `api_asset` 的候选查询，支持未删除过滤、关键字匹配、状态过滤、`boundProfileId` 过滤和分页计数。
- [x] 3.3 查询绑定摘要时只返回 `proxyProfileId`、`proxyProfileCode`、`proxyProfileName`，不得读取或映射代理主机、端口、用户名、密码。

## 4. Web 接入

- [x] 4.1 在 `PlatformProxyProfileWebDelegate` 中新增候选搜索方法，完成请求参数到服务查询模型、服务结果到响应 DTO 的映射。
- [x] 4.2 在 `PlatformProxyProfileController` 中新增静态路径 `GET /asset-binding-candidates`，确保不会被 `GET /{profileId}` 误匹配。
- [x] 4.3 更新控制台会话拦截或鉴权测试，确认新路径继承平台代理档案管理员权限边界。

## 5. 测试与验证

- [x] 5.1 增加 OpenAPI/Controller 测试，断言端点路径、查询参数、分页响应、静态路径优先级和非管理员拒绝访问。
- [x] 5.2 增加应用服务测试，覆盖关键字搜索、状态过滤、绑定档案过滤、分页边界和角色校验。
- [x] 5.3 增加基础设施查询测试，覆盖未删除过滤、跨 owner 搜索、绑定摘要映射和敏感字段不出现在结果模型。
- [x] 5.4 运行后端相关 Maven 测试，至少覆盖 adapter、service、infrastructure 相关测试类。
- [x] 5.5 运行 `openspec status --change add-platform-asset-search`，确认变更处于可实现状态。
