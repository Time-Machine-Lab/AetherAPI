## Context

联调记录显示，分类创建可用，资产草稿注册可用，但资产修订接口稳定返回默认 `500`。从当前代码看，资产生命周期链路经过 `ApiAssetController -> ApiAssetWebDelegate -> ApiAssetApplicationService -> ApiAssetRepositoryPort -> MybatisApiAssetRepository`，业务规则主要在 `ApiAssetAggregate` 中，已存在 `AssetDomainException` 到业务错误码的全局映射。

因此问题重点不应通过前端规避，也不应新增临时接口，而应稳定资产写模型更新路径：确保聚合重建、字段合并、持久化更新和异常映射都按现有契约工作。

## Goals / Non-Goals

**Goals:**

- 修复 `PUT /assets/{apiCode}` 在草稿资产补齐分类、上游配置、认证方案等字段时返回默认 `500` 的问题。
- 保证 `PATCH /assets/{apiCode}/enable` 在资产补齐后可以启用资产，并让启用资产出现在 discovery 列表中。
- 保证资产写操作失败时返回业务错误响应，而不是 Spring 默认错误页。
- 补充测试证明资产生命周期主链路可用于真实联调造数。

**Non-Goals:**

- 不新增资产接口。
- 不改变 API 市场 discovery 的读取契约。
- 不引入批量导入、管理端或样本数据管理系统。

## Decisions

### 1. 优先修复写模型更新路径，而不是预置固定资产绕过

- 决策：以修复资产修订接口为主，预置样本最多作为后续环境补充，不作为本次核心方案。
- 原因：B5 的“没有可发现资产样本”本质上是 B1 导致无法自造资产；只预置数据不能解决主链路问题。
- 备选方案：直接在环境中插入一条 `ENABLED` 资产。
- 不采用原因：会掩盖资产管理接口不可用的问题，后续联调仍会卡在资产维护动作。

### 2. 保持 Controller 契约不变，修复 application / domain / persistence

- 决策：不改 `ApiAssetController` 的 URL、方法和 DTO 语义，重点检查 DTO 字段 set 标记、聚合 merge、仓储 update、MyBatis 版本字段与空值处理。
- 原因：`docs/api/api-asset-management.yaml` 已经定义了修订接口，当前问题是实现没有满足契约。
- 备选方案：新增“补齐资产配置”专用接口。
- 不采用原因：会制造重复接口，破坏现有资产生命周期模型。

### 3. 业务异常继续由全局异常处理器统一映射

- 决策：可预期业务失败继续抛领域异常或参数异常，由 `GlobalExceptionHandler` 映射成业务错误码。
- 原因：避免在 controller 或 delegate 中写业务规则，符合 DDD 分层约束。
- 备选方案：在 controller 捕获所有异常并手工拼响应。
- 不采用原因：会让 adapter 层承载业务规则和错误分类。

## Risks / Trade-offs

- [仅靠静态分析无法确定真实 500 的底层异常栈] -> 实现时必须复现 B1 请求并查看后端日志，优先定位 DataAccess/MyBatis/转换异常。
- [MyBatis 乐观锁版本字段可能导致更新未命中] -> 测试需要验证数据库真实状态变更，而不只是 controller 返回内存模型。
- [SQL 顶层文档文件名 `api-asset.sql` 与表名 `api_asset` 不一致] -> 若实现期需要改动表结构，必须先用 SQL 模板修正顶层文档命名与内容。

## Migration Plan

1. 复现 `POST /assets` 后执行 `PUT /assets/{apiCode}` 的默认 `500`。
2. 定位具体异常栈并修复资产更新路径。
3. 补充资产生命周期集成或仓储测试，确认更新真实落库。
4. 复测启用和 discovery 列表可见性。
