## Why

Import Agent 已经具备多子 agent 规划、结构化澄清和 Schema 规范化能力，但体验上仍偏“问用户补字段”：在文档信息足够时，请求示例、响应示例、请求/响应 Schema、异步任务配置等内容应由 agent 主动生成；必须追问时，也应给出基于已知信息的推荐默认值，减少用户重复输入和配置负担。

现有 `docs/api/api-import-agent.yaml` 已暴露 `requestExample`、`responseExample`、`requestJsonSchema`、`responseJsonSchema`、`asyncTaskConfig` 与结构化 `clarificationItems.currentValue`，但缺少“推荐默认值/默认来源/置信度”这类面向前端交互的字段。因此本变更需要在不改数据库结构的前提下，增强 planner 主动补全，并同步更新 Import Agent API 顶层契约。

## What Changes

- 增强 Import Agent planner/subagent 编排：当文档、示例、URL、字段说明和历史计划提供足够证据时，主动补全请求示例、响应示例、请求/响应 JSON Schema 与异步任务配置。
- 明确 “证据足够则生成，证据不足才提问”：避免让用户手写后端配置；鉴权、异步查询、Schema 等问题都应让用户提供业务信息，由 agent 生成计划字段。
- 扩展结构化澄清项，在 `ImportAgentClarificationItemResp` 中新增可选推荐默认值元数据，例如 `defaultValue`、`defaultLabel`、`defaultSource`、`defaultConfidence`，供前端展示“一键采用/可编辑”的默认答案。
- 更新 `docs/api/api-import-agent.yaml` 作为权威 API 契约；该文件仍只对应 `ApiImportAgentController.java`。不新增 SQL 字段，不修改 `docs/sql/`。
- 保持兼容：旧客户端仍可只使用 `clarificationQuestions` 与 `currentValue`；新增默认值字段均为可选字段。
- 不引入运行时请求/响应校验，不改变 Unified Access 调用行为，不在本变更中做密钥脱敏。

## Capabilities

### New Capabilities

- `api-import-agent-autofill-defaults`: Import Agent 主动生成请求/响应示例、请求/响应 Schema、异步任务配置，并为结构化澄清项提供推荐默认值。

### Modified Capabilities

- 无。当前已归档 specs 中尚无 Import Agent 规划与澄清能力；本变更以新增能力描述 Import Agent 内部规划行为和公共响应契约增量。

## Impact

- 权威 API 文档：需要更新 `docs/api/api-import-agent.yaml`，在 `ImportAgentClarificationItemResp` 增加可选默认值字段，并保持一份 `.yaml` 只服务 `ApiImportAgentController.java`。
- 后端 API/DTO：影响 `ImportAgentClarificationItemResp`、service model、delegate 映射与相关测试。
- 后端 planner：影响示例生成、Schema 生成、异步任务识别/折叠、plan review、clarification strategy 等 Import Agent planner 组件。
- 后端测试：需要覆盖证据足够时主动补全、证据不足时仍提问、默认值元数据输出、旧客户端字段兼容。
- 数据库与 SQL：不新增表、不改字段；无需更新 `docs/sql/`。
