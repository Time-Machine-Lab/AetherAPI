# 控制台联调回归矩阵

## 1. 范围来源

本矩阵仅覆盖 `aether-console`，范围来自以下现有权威文档与已存在代码入口：

- `docs/spec/AetherAPI 前端技术栈与开发规范文档.md`
- `aether-console/DESIGN.md`
- `docs/api/console-auth.yaml`
- `docs/api/api-catalog-discovery.yaml`
- `docs/api/api-category-lifecycle.yaml`
- `docs/api/api-asset-management.yaml`
- `docs/api/api-credential.yaml`
- `docs/api/unified-access.yaml`
- `docs/api/api-call-log.yaml`

当前不包含 `aether-admin-console`、`aether-web-marketing`，也不扩展未在上述权威文档中定义的接口或行为。

## 2. 执行顺序

联调验证按以下顺序执行，不跳步：

1. 先通过自动化回归：`pnpm test`
2. 再通过工程门槛：`pnpm lint`、`pnpm type-check`、`pnpm build`
3. 最后执行真实后端冒烟

若自动化回归未通过，真实后端冒烟结果不能作为最终验收结论。

## 3. 环境边界

### 3.1 当前实现里的真实风险点

- `src/api/http.ts` 仍保留 mock adapter 分支，真实联调前必须确认未命中本地 mock 数据。
- `env.apiBaseUrl` 默认值为 `/api/v1`，真实联调必须补充实际接口基地址与代理链路说明。
- 统一接入 `X-Aether-Api-Key` 与控制台会话 `Bearer token` 是两条独立认证链路，测试记录必须分开描述。

### 3.2 真实联调前置清单

- 固定控制台测试账号
- 至少一个可浏览 API 资产
- 至少一个可管理分类或资产
- 可回收测试 API Key
- 可生成调用日志的真实上游
- 失败证据留存位置

## 4. 回归矩阵

| 业务域 | 入口 | 成功态 | 失败态/边界 | 自动化 | 真实冒烟 | 环境依赖 | 验收结论 |
| --- | --- | --- | --- | --- | --- | --- | --- |
| 控制台认证 | `sign-in.vue` / `route-guards.ts` | 登录成功、受保护路由可进入、刷新后恢复、退出后回退登录页 | 参数错误、凭证错误、401 会话失效、5xx 恢复失败但不误登出 | 是 | 是 | 测试账号、真实会话接口 | 接口层已通过，页面级冒烟待补 |
| API 市场浏览 | `index.vue` | 列表可加载、详情可查看、最近访问可更新 | 空态、接口失败、契约字段缺失 | 是 | 是 | 已发布 API 资产 | 当前真实环境仍无可发现资产，阻塞 |
| 分类管理 | `workspace.vue` | 列表、创建、重命名、启用/停用闭环可完成 | 参数错误、状态冲突、无权限或数据缺失 | 是 | 是 | 可操作分类数据 | 真实接口创建已通过，页面级链路待补 |
| 资产管理 | `workspace.vue` | 查询、注册、详情、状态变更或 AI 绑定闭环可完成 | 参数错误、状态冲突、接口失败 | 是 | 是 | 可操作资产数据 | 注册草稿通过，`PUT /assets/{apiCode}` 稳定 `500`，主链路阻塞 |
| API Key 生命周期 | `CredentialWorkspace.vue` | 列表、详情、创建、启用、停用、吊销正常 | 创建失败、状态冲突、会话失效 | 是 | 是 | 可创建和回收的测试 Key | 创建通过，`disable/revoke` 稳定 `500` |
| Unified Access | `playground.vue` | 调用成功、成功响应透传、调用说明完整 | `INVALID_API_CODE`、`INVALID_CREDENTIAL`、`TARGET_NOT_FOUND`、`TARGET_UNAVAILABLE` | 是 | 是 | 可用 API Key、真实上游 | 无效 key 校验通过；有效 key 调未知接口返回默认 `500`，成功调用被阻塞 |
| API 调用日志 | `ApiCallLogWorkspace.vue` | 列表、筛选、详情可查，关键排障字段可见 | 空态、查询失败、详情失败、不可越权读取 | 是 | 是 | 日志写入链路可用 | 空态查询通过，但当前失败调用未观察到日志落库 |

## 5. 当前已确认的自动化切入点

- API 层映射测试：继续沿用 `Vitest`
- 认证编排测试：补齐 `useConsoleAuth`、`useAuthStore`、`route-guards`、`http`
- 页面状态与模块编排测试：优先覆盖不依赖新增测试平台的逻辑层与状态层

## 6. 当前待真实环境补位的链路

- 登录后真实会话建立与刷新恢复
- API Key 创建后一次性明文展示
- 通过公开资产接口把草稿资产修订并启用为可调用资产
- 使用有效 API Key 成功调用 Unified Access 并产生日志
- 从调用日志页查询并查看该次真实调用详情

## 7. 完成定义

一轮联调通过必须同时满足：

- 自动化回归通过
- 工程门槛通过
- 真实冒烟关键闭环至少完成“登录 -> 受保护页 -> API Key -> Unified Access -> 调用日志”
- 测试记录中已写明环境、步骤、请求入参、响应摘要、错误码或 `traceId`、未覆盖缺口
