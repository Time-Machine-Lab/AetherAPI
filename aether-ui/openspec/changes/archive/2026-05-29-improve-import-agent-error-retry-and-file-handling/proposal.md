## Why

`aether-console` 的 import-agent 对话在后端返回错误时会清空当前乐观展示的用户消息，用户看到的效果像是消息被撤回；同时本地文本附件会在前端拼入请求前被截断，导致后端无法基于完整内容做统一长度校验和错误反馈。

本次变更需要把这两类失败体验收敛到对话流内：用户能看到自己已经发出的内容、能看到明确报错信息，并能直接重新发送同一条请求。

## What Changes

- import-agent 创建会话或追加轮次失败时，前端保留本次已发送的用户消息气泡，不再因为 stream `error` 或请求异常清空该消息。
- 失败信息作为代理回复气泡展示在对话流中，内容使用现有 `NormalizedHttpError` 或 stream error message 解析出的用户可见报错文案。
- 失败回复气泡提供“重新发送”按钮，点击后复用上一次失败请求的 create-session 或 append-turn payload，而不是要求用户重新输入。
- 本地文本附件仍在界面卡片中展示短预览，但发送给后端的 `documentSummary` 或 append-turn `message` MUST 使用完整提取文本，不再按前端固定长度截断。
- 前端保留现有本地文件类型、空文件和数量校验；文件内容总长度超出后端允许范围时，由后端通过现有错误响应或 stream `error` 事件返回，并由前端展示在对话流中。
- 不新增后端接口、不修改 `../docs/api/api-import-agent.yaml` 契约；本次只调整 `aether-console` import-agent 前端状态编排、视图呈现、i18n 和测试。

## Capabilities

### New Capabilities
- `console-import-agent-message-resilience`: 定义 import-agent 对话在发送失败、重发失败请求、本地附件完整内容交给后端校验时的前端韧性行为。

### Modified Capabilities
- 无。现有后端 API 契约保持不变；本次新增的是围绕 import-agent 工作流的前端韧性行为契约。

## Impact

- 影响前端代码：`aether-console/src/composables/useImportAgentWorkspace.ts`、`aether-console/src/features/import-agent/ImportAgentWorkspace.vue`、locale 资源和 import-agent 相关单元/组件测试。
- 依赖文档边界：实现必须继续对齐 `../docs/api/api-import-agent.yaml`、`../docs/spec/AetherAPI 前端技术栈与开发规范文档.md` 和 `aether-console/DESIGN.md`。
- 预计不新增运行时依赖。
- 预计不涉及数据库、后端服务或 API 路径变更。
