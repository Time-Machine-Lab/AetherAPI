## Context

`aether-console` 已有平台代理档案工作区，API 层、composable 和 UI 均围绕 `docs/api/platform-proxy-profile.yaml` 实现。当前资产绑定面板使用 `bindingApiCode` 文本输入加代理档案下拉，满足接口调用但体验不足。

后端已在同一个平台代理档案契约中新增 `GET /api/v1/platform/proxy-profiles/asset-binding-candidates`，返回资产候选分页数据，字段限定为绑定所需摘要。前端可以在不改变 owner 资产工作台和市场发现接口的前提下，把绑定处升级为远程搜索选择。

## Goals / Non-Goals

**Goals:**

- 对接 `asset-binding-candidates` 管理员搜索接口。
- 在绑定面板提供可搜索候选列表，选择候选后自动填充 `apiCode`。
- 展示资产名称、`apiCode`、状态、发布者和当前代理绑定摘要，让管理员绑定前能确认目标。
- 搜索失败时保留手工输入 `apiCode` 的兜底能力。
- 继续避免在普通资产 owner 工作台、市场发现和 Unified Access 面板暴露平台代理元数据。

**Non-Goals:**

- 不实现完整的平台资产管理页。
- 不修改后端接口契约或新增前端全局组件库规范。
- 不把代理主机、端口、用户名、密码展示在资产候选列表。
- 不改变绑定/解绑 API 的请求体和结果映射。

## Decisions

### 1. 扩展现有 platform-proxy-profile API slice

新增 DTO、类型和 `listPlatformProxyAssetCandidates` 函数，继续放在 `src/api/platform-proxy-profile/*`。

原因：

- 后端契约仍属于 `PlatformProxyProfileController`。
- 资产候选搜索只服务代理绑定流程，不应混入 current-user catalog API。
- 现有 API adapter 测试可以继续断言端点路径、参数映射和敏感字段不映射。

### 2. 在 composable 中集中编排候选搜索状态

`usePlatformProxyProfiles` 新增候选关键字、候选列表、加载、错误、分页与选择函数。页面组件只渲染状态和触发动作。

原因：

- 绑定、解绑、候选选择和代理档案选择需要共享同一组错误处理和管理员门禁。
- 保持页面组件不直接发起裸请求，符合前端分层规范。
- 测试可以聚焦 composable 状态流转，避免把业务逻辑塞入 Vue 模板。

### 3. 使用轻量搜索列表而不是引入新依赖

绑定面板使用现有 `Input`、`Button`、`DisplayTag` 和列表样式实现远程搜索候选。搜索结果作为紧凑列表展示，点击候选填充 `bindingApiCode`。

原因：

- 项目当前没有通用 Combobox 组件；为单一场景引入新组件依赖会扩大改动面。
- 现有控制台视觉以卡片、输入框、标签和紧凑列表为主，轻量列表更容易保持一致。
- 手工输入兜底需要和搜索结果并存，列表模式比原生 `select` 更适合展示多字段摘要。

### 4. 失败不清空用户输入和上次绑定结果

候选搜索失败只设置候选错误，不覆盖 `bindingApiCode`、`bindingProfileId` 或最后一次 `bindingResult`。

原因：

- 搜索是绑定辅助能力，不应阻断管理员直接输入已知 `apiCode`。
- 已有绑定失败策略是“失败不猜测、不覆盖最后结果”，候选搜索也应保持一致。

## Risks / Trade-offs

- [Risk] 候选搜索接口不可用时下拉无法工作。-> Mitigation：保留 `apiCode` 输入框，搜索失败只提示，不阻断绑定/解绑。
- [Risk] 结果字段较多导致面板拥挤。-> Mitigation：主行展示名称和状态，副行展示 `apiCode`、发布者和当前绑定摘要。
- [Risk] 管理员误以为候选列表就是资产管理入口。-> Mitigation：只提供选择动作，不提供资产编辑/发布/删除入口。
- [Risk] 候选响应未来增加敏感字段。-> Mitigation：API adapter 显式白名单映射前端领域类型，并补测试确认不映射代理连接字段。
