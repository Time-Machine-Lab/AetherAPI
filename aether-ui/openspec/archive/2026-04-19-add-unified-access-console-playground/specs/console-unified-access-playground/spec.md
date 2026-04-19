## ADDED Requirements

### Requirement: 控制台 SHALL 提供统一接入调用工作台
系统 SHALL 在 `aether-console` 中提供一个受保护的“统一接入调用工作台”页面，并以 `docs/api/unified-access.yaml` 作为唯一权威契约来源。该页面 SHALL 允许用户输入或选择 `apiCode`、选择支持的 HTTP 方法（`GET`、`POST`、`PUT`、`PATCH`、`DELETE`）、在方法允许时填写请求体，并通过前端 API 层发起调用，而不是在页面组件中直接发送裸请求。

#### Scenario: 用户从控制台进入统一接入调用工作台
- **WHEN** 已认证的控制台用户打开统一接入调用工作台页面
- **THEN** 页面展示 `apiCode`、HTTP 方法、API Key、可选请求头和请求体等字段级输入控件
- **THEN** 页面在现有控制台布局内渲染，并遵循控制台关于 surface、field、notice、status 和 action 的语义规则

#### Scenario: 无请求体方法隐藏或禁用请求体输入
- **WHEN** 用户选择 `GET` 或 `DELETE`
- **THEN** 页面 MUST 隐藏或禁用请求体编辑控件
- **THEN** 页面 MUST 阻止主表单提交流程发送请求体载荷

### Requirement: 控制台 SHALL 支持基于 Discovery 的 apiCode 辅助选择
系统 SHALL 允许统一接入调用工作台使用 Discovery 能力作为目标 API 选择的辅助来源，并以 `docs/api/api-catalog-discovery.yaml` 为权威契约依据。该能力 MUST 保持为辅助体验，MUST NOT 取代手动输入 `apiCode` 的路径。

#### Scenario: 用户从 Discovery 结果中选择目标 API
- **WHEN** 用户从基于 Discovery 的辅助选择器中选中一个已启用资产
- **THEN** 调用工作台将该资产的 `apiCode` 回填到输入框中
- **THEN** 页面 MAY 仅基于已文档化的 Discovery 字段预填推荐方法、请求模板和示例快照

#### Scenario: 用户手动输入 apiCode
- **WHEN** 用户不经过 Discovery 选择，而是直接输入一个 `apiCode`
- **THEN** 调用工作台 MUST 允许提交该手动输入值
- **THEN** 页面 MUST NOT 将 Discovery 选择作为调用前置条件

### Requirement: 控制台 SHALL 安全地分类并展示统一接入响应
系统 SHALL 按照 `docs/api/unified-access.yaml` 定义的响应语义展示统一接入调用结果，不得凭空发明固定的成功包裹结构。平台前置失败 MUST 以结构化失败方式展示；成功透传响应 MUST 按响应内容类型分类展示。

#### Scenario: 平台前置失败按结构化方式展示
- **WHEN** 统一接入调用返回 `400`、`401`、`404` 或 `503`，且响应体为平台失败结构
- **THEN** 页面 MUST 在字段可用时展示 `code`、`message`、`failureType`、`traceId` 和 `apiCode`
- **THEN** 页面 MUST 在视觉上将该失败与成功透传响应明确区分

#### Scenario: JSON 成功响应不假设存在 TML 包裹
- **WHEN** 统一接入调用以 `application/json` 成功返回
- **THEN** 页面 MUST 以格式化的原始 JSON 响应方式展示结果
- **THEN** 页面 MUST NOT 假设响应中存在 `code`、`message` 或 `data` 包裹字段

#### Scenario: 二进制成功响应采用非内联解析路径
- **WHEN** 统一接入调用以 `application/octet-stream` 成功返回
- **THEN** 页面 MUST 提供下载导向或原始二进制处理路径
- **THEN** 页面 MUST NOT 默认将该响应按 JSON 解析

### Requirement: 控制台 SHALL 保护 API Key 输入语义
系统 SHALL 将统一接入调用所需的 API Key 输入视为敏感数据，并 MUST 与当前用户 API Key 管理中“明文仅展示一次”的安全模型保持一致。

#### Scenario: API Key 输入不被呈现为可恢复的平台状态
- **WHEN** 用户在当前会话内没有可用的内存态 Key 值时进入调用工作台
- **THEN** 页面 MUST 要求用户显式输入 API Key
- **THEN** 页面 MUST NOT 暗示控制台可以再次从服务端获取完整明文 Key

#### Scenario: API Key 字段提供基础安全控制
- **WHEN** API Key 输入框被渲染
- **THEN** 该输入框 MUST 默认采用遮罩或密码输入样式
- **THEN** 页面 MUST 提供一个清晰可见的方式，用于从表单状态中移除当前 API Key 值
