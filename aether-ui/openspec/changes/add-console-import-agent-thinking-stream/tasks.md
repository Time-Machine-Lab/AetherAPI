## 1. 契约与依赖确认

- [x] 1.1 阅读 `../docs/spec/AetherAPI 前端技术栈与开发规范文档.md`、`aether-console/DESIGN.md`、本 change 的 `proposal.md` / `design.md` / `specs/console-import-agent-workflow/spec.md`，确认本次只增强现有 Import Agent 工作区，不新增路由或独立页面。
- [x] 1.2 对齐后端更新后的 `../docs/api/api-import-agent.yaml`，以前端消费 `thinking` SSE 事件为边界，不在前端自行发明 thinking 字段或推导规划事实。
- [x] 1.3 确认后端未返回 `thinking` 事件时，前端必须保持现有 `status` / `message` / `session` / `error` / `done` 流程可用。

## 2. API 类型与 SSE 解析

- [x] 2.1 扩展 `src/api/import-agent/` 下的 DTO / types，新增 `ImportAgentThinkingEvent` 或等价类型，字段与后端 `thinking` 载荷保持一致。
- [x] 2.2 更新 `import-agent.api.ts` 的 SSE 解析逻辑，新增 `thinking` 分支和专用回调，同时继续安全忽略未知事件。
- [x] 2.3 增加 API 层测试，覆盖 `thinking` 事件解析、旧事件集合兼容、无效 thinking payload 的容错处理。

## 3. Composable 流式状态

- [x] 3.1 更新 `useImportAgentWorkspace`，新增 `streamingThoughts` 或等价状态，独立保存 thinking 时间线，不拼接到 `streamingReply`。
- [x] 3.2 在 create-session、append-turn、取消、失败和完成路径中统一重置或冻结 thinking 状态，避免多轮对话之间串台。
- [x] 3.3 保持最终 session 快照仍驱动计划卡片、澄清问题和会话事实；thinking 只作为流式辅助反馈。

## 4. 工作区展示与 i18n

- [x] 4.1 更新 `ImportAgentWorkspace.vue`，在现有流式区域中增加轻量 thinking 时间线或步骤卡片，与最终 assistant reply 区域分开展示。
- [x] 4.2 当没有 thinking 事件时完全折叠 thinking 区域，保留当前状态提示和 reply 渲染作为退化体验。
- [x] 4.3 为 `zh-CN` 与 `en-US` locale 补充 thinking 模块标题、阶段标签、加载态和降级态文案，避免组件内硬编码展示文本。
- [x] 4.4 检查移动端和桌面端布局，确保 thinking 列表不会遮挡回复、计划卡片或输入区域。

## 5. 验证

- [x] 5.1 为 composable 增加测试，覆盖 thinking 累积、流重置、失败清理、完成后保留展示和多轮 append-turn 场景。
- [x] 5.2 增加工作区 UI 测试，证明 thinking 与 reply 分开渲染，并且后端不发送 thinking 时不会出现空壳面板。
- [x] 5.3 运行 `aether-console` 相关 type-check / test / lint 校验命令，并记录任何超出本次 change 范围的环境问题。
- [x] 5.4 在 `aether-ui` 下运行 `openspec validate add-console-import-agent-thinking-stream --strict`。
