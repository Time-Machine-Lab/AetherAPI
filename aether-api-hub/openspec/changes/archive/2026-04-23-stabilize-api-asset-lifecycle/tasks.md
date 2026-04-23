## 1. 契约与复现

- [x] 1.1 阅读 `docs/spec/Aether API HUB 后端代码开发规范文档.md`、`docs/api/api-asset-management.yaml`、`docs/sql/api-asset.sql` 与资产领域设计，确认本变更不新增接口。
- [x] 1.2 复现 B1：创建分类、注册草稿资产、执行 `PUT /assets/{apiCode}`，记录真实异常栈与返回体。

## 2. 资产生命周期修复

- [x] 2.1 定位并修复资产修订写路径中的聚合重建、字段合并、持久化更新或异常映射问题。
- [x] 2.2 验证 `PATCH /assets/{apiCode}/enable` 可在资产补齐后成功启用，并在配置不完整时返回业务错误。
- [x] 2.3 验证 `PATCH /assets/{apiCode}/disable` 对已存在资产返回业务结果或业务错误，不再返回默认 `500`。

## 3. 回归验证

- [x] 3.1 补充资产草稿注册、修订、启用、停用的应用服务或 Web 层测试。
- [x] 3.2 补充“启用资产出现在 discovery 列表”的联动验证。
- [x] 3.3 如实现期发现 SQL 顶层文档必须调整，先使用 `tml-docs-spec-generate` 的 SQL 模板修正对应 `docs/sql/` 文件，再继续代码实现。
