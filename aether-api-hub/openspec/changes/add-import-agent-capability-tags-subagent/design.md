## Context

API Catalog 领域层通过 `AiCapabilityProfile.of(provider, model, streamingSupported, capabilityTags)` 明确要求 AI 能力标签非空；`ApiImportAgentApplicationService` 在执行导入时会把 Import Agent 计划中的 `aiProfile.capabilityTags` 传给 `attachAiCapabilityProfile`。如果 planner 只生成 provider/model 而漏掉 tags，执行阶段会失败并暴露 `AI capability tags must not be empty`。

Import Agent 当前的内部子 agent 已经覆盖文档事实、鉴权、异步模式、Schema、计划审查和澄清策略，但没有一个角色专门负责 AI capability tags。现有 `docs/api/api-import-agent.yaml` 已包含 `ImportAiProfileResp.capabilityTags`，因此本变更只增强后端 planner 和执行前审查，不新增公共接口字段，也不修改 `docs/sql/`。

## Goals / Non-Goals

**Goals:**

- 新增 `capability_tags` 子 agent，负责 AI API 的 `aiProfile.capabilityTags` 生成、补齐、规范化和冲突审查。
- 在 planner 阶段尽量根据证据生成非空标签，避免导入执行阶段才触发领域异常。
- 对无法推断 tags 的 AI API 输出中文澄清项或中文追问，要求用户确认能力类别，而不是暴露底层异常。
- 更新 LLM prompt/tool schema，使 AI profile 的 provider/model/streamingSupported 与 capabilityTags 成组生成。
- 补充聚焦测试，覆盖自动生成、已有值保留、空值降级和执行前兜底。

**Non-Goals:**

- 不新增 capability tag 管理表、字典表或运行时标签配置接口。
- 不改变 `AiCapabilityProfile` 的领域不变量；tags 仍必须非空。
- 不修改 `docs/api/api-import-agent.yaml`、`docs/sql/` 或 API 资产主数据表结构。
- 不要求 capability tags 形成全局枚举；本阶段只做导入计划内的稳定字符串标签。
- 不改变 Unified Access 调用、鉴权、异步查询或 Schema 生成逻辑。

## Decisions

### 决策 1：新增独立 `capability_tags` 子 agent

新增 `ImportAgentPlannerSubagentRole.CAPABILITY_TAGS` 和 `CapabilityTagsPlannerSubagent`，并注册到 `ImportAgentPlannerSubagentRegistry`。该子 agent 只负责 `assetPlans[].aiProfile.capabilityTags`，不创建资产、不改鉴权、不折叠异步任务。

建议执行顺序位于 `schema_generation` 之后、`plan_review` 之前，例如 order 40。这样 tags 推断可以消费已生成的 schema/example/asyncTaskConfig，plan review 也能在最后检查 AI profile 是否完整。

备选方案是扩展 `plan_review` 直接补 tags。拒绝原因是 review 应偏向审查与降级，生成能力标签属于正向补全职责，独立子 agent 更清晰。

### 决策 2：先从明确证据生成，再用保守启发式兜底

`capability_tags` 子 agent 的输入证据包括：

- `aiProfile.provider`、`aiProfile.model`。
- `assetName`、`apiCode`、`upstreamUrl`、`categoryCode`。
- 请求/响应示例与 JSON Schema 字段名。
- `asyncTaskConfig` 是否存在。
- `documentSummary`、`importIntent`、最新用户消息和最近 turns。

生成规则应优先使用文档或模型名中的明确能力词，例如 `text-to-video`、`image-to-video`、`video-generation`、`image-generation`、`chat`、`embedding`、`rerank`、`speech`、`vision`。当只有较弱证据时，可以生成较宽泛但仍有意义的标签，例如视频接口使用 `video-generation`，对话接口使用 `chat`。

如果完全无法判断，则不得写空数组后继续执行，应交给澄清策略询问能力类别。

### 决策 3：tags 需要规范化、去重并过滤空值

子 agent 应把 tags 规范成小写短横线格式：

- trim 后转小写。
- 空白、下划线和非字母数字分隔符归一为 `-`。
- 去掉首尾分隔符。
- 去重并保持稳定顺序。
- 丢弃空值。

已有合法 tags 应保留；新推断 tags 只在当前 tags 为空时补齐，或在明确可增强且不冲突时追加。若新 tags 与已有 tags 明显冲突，不应覆盖，应追加中文澄清问题。

### 决策 4：planner prompt 和 tool schema 要明确 capabilityTags 必填语义

当前 `buildAiProfileSchema` 只要求 `provider` 和 `model`，这容易让 LLM 认为 capabilityTags 可省略。实现应调整工具定义和 prompt：

- 对 AI profile 输出明确要求：若生成 `aiProfile.provider` 与 `aiProfile.model`，必须同时生成非空 `capabilityTags`。
- `capabilityTags` 仍可在内部 draft 中被后端子 agent 补齐，避免完全依赖 LLM。
- prompt 文案使用中文，面向用户的问题也保持中文。

### 决策 5：执行前兜底不替代 planner 生成

`ApiImportAgentApplicationService` 执行导入前应避免把空 tags 传入领域层。如果计划仍缺 tags，应将计划保持为不可执行或在开始运行前给出可读失败步骤，不应让 `IllegalArgumentException` 的英文底层消息成为主要用户反馈。

但执行前兜底只是最后防线；核心修复仍应在 planner 阶段通过 `capability_tags` 子 agent 生成或澄清。

## Risks / Trade-offs

- [Risk] 由启发式生成的 tags 可能不完全符合产品运营分类。 -> Mitigation：先采用常见能力标签，已有 tags 不覆盖；后续可独立引入标签字典或管理能力。
- [Risk] tags 过多会降低可读性。 -> Mitigation：限制生成少量高置信标签，通常 1-3 个。
- [Risk] LLM 和子 agent 都生成 tags 时产生重复或格式不一致。 -> Mitigation：统一规范化和去重。
- [Risk] 执行前兜底隐藏 planner 缺陷。 -> Mitigation：测试重点覆盖 planner 生成；执行前兜底只用于防止领域异常泄漏。

## Migration Plan

1. 确认 `docs/api/api-import-agent.yaml` 和领域 `AiCapabilityProfile` 的现有约束仍为权威契约；无需更新 API/SQL 文档。
2. 新增 `CAPABILITY_TAGS` role、`CapabilityTagsPlannerSubagent` 并注册到默认 subagent registry。
3. 更新 planning tool schema 和 planner prompt，要求 AI profile 生成时携带非空 tags。
4. 扩展 `plan_review` 或 `clarification_strategy`，当 AI API tags 仍为空时输出中文澄清项。
5. 增加 service 执行前兜底，避免空 tags 直接触发领域异常。
6. 补充 planner/service 聚焦测试。
7. 回滚方式：从 registry 移除 `capability_tags` 子 agent，并保留现有领域约束；该回滚不涉及数据迁移。

## Open Questions

- 是否需要在后续单独维护平台级标准 capability tag 字典？本提案暂不引入。
- `video-generation` 与 `text-to-video` 是否都应保留，还是只保留最具体标签？本提案建议保留少量可解释标签，优先具体能力。
