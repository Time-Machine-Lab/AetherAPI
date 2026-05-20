## ADDED Requirements

### Requirement: 控制台渲染结构化澄清控件

控制台必须在现有 import-agent conversation workspace 内，把 Import Agent `clarificationItems` 渲染为结构化输入控件。这些控件必须嵌入主计划卡片或 assistant response 流中，不得要求单独路由或侧边栏。

#### Scenario: 计划包含结构化澄清项

- **WHEN** 当前 session 的 current plan 包含 `clarificationItems`
- **THEN** import-agent workspace 根据每个 item 的 `inputType` 渲染对应输入控件
- **AND** 当目标元数据可用时，workspace 按相关资产、类别或全局计划上下文对控件分组

#### Scenario: 后端只返回 legacy questions

- **WHEN** 当前 session 的 current plan 有 `clarificationQuestions` 但没有 `clarificationItems`
- **THEN** workspace 继续渲染 legacy question cards
- **AND** 用户可以继续通过自由文本 composer 推进流程

### Requirement: 控制台提交结构化澄清答案

控制台必须通过 Import Agent API 把用户输入的澄清答案作为结构化 answer payload 提交，而不是转换成编号自由文本。

#### Scenario: 用户提交 guided answers

- **WHEN** 用户填写一个或多个澄清控件并发送 turn
- **THEN** API 请求将这些值作为结构化澄清答案提交
- **AND** 只有当用户额外输入聊天文本时，才包含自由文本 message

#### Scenario: 用户不输入聊天文本，只提交答案

- **WHEN** 用户填写澄清控件但让聊天输入框保持为空
- **THEN** workspace 仍然可以提交结构化答案
- **AND** 可见 pending turn 展示提交摘要，而不是原始字段值

### Requirement: 控制台从最终 session 快照对账澄清状态

控制台必须使用最终 Import Agent session 快照作为 accepted、pending 和 remaining clarification state 的事实来源。

#### Scenario: 后端接受答案

- **WHEN** stream 返回最终 `session` 事件，且计划不再包含之前已回答的澄清项
- **THEN** workspace 根据 session 快照更新计划卡片，展示该 blocker 已解决

#### Scenario: 后端仍要求澄清

- **WHEN** stream 返回最终 `session` 事件，且计划仍包含澄清项
- **THEN** workspace 继续展示这些 item 作为剩余 blocker
- **AND** 在计划可执行前，confirm 和 start-run 操作保持禁用

### Requirement: 控制台保留自由文本上下文

控制台必须保留现有 message textarea，使用户可以添加结构化字段无法表达的上下文。

#### Scenario: 用户同时提交字段答案和额外指令

- **WHEN** 用户填写 guided clarification controls，并输入额外说明
- **THEN** workspace 同时提交结构化答案和自由文本 message
- **AND** conversation 展示自由文本 message
