## 1. 权威契约与设计上下文

- [x] 1.1 确认后端 `../docs/api/api-asset-management.yaml` 已为资产所有者请求/响应契约暴露 `upstreamRequestHeaders`。
- [x] 1.2 确认后端 `../docs/api/api-import-agent.yaml` 已暴露 `assetPlans[].upstreamRequestHeaders` 以及兼容的澄清项路径。
- [x] 1.3 实现前重新阅读 `../docs/spec/AetherAPI 前端技术栈与开发规范文档.md` 和 `aether-console/DESIGN.md`，重点关注表单、状态反馈和敏感配置展示规则。

## 2. Catalog API 映射与 Workspace 状态

- [x] 2.1 在 `src/api/catalog/*` 中增加上游请求头 DTO/type 定义。
- [x] 2.2 在所有者资产详情响应和修订请求体中映射 `upstreamRequestHeaders`。
- [x] 2.3 为编辑抽屉增加可重复的请求头 name/value 行状态，覆盖预填、添加、移除、规范化和清空行为。
- [x] 2.4 在资产详情中展示 owner-only 上游请求头，并与 `authConfig` 保持分离。
- [x] 2.5 确保市场详情和文档导出不会展示或推断 owner-only 上游请求头。

## 3. Import Agent API 与 Workspace UI

- [x] 3.1 在 `src/api/import-agent/*` 中增加上游请求头 DTO/type 定义。
- [x] 3.2 从会话快照和流式结果中映射 `assetPlans[].upstreamRequestHeaders`。
- [x] 3.3 在 `ImportAgentWorkspace.vue` 的资产计划卡片中独立展示 planned headers。
- [x] 3.4 确保指向上游请求头的结构化澄清项可以通过现有澄清答案流程渲染和提交。
- [x] 3.5 为请求头编辑器标签、提示、空状态、计划区块和澄清标签补充 zh-CN 与 en-US i18n 文案。

## 4. 验证

- [x] 4.1 新增或更新 catalog API 映射测试，覆盖所有者资产上游请求头。
- [x] 4.2 新增或更新 workspace 测试，覆盖请求头行预填、保存、移除和清空行为。
- [x] 4.3 新增或更新 Import Agent API 映射测试，覆盖计划中的上游请求头。
- [x] 4.4 新增或更新 Import Agent workspace 测试，覆盖计划展示和请求头澄清提交。
- [x] 4.5 运行目标 `aether-console` 测试和类型检查。
- [x] 4.6 在 `aether-ui` 下运行 `openspec validate add-console-api-asset-upstream-headers --strict`。
