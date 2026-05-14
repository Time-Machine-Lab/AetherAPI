## 1. 顶层契约与测试范围确认

- [x] 1.1 重新阅读 `../docs/spec/AetherAPI 前端技术栈与开发规范文档.md`、`aether-console/DESIGN.md`、`../docs/api/console-auth.yaml`、`../docs/api/api-catalog-discovery.yaml`、`../docs/api/api-category-lifecycle.yaml`、`../docs/api/api-asset-management.yaml`、`../docs/api/api-credential.yaml`、`../docs/api/unified-access.yaml`、`../docs/api/api-call-log.yaml`，确认本次测试范围完全来源于现有权威文档。
- [x] 1.2 对照 `src/pages/sign-in.vue`、`src/pages/index.vue`、`src/pages/workspace.vue`、`src/pages/playground.vue`、`src/app/route-guards.ts`、`src/api/http.ts` 与各业务模块 API/feature/composable，梳理已经完成真实对接的链路和仍可能命中 mock 或演示数据的边界。
- [x] 1.3 明确本次提案只覆盖 `aether-console`，不包含 `aether-admin-console` 与 `aether-web-marketing`；如实现期发现需要更新统一前端规范、`aether-console/DESIGN.md` 或根目录 API 契约，先停下完成顶层文档同步。
- [x] 1.4 产出一份核心回归矩阵，至少列出控制台认证、API 市场浏览、分类管理、资产管理、API Key 生命周期、Unified Access、API 调用日志 7 类链路的成功态、失败态、环境依赖和验收结论字段。

## 2. 测试基础设施与环境前置

- [x] 2.1 盘点 `aether-console/package.json`、`vite.config.ts` 和现有 `*.spec.ts`，确认继续沿用 `Vitest` 作为自动化回归主入口，不默认引入新的 E2E 平台。
- [x] 2.2 为认证、路由守卫、HTTP 拦截与页面状态编排补齐共享测试夹具，包括 localStorage 模拟、router push 监控、标准化 HTTP 错误对象、会话初始态与常用接口响应构造器。
- [x] 2.3 梳理 `src/api/http.ts` 中 mock adapter 的启用条件，明确真实联调启动方式、必要环境变量和校验手段，确保测试不会误打到本地 mock 数据。
- [x] 2.4 约定真实后端冒烟所需的固定控制台账号、测试 API 资产、测试分类、可回收 API Key、可观察日志样本与清理策略。
- [x] 2.5 约定联调执行记录格式，至少包含构建版本、前端分支、后端环境、接口基地址、测试账号、执行步骤、结果、错误码或 `traceId`、截图或日志位置。
- [x] 2.6 将“自动化回归先通过，再执行真实后端冒烟”的顺序写入实现任务基线，避免手工验证掩盖自动化缺口。

## 3. 控制台认证与受保护路由回归

- [x] 3.1 为 `src/api/console-auth/console-auth.api.ts` 补齐登录成功、参数错误、凭证错误、当前会话查询成功与 401 失败映射测试。
- [x] 3.2 为 `src/composables/useConsoleAuth.ts` 补齐 `signIn` 成功写入会话、无 token 恢复、有效 token 恢复、401 清会话、5xx 仅标记初始化等行为测试。
- [x] 3.3 为 `src/stores/useAuthStore.ts` 补齐会话持久化、初始化标记、清理会话与刷新恢复相关测试。
- [x] 3.4 为 `src/app/route-guards.ts` 补齐 `guestOnly`、`requiresAuth`、首次进入受保护路由触发恢复、未登录跳转登录页和 `redirectName` 保留等测试。
- [x] 3.5 为 `src/api/http.ts` 补齐 `Authorization` 注入、401 `CONSOLE_SESSION_UNAUTHORIZED` 自动清会话并跳转登录页、Unified Access 非会话失败不误踢出登录态等测试。
- [x] 3.6 为登录页和控制台壳层相关交互补齐最小必要的页面级测试，确认登录中、登录失败、会话恢复中和退出登录反馈符合现有 i18n 与设计语义。
- [x] 3.7 在真实后端环境手工验证登录成功、刷新恢复、直接访问受保护页面、会话失效后回退登录页 4 条认证主链路，并记录结果与失败证据。

## 4. API 市场与工作台管理业务流回归

