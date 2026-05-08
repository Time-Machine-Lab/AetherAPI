## ADDED Requirements

### Requirement: 控制台 MUST 提供管理员代理档案工作区
`aether-console` SHALL 为具备管理员能力的控制台会话提供受保护工作区入口，用于管理 `docs/api/platform-proxy-profile.yaml` 定义的平台代理档案。

#### Scenario: 管理员打开代理档案工作区
- **WHEN** 具备管理员能力的已认证控制台会话打开代理档案工作区入口
- **THEN** 控制台在现有受保护控制台壳层内渲染代理档案管理工作区
- **THEN** 工作区使用现有控制台布局、状态反馈、列表行、标签、字段、动作和 i18n 模式

#### Scenario: 非管理员不能使用代理档案工作区动作
- **WHEN** 不具备管理员能力的已认证控制台会话进入代理档案工作区
- **THEN** 控制台不暴露可用的代理档案变更动作
- **THEN** 后端返回的未认证或无权限响应会被渲染为带 i18n 文案的 access-denied 状态

### Requirement: 控制台 MUST 支持代理档案列表与筛选
代理档案工作区 SHALL 通过 `GET v1/platform/proxy-profiles` 查询平台代理档案列表，且只使用契约定义的 `enabled`、`keyword`、`page` 和 `size` 查询参数。

#### Scenario: 管理员加载代理档案列表
- **WHEN** 管理员打开代理档案工作区
- **THEN** 控制台使用契约定义的分页参数请求代理档案列表端点
- **THEN** 工作区在字段可用时展示返回的 `profileCode`、`profileName`、`proxyType`、`proxyHost`、`proxyPort`、`credentialConfigured`、`enabled` 和时间字段

#### Scenario: 管理员筛选代理档案
- **WHEN** 管理员输入关键字或启用状态筛选条件并提交查询
- **THEN** 控制台只发送 API 契约支持的 `keyword` 和 `enabled` 筛选条件
- **THEN** 列表刷新，并通过现有控制台反馈组件处理 loading、空态和错误状态

### Requirement: 控制台 MUST 支持代理档案生命周期变更
代理档案工作区 SHALL 允许具备管理员能力的会话通过 `docs/api/platform-proxy-profile.yaml` 定义的端点创建、更新、启用、禁用和删除代理档案。

#### Scenario: 管理员创建代理档案
- **WHEN** 管理员提交有效的 `profileCode`、`profileName`、`proxyType`、`proxyHost`、`proxyPort`、可选 `username`、可选 `password` 和可选 `enabled`
- **THEN** 控制台调用 `POST v1/platform/proxy-profiles`，请求体对齐 `CreatePlatformProxyProfileReq`
- **THEN** 工作区基于返回的 `PlatformProxyProfileResp` 刷新档案详情或列表状态

#### Scenario: 管理员更新代理档案
- **WHEN** 管理员编辑既有代理档案并提交表单
- **THEN** 控制台调用 `PUT v1/platform/proxy-profiles/{profileId}`，请求体对齐 `UpdatePlatformProxyProfileReq`
- **THEN** 工作区不发送契约未定义字段

#### Scenario: 管理员切换启用状态
- **WHEN** 管理员启用或禁用代理档案
- **THEN** 控制台调用对应的 `PATCH v1/platform/proxy-profiles/{profileId}/enable` 或 `PATCH v1/platform/proxy-profiles/{profileId}/disable` 端点
- **THEN** 档案行与详情状态反映后端返回的 `enabled` 值

#### Scenario: 管理员删除代理档案
- **WHEN** 管理员确认删除某个代理档案
- **THEN** 控制台调用 `DELETE v1/platform/proxy-profiles/{profileId}`
- **THEN** 工作区基于返回响应更新列表/详情状态，不自行发明本地删除语义

### Requirement: 控制台 MUST 安全渲染代理凭据
代理档案工作区 SHALL 防止原始代理密码被展示、记录、复制，或从档案读取响应中被重构。

#### Scenario: 档案详情存在已配置凭据
- **WHEN** 档案详情返回 `credentialConfigured: true`
- **THEN** 工作区展示只读的已配置凭据状态
- **THEN** 工作区不展示原始密码值

#### Scenario: 管理员编辑凭据
- **WHEN** 管理员打开既有代理档案的编辑表单
- **THEN** 密码输入框被视为替换输入，且不会使用读取响应预填
- **THEN** 可见的辅助说明和标签通过 i18n 提供

#### Scenario: 档案响应没有凭据状态
- **WHEN** 档案响应省略可选 `username` 或返回 `credentialConfigured: false`
- **THEN** 工作区渲染未配置或缺失状态，且不推断任何秘密值
