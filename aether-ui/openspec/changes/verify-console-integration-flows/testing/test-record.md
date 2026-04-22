# 控制台联调测试记录

## 1. 记录说明

本文件用于沉淀本次 change 的测试过程。要求尽量可读，尤其是以下信息：

- 入口页面或入口模块
- 请求入参
- 响应摘要
- 错误码或 `traceId`
- 结论与后续动作

## 2. 环境信息

| 字段 | 值 |
| --- | --- |
| Change | `verify-console-integration-flows` |
| 前端目录 | `aether-ui/aether-console` |
| 前端分支/提交 | 本地工作区，未单独提交 |
| 后端环境 | 本地运行中的 `aether-api-hub` |
| 接口基地址 | `http://localhost:8080/api/v1` |
| Mock 开关状态 | 本轮真实接口验证直接调用后端，不经过前端 mock adapter |
| 测试账号 | `console@aetherapi.local` |
| 测试时间 | `2026-04-22` |
| 记录人 | Codex |

## 3. 自动化回归记录

### 3.1 测试基础与夹具

- 已建立共享测试辅助：`src/test/console-test-kit.ts`
- 当前自动化主入口：`Vitest`
- 当前约定：自动化回归先通过，再进入真实后端冒烟

### 3.2 已实现用例记录

#### A. 控制台认证 API 映射

- 测试文件：`src/api/console-auth/console-auth.api.spec.ts`
- 覆盖点：
  - 登录成功映射
  - 参数错误透传
  - 凭证错误透传
  - 当前会话查询成功映射
  - 当前会话 401 透传

示例请求入参：

```json
{
  "loginName": "console@aetherapi.local",
  "password": "change-me-console-password"
}
```

示例响应摘要：

```json
{
  "accessToken": "console-access-token",
  "expiresAt": "2026-04-22T12:00:00Z",
  "currentUser": {
    "userId": "console-user-001",
    "loginName": "console@aetherapi.local",
    "displayName": "Aether Console Operator",
    "email": "console@aetherapi.local",
    "role": "OWNER"
  }
}
```

结论：已通过

#### B. 控制台会话编排

- 测试文件：`src/composables/useConsoleAuth.spec.ts`
- 覆盖点：
  - 登录成功写入会话
  - 无 token 恢复直接完成初始化
  - 有 token 恢复成功
  - 401 自动清会话
  - 5xx 不误登出

结论：已通过

#### C. 认证状态存储

- 测试文件：`src/stores/useAuthStore.spec.ts`
- 覆盖点：
  - 会话持久化
  - 刷新恢复读取
  - 清理会话
  - 初始化标记

结论：已通过

#### D. 路由守卫与 HTTP 拦截

- 测试文件：
  - `src/app/route-guards.spec.ts`
  - `src/api/http.spec.ts`
- 覆盖点：
  - 受保护路由首次进入触发恢复
  - 未登录跳转登录页并保留 `redirectName`
  - `Authorization` 注入
  - `CONSOLE_SESSION_UNAUTHORIZED` 清会话并回跳
  - Unified Access 非会话 401 不误登出

结论：已通过

#### E. 市场发现、API Key 与 Unified Access 解析

- 测试文件：
  - `src/api/catalog/discovery.api.spec.ts`
  - `src/api/credential/credential.api.spec.ts`
  - `src/api/unified-access/unified-access.api.spec.ts`
- 覆盖点：
  - 市场列表与详情映射
  - API Key 列表、详情、创建、启停用/吊销映射
  - Unified Access 成功 JSON、平台失败、文本响应分类

结论：已通过

### 3.3 自动化回归汇总

- `pnpm test`：通过，`12` 个测试文件、`37` 个测试点全部通过
- `pnpm type-check`：通过
- `pnpm build`：通过
- `pnpm lint`：通过，但存在仓库中既有的 `src/pages/index.vue` 缩进 warning，本轮未扩大该问题范围

## 4. 真实后端冒烟记录

### 4.1 记录模板

每条真实冒烟记录至少补充以下字段：

| 字段 | 内容 |
| --- | --- |
| 场景名称 | 例如：登录并刷新恢复 |
| 入口 | 页面路由或功能入口 |
| 前置数据 | 使用的账号、API 资产、API Key、时间窗口 |
| 请求入参 | 表单、query、body、header 摘要 |
| 响应摘要 | 状态码、关键字段、结果说明 |
| 错误码/traceId | 若失败则必填 |
| 结论 | 通过 / 失败 / 阻塞 |
| 备注 | 清理动作、截图、日志地址 |

### 4.2 首批计划场景

