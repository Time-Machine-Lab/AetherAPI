## ADDED Requirements

### Requirement: Import Agent 计划暴露结构化澄清项

当缺失字段或冲突字段阻止执行时，系统必须在 Import Agent 计划响应中暴露机器可寻址的结构化澄清项。系统必须保留 `clarificationQuestions`，作为由结构化澄清项派生的向后兼容文本摘要。

#### Scenario: 缺失可执行字段时生成结构化澄清项

- **WHEN** planner 生成了一个不可执行计划，原因是资产缺少 `apiCode`、`assetName`、`assetType`、`authConfig` 或 async-task 字段
- **THEN** 返回的计划包含 `clarificationItems`，每个 item 都有稳定 id、目标路径、字段 key、输入类型、required 标记和面向用户的标签
- **AND** 返回的计划同时包含等价的 `clarificationQuestions` 文本，供旧客户端使用

#### Scenario: 枚举字段包含可选项

- **WHEN** 澄清项指向 `assetType`、`authScheme`、`requestMethod` 或 `asyncTaskConfig.authMode` 等枚举型字段
- **THEN** 澄清项包含允许的 option value，客户端可以渲染为 select 或 segmented control

### Requirement: 结构化澄清答案确定性细化当前计划

系统必须在追加 Import Agent turn 时接受结构化澄清答案，并且必须在调用 planner provider 生成下一版计划前，把这些答案应用到当前计划。

#### Scenario: 答案更新匹配的计划字段

- **WHEN** 当前计划中存在一个指向 `/assetPlans/0/authConfig` 的澄清项
- **AND** 用户针对该 item 提交了澄清答案
- **THEN** 下一次 planning request 中的 current plan 已经更新了该资产的 `authConfig`
- **AND** 结果计划不会再次重复同一个缺失字段澄清，除非还有其他校验规则未通过

#### Scenario: 自由文本聊天仍然可用

- **WHEN** 客户端追加 turn 时只带普通 `message`，不带结构化答案
- **THEN** 系统继续通过现有 planning flow 处理该消息
- **AND** session 保持与旧客户端兼容

### Requirement: 匿名资产计划被更新而不是被复制

当结构化答案通过 clarification id 或 target path 指向匿名资产计划时，系统必须保留并更新现有匿名资产计划。用户补充身份字段后，系统不得遗留旧的匿名资产副本。

#### Scenario: 用户为匿名资产提供 apiCode

- **WHEN** 当前计划包含一个没有 `apiCode` 的资产计划
- **AND** 一个澄清答案指向该资产的 `apiCode`
- **THEN** 下一版计划在原资产计划上写入提供的 `apiCode`
- **AND** 下一版计划中不会同时存在更新后的资产和之前的匿名副本

### Requirement: Staged tool-calling 是优先的已配置 planner 路径

当配置的 provider 支持时，系统必须默认使用分阶段 Import Agent tool-calling 路径，同时保留可以关闭 tool-calling 的配置开关用于回滚。

#### Scenario: 已配置环境启动 planner

- **WHEN** Import Agent LLM planning 已完成 provider、base URL 和 model 配置
- **THEN** planner 默认使用 extract / fill / submit tool-calling 阶段
- **AND** 运维人员仍然可以通过现有配置属性关闭 tool-calling
