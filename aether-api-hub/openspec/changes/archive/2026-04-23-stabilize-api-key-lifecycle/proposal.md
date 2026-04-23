## Why

真实联调中当前用户 API Key 能创建和列表读取，但 `PATCH /current-user/api-keys/{credentialId}/disable` 与 `/revoke` 返回默认 `500`。这导致 API Key 生命周期闭环不完整，也无法清理联调中创建的测试 Key。

## What Changes

- 修复 API Key 停用、启用、吊销等状态变更写路径，确保成功时返回更新后的凭证状态。
- 修复状态不允许、凭证不存在、凭证不属于当前用户等失败路径，确保返回契约中的业务错误而不是默认 `500`。
- 补充 API Key 生命周期测试，覆盖创建、停用、启用、吊销、重复操作与列表状态筛选。
- 本变更不暴露 Consumer 概念，不新增 API Key 接口，不改变 `docs/api/api-credential.yaml` 契约。

## Capabilities

### New Capabilities
- `api-key-lifecycle-stability`: 覆盖当前用户 API Key 状态变更、业务错误分类与测试数据清理能力。

### Modified Capabilities

None.

## Impact

- 影响 `ApiCredentialController.java`、`ApiCredentialWebDelegate.java`、`ApiCredentialApplicationService.java`、API Key 聚合、仓储与 MyBatis 持久化实现。
- 间接影响前端联调数据清理和 Unified Access 凭证状态验证。
- 不影响控制台登录、资产管理和 Unified Access 目标解析。
