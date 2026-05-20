## 背景

当前 `aether-console` 已经接入 import-agent 的流式会话接口，但 UI 只能看到 `planning / replying / completed` 这样的粗粒度状态和最终 reply delta。真正对用户有帮助的中间过程，例如“正在提取文档事实”“识别到鉴权缺口”“因为证据冲突转为澄清”等内容，仍然只存在于后端日志里，前端无法消费，也无法在工作区中展示。

如果直接把这些过程混进最终回复文本，会破坏对话语义；如果继续只显示单行状态，用户在长文档或复杂导入任务中又无法判断系统是否在推进。因此，前端需要一个专门的提案来承接后端新增的 `thinking` SSE 事件，把 import-agent 的流式体验从“正在回复”升级为“正在规划 + 正在思考 + 正在回复”的结构化工作流，同时继续避免展示 raw CoT 或敏感内部细节。

## 变更内容

- 在 `aether-console` 的 import-agent 流式消费链路中新增 `thinking` SSE 事件解析与类型定义，对接后端升级后的 `../docs/api/api-import-agent.yaml` 契约。
- 在 import-agent 工作区状态模型中新增独立的 streaming thoughts / reasoning timeline 状态，而不是把 thinking 文本混进现有 `streamingReply`。
- 在 `ImportAgentWorkspace` 中新增“代理思考中”展示区，以时间线或卡片列表形式流式呈现 planner / subagent 的安全思考摘要，并与最终 reply 区分渲染。
- 保持现有会话快照、计划卡片、运行结果和最终 reply 展示逻辑不变；当后端未返回 `thinking` 事件时，前端仍可退化为现有 `status + reply` 模式。
- 为 thinking 事件展示补充 i18n、状态编排测试和 UI 行为测试，确保不会破坏当前 import-agent 工作区。

## 能力变更

### 已修改能力

- `console-import-agent-workflow`
  - 控制台 import-agent 工作流在消费 stream 会话接口时，必须能够解析并展示结构化 `thinking` 事件，把规划过程摘要与最终 reply delta 分开呈现。
  - 当前端未收到 `thinking` 事件时，工作流必须继续保持现有 `status` 和 `message` 渲染能力，而不是因为缺少 thinking 数据而失效。

## 影响范围

- 受影响应用：`aether-console`
- 依赖的权威契约：
  - `../docs/api/api-import-agent.yaml`
  - `../docs/spec/AetherAPI 前端技术栈与开发规范文档.md`
  - `aether-console/DESIGN.md`
- 预期影响区域：
  - `src/api/import-agent/import-agent.dto.ts`
  - `src/api/import-agent/import-agent.types.ts`
  - `src/api/import-agent/import-agent.api.ts`
  - `src/composables/useImportAgentWorkspace.ts`
  - `src/features/import-agent/ImportAgentWorkspace.vue`
  - `src/locales/zh-CN/common.ts`
  - `src/locales/en-US/common.ts`
- 权威文档影响：
  - 前端实现依赖后端 change 同步更新 `../docs/api/api-import-agent.yaml` 的 stream 事件契约；本前端提案本身不单独发明新后端字段。
