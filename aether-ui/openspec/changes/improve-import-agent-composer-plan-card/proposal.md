## Why

当前 `aether-console` 的 Import Agent 工作区在两处细节上影响连续对话体验：用户发送消息后输入框仍保留旧内容，容易造成重复发送；同时“当前计划”卡片在模型流式回答时占据大量视野，使用户更难聚焦最新回复，回答结束后又需要重新查看计划。

这次优化面向已有前端交互，不改变后端接口和会话真相源，只让对话输入与计划卡片状态更符合流式助手工作区的预期。

## What Changes

- 用户触发发送后，前端应立即清空当前输入框内容，并在失败时按既有错误提示处理，不因为清空输入而影响本次请求 payload。
- “当前计划”卡片新增展开 / 收缩能力，用户可以手动切换展示状态。
- 当 Import Agent 开始流式回答时，“当前计划”卡片自动收缩，让最新用户消息、思考过程和回复成为视觉焦点。
- 当本次流式回答完成并收到最终会话快照后，“当前计划”卡片自动展开，方便用户继续确认或补充计划。
- 保持计划内容、确认按钮、执行入口、澄清控件和运行结果使用现有数据来源，不新增 API 字段。

## Capabilities

### New Capabilities

无。

### Modified Capabilities

- `console-import-agent-workflow`: 优化 Import Agent 工作区的消息发送清空行为，以及当前计划卡片在流式回答过程中的展开 / 收缩交互。

## Impact

- 受影响应用：`aether-console`
- 受影响区域：
  - `src/composables/useImportAgentWorkspace.ts`
  - `src/features/import-agent/ImportAgentWorkspace.vue`
  - `src/locales/zh-CN/common.ts`
  - `src/locales/en-US/common.ts`
  - 相关 composable / component 测试
- 权威文档影响：
  - 不需要更新 `../docs/api/*.yaml`，因为本次不新增接口、不修改请求或响应字段。
  - 不需要更新 `aether-console/DESIGN.md`，因为本次沿用既有 Import Agent conversation workspace 视觉规则，只增加局部交互状态。
