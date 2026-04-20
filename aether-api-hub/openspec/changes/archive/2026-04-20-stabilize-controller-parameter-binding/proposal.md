## Why

当前前后端联调中，多个 GET 接口在进入业务逻辑前就被 Spring Web 参数绑定阶段直接拦截为 `400`，并错误落成 `CATEGORY_CODE_INVALID` 一类的分类错误码，还向客户端暴露编译器参数与反射提示信息。这说明根因在后端 Controller 参数绑定与异常映射稳定性，而不是前端拼参错误，因此需要先以提案形式明确修复范围与边界。

## What Changes

- 定义一个新的后端能力，要求受影响 Web Controller 的路径参数与查询参数绑定具备确定性，不再依赖运行时反射参数名推断。
- 同时修复两类根因：一类是 Maven 构建缺少参数名元数据兜底，另一类是 Controller 注解未显式声明参数名。
- 统一框架级参数绑定失败的客户可见错误语义，使分类、资产、当前用户 API Key、目录发现、调用日志等接口在绑定失败时返回各自接口族对应的 `400` 错误，而不是一律落到 `CATEGORY_CODE_INVALID`。
- 要求参数绑定失败响应不再向客户端暴露 `-parameters`、反射元数据、编译器提示等内部实现细节。
- 记录并更新 repo 根目录 `docs/api/` 下的权威接口文档；本次变更影响客户端可见行为，因此必须优先更新对应 Controller 的 API 文档，而不是在 `aether-api-hub/docs/api/` 下新建另一套目录。
- 为已知报错接口和同类隐式绑定写法的扫描结果补充回归测试要求。

## Capabilities

### New Capabilities

- `web-parameter-binding-stability`: 定义受影响后端 Web Controller 的稳定参数绑定行为，以及参数绑定失败时的响应约束。

### Modified Capabilities

- None.

## Impact

- 影响代码：`aether-api-hub-standard/aether-api-hub-adapter` 下的相关 Controller、全局异常处理器，以及父级 Maven 编译配置。
- 影响接口：至少包括 `GET /api/v1/categories`、`GET /api/v1/categories/{categoryCode}`、`GET /api/v1/assets/{apiCode}`、`GET /api/v1/current-user/api-keys`，并需要扫描同类隐式绑定写法的接口，例如目录发现与调用日志查询接口。
- 影响文档：需要优先更新 repo 根目录 `docs/api/` 下现有或缺失的 Controller 级权威 API 文档；其中已有文档应更新，不存在的单 Controller 文档才新增。
- 不在范围内：前端代码、数据库结构变更、接口路径与请求体设计重构。
