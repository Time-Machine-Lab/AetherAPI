## ADDED Requirements

### Requirement: 公开市场必须列出已启用的 API 资产
`aether-console` 的市场页面必须在无需认证的情况下公开访问，并且必须通过 Discovery 列表契约加载已启用的 API 资产。

#### Scenario: 匿名访客打开市场页
- **WHEN** 用户访问市场路由
- **THEN** 页面调用 `GET /api/v1/discovery/assets`
- **THEN** 页面使用返回结果中的 `apiCode`、展示名称、资产类型和分类摘要渲染每个资产

### Requirement: 市场必须支持按需查看资产详情
市场页面必须允许用户通过调用 Discovery 详情契约查看单个已启用资产的详细信息，而不是在页面中内嵌写模型字段。

#### Scenario: 用户打开资产详情面板
- **WHEN** 用户从市场列表中选择某个资产摘要
- **THEN** 页面调用 `GET /api/v1/discovery/assets/{apiCode}`
- **THEN** 详情面板展示契约返回的请求方法摘要、认证方案摘要、请求模板和示例快照

### Requirement: 市场必须区分标准 API 与 AI API
市场页面必须清晰区分 `STANDARD_API` 与 `AI_API` 资产，并且仅在详情契约返回 AI 能力元数据时渲染相应内容。

#### Scenario: 用户查看 AI API
- **WHEN** 当前选中的资产详情满足 `assetType = AI_API` 且 `aiCapabilityProfile` 非空
- **THEN** 页面在独立的 AI 能力区域中展示提供商、模型、流式支持与能力标签

#### Scenario: 用户查看标准 API
- **WHEN** 当前选中的资产详情满足 `assetType = STANDARD_API`
- **THEN** 页面不渲染 AI 能力区域

### Requirement: 市场必须传达加载、空态与错误状态
市场页面必须为列表和详情请求提供稳定的用户反馈状态。

#### Scenario: Discovery 列表为空
- **WHEN** `GET /api/v1/discovery/assets` 返回空的 `items` 数组
- **THEN** 市场页面渲染空态提示，而不是一个空的卡片网格

#### Scenario: Discovery 请求失败
- **WHEN** 列表请求或详情请求失败
- **THEN** 页面基于标准化后的 HTTP 错误载荷渲染错误提示
