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

### 4.4 后端修复后复测记录（2026-04-23）

#### 4.4.1 执行环境

| 字段 | 值 |
| --- | --- |
| 后端基地址 | `http://localhost:8089/api/v1` |
| 后端进程 | Java 17，来自当前仓库 `aether-api-hub` 的 `target/classes` |
| 控制台账号 | `console@aetherapi.local` |
| 测试时间 | `2026-04-23 17:40-17:42` |
| 复测目的 | 验证 B1-B5 是否已由后端修复解除 |

补充说明：使用 Java 17 直接调用 Maven launcher 跑后端 service/adapter 相关测试通过；普通 `mvn` 当前被本机 `mvn.cmd` 固定到 JDK 11，不能作为本项目有效验证入口。

#### 4.4.2 后端自动化补充验证

- 命令入口：Java 17 直接运行 Maven launcher，目标模块 `aether-api-hub-service` 与 `aether-api-hub-adapter`
- 结果：通过
- 覆盖摘要：
  - domain：`47` 个测试点通过
  - service：`51` 个测试点通过
  - adapter：`24` 个测试点通过

#### 4.4.3 真实接口复测摘要

| 场景 | 请求 | 结果 | 结论 |
| --- | --- | --- | --- |
| 登录与会话恢复 | `POST /console/auth/sign-in`、`GET /console/auth/current-session` | `200`，返回控制台用户 | 通过 |
| 分类创建 | `POST /categories` | `200`，生成 `opsx-cat-fix2-20260423174133` | 通过 |
| 资产草稿注册 | `POST /assets` | `200`，生成 `opsx-asset-fix2-20260423174133`，状态 `DRAFT` | 通过 |
| 资产修订 | `PUT /assets/opsx-asset-fix2-20260423174133` | `500 Internal Server Error` | B1 仍阻塞 |
| 资产启用 | `PATCH /assets/opsx-asset-fix2-20260423174133/enable` | `400 ASSET_ACTIVATION_INCOMPLETE` | 因 B1 未解除无法启用 |
| 市场发现 | `GET /discovery/assets?page=1&pageSize=20` | `{"items":[]}` | B5 仍阻塞 |
| API Key 创建 | `POST /current-user/api-keys` | `200`，生成 `credentialId=1e4b38fb-9bf9-43ab-8fd6-298cd0d94602` | 通过 |
| 无效 Key 调 Unified Access | `GET /access/unknown-api` | `401 API_CREDENTIAL_NOT_FOUND`，`failureType=INVALID_CREDENTIAL` | 通过 |
| 有效 Key 调未知接口 | `GET /access/unknown-api` | `500 Internal Server Error` | B2 仍阻塞 |
| 有效 Key 调新建资产 | `GET /access/opsx-asset-fix2-20260423174133` | `500 Internal Server Error` | 受 B1/B2 叠加影响 |
| 调用日志查询 | `GET /current-user/api-call-logs` 及目标接口过滤 | `items=[]` | B4 仍阻塞 |
| API Key 停用/吊销 | `PATCH /current-user/api-keys/{id}/disable`、`/revoke` | 均为 `500 Internal Server Error` | B3 仍阻塞 |
| 资产停用 | `PATCH /assets/opsx-asset-fix2-20260423174133/disable` | `500 Internal Server Error` | 资产生命周期仍不完整 |

#### 4.4.4 关键响应证据

资产修订仍返回默认 `500`：

```json
{
  "timestamp": "2026-04-23T09:41:34.498+00:00",
  "status": 500,
  "error": "Internal Server Error",
  "path": "/api/v1/assets/opsx-asset-fix2-20260423174133"
}
```

有效 API Key 调未知接口仍返回默认 `500`：

```json
{
  "timestamp": "2026-04-23T09:41:36.536+00:00",
  "status": 500,
  "error": "Internal Server Error",
  "path": "/api/v1/access/unknown-api"
}
```

API Key 停用/吊销仍返回默认 `500`：

```json
{
  "timestamp": "2026-04-23T09:41:38.804+00:00",
  "status": 500,
  "error": "Internal Server Error",
  "path": "/api/v1/current-user/api-keys/1e4b38fb-9bf9-43ab-8fd6-298cd0d94602/disable"
}
```

```json
{
  "timestamp": "2026-04-23T09:41:39.008+00:00",
  "status": 500,
  "error": "Internal Server Error",
  "path": "/api/v1/current-user/api-keys/1e4b38fb-9bf9-43ab-8fd6-298cd0d94602/revoke"
}
```

日志查询仍为空：

```json
{
  "items": [],
  "page": 1,
  "size": 20,
  "total": 0
}
```

#### 4.4.5 当前判断

- 后端代码层的 service/adapter 测试是通过的，但 `8089` 真实运行实例仍复现 B1-B5。
- 如果 `8089` 是在最新 class 编译前启动的，需要重启后端后再复测同一脚本。
- 如果重启后仍复现，则这些阻塞并未被真实接口层或真实数据库环境解除，不能继续把完整闭环标记为通过。

