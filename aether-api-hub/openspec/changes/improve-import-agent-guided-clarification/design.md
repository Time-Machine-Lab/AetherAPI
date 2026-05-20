## 上下文

当前 Import Agent 工作流已经有正确的顶层形态：`ApiImportAgentController` 暴露 session、turn、confirm、run 端点，`ApiImportAgentApplicationService` 负责 session 生命周期，planner 通过单一的 `ApiImportAgentPlannerPort` 接入。体验问题主要来自澄清循环，而不是 session 模型本身。

现在 planner 返回的 `clarificationQuestions` 是普通字符串。控制台只能展示这些字符串，并要求用户在自由文本框里输入编号回复。下一轮后端规划时，系统必须猜测“1. xxx 2. no 3. Beijing”分别对应 `assetPlans[0].authConfig`、`assetPlans[0].region` 或 `asyncTaskConfig.authMode` 等字段。只要计划里存在没有 `apiCode` 的匿名资产，这种推断就非常脆弱。

本变更保持在现有 `ApiImportAgentController` 契约文件 `docs/api/api-import-agent.yaml` 内完成。API 文档必须先用 `tml-docs-spec-generate` 的 API 模板更新。由于现有 JSON 快照字段可以存储新增计划字段，预计不需要 SQL 文档更新。

## 目标 / 非目标

**目标：**

- 让澄清项具备机器可寻址能力，并能跨 turn 稳定定位。
- 让客户端提交结构化答案，避免计划细化依赖编号文本解析。
- 保持旧版自由文本聊天和 `clarificationQuestions` 可用。
- 确保答案更新已有的不完整资产计划，包括匿名资产，而不是制造重复资产或遗留阻塞项。
- 对已配置 provider 优先使用 staged tool-calling，使结构化契约真正生效。

**非目标：**

- 不新增 Import Agent controller，也不引入独立 workflow resource。
- 不为澄清状态新增数据库表。
- 不向控制台暴露原始 chain-of-thought 或 provider payload。
- 不把资产写入规则移出 `ApiAssetUseCase` / `CategoryUseCase`。

## 决策

### 决策 1：新增 `clarificationItems`，保留 `clarificationQuestions`

`ImportAgentPlanModel` 增加结构化澄清项列表。每个 item 应包含：

- `id`：由计划版本、目标路径、字段 key 和可选资产身份生成的稳定 id。
- `targetPath`：类似 JSON Pointer 的路径，例如 `/assetPlans/0/authConfig`。
- `fieldKey`：规范字段名，例如 `authConfig`。
- `label` 和可选 `description`：面向用户展示的文案。
- `inputType`：`TEXT`、`SELECT`、`BOOLEAN` 或 `MULTILINE`。
- `required`：是否必须回答后才能执行。
- `options`：枚举/选择型字段的可选值。
- `currentValue`：当已有答案时，可选的当前展示值。

`clarificationQuestions` 保留为派生文本列表，用于旧客户端和 assistant 回复。

备选方案：只保留文本问题并增强 prompt。拒绝原因是它仍然要求 LLM 从用户文本里推断字段映射。

### 决策 2：扩展 append-turn，支持结构化答案

`AppendImportAgentTurnReq` 接收可选 `clarificationAnswers`，每个 answer 包含 `clarificationId`、`targetPath`、`fieldKey` 和 `value`。`message` 继续支持自由文本指令；如果 API 契约允许，guided UI 可以在存在答案时提交空消息或通用消息。

应用服务在调用 `plannerPort.plan(...)` 前先把这些答案写入当前计划。planner 收到的 current plan 已经包含确定性用户答案，它只需要复核和补齐剩余字段。

备选方案：新增独立 `/clarifications` 端点。拒绝原因是 append-turn 已经表达“用用户输入推进 session”，第一版保持 public controller 表面更小。

### 决策 3：确定性答案应用属于服务/规划支持层，不属于 adapter

adapter 只负责把请求 DTO 映射成 command。字段路径校验、答案应用和冲突处理属于 workflow 规则，应放在服务层或 planner support 中。基础设施 planner 可以规范化最终 JSON，但不应该是唯一理解已提交答案的地方。

备选方案：让前端改写最新计划并发回后端。拒绝原因是后端必须保持 session truth source。

### 决策 4：匿名资产优先通过 target path 匹配，而不是只依赖身份字段

如果澄清项指向匿名资产计划，target path 和生成的 clarification id 必须在下一轮继续定位同一个资产。当答案提供 `apiCode`、`assetName` 或 `assetType` 时，后端必须更新现有匿名资产，而不是追加一个新资产并保留旧的空资产。

备选方案：要求 planner 返回任何资产计划前都必须先具备 `apiCode`。拒绝原因是用户常常需要逐步澄清身份字段。

### 决策 5：tool-calling 应成为已配置环境的默认路径

分阶段 planning tools 和 schema 约束已经存在，但当前应用配置里 `tool-calling-enabled` 默认是 false。本变更应将已配置环境默认切到 tool-calling，同时保留该属性作为回滚控制。

备选方案：继续依赖 direct JSON path 和更强 prompt。拒绝原因是当前糟糕体验正是弱结构和反复推断造成的。

## 风险 / 权衡

- [API 契约扩展] -> 保持新增字段为 additive，并保留旧字段。
- [planner 重排资产后结构化答案指向过期路径] -> 使用稳定目标元数据生成 clarification id，并在 planner 重排前应用答案。
- [某些 provider 与 tool-calling 不兼容] -> 保留现有 `tool-calling-enabled` 配置作为回滚开关。
- [模型/schema 复杂度上升] -> 将确定性答案应用保持小而可测，不把它扩展成通用 JSON patch 引擎。

## 迁移计划

1. 通过 `tml-docs-spec-generate` 更新 `docs/api/api-import-agent.yaml`，加入 `clarificationItems` 和 `clarificationAnswers`。
2. 增加后端 DTO/model 字段，同时保持旧请求和旧响应行为。
3. 实现 planner 调用前的确定性答案应用。
4. 更新 planner JSON support 和 validator，生成结构化 item 和派生文本问题。
5. 在应用配置中调整 tool-calling 默认值，并保留环境级覆盖。
6. 增加 legacy free text、structured answers 和匿名资产更新测试。
7. 前端随后消费新增字段；旧客户端继续通过 `clarificationQuestions` 工作。

## 待确认问题

- 当 `clarificationAnswers` 非空时，`message` 是否仍然必填，还是允许 answer-only turn？