#### 场景 1：登录并进入受保护页面

- 入口：`/sign-in`
- 请求入参：

```json
{
  "loginName": "console@aetherapi.local",
  "password": "change-me-console-password"
}
```

- 响应摘要：

```json
{
  "accessToken": "Y3MxfG...<省略>...wy5g",
  "tokenType": "Bearer",
  "expiresAt": "2026-04-23T02:49:49.926120300Z",
  "expiresInSeconds": 43200,
  "currentUser": {
    "userId": "console-operator",
    "loginName": "console@aetherapi.local",
    "displayName": "Aether Console Operator",
    "email": "console@aetherapi.local",
    "role": "OWNER"
  }
}
```

- 错误码/traceId：无
- 结论：通过

#### 场景 2：刷新页面后恢复控制台会话

- 入口：受保护页面刷新
- 请求入参：

```http
GET /console/auth/current-session
Authorization: Bearer <登录返回的 accessToken>
X-App-Id: console
```

- 响应摘要：

```json
{
  "currentUser": {
    "userId": "console-operator",
    "loginName": "console@aetherapi.local",
    "displayName": "Aether Console Operator",
    "email": "console@aetherapi.local",
    "role": "OWNER"
  }
}
```

- 错误码/traceId：
  - 未带 token 时返回 `401 CONSOLE_SESSION_UNAUTHORIZED`
- 结论：接口层通过；前端页面级“刷新后恢复”仍待浏览器侧冒烟

#### 场景 3：创建 API Key 并调用 Unified Access

- 入口：`workspace` -> `credentials` / `playground`
- 请求入参：

```json
{
  "credentialName": "opsx-20260422225055",
  "credentialDescription": "OpenSpec integration verification"
}
```

- 响应摘要：
  - 创建后可在列表中读回：

```json
{
  "credentialId": "0f606e46-74fb-4bc6-88ea-85b18684fdd3",
  "credentialCode": "cred_1946e006da9343af9cf8700965b8d10e",
  "credentialName": "opsx-20260422225055",
  "status": "ENABLED",
  "maskedKey": "ak_live_****120c"
}
```

- 额外真实结果：
  - 使用假的 `X-Aether-Api-Key` 调 `GET /access/unknown-api` 时，真实后端返回：

```json
{
  "code": "API_CREDENTIAL_NOT_FOUND",
  "message": "API credential was not found",
  "failureType": "INVALID_CREDENTIAL",
  "traceId": null,
  "apiCode": "unknown-api"
}
```

  - 对新建 API Key 执行 `PATCH /current-user/api-keys/{id}/disable` 与 `PATCH /revoke` 时，真实后端返回 `500 Internal Server Error`
  - 一次基于新建有效 key 的 `GET /access/unknown-api` 触发了 `500`，说明 Unified Access 在“有效 key + 不存在 apiCode”路径上存在后端异常，需后续单独定位
- 错误码/traceId：
  - `INVALID_CREDENTIAL` 可稳定复现
  - `disable/revoke` 当前仅返回 Spring 默认 `500` 包装，未给出业务错误码
- 结论：部分通过，存在后端阻塞

#### 场景 4：从调用日志查看对应调用详情

- 入口：`workspace` -> `api-call-logs`
- 请求入参：

```http
GET /current-user/api-call-logs?page=1&size=20
Authorization: Bearer <accessToken>
X-App-Id: console
```

- 响应摘要：

```json
{
  "items": [],
  "page": 1,
  "size": 20,
  "total": 0
}
```

- 错误码/traceId：无
- 结论：当前环境无可用调用日志样本，无法继续验证详情闭环

#### 场景 5：通过真实接口创建测试分类

- 入口：`workspace` -> 分类管理（后端直调验证）
- 前置数据：
  - 已登录控制台账号 `console@aetherapi.local`
  - 本轮分类编码：`opsx-cat-20260422231328`
- 请求入参：

```http
POST /categories
Authorization: Bearer <accessToken>
X-App-Id: console
Content-Type: application/json
```

```json
{
  "categoryCode": "opsx-cat-20260422231328",
  "categoryName": "OpenSpec 测试分类 20260422231328"
}
```

- 响应摘要：

```json
{
  "id": "e27f49cc-011f-4299-8f6f-d49ba35659c7",
  "categoryCode": "opsx-cat-20260422231328",
  "categoryName": "OpenSpec 测试分类 20260422231328",
  "status": "ENABLED",
  "createdAt": "2026-04-22T15:13:28.251749500Z",
  "updatedAt": "2026-04-22T15:13:28.251749500Z"
}
```

