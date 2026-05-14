## Why

平台代理档案工作区的资产绑定面板目前要求管理员手工输入 `apiCode`，容易选错资产，也不能在绑定前确认资产名称、状态、发布者和当前代理绑定。后端已新增 `GET /api/v1/platform/proxy-profiles/asset-binding-candidates`，前端应对接该搜索接口，把手输体验升级为远程搜索选择。

## What Changes

- 在 `aether-console` 的平台代理档案 API slice 中新增资产绑定候选搜索函数，严格映射 `docs/api/platform-proxy-profile.yaml` 的 DTO。
- 在 `usePlatformProxyProfiles` 中新增候选搜索状态、关键字、分页/加载/错误状态和选中候选逻辑。
- 将资产绑定面板中的 `apiCode` 手输控件改为可搜索候选列表，展示 `apiCode`、资产名称、资产状态、发布者和当前代理绑定摘要。
- 保留手工输入兜底能力：管理员仍可直接输入 `apiCode` 绑定或解绑，避免搜索接口临时失败时阻断运维操作。
- 不改变代理档案列表、创建/编辑、启用/禁用、删除和绑定/解绑端点语义。

## Capabilities

### New Capabilities

- `console-platform-proxy-asset-candidate-select`: `aether-console` 平台代理绑定面板支持远程搜索 API 资产候选并选择候选完成绑定/解绑。

### Modified Capabilities

- 无。

## Impact

- 前端 API：`src/api/platform-proxy-profile/*`
- 前端状态编排：`src/composables/usePlatformProxyProfiles.ts`
- 前端 UI：`src/features/platform-proxy/PlatformProxyWorkspace.vue`
- i18n：`src/locales/zh-CN/common.ts`、`src/locales/en-US/common.ts`
- Mock 与测试：`platform-proxy-profile.mock.ts`、API/composable 相关 `*.spec.ts`
- 权威契约：消费已更新的 `docs/api/platform-proxy-profile.yaml`，本前端变更不新增后端接口。
