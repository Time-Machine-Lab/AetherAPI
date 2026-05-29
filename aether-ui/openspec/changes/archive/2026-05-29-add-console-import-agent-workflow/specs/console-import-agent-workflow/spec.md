## ADDED Requirements

### Requirement: 控制台 SHALL 在受保护工作台提供 import-agent 工作流入口
`aether-console` SHALL 在现有受保护控制台工作台内提供 import-agent 入口，使已登录用户能够进入当前用户导入代理工作流，而不需要离开 `ConsoleLayout` 或通过外部工具直接调用接口。

#### Scenario: 用户从侧边导航进入 import-agent 分区
- **WHEN** 已登录用户查看控制台侧边导航并选择 import-agent 入口
- **THEN** 前端 MUST 将目标定位到受保护工作台内的 import-agent 分区
- **THEN** import-agent 分区 MUST 使用现有 `ConsoleLayout` 与工作台页的路由守卫能力

#### Scenario: 用户直接访问 import-agent hash
- **WHEN** 已登录用户直接进入 import-agent 对应的工作台 hash 入口
- **THEN** 前端 MUST 渲染 import-agent 工作流界面，而不是回退到资产管理或其他默认分区

### Requirement: 控制台 SHALL 支持当前用户以对话方式创建并恢复 import-agent 会话
控制台 import-agent 工作流 SHALL 允许当前登录用户通过首条消息创建导入会话，并可在首次发送时附带 `documentSource`、`documentSummary` 与可选的 `publisherDisplayName` 作为高级上下文；前端 MUST 保存最近一次活动会话标识以支持刷新后恢复。

#### Scenario: 用户发送第一条消息创建导入会话
- **WHEN** 用户在对话输入区提交合法的首条导入消息和可选上下文信息
- **THEN** 前端 MUST 调用 `POST /api/v1/current-user/import-agent/sessions`
- **THEN** 前端 MUST 渲染服务端返回的会话状态、轮次记录和当前计划快照

#### Scenario: 用户重新进入工作台时恢复最近会话
- **WHEN** import-agent 分区加载且前端存在当前用户最近一次活动会话的 `sessionId`
- **THEN** 前端 MUST 调用 `GET /api/v1/current-user/import-agent/sessions/{sessionId}` 恢复会话
- **THEN** 恢复成功后 MUST 以服务端返回的最新会话详情替换本地快照

#### Scenario: 缓存会话不可恢复
- **WHEN** 最近活动会话恢复请求返回 404、401 或其他表明当前用户不可继续访问该会话的结果
- **THEN** 前端 MUST 清理本地缓存的活动 `sessionId`
- **THEN** 前端 MUST 回到“创建新会话”的初始界面，而不是继续展示陈旧会话内容

### Requirement: 控制台 SHALL 展示 plan-driven 对话与澄清轮次
控制台 import-agent 工作流 SHALL 按后端会话快照展示用户轮次、代理轮次、当前计划摘要、可执行状态和澄清问题，并允许用户在会话上继续追加澄清消息。

#### Scenario: 会话需要进一步澄清
- **WHEN** 当前会话状态为 `WAITING_FOR_CLARIFICATION`，或当前计划包含 `clarificationQuestions`
- **THEN** 前端 MUST 展示代理提出的澄清问题与已有轮次消息
- **THEN** 前端 MUST 允许用户提交新的澄清消息到 `POST /api/v1/current-user/import-agent/sessions/{sessionId}/turns`

#### Scenario: 用户追加澄清后获得新计划
- **WHEN** 追加轮次请求成功
- **THEN** 前端 MUST 使用返回的完整会话详情替换当前会话视图
- **THEN** 前端 MUST 更新当前计划版本、摘要、澄清问题和轮次消息，而不是仅在本地追加一条用户消息

#### Scenario: 最新计划在消息流内展示
- **WHEN** 会话存在当前计划快照
- **THEN** 前端 MUST 在主对话流中展示最新计划摘要、澄清问题、分类计划、资产计划与确认/执行动作
- **THEN** 右侧信息区 MUST 弱化为会话摘要，而不是继续把计划区作为并列主面板

