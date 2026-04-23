## 1. 契约与复现

- [x] 1.1 阅读 `docs/api/unified-access.yaml`、`docs/api/api-call-log.yaml`、`docs/sql/api_call_log.sql` 与后端开发规范。
- [x] 1.2 复现 B2/B4：使用有效 API Key 调用 `/access/unknown-api`，记录响应、异常栈和日志表写入情况。

## 2. Unified Access 失败分类修复

- [x] 2.1 为 `UnifiedAccessController` 的 `@PathVariable` 显式指定 `apiCode`，并补齐 Web 层参数绑定测试。
- [x] 2.2 修复有效 API Key 调未知 `apiCode` 时的响应，确保返回 `ASSET_NOT_FOUND + TARGET_NOT_FOUND`，不返回默认 `500`。
- [x] 2.3 确保日志写入异常不会覆盖 Unified Access 原始平台失败响应。

## 3. 失败日志沉淀修复

- [x] 3.1 调整平台前置失败日志构造，使有效 API Key 后的目标失败携带 Consumer 与 credential 快照。
- [x] 3.2 验证当前用户调用日志列表可以查到该失败记录，并可继续查询详情。
- [x] 3.3 补充应用服务和 Web 层回归测试，覆盖 unknown-api 响应与失败日志落库。
