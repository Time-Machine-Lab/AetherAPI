## ADDED Requirements

### Requirement: 受影响的控制面接口必须具备确定性的参数绑定
系统 MUST 对受影响的分类、资产、当前用户 API Key 接口稳定完成路径参数与查询参数绑定，使这些请求不依赖运行时对 Java 反射参数名的隐式推断。

#### Scenario: 分类列表请求能够绑定分页参数
- **WHEN** 客户端请求 `GET /api/v1/categories`，并携带可选的 `status`、`page`、`size` 参数
- **THEN** 系统能够成功完成参数绑定并执行分类列表流程，而不是因为缺少参数名元数据直接返回框架级 `400`

#### Scenario: 资产详情请求能够绑定 apiCode
- **WHEN** 客户端请求 `GET /api/v1/assets/{apiCode}`，并传入如 `deepseek-v3` 这样的具体资产编码
- **THEN** 系统能够成功绑定 `apiCode` 并进入资产详情逻辑，而不是在委托执行前绑定失败

#### Scenario: 当前用户 API Key 列表能够绑定分页参数
- **WHEN** 客户端请求 `GET /api/v1/current-user/api-keys?page=1&size=20`
- **THEN** 系统能够成功绑定分页参数并执行当前用户 API Key 列表流程，而不是在请求绑定阶段失败

### Requirement: 参数绑定失败必须返回接口族对应的客户端错误
系统 MUST 将框架级请求参数绑定失败表示为与目标接口族一致的 `400` 响应，而不是把无关接口统一映射成分类错误码。

#### Scenario: 分类接口绑定失败返回分类错误
- **WHEN** 分类管理接口发生请求参数绑定失败
- **THEN** 响应使用分类领域下的无效请求错误码

#### Scenario: 资产接口绑定失败返回资产错误
- **WHEN** 资产管理接口发生请求参数绑定失败
- **THEN** 响应使用资产领域下的无效请求错误码，且不返回 `CATEGORY_CODE_INVALID`

#### Scenario: API Key 接口绑定失败返回凭证错误
- **WHEN** 当前用户 API Key 接口发生请求参数绑定失败
- **THEN** 响应使用 API 凭证领域下的无效请求错误码，且不返回分类错误码

### Requirement: 参数绑定失败响应不得暴露编译器内部信息
系统 MUST NOT 在客户端可见的参数绑定失败消息中暴露编译器参数、反射提示或其他内部框架诊断信息。

#### Scenario: 客户端响应中移除编译器提示
- **WHEN** 受影响接口发生框架级请求参数绑定失败
- **THEN** 客户端可见响应消息只描述请求无效，不包含 `-parameters` 或反射元数据等内部提示

### Requirement: 参数绑定稳定性变更必须维护按 Controller 拆分的 API 权威文档
系统 MUST 在实现前将客户端可见的参数绑定与无效请求响应变化记录到按 Controller 拆分的顶层 API 文档中。

#### Scenario: 生成分类接口权威文档
- **WHEN** 项目记录分类接口的参数绑定稳定性变更
- **THEN** 必须使用 `tml-docs-spec-generate` 与 API 模板维护 `docs/api/category.yaml`，并映射到 `CategoryController.java`

#### Scenario: 生成资产接口权威文档
- **WHEN** 项目记录资产接口的参数绑定稳定性变更
- **THEN** 必须使用 `tml-docs-spec-generate` 与 API 模板维护 `docs/api/api-asset.yaml`，并映射到 `ApiAssetController.java`

#### Scenario: 生成 API Key 接口权威文档
- **WHEN** 项目记录当前用户 API Key 接口的参数绑定稳定性变更
- **THEN** 必须使用 `tml-docs-spec-generate` 与 API 模板维护 `docs/api/api-credential.yaml`，并映射到 `ApiCredentialController.java`
