## ADDED Requirements

### Requirement: Import Agent 工作台 MUST 展示计划中的上游请求头

控制台 Import Agent 流程 MUST 把 `assetPlans[].upstreamRequestHeaders` 中的结构化上游请求头作为独立计划内容展示，并与鉴权配置保持分离。

#### Scenario: 计划包含上游请求头

- **WHEN** Import Agent 会话快照中的资产计划包含 `upstreamRequestHeaders`
- **THEN** 计划卡片必须在专门的上游请求头区域展示这些请求头名称和值

#### Scenario: 计划没有上游请求头

- **WHEN** Import Agent 资产计划省略 `upstreamRequestHeaders` 或返回空列表
- **THEN** 流程必须继续渲染资产计划的其余内容，不得报错，也不得产生无意义的空状态噪音

#### Scenario: 疑似敏感值不得过度暴露

- **WHEN** 计划中的上游请求头值看起来像 token、key、Bearer 值、secret 或长不透明凭证
- **THEN** 计划展示应该避免突出完整值，并且不得把它复制到无关摘要文案中

### Requirement: Import Agent 工作台 MUST 支持请求头澄清回答

控制台 Import Agent 流程 MUST 支持指向上游请求头字段的结构化澄清项，并通过现有澄清答案提交流程处理。

#### Scenario: 展示请求头值澄清项

- **WHEN** 后端返回的澄清项目标路径指向 `assetPlans[].upstreamRequestHeaders`
- **THEN** 流程必须为对应请求头名称或值渲染清晰的输入提示

#### Scenario: 提交请求头澄清回答

- **WHEN** 用户回答请求头澄清项
- **THEN** 流程必须通过现有 append-turn 澄清载荷提交答案，并保留原始澄清 ID、目标路径和字段 key

#### Scenario: 未知请求头澄清字段保持兼容

- **WHEN** 后端返回未来新增、但前端尚未特化处理的请求头相关澄清字段
- **THEN** 流程必须回退到现有通用澄清项渲染，不得导致会话失败
