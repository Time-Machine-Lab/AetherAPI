## Why

真实联调中“有效 API Key + 不存在 apiCode”调用 `GET /access/unknown-api` 返回默认 `500`，没有按契约返回 `TARGET_NOT_FOUND`，且失败调用后当前用户日志列表没有出现记录。这会阻塞 Unified Access 错误展示、失败分类验证和调用日志排障闭环。

## What Changes

- 修复 Unified Access 前置失败路径，确保无效目标 API 返回契约中的平台失败响应。
- 确保 Unified Access controller 参数绑定稳定，不因 `@PathVariable` / `@RequestParam` 反射参数名问题绕过业务逻辑。
- 修复平台前置失败日志沉淀，尤其是有效 API Key 对未知目标 API 的失败调用，应产生可查询日志。
- 本变更不改变 Unified Access 成功透传语义；成功响应仍不使用 TML Result 包装。

## Capabilities

### New Capabilities
- `unified-access-failure-stability`: 覆盖 Unified Access 前置失败分类、响应契约和 controller 参数绑定稳定性。

### Modified Capabilities
- `observability-call-log-foundation`: 增强平台前置失败调用日志落库要求。

## Impact

- 影响 `UnifiedAccessController.java`、`UnifiedAccessWebDelegate.java`、`UnifiedAccessApplicationService.java`、`GlobalExceptionHandler.java`、`ObservabilityApplicationService.java` 以及调用日志持久化实现。
- 影响 `docs/api/unified-access.yaml` 和 `docs/api/api-call-log.yaml` 的实现对齐，但预计不需要改接口契约。
- 不影响 API Asset 写模型和 API Key 生命周期管理接口。
