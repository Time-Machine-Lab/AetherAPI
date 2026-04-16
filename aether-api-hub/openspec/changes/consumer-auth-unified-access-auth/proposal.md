## Why

`API Key` 管理解决了“用户拿到什么凭证”，但 `Unified Access` 仍缺少“收到请求后如何识别调用方、返回什么调用上下文、如何更新凭证最近使用信息”的稳定鉴权能力。若不将这部分单独拆出，统一接入、日志归属与错误码约束会继续分散在不同层中，也会与前端联调接口改动互相干扰。

## What Changes

- 新增统一接入使用的凭证校验能力，覆盖 API Key 指纹匹配、凭证状态校验、隐式 Consumer 可用性校验、调用主体上下文组装与标准失败原因返回。
- 新增统一接入侧的应用服务边界，用于向后续路由转发与日志模块提供 `Consumer Context`，但不新增面向用户的显式 `Consumer` 管理接口，也不额外发明独立的用户可见 HTTP 鉴权接口。
- 补齐凭证最近使用信息更新规则，使鉴权成功或失败后的最近使用快照能够被控制台与后续治理能力复用。
- 本提案不新增面向前端或外部调用方的业务 API 文档；若后续决定将鉴权能力外置为独立适配接口，应通过新的 change 单独补充 `docs/api/` 权威文档，避免在当前阶段过度设计。
- 约束并发边界：本提案只负责统一接入鉴权与上下文返回，不负责 API Key 创建、用户侧凭证管理接口、控制台凭证列表或日志检索接口。

## Capabilities

### New Capabilities
- `consumer-auth-credential-validation`: 定义统一接入对 API Key 的校验、错误分类、调用主体上下文返回与最近使用信息更新行为。

### Modified Capabilities
- None.

## Impact

- 受影响文档：[Aether API Hub架构设计文档](D:/Code/Project/Github/AetherAPI/docs/design/aehter-api-hub/Aether API Hub架构设计文档.md)、[Aether API Hub Consumer & Auth领域设计文档](D:/Code/Project/Github/AetherAPI/docs/design/aehter-api-hub/Aether API Hub Consumer & Auth领域设计文档.md)。
- 受影响代码：`aether-api-hub-standard` 中与统一接入鉴权相关的 `service`、`domain`、`adapter`、`api`、`infrastructure` 模块，以及后续调用日志记录接入点。
- 边界冲突提示：本提案依赖 `consumer-auth-key-management` 提案定义的凭证主数据与存储字段；如果缺少对应顶层 SQL 文档，应先由凭证管理提案补齐，避免两个 change 同时修改同一份存储设计文件。
