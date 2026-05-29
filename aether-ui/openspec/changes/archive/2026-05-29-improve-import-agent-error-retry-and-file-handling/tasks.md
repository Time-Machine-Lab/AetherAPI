## 1. 对齐与确认

- [x] 1.1 重新阅读 `../docs/spec/AetherAPI 前端技术栈与开发规范文档.md`、`aether-console/DESIGN.md` 和 `../docs/api/api-import-agent.yaml`，确认本次变更不需要更新后端 API 契约、共享前端规范或设计系统文档。
- [x] 1.2 检查 `src/composables/useImportAgentWorkspace.ts`、`src/features/import-agent/ImportAgentWorkspace.vue`、import-agent locale 资源和现有 import-agent 测试，确认所有受影响的状态和渲染路径。

## 2. Composable 行为

- [x] 2.1 为 create-session 失败补充测试：pending 用户消息保留、暴露代理错误回复、草稿保持清空、重新发送复用原始 create payload。
- [x] 2.2 为 append-turn 失败补充测试：pending 后续消息或澄清摘要保留、暴露代理错误回复、草稿保持清空、重新发送复用原始 session id 和 append payload。
- [x] 2.3 更新 `useImportAgentWorkspace`，将失败代理回复与普通 composer error 分开建模，并避免发送失败时清空 `pendingTurn`。
- [x] 2.4 保存最近一次失败的 create-session 或 append-turn payload，并暴露重新发送动作，通过相同 stream callbacks 重放请求。
- [x] 2.5 在收到成功的最终 `session` 快照或重置工作区时，清理失败回复和重发 payload。

## 3. 附件内容处理

- [x] 3.1 补充测试证明附件文件在 UI 中保持紧凑预览，但完整提取内容会进入 create-session `documentSummary`。
- [x] 3.2 补充测试证明附件文件在 UI 中保持紧凑预览，但完整提取内容会进入 append-turn `message`。
- [x] 3.3 更新附件状态模型，将 UI 预览文本和发往后端的完整内容分开保存。
- [x] 3.4 移除 import-agent 出站请求内容的前端固定长度截断，同时保留本地文件类型、空文件、大小和数量校验。

## 4. 对话界面

- [x] 4.1 更新 `ImportAgentWorkspace.vue`，使用现有控制台对话样式和 destructive 反馈在对话流中展示失败代理回复。
- [x] 4.2 使用现有 lucide 图标新增重新发送按钮，绑定 composable 的重发动作，并在重发中禁用按钮。
- [x] 4.3 更新附件预览文案，使“截断”语义明确指向预览展示，而不是发往后端的内容。
- [x] 4.4 新增或更新组件测试，覆盖失败回复渲染、重新发送按钮行为和附件预览截断文案。

## 5. 验证

- [x] 5.1 运行 import-agent composable 和组件相关的定向测试。
- [x] 5.2 运行 `aether-console` 可用的相关前端质量检查；如果某个命令无法在本地运行，需要记录原因。
- [x] 5.3 在 `aether-ui` 下运行 `openspec validate improve-import-agent-error-retry-and-file-handling --strict`。
