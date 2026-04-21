## Why

`aether-console` 当前仍然使用前端本地生成的 demo token，而后端没有控制台登录入口、没有签发控制台 bearer token、也没有把 bearer token 解析为 `Principal` 的认证链路，导致“当前用户”接口在真实联调时无法成立。现在 API Key 管理与调用日志等控制台能力已经进入联调阶段，必须补齐最小真实登录闭环。

## What Changes

- 新增控制台会话认证能力，明确它与现有 Consumer/API Key 鉴权职责分离。
- 新增控制台登录接口与当前会话接口，由后端签发可用于控制台请求的 bearer token。
- 新增控制台请求认证链路，在进入 `current-user` 类接口前完成 token 校验与 `Principal` 建立。
- 统一未登录或 token 无效时的接口失败语义，避免继续走到空 `Principal` 的兜底异常。
- 新增仓库根目录 `docs/api/console-auth.yaml` 作为顶层接口契约文档，并要求后续通过 `tml-docs-spec-generate` 的 API 模板生成。
- 本期不引入完整用户中心、RBAC、管理端或 Consumer 显式注册流程；也不改动 Unified Access 面向 API 消费者的鉴权语义。

## Capabilities

### New Capabilities
- `console-session-auth`: 覆盖控制台登录、会话恢复、bearer token 认证与当前用户上下文建立。

### Modified Capabilities

None.

## Impact

- 影响后端 Web Adapter 的控制台认证入口、请求认证拦截链路和当前用户上下文解析。
- 影响现有依赖 `Principal` 的控制台接口，包括 API Key 管理和调用日志查询接口的可用性。
- 影响仓库根目录顶层接口文档，需要新增 `docs/api/console-auth.yaml`，并保持其与目标 `ConsoleAuthController.java` 一一映射。
- 本期预计不新增 `docs/sql/*.sql` 文件，MVP 先采用后端受控的控制台登录主体配置，不在本变更内引入新的控制台用户表结构。
