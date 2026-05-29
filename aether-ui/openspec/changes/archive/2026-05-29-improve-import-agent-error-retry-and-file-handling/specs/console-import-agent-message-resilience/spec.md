## ADDED Requirements

### Requirement: import-agent 发送失败后 MUST 保留对话中的已发送消息
`aether-console` import-agent 在 create-session 或 append-turn 请求失败时，MUST 保留乐观展示的用户轮次，并 MUST 在同一对话流中把后端错误或前端标准化错误展示为代理回复。

#### Scenario: 创建会话流返回错误
- **WHEN** 用户发送第一条 import-agent 消息，且 create-session stream 返回 `error` 事件或在最终 `session` 快照可用前请求失败
- **THEN** 对话流 MUST 保留本次提交的用户消息，并将其展示为最新用户轮次
- **THEN** 对话流 MUST 展示一条包含解析后错误信息的代理错误回复
- **THEN** 本次发送尝试结束后，输入框草稿 MUST 保持已清空状态

#### Scenario: 追加轮次流返回错误
- **WHEN** 用户发送后续 import-agent 消息或结构化澄清答案，且 append-turn stream 返回 `error` 事件或在最终 `session` 快照可用前请求失败
- **THEN** 对话流 MUST 保留本次提交的用户消息或澄清提交摘要，并将其展示为最新用户轮次
- **THEN** 对话流 MUST 展示一条包含解析后错误信息的代理错误回复
- **THEN** 本次发送尝试结束后，输入框草稿 MUST 保持已清空状态

#### Scenario: 失败前已经收到部分流式内容
- **WHEN** create-session 或 append-turn stream 在返回错误前已经产生 status、thinking 或 assistant message delta
- **THEN** 对话流 MUST 保留本次提交的用户轮次
- **THEN** 最终可见的失败代理回复 MUST 包含解析后的错误信息
- **THEN** 除非已经收到合法的 `session` 事件，否则失败状态 MUST NOT 替换当前已确认的服务端会话快照

### Requirement: import-agent 发送失败后 MUST 支持重新发送
`aether-console` import-agent MUST 为最近一次失败的 create-session 或 append-turn 请求提供重新发送动作，并 MUST 使用同一份请求 payload 重新发送，而不要求用户重新输入消息。

#### Scenario: 重新发送失败的第一条消息
- **WHEN** 第一条 import-agent 消息发送失败，且用户点击代理错误回复上的重新发送动作
- **THEN** 前端 MUST 使用失败请求原始的 `CreateImportAgentSessionInput` payload 调用 create-session stream 端点
- **THEN** 重发期间前端 MUST 保留原始 pending 用户消息
- **THEN** 重发成功后，前端 MUST 使用返回的服务端会话快照替换 pending 状态

#### Scenario: 重新发送失败的后续消息
- **WHEN** append-turn 请求失败，且用户点击代理错误回复上的重新发送动作
- **THEN** 前端 MUST 使用失败请求原始的 session id 和 `AppendImportAgentTurnInput` payload 调用 append-turn stream 端点
- **THEN** 重发期间前端 MUST 保留原始 pending 用户消息或澄清提交摘要
- **THEN** 重发成功后，前端 MUST 使用返回的服务端会话快照替换 pending 状态

#### Scenario: 发送成功后清理重发状态
- **WHEN** create-session 或 append-turn 请求收到合法的最终 `session` 快照
- **THEN** 前端 MUST 清理该请求对应的失败代理回复和重发 payload
- **THEN** 已完成发送的对话内容 MUST NOT 再显示重新发送动作

### Requirement: import-agent 附件 MUST 将完整提取文本交给后端校验
`aether-console` import-agent 在构建 create-session 和 append-turn 请求内容时，MUST 使用受支持本地文本附件的完整提取文本，同时保持界面附件预览紧凑。

#### Scenario: 附件文本超过界面预览长度
- **WHEN** 用户附加一个受支持且非空的文本文件，并且该文件内容长度超过界面预览长度
- **THEN** 附件卡片 MAY 只展示缩短后的预览和预览已截断提示
- **THEN** create-session 的 `documentSummary` 或 append-turn 的 `message` payload MUST 包含完整提取文本
- **THEN** 前端 MUST NOT 仅因为内容超过预览长度而截断发往后端的请求内容

#### Scenario: 后端拒绝过大的附件内容
- **WHEN** 用户发送的 import-agent 请求包含完整附件文本，且该内容超过后端限制
- **THEN** 前端 MUST 让后端通过现有 HTTP error 或 stream `error` 契约返回校验错误
- **THEN** 对话流 MUST 保留本次提交的用户轮次
- **THEN** 代理错误回复 MUST 展示解析后的后端错误信息，并在失败请求可重放时展示重新发送动作

#### Scenario: 不支持的本地文件仍在前端拦截
- **WHEN** 用户选择不支持的文件类型、空文件，或单次选择文件数量超过本地附件数量限制
- **THEN** 前端 MUST 继续展示本地校验反馈
- **THEN** 前端 MUST NOT 将无效文件带入 create-session 或 append-turn 请求内容
