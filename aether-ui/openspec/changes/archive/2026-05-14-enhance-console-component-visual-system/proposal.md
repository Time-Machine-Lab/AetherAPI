## Why

当前 `aether-console` 多个页面存在组件表达单一的问题：大量信息以普通文字、基础 Card、Button 和 `<pre>` 承载，导致页面层次弱、可扫读性差，也容易把只读状态误表达成可点击操作。

需要单独建立控制台组件与视觉表达专项，让 API 市场、资产工作台、调用日志和 Playground 在不改变业务契约的前提下获得更专业、统一、可复用的界面表达。

## What Changes

- 更新 `aether-console/DESIGN.md`，补充控制台组件视觉表达规范，作为后续实现的权威设计依据。
- 建立面向控制台的元信息组件模式：图标 + 文本、图标 + 数值、图标 + 时间、图标 + 状态等。
- 建立统一的 Badge/Tag/Method/Status 表达规则，避免只读标签复用 Button 样式。
- 建立表单字段标题与字段组视觉规范，例如字段组标题、辅助说明、左侧强调线、帮助提示、必填/可选提示。
- 建立列表行组件模式，让资产列表、日志列表、API Key 列表能展示更多结构化信息，而不是只显示少量文字。
- 建立 JSON/代码展示组件规范，包括高亮或格式化、复制、折叠/展开、错误兜底和只读/可编辑区分。
- 建立空态、错误态、加载态、不可用态的组件化表达，减少各页面临时拼样式。
- 在 API 市场、资产管理、调用日志、Playground 中逐步替换单一表达，提升页面层次、信息密度和可读性。

## Capabilities

### New Capabilities

- `console-component-visual-system`：定义并落地控制台通用组件视觉表达，包括元信息、Badge/Tag、字段标题、列表行、JSON/代码展示和状态反馈。
- `console-page-visual-richness`：约束 API 市场、资产管理、调用日志、Playground 等页面如何使用组件组合提升信息层次和可扫读性。

### Modified Capabilities

无。本变更不改变业务行为和接口能力，只新增控制台视觉组件与页面表达要求。

## Impact

- 影响应用：`aether-ui/aether-console`。
- 需要先更新的权威设计文档：
  - `aether-ui/aether-console/DESIGN.md`
- 可能影响的前端区域：
  - `src/components/ui/*`
  - `src/components/*`
  - `src/features/catalog/*`
  - `src/features/api-call-log/*`
  - `src/features/credential/*`
  - `src/features/unified-access/*`
  - `src/pages/index.vue`
  - `src/pages/workspace.vue`
  - `src/pages/playground.vue`
  - `src/layouts/ConsoleLayout.vue`
  - `src/style.css`
  - `src/locales/zh-CN/*`
  - `src/locales/en-US/*`
- 不影响：
  - 后端 API 契约。
  - 鉴权流程。
  - 数据库或业务状态机。
  - API 订阅、上游转发、流式调用等功能能力。
