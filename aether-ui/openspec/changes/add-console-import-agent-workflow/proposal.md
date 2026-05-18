## Why

后端已经提供当前用户维度的 API Import Agent 会话、计划确认与执行接口，但 `aether-console` 仍缺少对应的前端工作流，导致控制台用户无法在现有受保护壳层内完成“提交导入意图 -> 查看计划 -> 补充澄清 -> 确认执行 -> 查看运行结果”的闭环。现在补上这条前端对接提案，可以把已完成的后端 import-agent 能力接入现有控制台导航、认证与 API 层模式，避免继续依赖手工接口调试或临时脚本验证。

## What Changes

- 在 `aether-console` 中新增面向当前登录用户的 import-agent 工作流，对接 `../docs/api/api-import-agent.yaml` 已定义的会话创建、轮次追加、计划确认、执行启动和运行详情查询接口。
- 在现有 `ConsoleLayout` 和受保护 `console-workspace` 工作台内新增 import-agent 入口与对应页面编排，而不是发明一套新的控制台壳层或绕开既有会话守卫。
- 为 import-agent 补齐前端 API 模块、DTO/types 映射、页面状态模型、轮次消息展示、计划摘要展示、确认门禁、执行结果反馈与错误态处理，并保持用户可见文案接入 i18n。
- 为 import-agent 前端对接补充最小必要的自动化测试，覆盖 API 映射、核心状态切换、确认门禁与工作台入口行为，保证不会破坏现有工作台与导航模型。
- 本次提案默认不新增后端接口、不修改 `../docs/api/api-import-agent.yaml` 契约；但前端会同步把工作区交互改成对话式消息流，并补充 `aether-console/DESIGN.md` 与当前 OpenSpec 设计说明，明确本地文件附加只在浏览器侧做文本提取，不引入新的后端上传契约。

## Capabilities

### New Capabilities
- `console-import-agent-workflow`: 定义 `aether-console` 中当前用户 import-agent 会话创建、澄清轮次、计划确认、执行触发与运行结果查看的前端工作流。

### Modified Capabilities
- `console-section-visibility`: 控制台受保护壳层的可见入口集合需要新增 import-agent 工作流入口，并为其提供与现有工作台 hash 导航一致的可达性与回退行为。

## Impact

- 受影响应用：`aether-console`
- 依赖的权威契约：`../docs/api/api-import-agent.yaml`、`../docs/spec/AetherAPI 前端技术栈与开发规范文档.md`、`aether-console/DESIGN.md`、必要的 `../docs/design/**/*.md`
- 预期影响的前端区域：`src/api/**` 新增 import-agent 业务域模块、`src/pages/workspace.vue` 或相邻受保护页面编排、`src/features/console/console-shell.ts`、`src/layouts/ConsoleLayout.vue`、相关 i18n 资源和 `*.spec.ts`
- 本次提案以现有控制台登录态、工作台 hash 导航、统一 axios 实例和 shadcn-vue 组件模式为前提，不引入新的前端框架、状态管理方案或 E2E 平台