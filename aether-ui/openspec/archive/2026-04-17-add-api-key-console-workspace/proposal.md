## 背景

`aether-console` 虽然已经预留了 `credentials` 导航入口，但目前还没有真正面向开发者的 API Key 工作流。后端归档提案 `2026-04-17-consumer-auth-key-management` 和 `2026-04-17-consumer-auth-unified-access-auth` 已经明确了凭证契约、隐藏的 `Consumer` 模型以及最近使用快照语义，因此前端需要补上一份配套提案，把这些权威文档落成可用的控制台体验，同时避免凭空发明新的鉴权行为。

## 变更内容

- 在 `aether-console` 中新增面向当前登录用户的凭证工作区，并与 `docs/api/api-credential.yaml` 对齐，覆盖 API Key 创建、掩码列表浏览、详情查看以及启用 / 停用 / 吊销操作。
- 增加“明文密钥仅展示一次”的交互体验，并提供明确的安全提示，确保控制台不会暗示用户后续还能再次取回完整 API Key。
- 在控制台中展示凭证状态、过期时间和 `lastUsedSnapshot`，帮助开发者判断某个 key 是否可用，以及是否已经被统一接入消费过。
- 复用现有的 `credentials` 导航入口、`ConsoleLayout`、API 层、i18n 结构和 `aether-console/DESIGN.md` 中定义的控制台语义角色；不引入页面层裸请求、新的全局业务 Store，也不暴露显式的 `Consumer` 管理界面。
- 在控制台中增加边界清晰的凭证说明，解释“当前用户 API Key”模型并指向已有权威文档；如果未来实现需要新的统一接入请求示例或新的视觉规则，必须先更新对应的 `docs/api/*.yaml` 或 `aether-console/DESIGN.md`。

## 能力

### 新增能力

- `console-api-key-management`：定义 `aether-console` 中“当前用户 API Key 管理”工作流，包括创建、列表、详情、生命周期操作、明文一次性展示以及最近使用快照展示。
- `console-api-key-guidance`：定义控制台侧的凭证说明体验，包括隐藏 `Consumer` 的表达方式、安全提醒，以及不越界发明统一接入请求契约的接入指引。

### 修改能力

- 无。

## 影响范围

- 受影响应用：`aether-console`
- 受影响前端区域：`src/pages`、`src/features`、`src/api`、`src/composables`、`src/locales`，以及控制台工作区导航 / 通知区域
- 依赖的权威文档：`docs/api/api-credential.yaml`、`docs/design/aehter-api-hub/Aether API Hub Consumer & Auth领域设计文档.md`、`docs/spec/AetherAPI 前端技术栈与开发规范文档.md`、`aether-console/DESIGN.md`
- 上游依赖：后端已归档的 `consumer-auth-key-management` 契约交付，以及 `consumer-auth-unified-access-auth` 对 `lastUsedSnapshot` 语义的落实
- 本提案默认不要求新增前端顶层权威文档；如果后续实现发现 API 契约、鉴权说明或控制台视觉规则仍有缺口，必须先补齐对应顶层文档，再进入代码开发
