## 1. 契约与现状确认

- [x] 1.1 实现前阅读并遵守 `docs/spec/Aether API HUB 后端代码开发规范文档.md`。
- [x] 1.2 确认 `docs/api/api-category-lifecycle.yaml` 已定义启用分类可被新资产引用、分类查询和分类有效性语义。
- [x] 1.3 确认 `docs/api/api-import-agent.yaml` 已包含导入计划中的 `categoryPlans` 和 `assetPlans[].categoryCode` 字段，本变更不新增公共 API 字段。
- [x] 1.4 确认 `docs/sql/api-category.sql` 已定义分类主数据表，本变更不修改数据库结构。

## 2. Planner Request 分类候选

- [x] 2.1 新增轻量分类候选 service model，用于携带 `categoryCode`、`categoryName` 和状态信息。
- [x] 2.2 扩展 `ImportAgentPlannerRequest`，加入不可变的启用分类候选列表，并保持旧调用点可迁移。
- [x] 2.3 在 `ApiImportAgentApplicationService.createSession` 调 planner 前查询 `CategoryUseCase.listCategories(CategoryStatus.ENABLED, 1, 100)` 并注入 request。
- [x] 2.4 在 `ApiImportAgentApplicationService.appendTurn` 调 planner 前刷新启用分类候选并注入 request。
- [x] 2.5 为分类候选为空或分类查询异常设计防御行为，确保 planner 仍能生成中文澄清而不是中断对话。

## 3. 分类选择子 Agent

- [x] 3.1 在 `ImportAgentPlannerSubagentRole` 新增 `CATEGORY_CLASSIFICATION`。
- [x] 3.2 实现 `CategoryClassificationPlannerSubagent`，只消费 planner request 中的候选分类和当前候选计划，不直接访问数据库。
- [x] 3.3 实现分类匹配规则：优先匹配已有有效分类，其次根据资产名、apiCode、URL、AI profile、请求/响应示例、schema 和异步配置推断。
- [x] 3.4 实现低置信度处理：不编造分类编码，追加中文澄清问题，并尽量提供 2-3 个候选默认值。
- [x] 3.5 实现 `categoryPlans` 同步：自动选择已有分类时生成或复用 `USE_EXISTING` 分类计划，并避免重复项。
- [x] 3.6 将分类子 agent 注册到默认 registry，排序在证据补全类子 agent 之后、`PLAN_REVIEW` 之前，并与 capability tags 子 agent 的顺序保持兼容。

## 4. Prompt、审查与回复

- [x] 4.1 更新 `OpenAiCompatibleImportAgentPlannerProvider` prompt，把启用分类候选写入规划上下文，并要求模型优先从候选中选择 `categoryCode`。
- [x] 4.2 明确 prompt 禁止在未要求创建新分类时凭空生成分类编码。
- [x] 4.3 更新 `PlanReviewPlannerSubagent`，校验资产分类必须来自启用分类候选，且缺失或无效时计划不可执行。
- [x] 4.4 更新 `ClarificationStrategyPlannerSubagent` 或相关回复生成逻辑，确保分类相关追问为中文，并给出候选默认值。
- [x] 4.5 更新思考流标题和描述，展示分类选择子 agent 的执行开始、完成和追问数量。

## 5. 测试与验证

- [x] 5.1 增加应用服务测试：创建会话和追加对话都会查询启用分类并传给 planner。
- [x] 5.2 增加子 agent 测试：视频生成 API 自动匹配 video 类分类，LLM/对话 API 自动匹配 llm 或 AI 类分类。
- [x] 5.3 增加保留测试：已有有效 `categoryCode` 不被覆盖。
- [x] 5.4 增加无效分类测试：不存在或未启用分类不会被静默接受。
- [x] 5.5 增加低置信度测试：多个候选无法判断时产生中文追问且计划不可执行。
- [x] 5.6 增加分类计划一致性测试：多个资产共享同一分类时只生成一个 `USE_EXISTING` 分类计划。
- [x] 5.7 运行后端 Import Agent 相关测试和 OpenSpec 校验。
