## Why

仅有统一入口和目标匹配，还不足以让 Aether API Hub 真正成为“可统一调用”的平台。`Unified Access` 还必须把已经完成前置校验和目标解析的请求稳定转发到真实上游，并在成功场景下尽量保留上游原始语义返回给调用方，尤其要为 AI API 的流式能力保留边界。

## What Changes

- 新增上游请求组装与转发能力，覆盖请求透传、必要头处理、平台到上游的鉴权注入和真实上游调用执行。
- 新增统一接入成功返回与转发失败返回的行为约束，明确成功调用不使用 `TML-SDK Result` 包装，而是优先保留上游状态码、响应头和响应体语义。
- 新增 AI API 流式透传边界，要求统一接入层在目标 API 支持流式时不阻断该能力。
- 本提案不新增面向前端或管理端的额外业务接口文档，不新增独立 SQL 顶层文件；它默认复用 `unified-access-entry-routing` 约定的统一入口接口与现有主数据。
- 约束并发边界：本提案只负责调用执行、结果回传和流式透传边界，不负责统一入口路径设计、目标匹配规则、前置平台错误分类和显式控制器契约拆分。

## Capabilities

### New Capabilities
- `unified-access-upstream-proxy`: 定义上游请求转发、成功响应原样回传、失败归类与流式透传边界。

### Modified Capabilities
- None.

## Impact

- 受影响文档：[Aether API Hub架构设计文档](D:/Code/Project/Github/AetherAPI/docs/design/aehter-api-hub/Aether API Hub架构设计文档.md)、[Aether API Hub Unified Access领域设计文档](D:/Code/Project/Github/AetherAPI/docs/design/aehter-api-hub/Aether API Hub Unified Access领域设计文档.md)。
- 受影响代码：`aether-api-hub-standard` 中与 HTTP 上游调用执行、请求映射、响应回传和流式透传相关的 `service`、`adapter`、`infrastructure` 模块。
- 边界冲突提示：本提案依赖 `unified-access-entry-routing` 定义好的调用入口与目标快照，不应重复发明第二套入口接口，也不应把成功上游响应包装成平台管理类返回结构。
