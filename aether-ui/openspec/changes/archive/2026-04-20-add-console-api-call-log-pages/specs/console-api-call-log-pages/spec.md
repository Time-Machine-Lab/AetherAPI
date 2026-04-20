## ADDED Requirements

### Requirement: 控制台 MUST 提供当前用户 API 调用日志入口
`aether-console` MUST 在受保护控制台内提供当前用户 API 调用日志入口，使用户可以从现有控制台壳层进入日志查询体验，而不需要跳转到第二套应用或未认证页面。

#### Scenario: 用户从控制台进入 API 调用日志分区
- **WHEN** 已登录用户通过控制台侧边栏或工作区导航进入 API 调用日志
- **THEN** 系统在现有受保护控制台中渲染该分区
- **THEN** 该分区 MUST 与现有控制台的导航、布局和状态反馈语义保持一致

### Requirement: 控制台 MUST 支持当前用户日志列表、分页与最小筛选
控制台 MUST 基于 `docs/api/api-call-log.yaml` 提供当前用户 API 调用日志的分页列表，并支持按目标 API 编码与调用时间范围进行最小筛选。

#### Scenario: 用户查看自己的调用日志列表
- **WHEN** 已登录用户打开 API 调用日志分区且未提供额外筛选条件
- **THEN** 系统 MUST 请求当前用户日志列表接口
- **THEN** 页面 MUST 以分页列表形式展示当前用户范围内的日志摘要
- **THEN** 每条摘要在字段可用时 MUST 展示 `targetApiCode`、`targetApiName`、`requestMethod`、`invocationTime`、`durationMs`、`resultType`、`success` 与 `httpStatusCode`

#### Scenario: 用户按目标 API 与时间范围筛选日志
- **WHEN** 用户提供 `targetApiCode`、`invocationStartAt` 或 `invocationEndAt` 中的任意组合
- **THEN** 系统 MUST 仅使用契约中定义的查询参数发起列表请求
- **THEN** 页面 MUST 使用新的筛选结果刷新日志列表
- **THEN** 页面 MUST 不发明契约中不存在的筛选条件

#### Scenario: 当前用户暂无调用日志
- **WHEN** 当前用户日志列表接口返回空结果
- **THEN** 页面 MUST 展示明确空态
- **THEN** 空态文案 MUST 保持国际化

### Requirement: 控制台 MUST 支持当前用户单条调用日志详情查看
控制台 MUST 支持用户查看属于自己范围内的单条调用日志详情，并在详情区域展示契约中定义的扩展信息而不推导未返回字段。

#### Scenario: 用户查看一条成功日志详情
- **WHEN** 用户选中一条属于自己范围内的调用日志
- **THEN** 系统 MUST 请求该日志详情接口
- **THEN** 详情区域 MUST 在字段可用时展示 `accessChannel`、`credentialCode`、`credentialStatus`、`createdAt` 与 `updatedAt`
- **THEN** 若存在 `aiExtension`，详情区域 MUST 展示 `provider`、`model`、`streaming` 与 `usageSnapshot`

#### Scenario: 用户查看一条失败日志详情
- **WHEN** 用户选中一条包含错误信息的调用日志
- **THEN** 详情区域 MUST 在字段可用时展示 `errorCode`、`errorType` 与 `errorSummary`
- **THEN** 页面 MUST 以后端返回错误对象为唯一事实来源
- **THEN** 页面 MUST 不展示完整原始请求体或响应体

#### Scenario: 用户请求不属于自己范围的日志详情
- **WHEN** 详情接口返回当前用户不可查看该日志的失败结果
- **THEN** 页面 MUST 展示边界清晰的错误反馈
- **THEN** 页面 MUST 不泄露该日志是否属于其他用户或额外日志元数据

### Requirement: 控制台 MUST 正确表达日志查询的状态反馈与契约边界
控制台 MUST 为加载中、请求失败、无结果与无效参数等场景提供清晰反馈，并保持所有说明与错误提示均在现有契约与前端规范边界内。

#### Scenario: 日志列表或详情请求失败
- **WHEN** 列表请求或详情请求失败
- **THEN** 页面 MUST 提供明确的错误状态反馈
- **THEN** 若响应中提供 `traceId` 或稳定错误码，页面 MUST 在不泄露额外信息的前提下按需展示排查线索

#### Scenario: 接口返回无效查询错误
- **WHEN** 用户提交的筛选条件触发契约定义的无效查询错误
- **THEN** 页面 MUST 反馈查询条件无效
- **THEN** 页面 MUST 允许用户调整筛选条件并重新发起查询

#### Scenario: 页面展示所有用户可见文案
- **WHEN** 页面渲染标题、筛选标签、空态、错误反馈或详情说明
- **THEN** 所有用户可见文案 MUST 通过现有 i18n 体系提供
- **THEN** 页面 MUST 不硬编码最终用户可见中文或英文文本
