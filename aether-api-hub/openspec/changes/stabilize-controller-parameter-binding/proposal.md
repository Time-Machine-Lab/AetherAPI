## Why

当前多个后端接口会直接返回 `400`，并携带 `CATEGORY_CODE_INVALID` 与编译器反射提示信息，而不是进入对应的控制器业务逻辑。该问题同时出现在分类、资产、当前用户 API Key 等多个适配层接口上，说明根因在后端请求参数绑定稳定性，而不是前端拼参错误，因此需要先以提案形式明确修复范围与约束。

## What Changes

- 定义一个新的后端能力，要求受影响控制面接口的路径参数与查询参数绑定具备确定性，不依赖运行时反射参数名推断。
- 统一框架级参数绑定失败的客户端可见错误语义，使分类、资产、当前用户 API Key 接口返回各自领域下的 `400` 错误码，而不是一律落到 `CATEGORY_CODE_INVALID`。
- 要求参数绑定失败响应不再向客户端暴露 `-parameters`、反射元数据等内部实现细节。
- 将当前仓库缺少 `aether-api-hub/openspec/project.md` 与 `aether-api-hub/docs/api/` 的现状作为实施前置缺口记录下来，并要求在实现前补齐对应 Controller 级 API 权威文档，因为本次变更会影响客户端可见错误行为。
- 为已知报错接口和共享异常映射路径补充回归测试要求。

## Capabilities

### New Capabilities

- `web-parameter-binding-stability`：定义受影响后端 Web Controller 的稳定参数绑定行为，以及参数绑定失败时的响应约束。

### Modified Capabilities

- 无。

## Impact

- 影响代码：`aether-api-hub-standard/aether-api-hub-adapter` 下的 Controller、全局异常处理器，以及父级 Maven 编译配置。
- 影响接口：`GET /api/v1/categories`、`GET /api/v1/categories/{categoryCode}`、`GET /api/v1/assets/{apiCode}`、`GET /api/v1/current-user/api-keys`。
- 影响文档：由于本次变更调整了客户端可见错误行为，而仓库当前不存在 `docs/api/` 目录，实现前需要先创建与 Controller 一一对应的 API 权威文档。
- 不在范围内：前端代码、数据库结构变更、接口路径与请求体设计重构。