- 补充校验：

```http
GET /categories/opsx-cat-20260422231328
Authorization: Bearer <accessToken>
X-App-Id: console
```

```json
{
  "id": "e27f49cc-011f-4299-8f6f-d49ba35659c7",
  "categoryCode": "opsx-cat-20260422231328",
  "categoryName": "OpenSpec 测试分类 20260422231328",
  "status": "ENABLED"
}
```

- 错误码/traceId：无
- 结论：通过，说明分类创建链路可作为后续资产造数前置条件

#### 场景 6：注册资产草稿并尝试补齐为可调用资产

- 入口：`workspace` -> 资产管理（后端直调验证）
- 前置数据：
  - 已创建分类：`opsx-cat-20260422231328`
  - 本轮资产编码：`opsx-proxy-20260422231328`
- 请求入参：

```http
POST /assets
Authorization: Bearer <accessToken>
X-App-Id: console
Content-Type: application/json
```

```json
{
  "apiCode": "opsx-proxy-20260422231328",
  "assetType": "STANDARD_API",
  "assetName": "OpenSpec 代理资产 20260422231328"
}
```

- 注册响应摘要：

```json
{
  "id": "c0178441-92be-4ea1-af8b-241b5a031c9d",
  "apiCode": "opsx-proxy-20260422231328",
  "assetName": "OpenSpec 代理资产 20260422231328",
  "assetType": "STANDARD_API",
  "categoryCode": null,
  "status": "DRAFT"
}
```

- 真实修订尝试：
  - 尝试 A：`PUT /assets/opsx-proxy-20260422231328`，仅传 `categoryCode`
  - 尝试 B：`PUT /assets/opsx-asset-revise-20260422231418`，仅传 `requestMethod/upstreamUrl/authScheme`
  - 尝试 C：`PUT /assets/opsx-asset-revise-20260422231418`，同时传 `assetName/categoryCode/requestMethod/upstreamUrl/authScheme`

- 尝试 A 请求体示例：

```json
{
  "assetName": "OpenSpec 代理资产 20260422231328",
  "assetType": "STANDARD_API",
  "categoryCode": "opsx-cat-20260422231328",
  "requestMethod": "GET",
  "upstreamUrl": "http://localhost:8080/api/v1/discovery/assets?page=1&pageSize=1",
  "authScheme": "NONE",
  "requestTemplate": "{\"query\":\"透传查询参数\"}",
  "requestExample": "{\"demo\":true}",
  "responseExample": "{\"items\":[]}"
}
```

- 响应摘要：

```json
{
  "timestamp": "2026-04-22T15:13:28.921+00:00",
  "status": 500,
  "error": "Internal Server Error",
  "path": "/api/v1/assets/opsx-proxy-20260422231328"
}
```

- 额外真实结果：
  - 对另一条测试资产 `opsx-asset-revise-20260422231418` 分别执行“仅改分类”“仅补上游配置”“组合补齐字段”三种修订，均返回相同形态的 Spring 默认 `500`
  - 在修订未生效的情况下执行 `PATCH /assets/opsx-proxy-20260422231328/enable`，返回：

```json
{
  "code": "ASSET_ACTIVATION_INCOMPLETE",
  "message": "Category code must be provided before enabling"
}
```

  - 执行 `PATCH /assets/opsx-proxy-20260422231328/disable` 也返回 Spring 默认 `500`
- 错误码/traceId：
  - `PUT /assets/{apiCode}` 当前未返回业务错误码，仅返回默认 `500`
  - `PATCH /assets/{apiCode}/enable` 可稳定返回 `ASSET_ACTIVATION_INCOMPLETE`
- 结论：阻塞。当前后端无法通过公开资产管理接口把草稿资产修订成可启用资产，因此“创建资产 -> 启用 -> 出现在 discovery -> 用 API Key 调用”主链路被卡在资产修订阶段

#### 场景 7：创建 API Key、用有效 key 调用未知接口并回查日志

- 入口：`workspace` -> `credentials` / `playground` / `api-call-logs`
- 前置数据：
  - 本轮 API Key 名称：`opsx-flow-20260422231513`
  - 本轮 `credentialId`：`9f4e950a-e475-45cd-95e8-84b9b7e8b9c4`
- 请求入参：

```http
POST /current-user/api-keys
Authorization: Bearer <accessToken>
X-App-Id: console
Content-Type: application/json
```

```json
{
  "credentialName": "opsx-flow-20260422231513",
  "credentialDescription": "flow debug"
}
```

- 创建响应摘要：