### 4.5 后端重启后闭环复测记录（2026-04-23）

#### 4.5.1 执行环境

| 字段 | 值 |
| --- | --- |
| 后端基地址 | `http://localhost:8089/api/v1` |
| 后端状态 | 已重启并加载 MyBatis-Plus 乐观锁插件配置 |
| 测试时间 | `2026-04-23 17:52` |
| 根因修复 | 补充 `OptimisticLockerInnerInterceptor`，解决 `MP_OPTLOCK_VERSION_ORIGINAL` 参数缺失 |

#### 4.5.2 复测摘要

| 场景 | 请求 | 结果 | 结论 |
| --- | --- | --- | --- |
| 登录与会话恢复 | `POST /console/auth/sign-in`、`GET /console/auth/current-session` | `200` | 通过 |
| 分类创建 | `POST /categories` | `200`，生成 `opsx-cat-lockfix-20260423175229` | 通过 |
| 资产草稿注册 | `POST /assets` | `200`，生成 `opsx-asset-lockfix-20260423175229` | 通过 |
| 资产修订 | `PUT /assets/opsx-asset-lockfix-20260423175229` | `200`，补齐分类和上游配置 | B1 解除 |
| 资产启用 | `PATCH /assets/opsx-asset-lockfix-20260423175229/enable` | `200`，状态 `ENABLED` | 通过 |
| 市场发现 | `GET /discovery/assets?page=1&pageSize=20` | 返回新启用资产 | B5 解除 |
| API Key 创建 | `POST /current-user/api-keys` | `200`，生成 `credentialId=a6701622-c8a6-4481-a4a2-894b6b602207` | 通过 |
| 无效 Key 调 Unified Access | `GET /access/unknown-api` | `401 API_CREDENTIAL_NOT_FOUND`，`failureType=INVALID_CREDENTIAL` | 通过 |
| 有效 Key 调未知接口 | `GET /access/unknown-api` | `404 ASSET_NOT_FOUND`，`failureType=TARGET_NOT_FOUND` | B2 解除 |
| 有效 Key 调新建资产 | `GET /access/opsx-asset-lockfix-20260423175229` | `200`，成功透传 discovery 响应 | 通过 |
| 调用日志查询 | `GET /current-user/api-call-logs` 及目标接口过滤 | 成功与失败调用均可查到日志 | B4 解除 |
| API Key 停用/吊销 | `PATCH /disable`、`PATCH /revoke` | 均 `200`，最终状态 `REVOKED` | B3 解除 |
| 资产停用 | `PATCH /assets/opsx-asset-lockfix-20260423175229/disable` | `200`，状态 `DISABLED` | 清理通过 |

#### 4.5.3 关键响应证据

资产修订成功：

```json
{
  "apiCode": "opsx-asset-lockfix-20260423175229",
  "categoryCode": "opsx-cat-lockfix-20260423175229",
  "status": "DRAFT",
  "requestMethod": "GET",
  "authScheme": "NONE"
}
```

资产启用后进入 discovery：

```json
{
  "items": [
    {
      "apiCode": "opsx-asset-lockfix-20260423175229",
      "assetName": "OpenSpec lockfix asset 20260423175229",
      "assetType": "STANDARD_API"
    }
  ]
}
```

有效 API Key 调未知接口返回契约失败分类：

```json
{
  "code": "ASSET_NOT_FOUND",
  "message": "Asset not found: unknown-api",
  "failureType": "TARGET_NOT_FOUND",
  "traceId": null,
  "apiCode": "unknown-api"
}
```

调用日志可查询到成功与失败两类记录：

```json
{
  "items": [
    {
      "targetApiCode": "opsx-asset-lockfix-20260423175229",
      "resultType": "SUCCESS",
      "success": true,
      "httpStatusCode": 200
    },
    {
      "targetApiCode": "unknown-api",
      "resultType": "TARGET_NOT_FOUND",
      "success": false,
      "httpStatusCode": 404
    }
  ],
  "total": 2
}
```

API Key 已清理为 `REVOKED`，资产已清理为 `DISABLED`：

```json
{
  "credentialId": "a6701622-c8a6-4481-a4a2-894b6b602207",
  "status": "REVOKED",
  "assetCode": "opsx-asset-lockfix-20260423175229",
  "assetStatus": "DISABLED"
}
```

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
- `2026-04-23` 复测新增残留：
  - 分类：`opsx-cat-fix-20260423174054`、`opsx-cat-fix2-20260423174133`
  - 资产草稿：`opsx-asset-fix-20260423174054`、`opsx-asset-fix2-20260423174133`
  - API Key：`1e4b38fb-9bf9-43ab-8fd6-298cd0d94602`
- `2026-04-23` 重启后闭环复测新增数据：
  - 分类：`opsx-cat-lockfix-20260423175229`
  - 资产：`opsx-asset-lockfix-20260423175229`，已通过公开接口停用
  - API Key：`a6701622-c8a6-4481-a4a2-894b6b602207`，已通过公开接口吊销
