## ADDED Requirements

### Requirement: 工作台 MUST 维护上游请求头

控制台资产工作台 MUST 允许资产所有者基于已记录的当前用户资产契约编辑、清空、保存和查看可为空的上游请求头配置。

#### Scenario: 现有请求头加载到编辑表单

- **WHEN** 资产详情响应包含 `upstreamRequestHeaders`
- **THEN** 打开资产编辑器时必须预填对应的请求头 name/value 行

#### Scenario: 保存请求头行

- **WHEN** 用户保存包含上游请求头行的自有资产
- **THEN** 修订资产请求必须包含 `upstreamRequestHeaders`，并携带规范化后的 name/value 条目

#### Scenario: 空白或已移除请求头会被清空

- **WHEN** 用户移除所有上游请求头行并保存资产
- **THEN** 修订资产请求必须发送契约中声明的空值或 null，以清空上游请求头

#### Scenario: 所有者详情展示请求头

- **WHEN** 自有资产详情包含上游请求头
- **THEN** 工作台必须将其作为仅所有者可见的上游配置展示，并在视觉上与 `authConfig` 分离

#### Scenario: 市场页面不暴露所有者请求头

- **WHEN** 用户查看已发布市场详情或导出市场文档
- **THEN** 控制台不得展示或推断仅所有者可见的上游请求头，除非未来 Discovery 显式暴露浏览安全的契约

### Requirement: 工作台 MUST 分离上游鉴权和额外请求头

控制台资产工作台 MUST 将 `authScheme` / `authConfig` 编辑与固定非鉴权上游请求头分离。

#### Scenario: 请求头编辑器不替代鉴权配置

- **WHEN** 资产使用 `HEADER_TOKEN` 或 `QUERY_TOKEN` 鉴权
- **THEN** 鉴权配置仍保留在现有鉴权字段中，不得移动到 `upstreamRequestHeaders`

#### Scenario: 保留请求头依赖后端校验

- **WHEN** 后端因请求头名称保留或不安全而拒绝保存上游请求头
- **THEN** 工作台必须展示现有规范化错误状态，不得静默改写用户配置
