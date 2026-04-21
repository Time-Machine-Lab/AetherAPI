## Why

后端归档提案 `2026-04-21-add-console-session-auth` 已经补齐了控制台登录、当前会话查询、控制台 bearer token 签发，以及受保护控制台接口的认证链路，仓库根目录也已经存在权威接口契约 `docs/api/console-auth.yaml`。但前端 `aether-console` 当前仍停留在“本地写入 demo token”的演示登录模式，`sign-in` 页面不会调用真实后端接口，刷新后的会话恢复、401 失效回退与受保护路由的真实认证状态也都尚未接入。

随着 API Key 管理、调用工作台和 API 调用日志等控制台能力已经进入联调范围，继续沿用本地 demo session 会让前端呈现出“看起来已经登录、实际上无法建立真实会话”的假闭环。因此需要新增一份前端对接提案，在不发明新契约、不扩展用户体系、不突破 `aether-console/DESIGN.md` 与前端统一分层规范的前提下，把控制台切换到真实的后端会话认证链路。

## What Changes

- 在 `aether-console` 中新增真实控制台会话认证前端能力，以 `docs/api/console-auth.yaml` 为唯一契约来源，对接控制台登录与当前会话恢复接口，替换现有本地 demo token 方案。
- 重构控制台认证状态管理，明确 `accessToken`、`currentUser`、会话恢复状态与本地持久化边界，使受保护路由、控制台壳层和统一请求入口都以真实会话状态为唯一事实来源。
- 调整登录页信息架构与交互，从“演示访问”切换为真实登录表单，补齐登录中、登录失败、会话恢复中、会话失效后的国际化反馈。
- 为控制台受保护页面建立统一的未登录跳转与会话失效回退机制，保证 API Key、调用日志、调用工作台等依赖控制台登录态的页面在 token 无效时回到登录入口，而不是继续持有失效本地状态。
- 明确控制台 bearer token 与 Unified Access `X-Aether-Api-Key` 的边界：控制台登录态只用于控制台业务接口鉴权，不替代调用工作台中的 API Key 鉴权语义。
- 本次变更不包含注册、找回密码、RBAC、管理端多角色体系、refresh token、SSO，也不改造 Unified Access 面向 API 消费者的鉴权规则。

## Capabilities

### New Capabilities

- `console-session-auth`: 定义 `aether-console` 中真实控制台登录、当前会话恢复、受保护路由认证联动、401 会话失效回退，以及控制台 bearer token 与 Unified Access API Key 边界表达。

### Modified Capabilities

- 无。

## Impact

- 受影响应用：`aether-console`
- 依赖的权威契约：`../docs/api/console-auth.yaml`
- 参考的权威设计与规范：`../docs/spec/AetherAPI 前端技术栈与开发规范文档.md`、`../docs/design/**/*.md`、`aether-console/DESIGN.md`
- 预期影响的前端区域：`src/pages/sign-in.vue`、`src/app` 路由守卫与启动逻辑、`src/stores` 认证状态、`src/api` 请求封装与认证 API 模块、`src/layouts/ConsoleLayout.vue`、`src/locales`
- 当前提案阶段不要求新增或修改根目录 `docs/api/`、统一前端规范文档或 `aether-console/DESIGN.md`；如果实现阶段发现现有权威文档不足以覆盖登录交互、会话恢复或认证失效反馈，必须先同步这些顶层文档再进入代码实现
