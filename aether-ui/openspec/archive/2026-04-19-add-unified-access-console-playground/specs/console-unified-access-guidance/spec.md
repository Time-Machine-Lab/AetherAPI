## ADDED Requirements

### Requirement: 控制台 SHALL 说明统一接入的使用边界
系统 SHALL 在控制台中提供统一接入使用说明，帮助开发者从调用方视角理解 Unified Access 的工作方式，并且仅使用 `docs/api/unified-access.yaml`、`docs/api/api-catalog-discovery.yaml` 及相关权威文档中已经明确定义的行为事实。

#### Scenario: 说明内容正确解释请求身份
- **WHEN** 用户阅读统一接入说明区域
- **THEN** 控制台 MUST 明确说明该调用使用 `X-Aether-Api-Key` 作为统一接入凭证
- **THEN** 控制台 MUST 明确区分该凭证与控制台自身的登录态或会话认证

#### Scenario: 说明内容不扩展未文档化行为
- **WHEN** 说明区域描述请求或响应行为
- **THEN** 其内容 MUST 仅提及权威文档中已经定义的方法、错误类型和响应语义
- **THEN** 其内容 MUST NOT 发明额外响应字段、未文档化请求头或新的路由规则

### Requirement: 控制台 SHALL 解释平台前置失败类型以支持调试
系统 SHALL 为 Unified Access 的平台前置失败分类提供面向用户的解释，帮助开发者快速判断失败来自 API 编码校验、凭证校验、目标解析还是目标可用性问题。

#### Scenario: INVALID_CREDENTIAL 提供明确解释
- **WHEN** Unified Access 响应中包含 `failureType = INVALID_CREDENTIAL`
- **THEN** 控制台 MUST 给出说明，指出该失败表示 API Key 无效、不可用，或其归属的调用主体上下文不可用

#### Scenario: 目标解析失败与目标不可用需要区分解释
- **WHEN** Unified Access 响应中包含 `failureType = TARGET_NOT_FOUND` 或 `TARGET_UNAVAILABLE`
- **THEN** 控制台 MUST 给出区分说明，明确“目标无法解析”与“目标存在但当前不可用”不是同一类问题

### Requirement: 控制台 SHALL 引导用户理解透传响应预期
系统 SHALL 帮助用户建立对 Unified Access 成功响应的正确预期，使开发者理解成功结果更接近上游透传语义，可能体现上游状态码、响应头、响应体，以及潜在的流式行为，而不是控制台自定义的统一包裹结构。

#### Scenario: 说明区域提醒用户成功响应是原始透传导向
- **WHEN** 用户打开调用工作台中的响应帮助或使用说明
- **THEN** 控制台 MUST 明确说明成功响应不会使用平台管理接口的 `TML-SDK Result` 结构进行包装
- **THEN** 控制台 MUST 引导用户在排查上游行为时优先查看原始响应内容

#### Scenario: 流式能力提示取决于目标 API 能力
- **WHEN** 当前所选 Discovery 资产标记 `streamingSupported = true`
- **THEN** 控制台 MUST 提示该目标 API 可能存在流式输出行为
- **THEN** 控制台 MUST NOT 承诺超出当前实现能力之外的前端流式交互体验
