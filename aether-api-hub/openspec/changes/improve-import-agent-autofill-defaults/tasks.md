## 1. 权威契约与规范确认

- [x] 1.1 实现前阅读并遵守 `docs/spec/Aether API HUB 后端代码开发规范文档.md`，确认 Import Agent planner、DTO、adapter、service 分层约束。
- [x] 1.2 使用 `tml-docs-spec-generate` 的 API 文档模板更新 `docs/api/api-import-agent.yaml`，目标文件仍只服务 `ApiImportAgentController.java`。
- [x] 1.3 在 `ImportAgentClarificationItemResp` 中新增可选字段 `defaultValue`、`defaultLabel`、`defaultSource`、`defaultConfidence`，并确认不需要更新 `docs/sql/`。

## 2. 默认值响应模型

- [x] 2.1 扩展后端 API 响应 DTO、service model 与 adapter delegate 映射，完整传递结构化澄清默认值字段。
- [x] 2.2 更新 clarification answer 应用逻辑，确保 `defaultValue` 不会在未提交 `clarificationAnswers` 时自动写入当前计划。
- [x] 2.3 增加旧客户端兼容测试，证明缺少默认值字段时原有 `clarificationItems.currentValue` 与 `clarificationQuestions` 流程不变。

## 3. 主动补全 Planner 能力

- [x] 3.1 增强示例生成逻辑：从文档片段、OpenAPI 示例、字段说明和当前计划中主动补全 `requestExample` 与 `responseExample`。
- [x] 3.2 增强 `schema_generation`，在示例或字段定义足够时主动生成 `requestJsonSchema` 与 `responseJsonSchema`，仍保持合法 JSON object 字符串约束。
- [x] 3.3 增强 `async_pattern`，在 submit/query 证据足够时生成 `asyncTaskConfig` 并避免把任务查询接口创建成第二个资产。
- [x] 3.4 扩展 `plan_review`，审查主动生成的示例、schema 与异步任务配置；非法或低置信度内容降级为中文澄清项。

## 4. 带默认值的澄清策略

- [x] 4.1 扩展 `clarification_strategy`，为可推断但仍需用户确认的字段生成 `defaultValue`、`defaultLabel`、`defaultSource`、`defaultConfidence`。
- [x] 4.2 对鉴权、异步查询、Schema 等问题使用中文业务化提问，要求用户提供相关信息或确认推荐值，不要求用户手写后端配置。
- [x] 4.3 对凭证值、API key、token 等高风险字段不生成伪默认值；只可提示用户提供凭证来源或业务信息。

## 5. 后端测试与验证

- [x] 5.1 增加 planner 测试：证据足够时生成请求/响应示例与 schema，证据不足时保持字段为空或生成澄清项。
- [x] 5.2 增加 async pattern 测试：submit/query 接口折叠为一个资产并生成 `asyncTaskConfig`，缺字段时给出带默认值的澄清项。
- [x] 5.3 增加 clarification default 测试：默认值字段被返回但不会自动应用为用户答案。
- [x] 5.4 增加 API/adapter 映射测试，覆盖默认值字段序列化与旧响应兼容。
- [x] 5.5 运行后端 Import Agent 聚焦测试集与 OpenSpec 校验。
