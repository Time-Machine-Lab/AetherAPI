## 1. 契约与规范确认

- [x] 1.1 实现前阅读并遵守 `../docs/spec/AetherAPI 前端技术栈与开发规范文档.md` 与 `aether-console/DESIGN.md`。
- [x] 1.2 等后端更新 `../docs/api/api-import-agent.yaml` 后，对齐 `ImportAgentClarificationItemResp` 的可选默认值字段。
- [x] 1.3 确认本变更不需要修改前端统一规范文档或 `aether-console/DESIGN.md`；若实现发现视觉规则缺口，再先更新对应权威文档。

## 2. API 类型与映射

- [x] 2.1 更新 `src/api/import-agent/import-agent.dto.ts`，为结构化澄清项增加可选 `defaultValue`、`defaultLabel`、`defaultSource`、`defaultConfidence`。
- [x] 2.2 更新 `src/api/import-agent/import-agent.types.ts` 与 DTO 到领域类型的映射，保持旧响应字段缺失时兼容。
- [x] 2.3 更新 API 层测试，覆盖新增字段保留、旧响应兼容和 append-turn 请求格式不变。

## 3. 澄清答案交互逻辑

- [x] 3.1 更新 `src/composables/useImportAgentWorkspace.ts`，让 `defaultValue` 可作为本地草稿或“采用推荐值”的填充值。
- [x] 3.2 确保页面加载或会话刷新时不会自动提交默认值；只有用户提交澄清表单时才生成 `clarificationAnswers`。
- [x] 3.3 增加 composable 测试，覆盖采用默认值、编辑默认值后提交、无默认值手动输入三类场景。

## 4. 工作台 UI 与 i18n

- [x] 4.1 更新 `src/features/import-agent/ImportAgentWorkspace.vue` 的结构化澄清区，展示推荐值、来源和置信度的次级辅助信息。
- [x] 4.2 为 TEXT、SELECT、BOOLEAN、MULTILINE 控件补齐默认值采用/编辑体验，长 JSON、URL 或多行文本不得撑破计划卡片。
- [x] 4.3 新增 zh-CN 与 en-US i18n 文案，覆盖“推荐值”“采用推荐值”“来自文档/推断/当前计划”“置信度”等文本。
- [x] 4.4 增加组件测试，覆盖有默认值、无默认值、低置信度、长文本和旧响应兼容。

## 5. 前端验证

- [x] 5.1 运行 Import Agent 相关 Vitest 测试。
- [x] 5.2 运行前端 type-check/build 或项目现有等价验证命令。
- [x] 5.3 在实现记录中说明与后端 change `improve-import-agent-autofill-defaults` 的依赖关系和联调验证点。
