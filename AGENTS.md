## Project OpenSpec Routing

在处理需求前，先判断需求归属的子项目，并读取对应的 OpenSpec 配置，而不是默认只看根目录 OpenSpec：

- 前端需求：如果需求明确指向 `aether-ui`，或涉及页面、路由、组件、样式、交互、Vue、Vite、Tailwind、`aether-console`、`aether-admin-console`、`aether-web-marketing`，优先读取 `@/aether-ui/openspec/config.yaml`。
- 后端需求：如果需求明确指向 `aether-api-hub`，或涉及接口、服务、数据库、SQL、后端业务逻辑、API 契约，优先读取 `@/aether-api-hub/openspec/config.yaml`。
- 跨端需求：如果同一需求同时涉及前端和后端联动、接口联调、全链路改动，必须同时读取 `@/aether-ui/openspec/config.yaml` 和 `@/aether-api-hub/openspec/config.yaml`。
- 归属不明确：如果用户没有明确说明前后端归属，先根据目标目录、文件路径、技术栈关键词和改动范围判断；仍不明确时，再补充读取根目录 `@/openspec/AGENTS.md` 作为通用 OpenSpec 工作流说明。

注意：当前前后端子项目下尚无各自的 `openspec/AGENTS.md`，因此子项目级 OpenSpec 约束以前端 `openspec/config.yaml` 和后端 `openspec/config.yaml` 为准。
