## Context

后端变更 `add-platform-proxy-profiles` 已将 `docs/api/platform-proxy-profile.yaml` 定义为管理员侧代理档案管理与 API 资产代理绑定的权威契约。API Catalog 只在 `api_asset` 中保存 `proxy_profile_id`，Unified Access 保持代理细节内部化，不向 API 消费者暴露。

`aether-console` 已具备受保护工作区壳层、文件路由、类型化 API slice、请求编排 composable、本地 mock、i18n，以及 `DataListRow`、`DisplayTag`、`FieldGroup`、`FieldLabel`、`StateBlock` 等控制台展示组件。当前鉴权模型只提供轻量 `currentUser.role` 字符串，不是完整权限系统。

实现必须保持平台代理设置与当前用户资产 owner 表单隔离。代理主机、端口和凭据属于平台运维数据；资产 owner 与 API 消费者不应通过市场、资产工作区、订阅、凭证、调用日志或 Unified Access Playground 查看或编辑这些值。

## Goals / Non-Goals

**Goals:**

- 新增类型化前端 API slice，严格映射 `docs/api/platform-proxy-profile.yaml`。
- 提供管理员侧工作区，支持代理档案列表、筛选、详情、创建、更新、启用、禁用和删除。
- 提供管理员侧绑定流程，允许按 `apiCode` 绑定一个已启用代理档案或解除绑定。
- 安全展示读取响应：显示 `credentialConfigured` 与允许展示的脱敏信息，不重构或展示代理密码。
- 基于管理员能力控制工作区入口与操作，同时以后端 401/403 响应作为最终事实来源。
- 复用现有控制台布局、工作区行节奏、字段组、状态标签、通知/状态块、i18n 和测试模式。

**Non-Goals:**

- 不修改后端 API、SQL 或权威文档。
- 不新增完整 RBAC UI、用户管理、权限编辑器或角色管理。
- 不新增代理健康看板、代理池、负载均衡、故障转移、配额、审批、审计或指标 UI。
- 不在市场、Unified Access 消费者响应、订阅工作区、凭证工作区或当前用户资产 owner 表单中暴露代理档案详情。
- 不新增客户端测试连接按钮，除非后端契约后续定义该能力。

## Decisions

### 1. 新增独立平台代理 API/领域 slice

创建 `src/api/platform-proxy-profile/platform-proxy-profile.api.ts`、`platform-proxy-profile.dto.ts`、`platform-proxy-profile.types.ts` 等前端 slice，对应 `docs/api/platform-proxy-profile.yaml` 中的全部端点。

选择该方案而不是放入 catalog API 的原因：

- 后端契约映射到 `PlatformProxyProfileController.java`，不是当前用户资产管理。
- 代理凭据、启用/删除生命周期属于平台运维，不属于资产 owner 元数据。
- 测试可以明确断言前端使用 `v1/platform/proxy-profiles`，且不会通过 `v1/current-user/assets` 发送代理字段。

### 2. 请求编排放在 composable 中

新增 `usePlatformProxyProfiles` 一类 composable，协调列表筛选、分页、选中详情、表单状态、变更操作和资产绑定状态。工作区组件只消费 composable 并渲染状态。

选择该方案而不是页面内直接请求的原因：

- 前端规范要求请求经过 API/composable 层，页面不得直接写裸请求。
- 代理档案列表/详情与绑定流程需要共享刷新和错误处理逻辑。
- 全局 Store 对阶段一管理员工作区来说过重，该状态只在局部工作区内使用。

### 3. 作为工作区分区接入，而不是新增独立应用

在现有 `ConsoleLayout` 导航中新增受保护工作区分区，例如 `#platform-proxy-profiles`。管理员会话可以渲染工作区；非管理员直接访问时展示明确不可用状态。

选择该方案而不是新增独立路由或应用的原因：

- 后端设计说明阶段一管理员与普通用户使用同一个控制台产品。
- 现有工作区行、紧凑操作和状态反馈已经适配管理场景。
- 放在控制台壳层内可复用现有鉴权、布局和 i18n 基础设施。

### 4. 前端角色控制只作为辅助门禁

