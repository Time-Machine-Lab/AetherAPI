## Context

当前 `aether-console` 已经具备受保护控制台的基本骨架，`src/app/route-guards.ts` 会根据 `meta.requiresAuth` 判断是否跳转登录页，`src/api/http.ts` 也会把 `useAuthStore` 中的 token 注入 `Authorization` 请求头。但现状仍是前端本地伪造会话：

- `src/stores/useAuthStore.ts` 的 `signIn()` 直接写入 `${appId}-demo-token`，并使用本地构造的用户资料；
- `src/pages/sign-in.vue` 仍然是演示访问表单，只采集操作员名称与邮箱，不调用后端登录接口；
- 登录页、多语言文案和布局说明都明确写着“本地演示会话”；
- 受保护路由只检查本地 token 是否存在，不会在刷新或直达受保护页面时向后端恢复当前会话，也不会在 token 失效时统一清理状态。

这与后端刚归档的 `2026-04-21-add-console-session-auth` 已经形成明显断层。后端现在已经提供：

- 根目录权威接口契约 `docs/api/console-auth.yaml`；
- `POST /console/auth/sign-in` 登录接口，入参为 `loginName` 与 `password`；
- `GET /console/auth/current-session` 会话恢复接口，返回当前控制台用户；
- 受保护控制台业务接口依赖真实控制台 bearer token 建立 `Principal`，而不是接受本地 demo token。

因此，前端需要在不扩展用户体系、不发明新字段、不把请求编排塞进页面层或 Store 的前提下，把控制台从“本地演示登录”平滑切换到“真实后端会话认证”。

本设计以以下约束为前提：

- 技术栈与分层遵循 `docs/spec/AetherAPI 前端技术栈与开发规范文档.md`：页面层不写裸请求，请求经统一 `axios` 实例发起，异步编排位于 `src/api`、`src/features` 或 `src/composables`，Store 只承载全局共享状态。
- 视觉与交互遵循 `aether-console/DESIGN.md` 中的控制台语义角色、表单与状态反馈规则；登录态变更不能破坏现有控制台壳层的布局语言。
- 契约边界以 `docs/api/console-auth.yaml` 为准；控制台 bearer token 与 Unified Access 的 `X-Aether-Api-Key` 必须保持语义分离。

## Goals / Non-Goals

**Goals:**

- 让 `aether-console` 通过 `docs/api/console-auth.yaml` 定义的真实后端接口完成登录与会话恢复，替换本地 demo token。
- 让受保护路由、控制台壳层、请求鉴权注入和 401 失效回退都依赖统一的真实会话状态，而不是分散判断。
- 保持前端统一分层：登录接口与会话恢复放在 API 层，会话编排下沉到组合式能力或等价的领域层，Store 只存全局共享登录态。
- 明确并保留“控制台登录态”和“Unified Access API Key”是两条不同鉴权语义，避免前端交互混淆。

**Non-Goals:**

- 不在本次提案中新增注册、找回密码、修改密码、RBAC、租户管理、SSO 或 refresh token 体系。
- 不引入新的根目录 `docs/sql/*.sql` 或前端额外用户模型文档，也不把控制台登录扩展为完整用户中心。
- 不修改 Unified Access 的 API Key 鉴权行为，不让控制台 bearer token 直接参与统一接入调用鉴权。
- 不在本次提案中改造 `usage`、`orders`、`billing` 等其他占位导航能力。

## Decisions

### 1. 前端认证能力以独立的 `console-session-auth` 领域对接后端契约，而不是继续扩充 demo auth

- 决策：新增前端控制台会话认证领域能力，直接围绕 `docs/api/console-auth.yaml` 建立登录、会话恢复与失效回退链路，不在现有 demo sign-in 上继续叠加临时逻辑。
- 原因：当前 demo auth 的问题不只是“字段不全”，而是整条认证链路没有与后端建立事实连接；继续在本地伪造 token 只会放大联调偏差。
- 备选方案：保留 demo sign-in，仅在少量页面上追加“真实登录”开关。
- 不采用原因：会导致控制台同时存在两套登录语义，受保护路由和请求层难以保持一致，也会增加后续清理成本。

### 2. 会话编排放在 API 层 + 组合式能力，Store 仅保存全局共享认证快照

- 决策：登录与当前会话恢复接口由独立 `src/api/console-auth/*` 模块负责，认证状态编排放在 `src/composables` 或 `src/features/auth` 等领域层中；`useAuthStore` 仅保存 `accessToken`、`currentUser`、恢复状态和本地持久化结果。
- 原因：前端统一规范明确要求异步请求与接口编排不要直接堆在 Store 中，同时页面层也不应出现裸请求。
- 备选方案：把 `signIn()`、`restoreSession()`、`handleUnauthorized()` 全部做成 Store action。
- 不采用原因：会把全局状态与请求编排深度耦合，降低可测试性，也偏离仓库既有的 API 分层模式。

