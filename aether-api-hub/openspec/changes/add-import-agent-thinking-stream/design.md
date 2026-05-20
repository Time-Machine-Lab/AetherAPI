## 上下文

当前 Import Agent 的流式接口已经具备 SSE 基础能力，但其语义仍然偏窄。`ApiImportAgentWebDelegate` 只会把会话流写成 `status`、`message`、`session`、`error` 和 `done` 五类事件；service 层通过 `Consumer<String>` 把最终回复 delta 透传给 adapter；planner provider 与 subagent orchestrator 虽然已经拥有较完整的内部阶段和日志，但这些信息只存在于后端内部实现中，不能以结构化流事件返回给前端。

因此，当前控制台即使已经接入 import-agent stream，也只能显示一条状态消息和最终回复增量，看不到“正在提取文档事实”“识别到异步模式”“因为证据冲突降级为澄清”这类对用户和调试都重要的中间过程。另一方面，这次 change 也不能走向“直接把原始模型思考内容暴露给前端”，因为现有上游模型接口并不保证提供稳定 reasoning 字段，且 raw CoT、敏感 prompt、密钥占位或 provider payload 都不适合进入前端可见流。

本次设计要解决的是：在保持单一 session 快照和单一最终回复语义不变的前提下，为 Import Agent stream 增加一个安全、结构化、可前端消费的 thinking 事件层。

## 目标 / 非目标

**目标：**

- 为 import-agent stream 新增结构化 thinking 事件，使前端可以在会话创建和轮次追加期间展示规划过程摘要。
- 用统一 stream emitter 替代当前只支持 reply delta 的 `Consumer<String>`，让 planner、subagent 和 reply 都能通过同一通道发事件。
- 明确 thinking 事件的脱敏边界，禁止把 raw CoT、auth secrets、完整 prompt、未脱敏 provider payload 暴露到前端。
- 保持现有 REST session / run 契约、现有 SSE `session` 最终快照和现有会话持久化模型不变。
- 让旧客户端在忽略 `thinking` 事件时仍可继续消费 `status` / `message` / `session` / `error` / `done`，降低协议升级风险。

**非目标：**

- 不暴露原始模型 chain-of-thought、推理 token 明细或完整 prompt。
- 不为 thinking 事件新增数据库持久化表、历史轨迹表或回放 API。
- 不新增独立的 stream endpoint、WebSocket 通道或额外的 import-agent 控制器资源。
- 不把整个 planner 改造成通用 agent tracing 平台；本次只服务 import-agent 流式可观测性。

## 设计决策

### 决策 1：thinking 事件展示的是安全思考摘要，而不是 raw CoT

stream 新增的事件类型是结构化 `thinking`，其作用是表达“系统当前在做什么”和“为何进入某个中间结论”，例如：

- 正在提取文档事实
- 识别到 1 个候选资产
- 检测到异步 submit / query 模式，准备并入 `asyncTaskConfig`
- 鉴权字段证据不足，转为 clarification

这些内容由本地 planner / orchestrator 生成或整理，不等同于上游模型原始思维链。

不采用“直接透传模型 reasoning 字段”的原因：不同上游模型对 reasoning 的支持并不稳定，而且 raw reasoning 容易泄露 prompt、密钥占位、幻觉性内部假设或其他不适合对用户展示的内容。

### 决策 2：用统一 stream emitter 替代单一 reply delta consumer

当前 service 层只能接收 `Consumer<String>`，它只适合传递 reply delta，不适合表达结构化事件。需要引入统一的 stream emitter 抽象，使 service 和 adapter 之间可以传递多种事件：

- `status`
- `thinking`
- `message`
- `session`
- `error`
- `done`

这让 planner 和 reply 可以共享同一流式通道，而不必把 thinking 伪装成 AGENT message。

不采用“把 thinking 文本混入 `message` 事件”的原因：前端无法区分哪些内容是思考摘要，哪些内容是最终用户可见回复，会破坏对话语义。

### 决策 3：thinking 事件只在稳定边界发射，不对每个内部细节逐 token 直播

thinking 事件的发射边界应当稳定且可测试，推荐至少覆盖：

- planning 开始
- extract / fill / submit 等阶段完成
- subagent 开始 / 完成
- 关键 merge / review 决策
- clarification downgrade
- reply 生成开始

每条 thinking 事件都应是可读的摘要，而不是高频碎片流。

不采用“每个内部判断都发事件”的原因：会使前端时间线噪声过多，也会抬高后端维护和测试成本。

### 决策 4：thinking 事件必须是加法式协议升级

SSE 契约新增 `thinking` 事件，但保留现有 `status` / `message` / `session` / `error` / `done` 事件语义。旧前端即使不解析 `thinking`，也应继续按原方式工作。

不采用“重写所有事件命名和载荷”的原因：会把本次增强变成一次破坏性协议重构，扩大联动面。

### 决策 5：最终 `session` 快照仍然是会话真相源

thinking 事件只是流式辅助信息。最终会话状态、最新计划版本、澄清问题、turns 和 run 关联仍以最终 `session` 事件和 REST 读取结果为准。

不采用“让前端仅靠 thinking 事件推导最终计划”的原因：会让 UI 与服务端真相源分叉，并把规划中间态误当最终事实。

### 决策 6：thinking 事件内容必须经过脱敏和摘要化

对于以下内容，thinking 事件必须避免直接透出：

- `authConfig` 明文
- API Key、Bearer Token、环境变量占位名建议
- 完整 prompt / system prompt
- 上游模型原始 JSON payload
- 原始异常堆栈或完整 provider 响应正文

允许透出的应是安全摘要，例如“检测到上游鉴权配置缺失，已转为追问”。

## 风险与权衡

- [service 层 emitter 重构会触及 createSession / appendTurn 主链路] -> 通过加法式 emitter 接口替换当前 `Consumer<String>`，并保留旧语义映射为 `message` 事件，降低回归风险。
- [thinking 事件内容过多导致前端噪声] -> 只在稳定阶段边界和关键决策点发事件，不做逐 token reasoning 流。
- [上游模型不提供 reasoning 字段] -> thinking 事件优先来自本地 planner / orchestrator 摘要，不依赖 provider 原生 reasoning 能力。
- [新增事件可能让旧前端解析失败] -> 保持 SSE 协议加法式扩展，未知事件允许被忽略。
- [不慎泄露内部敏感信息] -> 在 emitter 边界统一做 thinking 载荷约束与脱敏，测试覆盖典型敏感字段场景。

## 迁移计划

1. 先更新 `docs/api/api-import-agent.yaml` 的 SSE 契约，补充 `thinking` 事件类型和载荷示例。
2. 在 service 层引入统一 stream emitter，并让 adapter delegate 改为按事件类型写 SSE。
3. 让 planner provider、subagent orchestrator 和 reply port 在稳定边界发出 thinking / status / message 事件。
4. 增补 adapter、service、infrastructure 的回归测试，确认旧事件链和最终 session 快照语义不变。
5. 由前端 change 对接 thinking 事件解析与展示；旧前端在未升级前仍可安全忽略该事件。
