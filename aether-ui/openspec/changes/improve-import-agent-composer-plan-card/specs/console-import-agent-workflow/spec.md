## MODIFIED Requirements

### Requirement: 控制台 import-agent 工作流必须在发送消息后清空输入框
控制台 import-agent 工作流 MUST 在用户提交首条消息或追加轮次后，立即清空当前 composer 文本输入，同时保证本次请求仍使用提交瞬间构造出的 payload。

#### Scenario: 首条消息发送后清空输入框
- **WHEN** 用户在尚未创建会话时填写导入意图并触发发送
- **THEN** 前端必须先构造 create-session 请求 payload 和 pending user turn
- **AND** 前端必须立即清空首条消息输入框
- **AND** 流式响应仍必须使用提交前的导入意图创建会话

#### Scenario: 追加轮次发送后清空输入框
- **WHEN** 用户在已有会话中填写后续消息并触发发送
- **THEN** 前端必须先构造 append-turn 请求 payload 和 pending user turn
- **AND** 前端必须立即清空后续消息输入框
- **AND** 流式响应仍必须使用提交前的消息内容追加轮次

#### Scenario: 发送失败后不恢复陈旧输入
- **WHEN** create-session 或 append-turn 流式请求失败
- **THEN** 前端必须展示既有错误提示
- **AND** 前端不得因为失败自动把已提交内容重新塞回 composer，避免与 pending turn 和后续编辑状态冲突

### Requirement: 控制台 import-agent 当前计划卡片必须支持展开和收缩
控制台 import-agent 工作流 MUST 允许用户展开或收缩“当前计划”卡片，并在收缩态保留足够的计划识别信息和重新展开入口。

#### Scenario: 用户手动收缩当前计划
- **WHEN** 当前会话存在 currentPlan 且计划卡片处于展开态
- **THEN** 前端必须提供可访问的收缩操作
- **AND** 用户触发后，计划卡片必须隐藏摘要、澄清控件、分类计划和资产计划等详细内容
- **AND** 收缩态必须继续展示计划标题、关键状态和展开入口

#### Scenario: 用户手动展开当前计划
- **WHEN** 当前计划卡片处于收缩态
- **THEN** 前端必须提供可访问的展开操作
- **AND** 用户触发后，计划卡片必须重新展示完整计划内容和相关操作入口

### Requirement: 控制台 import-agent 当前计划卡片必须随流式回答自动收缩和展开
控制台 import-agent 工作流 MUST 在模型流式回答期间自动收缩“当前计划”卡片，并在回答完成且会话快照更新后自动展开。

#### Scenario: 发送消息后计划卡片自动收缩
- **WHEN** 用户触发 create-session 或 append-turn，并且前端开始展示 pending turn 或流式状态
- **THEN** 当前计划卡片必须自动进入收缩态
- **AND** 最新用户消息、thinking 时间线和回复增量必须成为主视图焦点

#### Scenario: 回答完成后计划卡片自动展开
- **WHEN** 本次流式请求完成并且前端收到最终 session 快照
- **THEN** 当前计划卡片必须自动进入展开态
- **AND** 用户必须可以立即查看最新计划、澄清控件、确认入口或执行入口

#### Scenario: 没有当前计划时不渲染计划折叠控件
- **WHEN** 当前会话没有 currentPlan
- **THEN** 前端不得渲染空的计划卡片或无效展开 / 收缩控件
