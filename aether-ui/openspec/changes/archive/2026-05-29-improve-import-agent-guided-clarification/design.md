## 上下文

`aether-console` 的 Import Agent 工作区已经符合项目设计方向：单列居中的对话栏、乐观用户 turn、流式 assistant 输出，以及嵌入式 plan / run 卡片。薄弱点在澄清体验。当前 plan card 把 `clarificationQuestions` 渲染成被动文本卡片，而 composer 仍然是唯一输入面。这会迫使用户把结构化缺失字段写成编号 prose。

后端变更 `improve-import-agent-guided-clarification` 将在 `docs/api/api-import-agent.yaml` 中新增结构化 `clarificationItems` 和结构化答案提交。前端实现必须等待该权威 API 更新。现有 `aether-console/DESIGN.md` 已经支持嵌入式 plan 卡片、更强的问题强调、字段输入和单列对话组合，因此除非实现引入超出本页面的新可复用组件模式，否则预计不需要更新设计文档。

## 目标 / 非目标

**目标：**

- 在对话列内把结构化澄清项渲染为 guided controls。
- 允许用户提交精确字段答案，不再依赖编号自由文本。
- 保持自由文本聊天可用于补充指令。
- 基于最终 session 快照展示哪些澄清项已接受、仍待处理或仍阻塞。
- 当后端只返回 legacy `clarificationQuestions` 时平滑降级。

**非目标：**

- 不新增独立路由、侧边栏或脱离 import-agent workspace 的多步向导。
- 不让前端从 thinking events 推断最终计划真相。
- 不在前端实现凭证保险箱。
- 不绕过现有 `src/api/import-agent` API 层，也不在 Vue 组件里直接写 HTTP 调用。

## 决策

### 决策 1：澄清控件放在计划卡片内

计划卡片会按资产、类别或全局计划上下文对 `clarificationItems` 分组。每个 item 根据 `inputType` 映射到控件：

- `SELECT`：基于后端 `options` 渲染 select 或 segmented control。
- `BOOLEAN`：toggle 或 yes/no segmented control。
- `TEXT` / `MULTILINE`：文本输入或多行文本框。

备选方案：独立 modal wizard。拒绝原因是 `DESIGN.md` 要求计划和运行状态嵌入主叙事列。

### 决策 2：答案提交与聊天文本分离

composable 维护一个以 clarification item id 为 key 的 `clarificationDrafts` map。发送时可以同时包含：

- 来自 `clarificationDrafts` 的结构化答案
- 可选的自由文本 `messageDraft`

如果用户只提交结构化答案，可见 pending turn 应展示摘要，例如“已提交 3 项澄清信息”，而不是把字段列表拼成聊天文本。

备选方案：自动拼装编号消息并通过 legacy `message` 字段发送。拒绝原因是这会重新制造解析问题。

### 决策 3：最终 session 快照是状态来源

页面可以在 stream 进行中乐观展示 pending answer 状态，但完成后的 accepted/pending 状态必须来自返回的 `session.currentPlan`。thinking events 可以描述进度，但不能直接驱动 plan card 状态变更。

备选方案：用户点击发送后立刻把字段标记为 accepted。拒绝原因是后端校验仍可能拒绝或重新解释答案。

### 决策 4：legacy fallback 保持简单

如果 `currentPlan.clarificationItems` 不存在或为空，但 `clarificationQuestions` 有值，页面继续展示现有问题卡片和自由文本 composer。这保证交错部署期间旧后端响应仍可用。

备选方案：结构化字段不存在时隐藏 legacy questions。拒绝原因是这会让旧后端响应不可用。

## 风险 / 权衡

- [前后端部署顺序不一致] -> DTO 字段保持 additive，并保留 legacy fallback。
- [计划卡片过于密集] -> 按资产分组，低优先级细节可折叠，只默认展开 active blockers。
- [用户需要补充非字段类上下文] -> 保留 message textarea。
- [答案被拒绝但 UI 看起来已接受] -> 以最终 session 快照为 accepted source，并明确展示剩余 blocker。

## 迁移计划

1. 等待 `../docs/api/api-import-agent.yaml` 定义澄清 item 和 answer。
2. 扩展 DTO 和 domain types，所有新增字段保持可选，旧响应仍可解析。
3. 在 API wrapper 和 composable 中增加结构化答案提交。
4. 在 `ImportAgentWorkspace.vue` 中渲染 guided clarification controls。
5. 增加 labels、statuses 和提交摘要的 i18n 文案。
6. 增加结构化答案提交、legacy fallback 和最终 session 对账测试。

## 待确认问题

- 主发送按钮是否应在聊天输入框为空时提交 guided answers，还是计划卡片需要自己的“提交澄清”按钮？
- 计划变为可执行后，已接受的澄清历史是否继续展示，还是折叠进计划摘要？
