## 1. 权威文档与范围确认

- [x] 1.1 实现前重新阅读 `../docs/spec/AetherAPI 前端技术栈与开发规范文档.md` 和 `aether-console/DESIGN.md`。
- [x] 1.2 确认 `../docs/api/platform-proxy-profile.yaml` 已定义本前端变更所需的全部代理档案管理和资产绑定端点。
- [x] 1.3 确认实现前无需更新 `../docs/api/`、前端统一规范或 `aether-console/DESIGN.md`；若发现缺口，必须先更新权威文档再编码。
- [x] 1.4 与后端行为确认管理员能力对应的 `currentUser.role` 标签，并将前端角色门禁隔离在 helper 中。

## 2. API 契约映射

- [x] 2.1 新增 `src/api/platform-proxy-profile/platform-proxy-profile.dto.ts`，DTO 对齐 `CreatePlatformProxyProfileReq`、`UpdatePlatformProxyProfileReq`、`BindProxyProfileReq`、`PlatformProxyProfileResp`、`PlatformProxyProfilePageResp` 和 `AssetProxyBindingResp`。
- [x] 2.2 新增 `src/api/platform-proxy-profile/platform-proxy-profile.types.ts`，定义前端领域类型、查询参数和请求 body 类型。
- [x] 2.3 新增 `src/api/platform-proxy-profile/platform-proxy-profile.api.ts`，通过统一 axios 实例实现列表、详情、创建、更新、启用、禁用、删除、绑定和解绑函数。
- [x] 2.4 增加 API adapter 测试，断言端点路径、查询/请求体映射、响应映射、可空绑定字段以及不会发送非契约字段。

## 3. Mock 与角色工具

- [x] 3.1 增加本地 mock handlers 和种子数据，覆盖平台代理档案列表/详情/生命周期以及资产绑定/解绑响应。
- [x] 3.2 新增管理员角色 helper，消费 `ConsoleCurrentUser.role`，但不引入完整权限系统。
- [x] 3.3 增加管理员角色匹配与后端 401/403 兜底处理假设的测试。

## 4. Composable 编排

- [x] 4.1 新增 `src/composables/usePlatformProxyProfiles.ts`，管理列表筛选、分页、选中档案、loading、空态、错误、创建/更新表单状态和变更后刷新。
- [x] 4.2 在 composable 中实现启用、禁用、删除、创建和更新动作，且只发送契约支持的请求 body。
- [x] 4.3 在 composable 中实现绑定状态，包括 `apiCode`、选中的已启用档案、绑定结果、解绑结果和失败请求状态保留。
- [x] 4.4 增加 composable 测试，覆盖列表/筛选、生命周期变更、凭据安全编辑默认值、绑定/解绑成功、禁用档案选择拦截和错误状态。

## 5. 工作区 UI

- [x] 5.1 新增 `src/features/platform-proxy/PlatformProxyWorkspace.vue`，复用现有 `DataListRow`、`DisplayTag`、`FieldGroup`、`FieldLabel`、`StateBlock`、`Button` 和 `Input` 模式。
- [x] 5.2 渲染档案列表、筛选、分页、详情、创建/编辑表单、启用/禁用/删除动作，以及带 i18n 文案的 loading/空态/错误状态。
- [x] 5.3 安全渲染凭据状态：展示 `credentialConfigured`，不预填原始密码，并将密码输入视为替换输入。
- [x] 5.4 新增资产绑定面板，包含 `apiCode` 输入、已启用档案选择、绑定动作、解绑动作、返回的绑定结果和失败反馈。
- [x] 5.5 为非管理员会话和直接 hash 访问增加 access-denied 或 unavailable 状态。

## 6. 控制台集成与隔离

- [x] 6.1 将代理档案工作区接入 `src/features/console/console-shell.ts` 和 `src/pages/workspace.vue`，使用受保护工作区 hash，例如 `#platform-proxy-profiles`。
- [x] 6.2 增加 `zh-CN` 和 `en-US` i18n key，覆盖导航、工作区标签、筛选、字段、状态标签、辅助文案、确认文案和错误。
- [x] 6.3 验证现有资产 owner 工作区不暴露代理档案字段，也不会通过当前用户资产 API 发送代理数据。
- [x] 6.4 验证市场、订阅、凭证、调用日志和 Unified Access 视图不渲染代理元数据。

## 7. 验证

- [x] 7.1 运行平台代理 API adapter、角色 helper、composable 和工作区行为的目标 Vitest 套件。
- [x] 7.2 运行受影响的现有测试：控制台壳层/工作区、资产 API/工作区、市场、Unified Access 和 mocks。
- [x] 7.3 在 `aether-console` 运行 `pnpm type-check`。
- [x] 7.4 运行可用的 lint/format 检查和 build。
- [x] 7.5 在 `aether-ui` 下运行 `openspec status --change add-platform-proxy-profiles`，确认前端 change 仍为 apply-ready。
