## Why

Import Agent 后端将增强主动补全与结构化澄清默认值能力；前端需要把这些默认值呈现为可理解、可采用、可编辑的引导式提问，而不是让用户面对空输入框或重复解释已知信息。

现有控制台 Import Agent 工作台已经支持结构化 `clarificationItems`，但交互主要围绕 `currentValue` 渲染，缺少推荐默认值的视觉层级、采用动作和提交策略。本变更让用户在“Agent 已推断出合理默认值”时能快速确认，同时仍保留修改空间。

## What Changes

- 在 `aether-console` Import Agent 工作台中展示后端返回的结构化澄清默认值，例如推荐值、推荐说明、来源和置信度。
- 对带默认值的问题提供“采用推荐值/编辑后提交”的交互；默认值不得静默当成用户答案，必须由用户确认或提交。
- 对没有默认值的问题保持现有手动输入/选择体验；旧后端响应没有新增字段时前端必须兼容。
- 更新 Import Agent API DTO/types 与 API 映射，跟随 `docs/api/api-import-agent.yaml` 的可选字段增量。
- 所有新增用户可见文案接入 i18n，目标应用仍遵守 `aether-console/DESIGN.md` 中的计划卡片与结构化澄清区布局约束。
- 不新增页面、不改变路由、不引入新的状态管理框架。

## Capabilities

### New Capabilities

- `console-import-agent-clarification-defaults`: 控制台 Import Agent 工作台展示并提交结构化澄清默认值。

### Modified Capabilities

- 无。现有已归档 specs 中没有 Import Agent 工作台默认值交互能力；本变更以新增能力描述。

## Impact

- 权威 API 文档：依赖后端变更更新 `../docs/api/api-import-agent.yaml` 中的 `ImportAgentClarificationItemResp` 可选默认值字段；前端实现前必须对齐该契约。
- 前端 API 层：影响 `src/api/import-agent/import-agent.dto.ts`、`import-agent.types.ts`、`import-agent.api.ts` 及相关测试。
- 前端业务逻辑：影响 `src/composables/useImportAgentWorkspace.ts` 的结构化答案准备与提交。
- 前端页面：影响 `src/features/import-agent/ImportAgentWorkspace.vue` 的澄清项渲染、默认值确认与 i18n 文案。
- 兼容性：旧响应没有默认值字段时应保持当前体验；新增字段均按可选字段处理。
