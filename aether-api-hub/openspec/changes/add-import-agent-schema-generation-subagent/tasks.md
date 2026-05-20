## 1. 权威契约确认

- [x] 1.1 实现前阅读并遵守 `docs/spec/` 中的后端开发规范，重点确认 Import Agent planner、DDD 分层、DTO 与测试约束。
- [x] 1.2 确认 `docs/api/api-import-agent.yaml` 已包含 Import Agent 计划响应中的 `requestJsonSchema` 与 `responseJsonSchema` 字段，且该文件仍仅服务 `ApiImportAgentController`。
- [x] 1.3 确认 `docs/api/api-asset-management.yaml` 与 `docs/sql/api-asset.sql` 已分别定义资产主数据的 JSON Schema 快照字段；本变更不新增 API 字段、不新增 SQL 字段、不调用 `tml-docs-spec-generate` 更新顶层契约。

## 2. Schema 规范化基础能力

- [x] 2.1 新增 planner 层共享 Schema 规范化工具，支持 JSON object 与 JSON object 字符串输入，并输出紧凑 JSON object 字符串。
- [x] 2.2 对 malformed JSON、Markdown、自然语言、示例正文、非 object JSON 值等非法 schema 输入返回空结果或诊断信息，不抛出不可恢复异常。
- [x] 2.3 在 draft parser、draft normalizer 或统一 planner JSON support 中接入 Schema 规范化，确保最终 plan 中的 `requestJsonSchema` / `responseJsonSchema` 只保留合法对象字符串或空值。

## 3. Schema Generation 子 Agent

- [x] 3.1 新增 `SCHEMA_GENERATION` subagent role，并实现 `SchemaGenerationPlannerSubagent`，只负责更新匹配资产的 `requestJsonSchema` 与 `responseJsonSchema`。
- [x] 3.2 将 `schema_generation` 注册到 `ImportAgentPlannerSubagentRegistry`，执行顺序放在 `async_pattern` 之后、`plan_review` 之前。
- [x] 3.3 让子 agent 消费 `extractedFacts.schemaHints`、已有 `assetPlans`、当前计划和示例字段中的 schema 证据；明确 `schemaHints` 只是输入，不代表已完成生成。
- [x] 3.4 当证据不足时保持 schema 字段为空；当 planner 尝试生成但内容非法时，产出面向用户的中文澄清问题。

## 4. Review 与执行前保护

- [x] 4.1 扩展 `PlanReviewPlannerSubagent`，对 schema 字段进行审查：非法新值不得覆盖当前合法值。
- [x] 4.2 在 review diagnostics 中记录 schema 字段的规范化、拒绝或保留行为，但不向公共 API 暴露 provider 原始 payload。
- [x] 4.3 在执行 Import Agent run 前复用 schema 规范化，避免旧 plan 快照中的非法 schema 字符串写入资产主数据。

## 5. 测试与验证

- [x] 5.1 增加 Schema 规范化单元测试：对象输入、合法字符串输入、非法字符串、非 object JSON、空值。
- [x] 5.2 增加子 agent 测试：`schemaHints` 被消费、示例推断基础 schema、证据不足时不生成伪 schema。
- [x] 5.3 增加 planner/review 测试：非法 schema 不覆盖已有合法 schema，并产生中文澄清或诊断。
- [x] 5.4 增加 Import Agent service 聚焦测试，证明执行导入前不会把非法 `requestJsonSchema` / `responseJsonSchema` 写入注册或修订命令。
- [x] 5.5 运行后端 Import Agent 聚焦测试集，并在变更记录中说明剩余风险：本阶段只做轻量 JSON object 合法性检查，不做完整 JSON Schema dialect 校验。
