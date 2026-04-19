## 背景

后端已归档提案 `2026-04-19-unified-access-entry-routing` 与 `2026-04-19-unified-access-upstream-proxy` 已经明确了统一接入入口、目标匹配、平台前置失败分类、上游转发，以及“成功尽量透传”的边界，并给出了唯一权威契约 `docs/api/unified-access.yaml`。但 `aether-console` 目前仍缺少一个面向开发者的“统一接入调用工作台”，导致 Unified Access 主链路在前端侧缺少可视化验证入口，也无法把 API Key 管理与真实调用体验串联起来。

因此需要新增一份前端配套提案：在不发明新后端契约、不破坏既有控制台分层与设计语义的前提下，为开发者提供一个可用、可复用、可对齐权威契约的 Unified Access 调用与使用指引体验。

## 变更内容

- 在 `aether-console` 中新增“统一接入调用工作台”页面能力，支持选择或输入 `apiCode`、选择请求方法、填写请求体与必要头信息，并通过 `docs/api/unified-access.yaml` 定义的统一入口发起调用。
- 复用 `docs/api/api-catalog-discovery.yaml` 的已启用资产浏览能力，为调用工作台提供可选目标 API 列表与详情预填，例如推荐方法、请求模板、示例快照以及 AI API 的流式支持标记。
- 在控制台中以“安全默认”方式处理 API Key 输入与提示，明确 `X-Aether-Api-Key` 的用途与敏感性，不暗示平台可以再次找回明文 Key，并为调试场景保留最小必要的本地暂存策略讨论空间。
- 增加平台前置失败的展示与解释能力，对 `docs/api/unified-access.yaml` 中的 `failureType`（`INVALID_API_CODE`、`INVALID_CREDENTIAL`、`TARGET_NOT_FOUND`、`TARGET_UNAVAILABLE`）进行结构化展示，并突出 `traceId` 以便联调排查。
- 对齐前端统一规范与控制台设计语言，页面、API 层、i18n、布局与状态反馈遵循 `docs/spec/AetherAPI 前端技术栈与开发规范文档.md` 与 `aether-console/DESIGN.md`，不在页面层写裸请求，不引入新的全局业务 Store 作为默认实现路径。

## 能力

### 新增能力

- `console-unified-access-playground`：定义 `aether-console` 中“统一接入调用工作台”的页面信息架构、输入模型（`apiCode`、`method`、`payload`、`headers`）、调用行为与响应展示规范，严格以 `docs/api/unified-access.yaml` 为契约来源，并可联动 Discovery 能力进行目标预填。
- `console-unified-access-guidance`：定义控制台侧的统一接入使用指引与安全提示，包括 API Key 请求头注入说明、平台前置失败分类解释，以及对“成功响应尽量透传”和“可能存在流式输出”的用户心智引导。

### 修改能力

- 无。

## 影响范围

- 受影响应用：`aether-console`
- 依赖的权威接口契约：`docs/api/unified-access.yaml`、`docs/api/api-catalog-discovery.yaml`，并参考 `docs/api/api-credential.yaml` 中“一次性明文展示”的安全约束用于提示与联动
- 参考的权威设计文档：`docs/design/aehter-api-hub/Aether API Hub架构设计文档.md`、`docs/design/aehter-api-hub/Aether API Hub Unified Access领域设计文档.md`、`docs/spec/AetherAPI 前端技术栈与开发规范文档.md`、`aether-console/DESIGN.md`
- 后续实现预计影响的前端区域：`src/pages`、`src/features`、`src/api`、`src/composables`、`src/locales`，以及控制台导航与工作台入口配置
