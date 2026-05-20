## 背景

控制台目前把 Import Agent 的澄清过程渲染成聊天文本和被动问题卡片。用户必须在输入框里用编号自由文本回答问题，这种方式非常脆弱，并且无法清楚反馈哪些答案已经被接受。

本变更把澄清体验改为现有会话界面内的 guided operator workflow：用户直接填写或确认缺失字段，计划卡片展示哪些信息已接受、哪些仍然阻止执行。

## 变更内容

- 将后端提供的 `clarificationItems` 渲染为结构化内联控件，而不是要求用户在 message textarea 中回答所有缺失字段。
- 通过 Import Agent API 提交结构化澄清答案，同时保留自由文本聊天用于补充说明。
- 每次 streamed turn 结束后展示每项的 accepted / pending / needs-review 状态，并以最终 `session` 快照作为状态来源。
- 调整当前计划卡片，将澄清项按资产和字段类别分组，展示清晰标签、当前值和剩余阻塞项。
- 保留 `aether-console/DESIGN.md` 描述的单列 Import Agent 对话布局；guided panel 嵌入主叙事列，不改成侧边栏。
- 增加兼容行为：如果后端只返回 legacy `clarificationQuestions`，继续渲染问题卡片和自由文本输入。
- 与 `add-console-import-agent-thinking-stream` 协同：thinking 事件只作为进度反馈，结构化澄清控件由最终 session plan 驱动。
- 如果后端变更尚未同步权威 API 契约，则实现前先更新 `../docs/api/api-import-agent.yaml`。

## 能力

### 新增能力

- `console-import-agent-guided-clarification`：控制台 Import Agent 工作区的 guided clarification UI 和结构化答案提交。

### 修改能力

- 暂无已归档 spec 需要修改。本变更会与进行中的 `console-import-agent-workflow` 和 `console-import-agent-thinking-stream` 协同，但这些 spec 尚未归档到 `openspec/specs/`。

## 影响范围

- 权威文档：前端实现前，`../docs/api/api-import-agent.yaml` 必须已经定义新的澄清 item 和 answer schema。`aether-console/DESIGN.md` 预计保持兼容；只有引入新的可复用交互规则时才需要先更新。
- API 层：`src/api/import-agent/import-agent.dto.ts`、`import-agent.types.ts` 和 `import-agent.api.ts`。
- 状态/composable：`src/composables/useImportAgentWorkspace.ts`。
- UI：`src/features/import-agent/ImportAgentWorkspace.vue`，以及可能从中抽取的小型本地组件。
- i18n：`src/locales/zh-CN/common.ts` 和 `src/locales/en-US/common.ts`。
- 测试：API 映射、SSE 兼容、composable 答案提交，以及页面围绕结构化澄清的行为。
