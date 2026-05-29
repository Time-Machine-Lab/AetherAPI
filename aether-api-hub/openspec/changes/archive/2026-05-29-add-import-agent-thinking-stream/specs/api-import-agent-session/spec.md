## MODIFIED Requirements

### Requirement: Import-agent 流式接口必须在最终 session 快照前暴露结构化 thinking 事件
系统 MUST 允许 import-agent 的创建会话流式接口和追加轮次流式接口，在最终 `session` 快照发出前输出结构化 `thinking` 事件，用于描述安全的规划进度摘要。

#### Scenario: 创建会话时先返回 planning thinking，再返回最终 session
- **WHEN** 当前用户调用创建会话的流式接口，且 planner 开始构建首版计划
- **THEN** 流中必须允许输出一个或多个 `thinking` 事件，用于描述事实提取、审查或澄清策略等稳定的规划进度摘要
- **AND** 流最终仍必须以 `session` 事件和终止性的 `done` 事件结束

#### Scenario: 追加轮次时返回 clarification 或 review thinking
- **WHEN** 当前用户追加一条澄清轮次，且 planner 正在更新当前草稿计划
- **THEN** 流中必须允许在 assistant reply delta 或最终 `session` 快照之前，输出描述 merge、review 或 clarification downgrade 决策的 `thinking` 事件

#### Scenario: 旧客户端可以忽略新增的 thinking 事件
- **WHEN** 某个客户端只理解现有的 `status`、`message`、`session`、`error` 和 `done` 事件
- **THEN** 流式契约必须保持为加法式扩展，使该客户端即使忽略 `thinking` 事件，也不影响其消费最终 session 结果

### Requirement: Thinking 事件必须输出安全摘要，而不是原始模型 chain-of-thought
系统 MUST 将 import-agent 的 `thinking` 事件视为安全的过程摘要。系统不得通过流式契约暴露原始模型 chain-of-thought、用户提交的密钥值或内部 payload。

#### Scenario: 带 secret 的鉴权字段不会作为 thinking 内容输出
- **WHEN** planner 或 subagent 的推理过程涉及 `authConfig`、API Key、Bearer Token 或其他携带 secret 的上游配置
- **THEN** 流中只能输出诸如“上游鉴权配置仍然缺失”之类的安全摘要，而不能暴露原始 secret 值

#### Scenario: prompt 与 provider payload 不会通过 thinking 事件输出给客户端
- **WHEN** planner 或 reply provider 在 import-agent 处理过程中构造 prompt、tool 参数或上游 provider payload
- **THEN** 流中不得把这些原始 prompt 正文或完整 provider payload 片段作为 `thinking` 事件内容输出

#### Scenario: 原始模型 reasoning 会被抑制或替换为稳定摘要
- **WHEN** 底层模型产出冗长或不稳定的内部 reasoning 文本
- **THEN** 系统必须抑制该文本，或将其替换为稳定的产品级摘要事件，而不是直接把原始 reasoning 文本转发给客户端