前端基于现有 `currentUser.role` 字符串实现一个小型 helper，例如 `isPlatformAdminRole(role)`。实现可识别与后端约定的管理员标签，但代理档案接口返回的 401/403 仍是最终权威。

选择该方案而不是构建完整权限系统的原因：

- 后端变更明确不引入完整 RBAC。
- 轻量 helper 能保持导航清晰，并便于后续替换为真实权限模型。
- 直接访问 URL 时仍必须依赖 API 错误处理兜底。

### 5. 代理凭据采用安全展示策略

创建/更新表单可以包含 `username` 与 `password` 字段，因为 API 接受这些输入。详情/列表响应不得重构秘密值。UI 应将 `credentialConfigured` 显示为状态，编辑时密码字段作为“替换输入”，不能当作已有值预填。

选择该方案而不是显示掩码密码的原因：

- API 契约返回的是 `credentialConfigured`，不是原始密码。
- 显示伪掩码容易让用户误以为前端知道秘密值，也可能把占位符误提交给后端。
- 空的替换输入更容易测试，也更安全。

### 6. 绑定流程与 owner 资产编辑保持分离

绑定工作流应接收 `apiCode`，加载可用的启用代理档案，并调用绑定/解绑端点。现有资产 owner 编辑器不得新增代理主机、端口、用户名、密码或绑定字段。

选择该方案而不是把代理控件加入资产编辑器的原因：

- 后端设计明确代理档案是平台管理员治理能力，不是资产 owner 的上游业务配置。
- 当前用户资产 API 不应暴露代理秘密。
- 独立管理员流程可避免信息泄露到市场或 Unified Access 消费者视图。

## Risks / Trade-offs

- [Risk] 前端角色标签可能与后端管理员能力标签不一致。-> Mitigation: 将角色匹配隔离在 helper 中，记录待确认标签，并以后端 401/403 为权威。
- [Risk] 操作员可能误解空密码输入框代表清空凭据。-> Mitigation: 使用 i18n helper 文案明确说明密码仅在填写时替换；只有后端契约支持时才提供显式清空能力。
- [Risk] 按自由输入 `apiCode` 绑定可能输错。-> Mitigation: 绑定/解绑后展示后端返回的 `AssetProxyBindingResp`，失败时保持错误状态，不猜测本地绑定结果。
- [Risk] 禁用档案仍在列表中出现，可能被误选用于绑定。-> Mitigation: 将启用状态渲染为只读标签，客户端对禁用/删除档案禁用绑定动作，同时仍依赖后端拒绝。
- [Risk] 代理元数据可能通过通用调试面板或 mock 泄露。-> Mitigation: 将代理 DTO 限定在 platform proxy slice，并增加测试确保当前用户资产、市场和 Unified Access adapter 不映射代理主机或凭据字段。

## Migration Plan

1. 新增平台代理档案 DTO、类型、API 函数和本地 mock handlers，对齐 `docs/api/platform-proxy-profile.yaml`。
2. 新增平台代理 composable，覆盖档案管理、筛选、表单、变更后刷新、绑定和角色/错误状态。
3. 新增管理员工作区组件，并接入控制台壳层/工作区导航与 i18n 文案。
4. 增加 API 映射、composable 状态流转、角色门禁、凭据安全展示假设和绑定成功/失败的聚焦测试。
5. 运行 `aether-console` 质量门禁：目标 Vitest、`pnpm type-check`、lint/format 检查和可用的 build。

回滚策略：隐藏或移除控制台导航入口与工作区组件，后端契约保持不变。现有资产 owner、市场、订阅、凭证、调用日志和 Unified Access 前端流程不依赖该新 UI，因此可保持原状。

## Open Questions

- 第一版前端门禁中，哪些 `currentUser.role` 值属于管理员能力：`OWNER`、`ADMIN`、两者都算，还是其他配置标签？
- 操作员留空密码替换字段时，更新请求应省略 password，还是必须等后端契约定义后才提供显式清空凭据动作？
- 绑定工作流是否需要从现有端点加载资产展示元数据，还是阶段一使用 `AssetProxyBindingResp` 加手输 `apiCode` 即可？
