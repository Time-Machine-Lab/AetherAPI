## Why

当前 `aether-console` 已经接入了真实控制台会话、API 市场浏览、分类与资产管理、API Key 生命周期、统一接入调用以及 API 调用日志等核心能力，项目状态已经从“前后端分别开发”进入“前后端联调与业务验收”阶段。与此同时，前端仓库当前可见的自动化测试仍主要覆盖 `app-config`、控制台导航壳层、本地化资源完整性和 API 调用日志 API 映射，缺少对真实认证恢复、受保护路由、工作台管理动作、统一接入错误边界以及日志追踪闭环的系统验证。

另外，`aether-console` 当前仍保留本地 mock adapter 分支与本地测试数据路径。如果不先把“真实后端联调应该验证哪些链路、在哪个环境验证、如何判断当前是否仍在 mock 模式、失败后要留什么证据”固化下来，团队很容易得到“页面能操作，但其实还没离开 mock 数据”的假闭环，也难以在后续接口回归时快速定位问题归属。

因此需要新增一份前端侧的联调测试提案，在不发明新接口、不扩展产品范围、不默认引入新测试平台的前提下，为 `aether-console` 建立一套可重复执行、可回归、可定位问题的接口与业务流程验证基线。

## What Changes

- 为 `aether-console` 新增一套面向真实后端联调阶段的验证方案，覆盖控制台登录与会话恢复、受保护路由、API 市场浏览、分类与资产管理、API Key 生命周期、统一接入调用以及 API 调用日志追踪等核心链路。
- 在现有 `Vitest + lint + type-check + build` 工程基础上补齐契约对齐的自动化回归测试，并明确哪些能力只能通过真实后端环境进行人工冒烟验证。
- 统一前端联调时的环境边界与执行记录要求，明确 mock 开关、接口基地址、测试账号、前置测试数据、失败步骤、错误码或 `traceId` 等关键信息的记录方式。
- 为后续 apply 阶段预留必要的测试辅助层、共享夹具、业务流回归矩阵与联调结果沉淀方式，但本提案阶段不修改根目录 `docs/api/*.yaml`，也不新增产品功能。
- 本次变更不默认引入新的 E2E 平台，不扩展 `aether-admin-console` 与 `aether-web-marketing`，也不改动后端接口契约；如实现期发现现有权威文档不足以支撑测试边界，必须先同步更新顶层文档，再继续实现。

## Capabilities

### New Capabilities

- `console-integration-verification`: 定义 `aether-console` 在真实后端联调阶段的测试范围、分层验证策略、环境边界、证据留存与回归门槛。

### Modified Capabilities

- 无。

## Impact

- 受影响应用：`aether-console`
- 依赖的权威契约：`../docs/api/console-auth.yaml`、`../docs/api/api-catalog-discovery.yaml`、`../docs/api/api-category-lifecycle.yaml`、`../docs/api/api-asset-management.yaml`、`../docs/api/api-credential.yaml`、`../docs/api/unified-access.yaml`、`../docs/api/api-call-log.yaml`
- 参考的权威规范与设计：`../docs/spec/AetherAPI 前端技术栈与开发规范文档.md`、`aether-console/DESIGN.md`、必要的 `../docs/design/**/*.md`
- 预期影响的前端区域：`src/app` 路由守卫与启动逻辑、`src/api/http.ts`、`src/composables/useConsoleAuth.ts`、`src/stores/useAuthStore.ts`、`src/pages/index.vue`、`src/pages/workspace.vue`、`src/pages/playground.vue`、`src/pages/sign-in.vue`、`src/features/credential`、`src/features/unified-access`、`src/features/api-call-log`、相关 `*.spec.ts`
- 当前 `aether-ui/openspec/project.md` 缺失，因此本提案以子项目 `openspec/config.yaml`、现有权威文档和当前代码结构作为主要依据
- 当前提案阶段不要求修改根目录 `docs/api/`、统一前端规范文档或 `aether-console/DESIGN.md`；如实现期发现这些顶层文档无法完整覆盖测试边界，必须先补齐权威文档再继续 apply