### 3. 受保护路由进入前需要等待一次“会话恢复判定”，避免刷新后误跳转或短暂泄露受保护内容

- 决策：应用启动或进入 `meta.requiresAuth` 路由时，如果本地存在已保存 token，则必须先调用当前会话接口完成一次恢复判定；恢复成功后才继续进入受保护页面，恢复失败则清理本地状态并回到登录页。
- 原因：当前本地只要有 token 字符串就视为已登录，这会在真实 token 过期、被清理或无效时造成“前端以为已登录、后端实际拒绝”的体验断层。
- 备选方案：仍由路由守卫同步检查 token 是否存在，等页面请求返回 401 后再被动踢回登录页。
- 不采用原因：会带来闪屏、错误页面和重复请求，且受保护内容可能先渲染再被打回，不符合真实会话心智。

### 4. 401 / `CONSOLE_SESSION_UNAUTHORIZED` 统一视为控制台会话失效信号，但不能误伤 Unified Access 业务失败语义

- 决策：统一请求层或认证编排层需要识别控制台会话失效响应，并触发清理本地认证态、回跳登录页、保留目标路由上下文等处理；同时必须避免把 Unified Access 的平台前置失败、API Key 无效等业务失败误判成控制台登录失效。
- 原因：控制台 bearer token 与 Unified Access API Key 是两条独立链路，前者用于控制台管理接口，后者用于开发者真实调用；两者失败语义必须清晰分开。
- 备选方案：所有 401 一律视为“重新登录”。
- 不采用原因：调用工作台未来可能需要展示更细的业务失败信息，粗暴拦截会污染统一接入的产品语义。

### 5. 登录页切换为真实登录表单，但继续复用现有控制台壳层和设计语义

- 决策：登录页改为收集 `loginName` 与 `password`，并提供登录中、失败态与必要帮助说明；整体仍复用 `ConsoleLayout`、现有卡片/字段/notice 语义和 `aether-console/DESIGN.md` 的表单层级，不新建第二套登录应用。
- 原因：当前登录页位置、视觉骨架与控制台壳层已经稳定，最小改动路径是替换表单语义与交互，而不是重建页面框架。
- 备选方案：单独新建完全不同的营销式登录页。
- 不采用原因：会割裂控制台产品体验，也没有必要引入新的视觉规范。

## Risks / Trade-offs

- [后端当前仅提供 access token、未提供 refresh token] → 前端首期以“刷新时调用 current-session 判定有效性”为主，不自行发明续期机制；后续如需无感续期，应由新的顶层契约单独定义。
- [会话恢复引入异步守卫后，路由切换复杂度增加] → 通过统一的“已初始化 / 恢复中 / 已认证 / 未认证”状态模型约束流程，避免各页面重复判断。
- [登录失败与会话失效提示容易和统一接入错误语义混淆] → 登录页、控制台管理接口和调用工作台分别保持独立错误映射，不共用“API 调用失败”文案。
- [当前本地存储方案与真实 token 持久化之间存在安全权衡] → 首期沿用现有本地持久化模式以保持最小改动，但所有失效判定以后端 current-session 为准；如果后续需要更严格的会话存储策略，应通过独立提案评估。

## Migration Plan

1. 在实现开始前重新核对 `docs/api/console-auth.yaml`、`docs/spec/AetherAPI 前端技术栈与开发规范文档.md` 与 `aether-console/DESIGN.md`，确认现有顶层文档足以覆盖本次前端对接。
2. 新增控制台认证 API 模块、类型映射与认证编排层，把 `useAuthStore` 从 demo token 模式切换到真实会话快照模式。
3. 改造登录页、路由守卫、布局层和统一请求入口，使登录、恢复、失效回退形成闭环。
4. 验证 API Key 管理、调用日志、调用工作台等受保护页面在真实控制台会话下可以正常工作，并确认 Unified Access 的 API Key 鉴权语义未受影响。
5. 如需快速回退，可暂时恢复前端 demo session 实现并停用真实登录接入；由于本次不修改 Unified Access 鉴权链路，回退范围可限定在 `aether-console` 前端。

## Open Questions

- 首期是否需要在登录页明确展示 token 过期后的重新登录说明，还是仅通过通用失效提示引导用户返回登录页即可？
- 前端是否需要保留“登录后回到原目标页”的完整 hash/查询参数恢复能力，还是首期只恢复到命名路由级别即可？
