## 上下文

当前 `aether-console` 的 import-agent 工作区已经实现了基于 SSE 的流式体验：`useImportAgentWorkspace` 会消费 `status`、`message`、`session`、`error`、`done` 事件，页面则用 `streamingPhase`、`streamingStatusMessage` 和 `streamingReply` 渲染一块“代理正在回复”的流式区域。这对最终 reply 的增量显示是足够的，但对 planning 过程仍然不透明。

与此同时，后端现在的 planner / subagent 内部已经拥有明确的阶段和决策点，但这些内容只存在于日志，不在前端协议中。前端如果想把“思考过程”展示给用户，不能把 thinking 和最终 reply 混成一条聊天消息，也不能指望从 reply 文本里再反推 reasoning。更合理的方式是对接后端新增的 `thinking` SSE 事件，在 UI 中给 reasoning 留一个独立容器。

本次前端 change 的目标是承接这种契约升级：把 thinking 当作一类新流事件处理，在现有 import-agent 工作区中以独立时间线展示，并在后端未返回 thinking 时优雅退化。

## 目标 / 非目标

**目标：**

- 让 `aether-console` 能解析 import-agent stream 中新增的 `thinking` 事件。
- 在 import-agent 工作区中把 thinking 摘要与最终 reply 分开显示，提升用户对规划进度的可理解性。
- 保持现有 `status`、`message`、`session`、`error`、`done` 行为不变，使 thinking 成为增量能力。
- 遵循现有前端技术栈、i18n、工作区组织和 `aether-console/DESIGN.md` 的视觉风格。

**非目标：**

- 不在前端自行生成或猜测 thinking 内容；thinking 必须来自后端结构化事件。
- 不把 thinking 文本作为聊天消息持久化到 turns 列表中。
- 不新增顶级路由、独立页面或新的状态管理方案。
- 不在本次提案中引入 raw CoT 展开器、调试日志面板或开发者专属 tracing 工具。

## 设计决策

### 决策 1：thinking 与 reply 必须分开建模和展示

前端状态模型不应继续只有 `streamingReply`。需要新增独立的 `streamingThoughts` 或等价时间线状态，用于保存流式 thinking 事件。

不采用“把 thinking 拼接进 `streamingReply`”的原因：那会让用户无法区分系统思考摘要与最终助手回复，也会污染最终对话语义。

### 决策 2：SSE parser 以加法方式支持 `thinking` 事件

`src/api/import-agent/import-agent.api.ts` 中的 SSE 解析逻辑需要新增 `thinking` 分支，但现有事件解析和默认分支保持不变。当前端尚未收到 `thinking` 事件时，仍然继续按 `status + message + session` 工作。

不采用“重写现有事件结构”的原因：thinking 只是对现有流协议的增强，不应把前端解析链路变成破坏性重写。

### 决策 3：thinking UI 以时间线或步骤卡形式插入现有流式区域

thinking 最适合显示为流式时间线，例如：

- 提取文档事实
- 识别鉴权方式
- 检查异步任务模式
- 审查冲突字段
- 生成澄清问题

这些内容应出现在当前“代理正在回复”区域的上方或内部单独分组中，而不是另开新的复杂侧栏。

不采用“单独再开一列 debug 面板”的原因：当前 import-agent 工作区已经包含对话、计划和运行结果，新增独立大面板会破坏现有主线阅读流。

### 决策 4：thinking 区域默认轻量展示，并允许在没有 thinking 时完全折叠

thinking 区域只在流式过程中或已有 thinking 事件时显示；如果后端没有返回 thinking，页面应退化为当前的流式状态和 reply 展示，不出现空壳容器。

不采用“始终保留一个空 reasoning 面板”的原因：会增加视觉噪声，也让未开启 thinking 能力的环境显得像故障态。

### 决策 5：thinking 文案和状态标签继续走 i18n

thinking 模块新增的标题、空态和标签文案必须进入 `src/locales/zh-CN/common.ts` 与 `src/locales/en-US/common.ts`，保持与现有 import-agent 工作区一致。

不采用“在组件内硬编码‘思考中’文案”的原因：违反当前前端规范，也不利于后续产品文案调整。

### 决策 6：前端不对 thinking 做持久化真相判断

thinking 事件只是流式辅助反馈。最终会话状态、计划版本和澄清问题仍以 `session` 事件和后续 REST 会话详情为准。前端不应根据 thinking 时间线去推导最终 plan state。

不采用“根据 thinking 更新计划卡片局部内容”的原因：会引入前端派生事实，与服务端会话真相源冲突。

## 风险与权衡

- [thinking 事件过多会让界面嘈杂] -> 仅渲染后端提供的稳定摘要事件，并通过紧凑时间线样式降低视觉负担。
- [后端尚未升级 thinking 契约时前端无法看到新内容] -> 保持对旧协议的完全兼容，未收到 thinking 时正常退化。
- [thinking 与状态提示重复] -> 保留 `streamingStatusMessage` 作为粗粒度阶段提示，thinking 只展示更具体的步骤摘要。
- [流式状态在 create / append 之间串台] -> 在每次新流开始时显式 reset `streamingThoughts`、`streamingReply` 和相关状态。

## 迁移计划

1. 先对齐后端 change 更新后的 `../docs/api/api-import-agent.yaml` thinking 事件契约。
2. 扩展前端 import-agent DTO / types / API 解析链路，新增 `thinking` 事件类型和回调。
3. 在 `useImportAgentWorkspace` 中增加 `streamingThoughts` 状态与重置逻辑。
4. 在 `ImportAgentWorkspace.vue` 中新增 thinking 时间线展示，并补齐 i18n。
5. 增补 composable 和页面行为测试，确认 thinking 事件与既有 reply / status 流程可共存。
