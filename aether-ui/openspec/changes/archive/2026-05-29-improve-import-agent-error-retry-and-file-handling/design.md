## Context

`aether-console` 的 import-agent 工作区已经采用 `Vue 3 + TypeScript + Vite`、统一 API 层、`useImportAgentWorkspace` composable、i18n 文案和 `ImportAgentWorkspace.vue` 对话式界面。`../docs/api/api-import-agent.yaml` 已定义 create-session、append-turn 及其 stream 端点，stream 事件包含 `status`、`message`、`thinking`、`session`、`error` 和 `done`；前端不需要新增接口。

当前问题集中在前端状态编排。发送 create-session 或 append-turn 时，前端会用 `pendingTurn` 乐观渲染用户消息，但 stream `error` 或请求异常会触发流状态重置，导致 `pendingTurn` 被清空，用户看到消息消失。附件方面，前端当前为了拼入 `documentSummary` 或 append-turn `message`，会对提取文本进行固定长度截断；这让后端无法对完整输入执行统一限制和返回一致错误。

本设计基于 `aether-console/DESIGN.md` 的 import-agent conversation workspace 约束：最新用户消息应乐观展示，状态和回复应在 assistant bubble 内表达，附件卡片可以展示短预览但不应成为独立上传工作流。

## Goals / Non-Goals

**Goals:**
- 失败时保留本次已发送用户消息，不再让对话流出现“撤回”效果。
- 将后端错误或标准化请求错误作为 assistant reply 展示，并在同一气泡提供重新发送入口。
- 为最后一次失败的 create-session 或 append-turn 保存可重放 payload，重发时不依赖已清空的输入框。
- 附件卡片继续使用短预览保护页面可读性，但发送给后端的文本内容不做前端长度截断。
- 继续沿用现有 API 层、composable、Vue 组件、i18n 和测试结构。

**Non-Goals:**
- 不新增后端上传接口、文件流式上传能力或新 DTO 字段。
- 不修改 `../docs/api/api-import-agent.yaml`、前端统一规范或 `aether-console/DESIGN.md`。
- 不改变会话恢复、计划确认、执行运行轮询等既有流程。
- 不移除前端对文件类型、空文件、单次附件数量和单文件读取大小的本地保护。

## Decisions

### 1. 将失败状态建模为 assistant reply，而不是普通 composer error

失败时 composable 保留 `pendingTurn`，同时写入一个专用的失败回复状态，例如 `failedReply`，包含错误文案、是否可重发、重发中状态等。视图层在现有 assistant streaming bubble 附近展示该错误回复，样式使用 destructive 文本和现有 `Button` / `RotateCcw` 图标。

替代方案是继续只在 composer 下方展示 `sessionError` / `turnError`。不采用该方案，因为错误脱离对话流，且无法解释“这条已发送消息的代理回复失败了”。

### 2. 保存最后一次失败请求的结构化 payload

create-session 失败时保存 `CreateImportAgentSessionInput`；append-turn 失败时保存 `sessionId` 和 `AppendImportAgentTurnInput`。重新发送调用同一 API，并复用同一 stream callbacks。重发开始时保持同一 `pendingTurn` 文案，清理旧失败回复，进入正常 streaming 状态。

替代方案是把原文本重新塞回输入框。该方案会丢失结构化澄清答案和附件拼接结果，也会让用户误以为需要编辑草稿后再发。

### 3. 区分附件预览文本和发送文本

`ImportAgentDraftAttachment` 保存完整 `content` 供请求拼接，同时保存短 `excerpt` 供 UI 预览。`truncated` 只表示预览被截断，不表示发送内容被截断。`formatAttachmentSection` 使用完整内容；附件卡片继续使用 `excerpt` 避免大文本撑开页面。

替代方案是完全不截断 UI 预览。该方案会伤害页面性能和可读性，也与当前设计文档要求“compact surface card with truncated preview”冲突。

### 4. 移除请求 payload 的前端固定长度截断

`documentSummary` 和 append-turn `message` 的拼接不再使用固定字符数裁剪。手工输入、结构化澄清答案和附件内容一起交给后端做最终长度与业务校验。前端只保留本地不可用文件的即时反馈。

替代方案是提高前端截断阈值。该方案仍会造成前后端校验不一致，也无法满足“超出长度由后端报错”的要求。

## Risks / Trade-offs

- [Risk] 大附件拼接到请求体后可能触发较慢请求或更大的内存占用 → 保留现有单文件读取大小和附件数量限制，并由后端返回明确错误。
- [Risk] 重发时后端可能已经处理了部分请求 → 仅对 create/append 的失败请求提供重发，成功拿到 `session` 快照后清空失败重发状态，避免对已成功会话重复提交。
- [Risk] stream `error` 可能先触发 callback 后又由 API 层 throw → 统一在 composable catch 中落最终错误文案，callback 不清空 `pendingTurn`。
- [Risk] `truncated` 文案语义从“发送内容截断”变为“预览截断” → 同步调整 i18n 文案，避免误导用户。

## Migration Plan

1. 更新 composable 状态模型和失败发送流程。
2. 更新 import-agent 对话视图，展示失败 assistant bubble 和重新发送按钮。
3. 更新附件拼接逻辑，使请求使用完整内容、UI 使用预览。
4. 更新 i18n 和相关单元/组件测试。
5. 若回滚，只需恢复前端状态编排与附件拼接逻辑，不涉及后端或数据迁移。
