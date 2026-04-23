## Context

当前 `UnifiedAccessApplicationService` 已经存在 `TARGET_NOT_FOUND` 平台失败模型，也会在捕获 `UnifiedAccessPlatformFailureException` 时调用 Observability 记录失败日志。但联调结果仍然返回默认 `500` 且日志为空，说明真实链路可能在以下位置中断：

- `UnifiedAccessController` 参数绑定失败，未进入 application service。
- 平台失败响应抛出后被其他异常覆盖。
- 失败日志写入自身抛错，导致原本应返回的业务失败被二次异常污染。
- 日志落库成功但当前用户查询维度无法关联到该调用。

本变更需要把前置失败路径做成稳定闭环：响应可分类、日志可沉淀、查询可回看。

## Goals / Non-Goals

**Goals:**

- `GET /access/unknown-api` 在有效 API Key 下返回 `404 + TARGET_NOT_FOUND` 平台失败响应。
- Unified Access 所有 HTTP 方法都显式绑定 `apiCode` 等参数，避免反射参数名依赖。
- 平台前置失败日志写入失败不能覆盖原始平台失败响应。
- 当前用户调用日志能够查询到与其 API Key/Consumer 关联的失败调用。

**Non-Goals:**

- 不改变成功转发时上游响应透传规则。
- 不引入完整链路追踪系统。
- 不把 Unified Access 成功或失败响应统一包装成 TML Result。

## Decisions

### 1. Controller 参数显式命名

- 决策：为 Unified Access 的 `@PathVariable` 明确写入 `apiCode`，并确认 query/header 参数无需依赖反射参数名。
- 原因：当前 controller 只有 Unified Access 使用未命名 `@PathVariable String apiCode`，这是前置 500 的高风险点。
- 备选方案：只依赖 Maven `-parameters`。
- 不采用原因：显式命名更稳，也符合接口联调稳定性要求。

### 2. 平台失败响应优先于日志失败

- 决策：如果平台前置失败已经形成，日志写入异常不得覆盖原始 `UnifiedAccessPlatformFailureException`。
- 原因：日志是观测能力，不能让观测失败改变调用方看到的业务失败分类。
- 备选方案：日志失败直接抛出。
- 不采用原因：会导致 `TARGET_NOT_FOUND` 重新退化为默认 `500`。

### 3. 失败日志需要保留可回查的调用主体关联

- 决策：有效 API Key 校验通过后发生的目标不存在、目标不可用等失败，日志必须包含 Consumer / Credential 快照。
- 原因：当前用户日志查询依赖用户隐式 Consumer 映射；如果失败日志没有 caller 关联，就无法在控制台维度查到。
- 备选方案：平台前置失败日志全部记匿名。
- 不采用原因：会导致 B4 继续存在，无法支撑控制台排障。

## Risks / Trade-offs

- [平台失败发生在凭证校验前时没有 Consumer 维度] -> 无效或缺失 API Key 的失败日志可以匿名，但有效 Key 后的目标失败必须带 caller 快照。
- [日志写入失败被吞掉后排障信息减少] -> 必须记录服务端 warning 日志，同时保持调用方响应正确。
- [当前用户日志查询与 Consumer 映射关系不一致] -> 实现时需要验证 `currentUserId -> Consumer -> api_call_log.consumer_id` 的查询链路。

## Migration Plan

1. 修复 Unified Access 参数绑定和平台失败响应路径。
2. 调整平台失败日志命令构造，保证有效 Key 后失败有 caller 快照。
3. 对日志写入异常做保护，避免覆盖原始响应。
4. 复测 `GET /access/unknown-api` 响应和 `GET /current-user/api-call-logs` 查询。
