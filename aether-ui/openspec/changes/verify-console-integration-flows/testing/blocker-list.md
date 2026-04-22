# 控制台联调阻塞清单

## 1. 说明

本清单只整理当前已确认的阻塞项，用于后续跟踪、分派与复测，不代表本轮要立即进入修复。

阻塞来源：

- `2026-04-22` 本地后端真实联调
- `aether-ui/openspec/changes/verify-console-integration-flows/testing/test-record.md`

## 2. 阻塞总览

| 编号 | 阻塞主题 | 当前状态 | 影响等级 | 影响链路 |
| --- | --- | --- | --- | --- |
| B1 | 资产修订接口返回默认 `500` | 待后端定位 | 高 | 资产管理 -> 资产启用 -> 市场发现 -> Unified Access 成功调用 |
| B2 | 有效 API Key 调未知接口返回默认 `500` | 待后端定位 | 高 | Unified Access 前置失败分类 -> 错误展示 -> 调用日志 |
| B3 | API Key 停用 / 吊销返回默认 `500` | 待后端定位 | 高 | API Key 生命周期 -> 测试数据清理 |
| B4 | 当前失败调用未观察到日志落库 | 待后端定位 | 中 | 调用日志列表 -> 日志详情 -> 联调排障 |
| B5 | 真实环境缺少可发现资产样本 | 环境阻塞 | 中 | 市场浏览 -> 工作台 -> Playground 闭环 |

## 3. 详细阻塞

### B1. 资产修订接口返回默认 `500`

**阻塞描述**

通过公开资产管理接口注册出草稿资产后，无法继续用 `PUT /assets/{apiCode}` 补齐分类或上游配置，导致资产始终停留在 `DRAFT`。

**复现路径**

1. 登录控制台账号 `console@aetherapi.local`
2. `POST /categories` 创建可用分类
3. `POST /assets` 注册草稿资产
4. 调用 `PUT /assets/{apiCode}` 尝试修订资产

**已验证的请求类型**

- 仅传 `categoryCode`
- 仅传 `requestMethod`、`upstreamUrl`、`authScheme`
- 同时传 `assetName`、`categoryCode`、`requestMethod`、`upstreamUrl`、`authScheme`

**契约预期**

- 请求成功时返回修订后的资产详情
- 请求失败时至少返回业务错误码，而不是 Spring 默认错误页

**实际结果**

```json
{
  "timestamp": "2026-04-22T15:13:28.921+00:00",
  "status": 500,
  "error": "Internal Server Error",
  "path": "/api/v1/assets/opsx-proxy-20260422231328"
}
```

**影响范围**

- 无法通过测试阶段自行造出“可启用且可发现”的资产
- `PATCH /assets/{apiCode}/enable` 只能得到 `ASSET_ACTIVATION_INCOMPLETE`
- 无法继续验证 discovery、playground、日志闭环

**当前替代方案**

- 无直接替代方案
- 只能依赖后端预置一条已启用资产，或先修复该接口

**后续复测入口**

- 修复后优先重测 `PUT /assets/{apiCode}`
- 再补测 `PATCH /assets/{apiCode}/enable`
- 再确认 `GET /discovery/assets` 是否出现新资产

### B2. 有效 API Key 调未知接口返回默认 `500`

**阻塞描述**

在 API Key 已成功创建且处于 `ENABLED` 状态时，调用 `GET /access/unknown-api` 没有返回契约中的 `TARGET_NOT_FOUND`，而是默认 `500`。

**复现路径**

1. `POST /current-user/api-keys` 创建测试 API Key
2. 取返回的 `plaintextKey`
3. 使用该 key 请求 `GET /access/unknown-api`

**契约预期**

应返回类似以下平台前置失败：

```json
{
  "code": "ASSET_NOT_FOUND",
  "message": "Asset not found: unknown-api",
  "failureType": "TARGET_NOT_FOUND"
}
```

**实际结果**

```json
{
  "timestamp": "2026-04-22T15:15:13.266+00:00",
  "status": 500,
  "error": "Internal Server Error",
  "path": "/api/v1/access/unknown-api"
}
```

**影响范围**

- 前端无法按契约区分 `TARGET_NOT_FOUND`
- 无法确认 Unified Access 平台前置失败分类是否正确
- 与调用日志落库问题叠加后，排障成本变高

