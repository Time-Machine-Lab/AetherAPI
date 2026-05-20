## 1. 契约对齐

- [ ] 1.1 重新阅读 `../docs/spec/AetherAPI 前端技术栈与开发规范文档.md`、`aether-console/DESIGN.md` 以及更新后的 `../docs/api/api-import-agent.yaml` 流式契约，确认本次 change 不需要新增页面壳或新的前端框架原语。
- [ ] 1.2 确认前端 change 依赖后端 thinking-stream 契约更新，而不是自行发明一套前端私有事件结构。

## 2. API 与流式状态

- [ ] 2.1 扩展 `src/api/import-agent/` 下的 DTO 和类型定义，使其能够建模新的 `thinking` SSE 事件载荷，并与现有 `status`、`message`、`session`、`error`、`done` 事件并存。
- [ ] 2.2 更新 import-agent 的 SSE 解析逻辑，使未知事件仍然可以被安全忽略，同时把 `thinking` 事件派发给专用回调。
- [ ] 2.3 更新 `useImportAgentWorkspace`，让其把流式 thinking 状态与最终 streamed reply 分开维护，并在 create-session 与 append-turn 流程之间正确重置。

## 3. 工作区展示

- [ ] 3.1 更新 `ImportAgentWorkspace.vue`，在流式过程中渲染独立的 thinking 时间线或思考卡片列表，并与最终 assistant reply 区域分开展示。
- [ ] 3.2 当后端没有发出任何 thinking 事件时，保留当前 `status` / `reply` 渲染路径作为退化方案。
- [ ] 3.3 在 `zh-CN` 与 `en-US` locale 资源中补充 thinking 标题、标签和空态 / 退化态文案。

## 4. 验证

- [ ] 4.1 为 composable / API 补充测试，覆盖 `thinking` 事件解析、状态累积、重置行为，以及只存在旧事件集合时的向后兼容。
- [ ] 4.2 增加工作区 UI 测试，证明 thinking 事件会独立于 reply 文本渲染，并在流重置或结束后正确清理。
- [ ] 4.3 运行 `aether-console` 相关前端校验命令，并记录任何超出本次 change 范围的环境问题。
