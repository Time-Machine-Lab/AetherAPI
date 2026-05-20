## Why

Import Agent 在导入 AI API 时可能只识别出 `aiProfile.provider` 和 `aiProfile.model`，但没有生成 `aiProfile.capabilityTags`，最终执行导入时会触发领域不变量 `AI capability tags must not be empty`。这说明当前 planner 缺少专门负责 AI 能力标签的生成与兜底审查，导致可由 Agent 推断的元数据被遗漏到执行阶段才失败。

现有 `docs/api/api-import-agent.yaml` 已暴露 `ImportAiProfileResp.capabilityTags`，领域模型 `AiCapabilityProfile` 已规定 tags 必填；本变更需要在不新增 API 字段、不修改数据库结构的前提下，让 Import Agent 在规划阶段主动生成非空、可解释、可复用的 AI 能力标签。

## What Changes

- 新增 Import Agent 内部 `capability_tags` 子 agent，专门为 AI API 计划生成、补齐和规范化 `assetPlans[].aiProfile.capabilityTags`。
- 子 agent 根据文档、模型名、上游路径、请求/响应示例、schema、异步模式和已知 capability hints 推断标签，例如 `text-to-video`、`image-to-video`、`video-generation`、`chat`、`embedding`、`rerank`、`vision` 等。
- 增强 planner 审查：AI API 若已有 `provider` 和 `model` 但 tags 为空，不应进入可执行计划；应先由子 agent 生成 tags，仍无法判断时输出中文结构化澄清项或中文追问。
- 增强执行前安全兜底：确认导入前应再次保证 AI API 的 `capabilityTags` 非空，避免领域层异常直接暴露为用户体验问题。
- 更新 planner prompt 和相关测试，要求 LLM/tool-calling 在生成 AI profile 时同时生成 `capabilityTags`。
- 不新增公共 API 字段，不修改 `docs/sql/`；现有 `docs/api/api-import-agent.yaml` 中的 `capabilityTags` 字段继续作为权威契约。

## Capabilities

### New Capabilities

- `api-import-agent-capability-tags-subagent`: Import Agent 内部 AI capability tags 生成、规范化、审查和澄清行为。

### Modified Capabilities

- 无。当前已归档 specs 中尚无 Import Agent AI capability tags 规划能力；本变更以新增能力描述后端 Import Agent 内部规划行为。

## Impact

- 权威文档：`docs/api/api-import-agent.yaml` 已有 `ImportAiProfileResp.capabilityTags`；本变更不新增接口字段。`docs/sql/` 不需要更新。
- 后端 infrastructure planner：影响 `ImportAgentPlannerSubagentRole`、`ImportAgentPlannerSubagentRegistry`、新增 capability tags 子 agent、planner prompt 和 plan review/clarification 策略。
- 后端 service 执行链路：确认导入前需避免 AI API 以空 tags 进入 `AiCapabilityProfile.of(...)`，把可推断内容前置到计划阶段，把不可推断内容变成中文澄清。
- 后端测试：需要覆盖 tags 自动生成、已有 tags 保留、空 tags 降级为澄清、执行前不再触发 `AI capability tags must not be empty` 的聚焦用例。
- 兼容性：对现有 API 响应和旧 session 为 additive；已有合法 `capabilityTags` 应继续保留。