- [x] 4.1 为 `src/pages/index.vue` 及相关市场编排补齐 API 市场列表加载、详情加载、空态、失败态与最近访问记录更新测试。
- [x] 4.2 为分类管理链路补齐分类列表、创建、重命名、启用/停用等工作台行为测试，并确认状态反馈与 i18n 文案对齐 `../docs/api/api-category-lifecycle.yaml`。
- [x] 4.3 为资产管理链路补齐资产查询、注册、详情查看、状态变更或 AI 能力绑定等关键动作测试，确保严格对齐 `../docs/api/api-asset-management.yaml`。
- [x] 4.4 为工作台页内多个分区的加载、空态、失败态与切换行为补齐基础页面测试，避免认证通过后仍出现局部业务流失效无反馈。
- [x] 4.5 在真实后端环境按“市场浏览 -> 进入工作台 -> 管理分类或资产 -> 返回确认”顺序执行一轮手工冒烟，确认页面数据来自真实接口而非本地 mock。
- [x] 4.5.1 通过真实后端接口创建测试分类，记录 `POST /categories` 与 `GET /categories/{categoryCode}` 的请求体、响应体、状态码与生成的分类编码。
- [x] 4.5.2 通过真实后端接口注册测试资产草稿，记录 `POST /assets` 返回的初始 `DRAFT` 状态、资产编码与详情快照。
- [x] 4.5.3 针对 `PUT /assets/{apiCode}` 依次尝试“仅改分类”“仅补上游配置”“组合补齐最小启用字段”三类修订请求，确认最小可行路径或稳定复现的错误边界。
- [x] 4.5.4 若资产修订成功则继续执行启用与市场发现验证；若资产修订失败，则沉淀 `500` 阻塞证据、影响范围与后续补测入口。
- [x] 4.6 如实现期发现分类管理或资产管理实际依赖的契约字段、交互提示或状态语义未被现有顶层文档覆盖，先补齐顶层文档再继续测试实现。

## 5. API Key、Unified Access 与调用日志闭环回归

- [x] 5.1 为 `src/features/credential/CredentialWorkspace.vue` 补齐列表筛选、详情加载、创建成功后仅一次展示明文 key、启用/停用/吊销状态切换、错误反馈测试。
- [x] 5.2 为 API Key 相关 API 映射与状态转换补齐单元测试，确保页面不会发明契约外字段或错误语义。
- [x] 5.3 为 `src/features/unified-access/UnifiedAccessPlayground.vue` 及其编排层补齐接口选择、请求参数编辑、成功透传、平台前置失败、无效 API Key 与 `traceId` 展示测试。
- [x] 5.4 为 `src/features/api-call-log/ApiCallLogWorkspace.vue` 补齐列表加载、筛选刷新、详情查看、空态、失败态以及错误码或 `traceId` 展示测试。
- [x] 5.5 补测控制台会话失效、Unified Access API Key 失效、调用日志查询失败三类边界，确认不会把控制台登录失效误判成 API Key 业务失败，反之亦然。
- [x] 5.6 在真实后端环境串行验证“创建 API Key -> 使用 API Key 调用 Unified Access -> 在 API 调用日志中检索并查看详情”的闭环，并记录关键步骤、耗时与可定位证据。
- [x] 5.6.1 通过真实后端接口创建测试 API Key，记录一次性返回的明文 key、`credentialId`、`credentialCode` 与脱敏展示字段。
- [x] 5.6.2 使用无效 `X-Aether-Api-Key` 调用 Unified Access，确认 `INVALID_CREDENTIAL` 路径的状态码、错误码与返回结构稳定可复现。
- [x] 5.6.3 使用有效 API Key 调用 `unknown-api`，确认当前后端是否返回契约中的 `TARGET_NOT_FOUND`，还是进入未包装的 `500` 异常路径。
- [x] 5.6.4 查询 `GET /current-user/api-call-logs` 与 `targetApiCode` 过滤结果，确认失败调用是否写入日志，以及当前是否能获取详情 `logId`。
- [x] 5.6.5 尝试通过 `PATCH /current-user/api-keys/{id}/disable` 与 `PATCH /revoke` 清理测试凭证，记录真实清理阻塞与残留项。
- [x] 5.6.6 在资产修订与启用能力恢复后，补做“有效 API Key -> 已创建资产 -> 成功 Unified Access 调用 -> 日志详情”完整闭环。
- [x] 5.7 验证调用日志中与 API Key、目标接口、调用结果相关的核心信息能支撑联调排障，但不会暴露契约未定义的敏感内容。
- [x] 5.8 清理冒烟过程中创建的测试 API Key 或临时数据，并把无法自动清理的残留项列入交付说明。

## 6. 回归门槛、结果沉淀与交付

- [x] 6.1 运行 `pnpm test`，确认新增自动化回归在 `aether-console` 下稳定通过。
- [x] 6.2 运行 `pnpm lint`、`pnpm type-check`、`pnpm build`，确认测试补强没有引入工程级回归。
- [x] 6.3 汇总自动化结果、真实后端冒烟结果、未覆盖缺口、环境依赖、已知风险与待补事项，形成一次可复用的联调测试记录。
- [x] 6.4 将“需要固定测试账号”“需要预置 API 资产”“需要真实上游配合生成日志”等外部依赖写入交付说明，避免后续团队重复踩环境坑。
- [x] 6.5 如果实现期发现现有 API 契约、统一前端规范或 `aether-console/DESIGN.md` 无法支撑测试落地，优先补齐权威文档，再继续 apply 阶段的代码与测试实现。