```json
{
  "credentialId": "9f4e950a-e475-45cd-95e8-84b9b7e8b9c4",
  "credentialCode": "cred_b926b95d908d4d238f74a8a2b003f707",
  "credentialName": "opsx-flow-20260422231513",
  "status": "ENABLED",
  "maskedKey": "ak_live_****5751",
  "plaintextKey": "ak_live_f98d7345bbd04dccbc8744abf4b9c622e298f4ef5751"
}
```

- Unified Access 调用：

```http
GET /access/unknown-api
X-Aether-Api-Key: ak_live_f98d7345bbd04dccbc8744abf4b9c622e298f4ef5751
Accept: application/json
```

- 响应摘要：

```json
{
  "timestamp": "2026-04-22T15:15:13.266+00:00",
  "status": 500,
  "error": "Internal Server Error",
  "path": "/api/v1/access/unknown-api"
}
```

- 日志回查：

```http
GET /current-user/api-call-logs?page=1&size=20
GET /current-user/api-call-logs?targetApiCode=unknown-api&page=1&size=20
Authorization: Bearer <accessToken>
X-App-Id: console
```

```json
{
  "items": [],
  "page": 1,
  "size": 20,
  "total": 0
}
```

- 清理尝试：
  - `PATCH /current-user/api-keys/9f4e950a-e475-45cd-95e8-84b9b7e8b9c4/disable` -> `500`
  - `PATCH /current-user/api-keys/9f4e950a-e475-45cd-95e8-84b9b7e8b9c4/revoke` -> `500`
- 错误码/traceId：
  - 有效 key 调未知接口当前未返回契约期望的 `TARGET_NOT_FOUND`，而是默认 `500`
  - 两次日志查询都未返回 `logId`，因此无法继续详情查询
  - `disable/revoke` 仍未返回业务错误码，仅返回默认 `500`
- 结论：部分通过。API Key 创建本身可用，但 Unified Access 与日志写入闭环仍被后端异常阻塞，且 API Key 无法清理

### 4.3 其他真实接口观察

- 初始环境下，`GET /discovery/assets?page=1&pageSize=5` 返回空数组，说明当前环境没有可直接消费的市场资产
- 初始环境下，`GET /categories?page=1&pageSize=5` 返回空数组，说明当前环境也没有预置分类
- 真实测试已证明：分类创建可用，但资产修订接口当前稳定 `500`，因此无法通过公开 API 自造“可启用且可发现”的测试资产
- 使用有效 API Key 调 `unknown-api` 目前也会落入默认 `500`，且未观察到当前用户维度的日志落库
- 因为资产修订与 Unified Access 两处阻塞并存，`Unified Access 成功调用 -> 调用日志详情` 的完整闭环暂时无法在当前后端版本完成

## 5. 已知外部依赖

- 需要固定控制台测试账号
- 需要至少一个已发布 API 资产，或先修复 `PUT /assets/{apiCode}` 以便测试阶段自行创建并启用资产
- 需要可回收测试 API Key
- 需要真实上游能生成调用日志，且 Unified Access 在有效 API Key 调用路径上不再返回默认 `500`
- 需要在真实环境中拿到稳定错误码或 `traceId`

## 6. 未覆盖与阻塞项

- 独立阻塞清单已整理到 `testing/blocker-list.md`，后续可直接按编号跟踪，不需要从本记录中重新归纳
- 登录页与受保护页面的浏览器级交互冒烟仍待执行
- 当前真实环境虽然可以创建分类，但 `PUT /assets/{apiCode}` 任意修订请求均稳定返回默认 `500`，导致资产管理闭环与 discovery 造数被阻塞
- 新建测试 API Key 后，`disable` / `revoke` 接口当前返回 `500`，存在清理阻塞
- Unified Access 在“有效 API Key + 不存在 apiCode”场景下稳定返回默认 `500`，而不是契约中的 `TARGET_NOT_FOUND`
- 调用日志列表查询接口本身可用，但在上述失败调用后未观察到日志落库，因此详情页联调仍无样本
- 当前残留测试数据：
  - 分类：`opsx-cat-debug`、`opsx-cat-20260422231328`、`opsx-cat-revise-20260422231418`
  - 资产草稿：`opsx-proxy-20260422231328`、`opsx-asset-revise-20260422231418`
  - API Key：`0f606e46-74fb-4bc6-88ea-85b18684fdd3`、`88bf85e2-002a-4cf5-a03d-3f8c1ccb5a70`、`9f4e950a-e475-45cd-95e8-84b9b7e8b9c4`
- 若后续发现契约或设计文档不足以支撑测试落地，需先补齐顶层文档
