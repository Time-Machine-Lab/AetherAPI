## Why

后端变更 `aether-api-hub/openspec/changes/add-platform-proxy-profiles` 已新增平台管理员维护的代理档案与 API 资产代理绑定能力，但 `aether-console` 还没有对应的前端操作入口。没有前端提案时，平台出站网络治理只能依赖直接调用接口完成。

## What Changes

- 在 `aether-console` 中新增管理员侧平台代理档案工作区，消费 `docs/api/platform-proxy-profile.yaml` 中定义的接口契约。
- 支持代理档案列表、关键字/启用状态筛选、详情查看、创建、更新、启用、禁用和删除。
- 新增管理员侧资产代理绑定流程，通过 `apiCode` 将一个已启用的代理档案绑定到 API 资产，或调用 `/platform/proxy-profiles/asset-bindings/{apiCode}` 解除绑定。
- 按后端契约安全展示代理凭据：表单允许输入用户名/密码，读取响应只展示后端返回的脱敏状态与 `credentialConfigured`，不得展示原始代理密码。
- 保持普通当前用户资产 owner 配置界面与平台代理设置隔离；owner 仍只维护上游 URL、请求方法、上游鉴权、示例和 AI 能力配置。
- API 市场、Unified Access Playground、订阅、凭证和调用日志体验不得展示代理元数据。
- 复用现有控制台壳层、工作区列表行、状态标签、字段组、状态反馈、i18n、API adapter、mock 与测试模式。
- 不在本前端提案中新增完整 RBAC UI、审批流、代理健康看板、代理池、配额、负载均衡、审计看板或后端契约变更。

## Capabilities

### New Capabilities

- `console-platform-proxy-profile-management`: `aether-console` 管理员工作区支持代理档案列表/详情/创建/更新/启用/禁用/删除，并保证凭据安全展示。
- `console-platform-proxy-asset-binding`: `aether-console` 管理员工作区支持按 `apiCode` 将一个已启用平台代理档案绑定到 API 资产，或解除绑定。

### Modified Capabilities

- 无。现有基线 spec `console-api-call-log-pages` 不受影响，本变更也不应向消费者侧 Unified Access 或市场能力暴露代理行为。

## Impact

- 影响应用：`aether-console`。
- 后端权威依赖：`docs/api/platform-proxy-profile.yaml`、`docs/sql/platform_proxy_profile.sql`、`docs/sql/api-asset.sql`、`docs/design/aehter-api-hub/Aether API Hub API Catalog领域设计文档.md`、`docs/design/aehter-api-hub/Aether API Hub Unified Access领域设计文档.md`，以及后端变更 `aether-api-hub/openspec/changes/add-platform-proxy-profiles`。
- 前端权威依赖：`docs/spec/AetherAPI 前端技术栈与开发规范文档.md` 和 `aether-ui/aether-console/DESIGN.md`。
- 可能影响的前端区域：`src/api/platform-proxy-profile/*`、`src/composables/usePlatformProxyProfiles.ts`、`src/features/platform-proxy/PlatformProxyWorkspace.vue`、`src/features/console/console-shell.ts`、`src/pages/workspace.vue`、`src/locales/**/common.ts`、本地 mocks 和相关 `*.spec.ts`。
- 契约变更：预计无。前端消费已生成的 `docs/api/platform-proxy-profile.yaml`。
- 实现时需要处理的潜在冲突：当前控制台会话只暴露轻量 `currentUser.role` 字符串。前端可以基于管理员角色标签隐藏或禁用入口，但后端 API 的鉴权拒绝仍是最终事实来源。