**当前替代方案**

- 仅能继续保留“无效 key 返回 `INVALID_CREDENTIAL`”这条已通过的失败路径
- 无法用“有效 key + 不存在 apiCode”补齐契约验证

**后续复测入口**

- 修复后重测 `GET /access/unknown-api`
- 确认返回状态码、`code`、`failureType` 是否与契约一致
- 再检查是否产生对应日志

### B3. API Key 停用 / 吊销返回默认 `500`

**阻塞描述**

当前用户成功创建测试 API Key 后，执行停用和吊销都会返回默认 `500`，无法完成测试数据清理，也无法验证状态切换。

**复现路径**

1. `POST /current-user/api-keys` 创建测试 API Key
2. 调用 `PATCH /current-user/api-keys/{credentialId}/disable`
3. 调用 `PATCH /current-user/api-keys/{credentialId}/revoke`

**契约预期**

- 成功时返回更新后的凭证状态
- 状态不允许时返回明确业务错误码

**实际结果**

```json
{
  "timestamp": "2026-04-22T15:15:13.906+00:00",
  "status": 500,
  "error": "Internal Server Error",
  "path": "/api/v1/current-user/api-keys/9f4e950a-e475-45cd-95e8-84b9b7e8b9c4/disable"
}
```

```json
{
  "timestamp": "2026-04-22T15:15:14.464+00:00",
  "status": 500,
  "error": "Internal Server Error",
  "path": "/api/v1/current-user/api-keys/9f4e950a-e475-45cd-95e8-84b9b7e8b9c4/revoke"
}
```

**影响范围**

- API Key 生命周期闭环不完整
- 测试过程中产生残留 key
- 交付说明必须额外记录残留项

**当前替代方案**

- 无法通过公开接口自行清理
- 只能在交付说明中保留残留 ID，等待后端处理

**后续复测入口**

- 修复后重测 disable / revoke
- 确认列表页、详情页、状态筛选是否与实际状态一致

### B4. 当前失败调用未观察到日志落库

**阻塞描述**

调用日志查询接口本身可用，但在本轮已执行的失败调用之后，没有查到当前用户维度的任何日志记录。

**复现路径**

1. 执行有效 API Key 调未知接口
2. 查询 `GET /current-user/api-call-logs?page=1&size=20`
3. 查询 `GET /current-user/api-call-logs?targetApiCode=unknown-api&page=1&size=20`

**契约预期**

- 即使是失败调用，也应至少保留当前用户可见的日志快照，支撑联调排障

**实际结果**

```json
{
  "items": [],
  "page": 1,
  "size": 20,
  "total": 0
}
```

**影响范围**

- 无法验证日志列表
- 无法验证日志详情
- 无法确认错误信息是否被正确沉淀

**当前替代方案**

- 暂无
- 需等上游成功调用链路或失败落库链路恢复后再补测

**后续复测入口**

- 修复 B2 后先复测失败调用是否落日志
- 如有日志，再补测详情接口与前端日志页

### B5. 真实环境缺少可发现资产样本

**阻塞描述**

初始环境中 `GET /discovery/assets` 返回空列表；当前又受 B1 影响，无法通过公开接口自造一条可发现资产。

**实际结果**

```json
{
  "items": []
}
```

**影响范围**

- 市场页只能验证空态，无法验证真实详情与最近访问更新
- Playground 无法选择真实目标资产

**当前替代方案**

- 后端预置一条已启用资产
- 或先解除 B1

## 4. 当前残留测试数据

### 分类

- `opsx-cat-debug`
- `opsx-cat-20260422231328`
- `opsx-cat-revise-20260422231418`

### 资产草稿

- `opsx-proxy-20260422231328`
- `opsx-asset-revise-20260422231418`

### API Key

- `0f606e46-74fb-4bc6-88ea-85b18684fdd3`
- `88bf85e2-002a-4cf5-a03d-3f8c1ccb5a70`
- `9f4e950a-e475-45cd-95e8-84b9b7e8b9c4`

## 5. 建议的后续跟踪顺序

1. 先处理 B1，因为它阻塞“造资产 -> 启用 -> discovery -> playground”整条主链路。
2. 再处理 B2 与 B4，恢复 Unified Access 失败分类与日志沉淀。
3. 最后处理 B3，补齐 API Key 生命周期闭环和测试清理能力。
