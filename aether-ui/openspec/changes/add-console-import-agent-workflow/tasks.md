## 1. 契约与落位确认

- [x] 1.1 重新核对 `../docs/spec/AetherAPI 前端技术栈与开发规范文档.md`、`aether-console/DESIGN.md`、`../docs/api/api-import-agent.yaml` 与现有 `console-workspace` / `ConsoleLayout` 结构，确认本次实现不需要先更新顶层文档。
- [x] 1.2 盘点 `src/features/console/console-shell.ts`、`src/layouts/ConsoleLayout.vue`、`src/pages/workspace.vue`、现有业务域 API 模块与测试写法，确定 import-agent 的文件落位、hash 入口和最近活动会话缓存键方案。

## 2. API 与状态编排

- [x] 2.1 在 `src/api/import-agent/` 下新增 DTO、types 与 API 模块，对齐会话、计划、轮次、运行批次和步骤结果字段映射。
- [x] 2.2 在 `src/features/import-agent/` 下实现工作流状态编排，覆盖创建会话、恢复最近会话、追加澄清、确认计划、启动执行、运行轮询和缓存清理。
- [x] 2.3 将 import-agent 的错误态、加载态和确认门禁统一接入现有 `NormalizedHttpError` 与 i18n 文案体系，不在页面层直写裸错误处理。

## 3. 工作台与导航集成

- [x] 3.1 扩展控制台侧边导航与工作台 hash 分区，新增 import-agent 入口，并保持对隐藏分区的既有回退规则。
- [x] 3.2 在受保护工作台中新增 import-agent 工作区界面，至少包括对话式消息输入区、轮次列表、计划摘要区、确认执行区和运行结果区。
- [x] 3.3 补齐中英文 i18n 资源以及与 `aether-console/DESIGN.md` 一致的空态、失败态和执行中反馈，不引入新的视觉体系。
- [x] 3.4 为 import-agent 工作区补充本地文本文件附加能力，在浏览器侧完成文件读取、截断、移除与错误反馈，并继续复用现有后端接口。

## 4. 测试与验收

- [x] 4.1 为 import-agent API 映射与状态编排补齐测试，覆盖创建、恢复、澄清、确认、执行、轮询停止和恢复失败清理等核心路径。
- [x] 4.2 为工作台入口与页面行为补齐测试，覆盖导航可见性、hash 进入 import-agent 分区、旧确认失效和运行终态展示。
- [x] 4.3 运行 `pnpm lint`、`pnpm type-check`、`pnpm build` 与针对 import-agent 的测试命令，确认前端对接未破坏现有控制台工程基线。