## 1. 契约与设计基线

- [x] 1.1 确认 `../docs/api/api-import-agent.yaml` 已为 `ApiImportAgentController` 更新 `clarificationItems` 和 `clarificationAnswers`；如果尚未更新，则通过后端变更使用 `tml-docs-spec-generate` 协同完成。
- [x] 1.2 实现前阅读 `../docs/spec/` 下的前端技术栈开发规范和 `aether-console/DESIGN.md`。
- [x] 1.3 在页面开发前，更新或确认 `aether-console/DESIGN.md` 第 10.8 节覆盖嵌入式 guided clarification controls 和单列计划卡片行为。

## 2. API 类型与映射

- [x] 2.1 扩展 `src/api/import-agent/import-agent.dto.ts`，新增可选的澄清项、选项、答案、输入类型、required 和 current-value DTO 字段。
- [x] 2.2 扩展 `src/api/import-agent/import-agent.types.ts`，新增澄清项、草稿答案、accepted/pending 状态和 pending-turn 摘要的 domain types。
- [x] 2.3 更新 `src/api/import-agent/import-agent.api.ts`，使 append-turn 调用可以在带或不带自由文本 message 的情况下提交结构化澄清答案。
- [x] 2.4 增加映射保护，确保缺少 `clarificationItems` 时仍然回退到 legacy `clarificationQuestions`。

## 3. 工作区状态

- [x] 3.1 在 `src/composables/useImportAgentWorkspace.ts` 中增加以澄清项 id 为 key 的 `clarificationDrafts` 状态。
- [x] 3.2 从非空 drafts 构造结构化 answer payload，并且只在用户输入了自由文本时包含 message。
- [x] 3.3 对 answer-only 提交展示 pending-turn 摘要，避免把字段列表拼成聊天文本。
- [x] 3.4 从最终 stream `session` 快照对账 accepted、pending 和 remaining clarification state，而不是由 thinking events 驱动。
- [x] 3.5 在成功完成、reset 或工作区销毁时清空已经提交的 clarification draft values。

## 4. Guided Clarification UI

- [x] 4.1 在 `src/features/import-agent/ImportAgentWorkspace.vue` 的计划卡片内渲染结构化澄清控件，并按资产、类别或全局计划上下文分组。
- [x] 4.2 根据后端 `inputType` 和 `options` 渲染 `SELECT`、`BOOLEAN`、`TEXT` 和 `MULTILINE` 控件。
- [x] 4.3 保留自由文本 composer 用于补充说明，并允许只提交结构化答案。
- [x] 4.4 使用最终 session plan 作为事实来源，展示 accepted、pending 和仍然阻塞的澄清状态。
- [x] 4.5 当后端只返回 `clarificationQuestions` 时，保留 legacy question-card 体验。

## 5. 文案

- [x] 5.1 在 `src/locales/zh-CN/common.ts` 和 `src/locales/en-US/common.ts` 中增加状态、提交摘要、标签和校验提示的 i18n 文案。
- [x] 5.2 确认可见文案使用面向操作员的友好标签，除非后端没有提供 label，否则不暴露后端字段名。

## 6. 测试与验证

- [x] 6.1 增加 API 映射测试，覆盖结构化澄清字段、answer payload、answer-only 调用和 legacy fallback。
- [x] 6.2 增加 composable 测试，覆盖 draft state、pending summaries、stream 对账和 draft 清理。
- [x] 6.3 增加组件测试，覆盖每种 input type、分组渲染、提交行为、accepted/pending 状态和 legacy question cards。
- [x] 6.4 运行前端单元测试套件或聚焦 Import Agent 的测试，并在变更记录中说明剩余缺口。
