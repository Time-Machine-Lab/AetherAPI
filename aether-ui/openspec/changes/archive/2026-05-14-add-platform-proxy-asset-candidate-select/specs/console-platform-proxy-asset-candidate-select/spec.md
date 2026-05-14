## ADDED Requirements

### Requirement: 控制台 MUST 支持搜索资产候选并选择绑定目标

`aether-console` SHALL 在平台代理档案工作区的资产绑定面板中提供 API 资产候选搜索能力，通过 `GET /api/v1/platform/proxy-profiles/asset-binding-candidates` 查询候选并允许管理员选择候选填充绑定目标 `apiCode`。

#### Scenario: 管理员搜索并选择资产候选

- **WHEN** 管理员在资产绑定面板输入候选搜索关键字并触发搜索
- **THEN** 控制台调用 `v1/platform/proxy-profiles/asset-binding-candidates`，传入 `keyword`、`page` 和 `size`
- **THEN** 工作区展示返回候选的资产名称、`apiCode`、资产状态、发布者展示名和当前代理绑定摘要
- **WHEN** 管理员选择一个候选资产
- **THEN** 工作区将该候选的 `apiCode` 写入绑定目标

#### Scenario: 候选搜索支持分页和状态展示

- **WHEN** 候选搜索结果包含分页信息
- **THEN** 工作区允许管理员加载上一页或下一页候选
- **THEN** 工作区展示当前候选页码、总数和资产状态标签

### Requirement: 控制台 MUST 保留手工 apiCode 兜底

资产候选搜索 SHALL 作为绑定辅助能力存在。控制台 MUST 保留 `apiCode` 输入能力，使管理员可在搜索失败、候选为空或已知资产编码时继续绑定或解绑。

#### Scenario: 候选搜索失败但手工绑定仍可用

- **WHEN** 候选搜索请求失败
- **THEN** 工作区展示候选搜索错误状态
- **THEN** 工作区不清空管理员已输入的 `apiCode`
- **THEN** 管理员仍可使用已输入的 `apiCode` 和已选择的代理档案提交绑定

#### Scenario: 候选为空时仍可输入 apiCode

- **WHEN** 候选搜索返回空列表
- **THEN** 工作区展示空状态
- **THEN** `apiCode` 输入框仍保持可编辑

### Requirement: 控制台 MUST 不映射候选响应中的敏感代理或上游配置

前端 API adapter SHALL 只从候选响应中映射绑定所需摘要字段。控制台 MUST NOT 将候选响应中的代理主机、代理端口、代理用户名、代理密码、上游鉴权配置、请求模板或示例正文映射到前端领域类型或 UI。

#### Scenario: 候选响应包含额外敏感字段

- **WHEN** 后端或 mock 响应中包含契约外的代理连接字段或上游配置字段
- **THEN** 前端候选领域类型不包含这些字段
- **THEN** 资产绑定面板不展示这些字段