### Requirement: 控制台 SHALL 支持在对话输入区附加本地文本文件
控制台 import-agent 工作流 SHALL 允许用户在首次消息或后续澄清中附加本地文本文件，并在浏览器内完成文本提取与截断后，把内容映射到现有 import-agent 请求上下文，而不是引入新的后端上传流程。

#### Scenario: 用户在首次消息前附加本地文件
- **WHEN** 用户在尚未创建会话时选择一个受支持的本地文本文件
- **THEN** 前端 MUST 在界面中展示已附加文件摘要与移除入口
- **THEN** 首次发送时 MUST 将文件内容摘要并入 `documentSummary`，并将文件名信息并入 `documentSource` 或等价上下文来源描述

#### Scenario: 用户在后续澄清中附加本地文件
- **WHEN** 用户在已有会话上附加本地文本文件并继续发送消息
- **THEN** 前端 MUST 将文件摘要并入本次澄清消息内容，再调用 `POST /api/v1/current-user/import-agent/sessions/{sessionId}/turns`

#### Scenario: 文件不受支持或超出限制
- **WHEN** 用户选择非文本文件、空文件、过大文件，或单次附加数量超出限制
- **THEN** 前端 MUST 给出明确的本地校验反馈
- **THEN** 前端 MUST 不把不合法文件带入 create/append 请求

### Requirement: 控制台 SHALL 对执行入口实施显式计划确认门禁
控制台 import-agent 工作流 SHALL 把计划确认作为执行前置步骤；当计划未确认、已过期或不可执行时，前端 MUST 阻止启动执行，并提示用户先完成对应动作。

#### Scenario: 用户确认可执行计划
- **WHEN** 当前计划为可执行且其版本尚未被确认
- **THEN** 前端 MUST 调用 `PATCH /api/v1/current-user/import-agent/sessions/{sessionId}/confirm`
- **THEN** 计划确认成功后 MUST 更新当前会话中的 `confirmedPlanVersion`

#### Scenario: 计划未确认时尝试执行
- **WHEN** 当前计划未确认或 `confirmedPlanVersion` 与当前计划版本不一致
- **THEN** 前端 MUST 不把执行按钮视为可直接通过的动作
- **THEN** 前端 MUST 给出与确认门禁相关的明确反馈，而不是静默失败

#### Scenario: 新轮次使旧确认失效
- **WHEN** 用户追加澄清后服务端返回的新会话中 `currentPlanVersion` 已变化，或 `confirmedPlanVersion` 被清空
- **THEN** 前端 MUST 取消旧确认态展示
- **THEN** 前端 MUST 要求用户针对最新计划重新确认后才能执行

### Requirement: 控制台 SHALL 展示运行状态与步骤结果
控制台 import-agent 工作流 SHALL 在执行启动后展示当前运行批次状态、影响的 API 编码、步骤结果与失败原因，并在执行进行中主动刷新运行详情直到进入终态。

#### Scenario: 用户启动导入执行
- **WHEN** 用户对已确认计划触发执行
- **THEN** 前端 MUST 调用 `POST /api/v1/current-user/import-agent/sessions/{sessionId}/runs`
- **THEN** 前端 MUST 立即展示返回的运行批次详情，包括 `runId`、`status`、`planVersion` 和步骤结果初值

#### Scenario: 运行仍在执行中
- **WHEN** 当前运行状态为 `EXECUTING`
- **THEN** 前端 MUST 使用 `GET /api/v1/current-user/import-agent/runs/{runId}` 刷新运行详情
- **THEN** 当前运行进入终态后 MUST 停止继续轮询

#### Scenario: 运行失败或完成
- **WHEN** 运行状态变为 `COMPLETED` 或 `FAILED`
- **THEN** 前端 MUST 展示终态结果、步骤结果和失败原因（若存在）
- **THEN** 前端 MUST 在合适时机同步会话详情，使工作流状态与 `latestRunId`、会话状态保持一致

#### Scenario: 最新运行结果在消息流内展示
- **WHEN** 会话已经产生当前运行批次或前端持有最近一次运行结果
- **THEN** 前端 MUST 在主对话流中展示运行状态、影响的 API、步骤结果和刷新入口
- **THEN** 右侧信息区 MUST 只保留运行状态摘要，而不是承载完整运行详情主视图