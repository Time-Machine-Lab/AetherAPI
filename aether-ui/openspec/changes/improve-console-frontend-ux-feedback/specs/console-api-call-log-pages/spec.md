## MODIFIED Requirements

### Requirement: 控制台 MUST 支持当前用户单条调用日志详情查看
控制台 MUST 支持用户查看属于自己范围内的单条调用日志详情，并在详情区域展示契约中定义的扩展信息而不推导未返回字段。详情区域 MUST 明确区分当前契约已提供的日志字段与尚未由 `docs/api/api-call-log.yaml` 暴露的请求/响应诊断字段。

#### Scenario: 用户查看一条成功日志详情
- **WHEN** 用户选中一条属于自己范围内的调用日志
- **THEN** 系统 MUST 请求该日志详情接口
- **THEN** 详情区域 MUST 在字段可用时展示 `accessChannel`、`credentialCode`、`credentialStatus`、`createdAt` 与 `updatedAt`
- **THEN** 若存在 `aiExtension`，详情区域 MUST 展示 `provider`、`model`、`streaming` 与 `usageSnapshot`
- **THEN** 详情区域 MUST 在字段可用时以清晰的状态或标签展示 `httpStatusCode`

#### Scenario: 用户查看一条失败日志详情
- **WHEN** 用户选中一条包含错误信息的调用日志
- **THEN** 详情区域 MUST 在字段可用时展示 `errorCode`、`errorType` 与 `errorSummary`
- **THEN** 页面 MUST 以后端返回错误对象为唯一事实来源
- **THEN** 页面 MUST 不展示完整原始请求体或响应体

#### Scenario: 用户查看尚未暴露的请求响应诊断信息
- **WHEN** 用户在日志详情中寻找上游地址、请求体、响应体、请求头或响应头
- **THEN** 页面 MUST 明确提示这些字段当前未由日志接口契约暴露
- **THEN** 页面 MUST NOT 使用资产配置、调用输入缓存或推导值伪装成该次调用的真实日志快照

#### Scenario: 用户请求不属于自己范围的日志详情
- **WHEN** 详情接口返回当前用户不可查看该日志的失败结果
- **THEN** 页面 MUST 展示边界清晰的错误反馈
- **THEN** 页面 MUST 不泄露该日志是否属于其他用户或额外日志元数据

### Requirement: 控制台 MUST 正确表达日志查询的状态反馈与契约边界
控制台 MUST 为加载中、请求失败、无结果、无效参数与字段不可用等场景提供清晰反馈，并保持所有说明与错误提示均在现有契约与前端规范边界内。

#### Scenario: 日志列表或详情请求失败
- **WHEN** 列表请求或详情请求失败
- **THEN** 页面 MUST 提供明确的错误状态反馈
- **THEN** 若响应中提供 `traceId` 或稳定错误码，页面 MUST 在不泄露额外信息的前提下按需展示排查线索

#### Scenario: 接口返回无效查询错误
- **WHEN** 用户提交的筛选条件触发契约定义的无效查询错误
- **THEN** 页面 MUST 反馈查询条件无效
- **THEN** 页面 MUST 允许用户调整筛选条件并重新发起查询

#### Scenario: 日志详情字段受契约限制
- **WHEN** 页面展示请求/响应诊断区域但接口未提供对应字段
- **THEN** 页面 MUST 使用非错误态说明该字段当前不可用
- **THEN** 页面 MUST 引导用户理解这需要后续更新 `docs/api/api-call-log.yaml` 与后端日志快照能力

#### Scenario: 页面展示所有用户可见文案
- **WHEN** 页面渲染标题、筛选标签、空态、错误反馈或详情说明
- **THEN** 所有用户可见文案 MUST 通过现有 i18n 体系提供
- **THEN** 页面 MUST 不硬编码最终用户可见中文或英文文本
