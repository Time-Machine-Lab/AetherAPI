## 1. 权威文档

- [x] 1.1 使用 `tml-docs-spec-generate` SQL 模板更新 `docs/sql/api-asset.sql`，为 `api_asset` 增加上游请求头字段。
- [x] 1.2 使用 `tml-docs-spec-generate` API 模板更新 `docs/api/api-asset-management.yaml`，补齐 `ApiAssetController.java` 的请求/响应契约。
- [x] 1.3 使用 `tml-docs-spec-generate` API 模板更新 `docs/api/api-import-agent.yaml`，补齐 Import Agent 计划、澄清和示例契约。
- [x] 1.4 更新 `docs/design/aehter-api-hub/` 下相关 API Catalog / Unified Access / Import Agent 设计文档，说明鉴权配置、固定上游请求头和调用方透传请求头的边界。

## 2. Catalog 领域与持久化

- [x] 2.1 新增结构化上游请求头模型/值对象，支持规范化和受保护名称校验。
- [x] 2.2 将上游请求头接入 API 资产聚合的创建、修订、重建、关键差异检查和访问器。
- [x] 2.3 将上游请求头接入资产所有者命令、服务模型和公开 Req/Resp DTO。
- [x] 2.4 将上游请求头接入 `ApiAssetDo`、转换器、查询记录、mapper SQL 和仓储持久化。
- [x] 2.5 保持创建、修订、清空、发布和所有者详情在 null/空请求头列表下的语义一致。

## 3. Unified Access 转发

- [x] 3.1 将上游请求头加入目标解析生成的 target snapshot 模型。
- [x] 3.2 在 JDK 与流式出站请求路径中应用配置请求头，顺序位于调用方请求头过滤之后，且不得绕过受保护名称规则。
- [x] 3.3 保持现有 `authConfig`、`Content-Type`、代理路由、流式响应、超时和失败分类行为。
- [x] 3.4 确保上游执行诊断和日志会脱敏敏感形态的配置请求头值。
- [x] 3.5 异步任务查询不得盲目继承提交请求头，除非后续显式建模任务查询请求头行为并更新文档。

## 4. Import Agent 计划与执行

- [x] 4.1 将 `upstreamRequestHeaders` 加入 Import Agent 计划模型、DTO 响应、JSON 序列化和持久化往返。
- [x] 4.2 扩展 planner 工具 schema 和指令，使固定请求头以结构化 `name` / `value` 输出，不写入 `authConfig`。
- [x] 4.3 扩展计划边界校验、审阅和结构化澄清处理，覆盖缺失或不安全请求头值。
- [x] 4.4 将确认后的 Import Agent 执行映射到 API Catalog 资产创建/修订命令，并携带上游请求头。
- [x] 4.5 在思考事件、助手回复、执行摘要和诊断中脱敏敏感形态的请求头值。

## 5. 验证

- [x] 5.1 新增或更新领域/服务测试，覆盖资产创建、修订、详情、清空请求头和不安全名称校验。
- [x] 5.2 新增或更新持久化测试，覆盖上游请求头往返。
- [x] 5.3 新增或更新 Unified Access 测试，证明配置请求头会转发、受保护名称不会转发、鉴权行为保持独立。
- [x] 5.4 新增或更新 Import Agent 测试，覆盖抽取 schema、计划序列化、澄清路径、执行映射和脱敏。
- [x] 5.5 运行受影响 catalog、unified access 和 import-agent 模块的目标 Maven 测试。
- [x] 5.6 在 `aether-api-hub` 下运行 `openspec validate add-api-asset-upstream-headers --strict`。