- 若后续发现契约或设计文档不足以支撑测试落地，需先补齐顶层文档
## 7. 2026-04-23 前端自动化补测记录

### 7.1 本轮补充范围

- 登录页编排：`src/composables/useSignInForm.ts` / `useSignInForm.spec.ts`
- API 市场编排：`src/composables/useCatalogDiscovery.ts` / `useCatalogDiscovery.spec.ts`
- 工作台编排：`src/composables/useWorkspaceCatalog.ts` / `useWorkspaceCatalog.spec.ts`，并清理 `src/pages/workspace.vue` 中已迁移的旧脚本中间态
- API Key 编排：`src/composables/useCredentialWorkspace.ts` / `useCredentialWorkspace.spec.ts`
- Unified Access 编排：`src/composables/useUnifiedAccessPlayground.spec.ts`
- API 调用日志编排：`src/composables/useApiCallLogWorkspace.ts` / `useApiCallLogWorkspace.spec.ts`

### 7.2 自动化结果

```text
pnpm test
Test Files: 18 passed
Tests: 61 passed
```

```text
pnpm type-check
vue-tsc --noEmit -p tsconfig.app.json
Result: passed
```

```text
pnpm lint
Result: passed with warnings only
Known warnings: src/pages/index.vue existing vue/html-indent warnings, no lint errors.
```

```text
pnpm build
Result: passed
```

### 7.3 覆盖结论

- `3.6` 已由 `useSignInForm.spec.ts` 与既有 `console-shell.spec.ts` 覆盖登录中、登录失败、恢复中和退出反馈的最小页面编排语义。
- `4.1` 已由 `useCatalogDiscovery.spec.ts` 覆盖市场列表加载、详情加载、空态/失败态入口和最近访问记录更新。
- `4.2`、`4.3`、`4.4` 已由 `useWorkspaceCatalog.spec.ts` 覆盖分类列表/创建/重命名/启停、资产查询/注册/状态变更/AI 标签绑定、工作台局部加载与错误反馈。
- `5.1` 已由 `useCredentialWorkspace.spec.ts` 覆盖 API Key 列表筛选、详情回退、创建后一次性明文 key、复制、停用/吊销状态切换和创建失败反馈。
- `5.3` 已由 `useUnifiedAccessPlayground.spec.ts` 覆盖接口选择、请求参数编辑、成功透传、平台前置失败、无效 JSON 边界和表单清理。
- `5.4` 已由 `useApiCallLogWorkspace.spec.ts` 覆盖调用日志列表加载、筛选刷新、详情查看、空/错态、日期范围校验、错误码与 trace 相关摘要展示数据。
- `5.5` 已由 `src/api/http.spec.ts`、`useUnifiedAccessPlayground.spec.ts`、`useApiCallLogWorkspace.spec.ts` 共同覆盖控制台会话失效、Unified Access API Key 业务失败、调用日志查询失败三类边界，确认不会把控制台登录失效和 API Key 业务失败混淆。

### 7.4 剩余缺口

- `3.7` 仍需浏览器级真实后端手工验证：登录成功、刷新恢复、直接访问受保护页面、会话失效后回退登录页。当前仅有接口级登录/会话恢复和自动化守卫测试证据，尚未记录真实浏览器操作证据。

## 8. 2026-04-23 浏览器级认证主链路复测记录

### 8.1 执行环境

| 字段 | 值 |
| --- | --- |
| 前端入口 | `http://127.0.0.1:5173` 临时静态服务 |
| 前端构建 | `VITE_API_BASE_URL=/api pnpm build` |
| API 代理 | 临时同源代理 `/api/*` -> `http://localhost:8089/api/*` |
| 浏览器 | Microsoft Edge headless via Chrome DevTools Protocol |
| 后端基地址 | `http://localhost:8089/api/v1` |
| 测试账号 | `console@aetherapi.local` |

### 8.2 认证主链路结果

```json
{
  "baseUrl": "http://127.0.0.1:5173",
  "results": [
    {
      "name": "unauthenticated direct protected route redirects to sign-in",
      "status": "passed",
      "url": "http://127.0.0.1:5173/sign-in?redirectName=console-workspace"
    },
    {
      "name": "sign-in succeeds and opens protected workspace",
      "status": "passed",
      "url": "http://127.0.0.1:5173/workspace",
      "sessionPersisted": true
    },
    {
      "name": "refresh restores session on protected route",
      "status": "passed",
      "url": "http://127.0.0.1:5173/workspace"
    },
    {
      "name": "expired session returns to sign-in on protected route",
      "status": "passed",
      "url": "http://127.0.0.1:5173/sign-in?redirectName=console-workspace"
    }
  ],
  "passed": true
}
```

### 8.3 结论

- `3.7` 的 4 条真实后端认证主链路均通过。
- 本轮浏览器级复测确认当前控制台登录、会话持久化、刷新恢复和过期 token 回退登录页均命中 `8089` 真实后端，而非前端 mock。
