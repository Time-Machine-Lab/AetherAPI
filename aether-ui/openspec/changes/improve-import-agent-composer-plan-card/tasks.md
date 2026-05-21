## 1. 规范与边界确认

- [x] 1.1 阅读 `../docs/spec/AetherAPI 前端技术栈与开发规范文档.md`、`aether-console/DESIGN.md` 和本 change 的 proposal / design / spec，确认本次只改前端交互，不更新后端接口契约。
- [x] 1.2 检查 `useImportAgentWorkspace`、`ImportAgentWorkspace.vue` 和现有测试，确认输入框清空、流式状态、计划卡片渲染的当前实现边界。

## 2. 输入框发送后清空

- [x] 2.1 调整 `useImportAgentWorkspace` 的 create-session 流程，先构造 payload 和 pending turn，再立即清空首条消息输入框。
- [x] 2.2 调整 append-turn 流程，先构造 append payload 和 pending turn，再立即清空后续消息输入框。
- [x] 2.3 保持附件、澄清答案、错误提示和 pending turn 行为不回退，确保请求失败时不会把旧文本自动塞回 composer。

## 3. 当前计划卡片展开 / 收缩

- [x] 3.1 在 Import Agent 工作区中新增当前计划卡片展开状态和切换操作，默认展开。
- [x] 3.2 为计划卡片标题区增加展开 / 收缩按钮，文案接入 `zh-CN` 与 `en-US` i18n。
- [x] 3.3 收缩态保留计划标题、版本、可执行 / 确认状态等摘要信息，隐藏计划详情、澄清控件、分类计划、资产计划和操作区。

## 4. 流式生命周期联动

- [x] 4.1 用户触发 create-session 或 append-turn 并进入 pending / streaming 状态时，自动收缩当前计划卡片。
- [x] 4.2 本次流式请求完成并收到最终 session 快照后，自动展开当前计划卡片。
- [x] 4.3 确认没有 currentPlan 时不渲染展开 / 收缩控件，也不出现空卡片。

## 5. 验证

- [x] 5.1 为 `useImportAgentWorkspace` 增加或更新测试，覆盖发送后清空输入框、payload 不丢失、失败不恢复旧输入。
- [x] 5.2 为 `ImportAgentWorkspace.vue` 增加或更新测试，覆盖计划卡片手动展开 / 收缩和流式期间自动收缩 / 完成后展开。
- [x] 5.3 运行相关 Vitest、`pnpm type-check`，并记录任何既有 lint warning。
- [x] 5.4 在 `aether-ui` 下运行 `openspec validate improve-import-agent-composer-plan-card --strict`。
