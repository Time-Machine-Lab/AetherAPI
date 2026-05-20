## 1. 契约与规范确认

- [ ] 1.1 实现前阅读并遵守 `docs/spec/Aether API HUB 后端代码开发规范文档.md`，确认 Import Agent planner、service 执行链路和领域 `AiCapabilityProfile` 分层边界。
- [ ] 1.2 确认 `docs/api/api-import-agent.yaml` 已包含 `ImportAiProfileResp.capabilityTags`，本变更不新增公共 API 字段。
- [ ] 1.3 确认本变更不涉及数据库结构，不需要更新 `docs/sql/`。

## 2. Capability Tags 子 Agent

- [ ] 2.1 在 `ImportAgentPlannerSubagentRole` 中新增 `CAPABILITY_TAGS` 角色，并在默认 registry 中注册新的 capability tags 子 agent。
- [ ] 2.2 实现 `CapabilityTagsPlannerSubagent`，只负责为 AI API 资产生成、补齐、规范化和去重 `aiProfile.capabilityTags`。
- [ ] 2.3 设计并实现 capability tag 推断规则，优先从 provider/model、assetName、apiCode、upstreamUrl、categoryCode、request/response 示例、JSON Schema 和 asyncTaskConfig 中提取高置信标签。
- [ ] 2.4 确保已有非空 tags 被保留；新增 tags 不覆盖用户或当前计划已有高置信配置。

## 3. Planner Prompt 与审查澄清

- [ ] 3.1 更新 `ImportAgentPlanningToolSupport.buildAiProfileSchema`，让 tool schema 明确 AI profile 输出应包含非空 `capabilityTags`。
- [ ] 3.2 更新 `OpenAiCompatibleImportAgentPlannerProvider` prompt，要求生成 provider/model 时同时生成中文语义可解释的 capability tags。
- [ ] 3.3 更新 `PlanReviewPlannerSubagent` 或 `ClarificationStrategyPlannerSubagent`，当 AI API tags 仍为空时保持计划不可执行并输出中文澄清。
- [ ] 3.4 确保面向用户的问题使用中文，要求用户确认能力类别，不暴露领域异常原文。

## 4. 执行前兜底

- [ ] 4.1 在 `ApiImportAgentApplicationService` 执行导入前增加 AI profile tags 校验，避免空 tags 直接进入 `attachAiCapabilityProfile`。
- [ ] 4.2 当执行前发现 AI API tags 为空时，返回可读的 Import Agent 失败步骤或阻止运行，而不是让 `AI capability tags must not be empty` 成为主要用户可见错误。

## 5. 测试与验证

- [ ] 5.1 增加 planner/subagent 测试：根据 `dashscope` + `happyhorse-1.0-t2v` 生成非空视频能力 tags。
- [ ] 5.2 增加规范化测试：大小写、空格、下划线、重复 tags 被归一化和去重。
- [ ] 5.3 增加保留测试：已有非空 `capabilityTags` 不被低置信推断覆盖。
- [ ] 5.4 增加澄清测试：无法推断 tags 的 AI API 计划不可执行并产生中文澄清。
- [ ] 5.5 增加 service 执行链路测试：空 tags 不再以领域异常原文暴露。
- [ ] 5.6 运行后端 Import Agent 聚焦测试集与 OpenSpec 严格校验。
