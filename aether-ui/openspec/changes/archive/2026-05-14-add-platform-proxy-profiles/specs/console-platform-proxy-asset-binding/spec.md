## ADDED Requirements

### Requirement: 控制台 MUST 通过管理员端点绑定代理档案到 API 资产
`aether-console` SHALL 提供管理员能力工作流，通过 `PUT v1/platform/proxy-profiles/asset-bindings/{apiCode}` 将一个平台代理档案绑定到一个 API 资产。

#### Scenario: 管理员将启用档案绑定到资产
- **WHEN** 管理员输入 `apiCode`、选择一个已启用且未删除的代理档案，并提交绑定动作
- **THEN** 控制台使用 `BindProxyProfileReq.profileId` 调用资产绑定端点
- **THEN** 工作区展示 `AssetProxyBindingResp` 返回的 `apiCode`、`proxyProfileId`、`proxyProfileCode` 和 `proxyProfileName`

#### Scenario: 禁用档案不能被选择用于绑定
- **WHEN** 档案列表包含已禁用或已删除的代理档案
- **THEN** 工作区以只读状态展示该档案
- **THEN** 该档案的绑定动作在提交前被禁用或不可用

#### Scenario: 绑定请求失败
- **WHEN** 后端因资产或档案无效、无权限、已禁用、已删除或不存在而拒绝绑定请求
- **THEN** 工作区展示带 i18n 文案的错误状态
- **THEN** 工作区不会用本地猜测值覆盖最后一次已知绑定结果

### Requirement: 控制台 MUST 解除 API 资产代理绑定
`aether-console` SHALL 允许具备管理员能力的会话通过 `DELETE v1/platform/proxy-profiles/asset-bindings/{apiCode}` 移除 API 资产的代理档案绑定。

#### Scenario: 管理员解除资产绑定
- **WHEN** 管理员对某个 `apiCode` 提交解绑动作
- **THEN** 控制台调用该 `apiCode` 对应的解绑端点
- **THEN** 工作区展示返回的 `AssetProxyBindingResp`，其中代理档案字段允许为空

#### Scenario: 解绑请求失败
- **WHEN** 后端拒绝解绑请求
- **THEN** 工作区展示带 i18n 文案的失败状态
- **THEN** 工作区保留当前 `apiCode` 和最后一次已知结果，以便修正或重试

### Requirement: 控制台 MUST 保持代理绑定与当前用户资产编辑隔离
普通当前用户资产 owner 工作区 SHALL NOT 暴露代理主机、代理端口、代理用户名、代理密码或代理档案绑定控件。

#### Scenario: 资产 owner 编辑上游配置
- **WHEN** 普通资产 owner 打开现有资产配置编辑器
- **THEN** 编辑器继续展示上游 URL、请求方法、上游鉴权、示例和 AI 能力字段
- **THEN** 编辑器不展示平台代理档案控件或代理凭据字段

#### Scenario: 当前用户资产 API 响应不包含代理秘密
- **WHEN** 当前用户资产工作区、市场、订阅、凭证、调用日志或 Unified Access UI 消费其现有 API adapter
- **THEN** 这些 adapter 不映射或渲染代理主机、代理端口、代理用户名或代理密码值

### Requirement: 控制台 MUST 避免在消费者侧流程展示代理元数据
市场发现、订阅视图、凭证、调用日志和 Unified Access Playground SHALL NOT 将平台代理档案细节呈现为消费者可见的 API 行为。

#### Scenario: 用户浏览市场资产详情
- **WHEN** 用户打开已发布资产详情
- **THEN** 详情视图不展示代理档案标识、代理端点数据或代理凭据

#### Scenario: 用户调用 Unified Access
- **WHEN** Unified Access 调用通过后端代理路由成功或失败
- **THEN** Playground 渲染既有上游成功或失败语义
- **THEN** Playground 不向用户可见结果添加代理档案元数据
