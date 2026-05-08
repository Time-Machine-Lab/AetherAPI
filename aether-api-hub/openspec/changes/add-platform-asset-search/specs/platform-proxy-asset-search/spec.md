## ADDED Requirements

### Requirement: 平台管理员 MUST 搜索代理绑定资产候选

系统 SHALL 提供受管理员权限保护的只读接口，用于在平台代理档案绑定工作流中分页搜索 API 资产候选。该接口 MUST 定义在 `docs/api/platform-proxy-profile.yaml`，并映射到 `PlatformProxyProfileController.java`。

#### Scenario: 管理员按关键字查询资产候选

- **WHEN** 管理员调用 `GET /api/v1/platform/proxy-profiles/asset-binding-candidates`，并传入 `keyword`、`page` 和 `size`
- **THEN** 系统返回未删除 API 资产的分页结果
- **THEN** `keyword` 匹配范围包括 `apiCode`、资产名称和发布者展示快照
- **THEN** 每个候选项包含 `apiCode`、资产名称、资产类型、状态、发布者展示快照、当前代理绑定摘要和时间字段

#### Scenario: 管理员按资产状态过滤候选

- **WHEN** 管理员传入 `status=DRAFT`、`status=PUBLISHED` 或 `status=UNPUBLISHED`
- **THEN** 系统只返回对应生命周期状态且未删除的 API 资产
- **THEN** 系统不把 `status` 解释为旧的 `ENABLED` 或 `DISABLED` 平台目录语义

#### Scenario: 管理员按当前代理档案过滤候选

- **WHEN** 管理员传入 `boundProfileId`
- **THEN** 系统只返回当前 `proxy_profile_id` 等于该档案 ID 的 API 资产
- **THEN** 返回项的绑定摘要包含 `proxyProfileId`、`proxyProfileCode` 和 `proxyProfileName`

#### Scenario: 非管理员不能搜索资产候选

- **WHEN** 未登录、缺少控制台会话或不具备管理员能力的调用方请求资产候选搜索接口
- **THEN** 系统拒绝请求
- **THEN** 系统不得返回任何跨 owner 资产摘要或代理绑定信息

### Requirement: 资产候选搜索 MUST 不泄漏代理和上游敏感配置

资产候选搜索响应 SHALL 只返回绑定下拉框需要的最小展示字段。系统 MUST NOT 在该接口响应中返回代理主机、代理端口、代理用户名、代理密码、上游鉴权配置、请求模板或请求/响应示例正文。

#### Scenario: 候选资产已绑定代理档案

- **WHEN** 搜索结果中的资产已绑定平台代理档案
- **THEN** 响应只返回该档案的 ID、编码和名称
- **THEN** 响应不返回该档案的连接地址、端口、账号或凭据配置状态

#### Scenario: 候选资产包含上游接入配置

- **WHEN** 搜索结果中的资产存在上游地址、鉴权配置、请求模板或示例数据
- **THEN** 响应不包含这些写模型配置字段
- **THEN** 普通 owner 资产接口和市场发现接口的响应结构不因本接口新增而改变

### Requirement: 资产候选搜索 MUST 保持分页和参数边界

系统 SHALL 对资产候选搜索参数执行边界收敛，避免前端下拉框触发无界查询。

#### Scenario: 分页参数超出边界

- **WHEN** 调用方传入小于 1 的 `page` 或 `size`
- **THEN** 系统按最小值 1 处理
- **WHEN** 调用方传入大于 100 的 `size`
- **THEN** 系统按最大值 100 处理

#### Scenario: 查询参数为空或包含多余空白

- **WHEN** 调用方不传 `keyword`、`status` 或 `boundProfileId`，或传入仅包含空白字符的值
- **THEN** 系统将该参数视为未设置
- **THEN** 系统仍只返回未删除资产
