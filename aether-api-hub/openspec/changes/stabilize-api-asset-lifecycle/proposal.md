## Why

真实联调中 `PUT /assets/{apiCode}` 对草稿资产执行分类、上游配置或组合字段修订时返回 Spring 默认 `500`，导致资产无法从 `DRAFT` 补齐为可启用状态。该问题直接阻塞“造资产 -> 启用 -> discovery -> playground / Unified Access”主链路，也使真实环境缺少可发现资产样本。

## What Changes

- 修复 API Asset 修订、停用等写模型更新路径，确保正常请求返回修订后的资产详情而不是默认 `500`。
- 补齐资产修订失败时的业务错误返回，至少覆盖资产不存在、参数非法、分类无效、状态不允许等可预期失败。
- 增加资产生命周期写操作的回归验证，覆盖创建草稿、修订分类、修订上游配置、启用、停用、discovery 可见性。
- 本变更不新增接口、不改变 `docs/api/api-asset-management.yaml` 契约；如实现期发现契约字段或 SQL 文档与代码不一致，必须先用 `tml-docs-spec-generate` 更新顶层文档。

## Capabilities

### New Capabilities
- `api-asset-lifecycle-stability`: 确保 API 资产生命周期写操作稳定、可返回业务错误、可支撑 discovery 造数闭环。

### Modified Capabilities

None.

## Impact

- 影响 `ApiAssetController.java`、`ApiAssetWebDelegate.java`、`ApiAssetApplicationService.java`、资产聚合、资产仓储与 MyBatis 持久化实现。
- 间接解除 discovery 与 playground 的测试数据阻塞。
- 不影响 Consumer/Auth、Unified Access 转发逻辑或 Observability 写入逻辑。
