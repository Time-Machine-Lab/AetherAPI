## 背景

`Unified Access` 在 Aether API Hub 的主链路中负责把“API 资源”和“调用身份”收敛为一次可执行调用，并在成功场景中尽量透传上游语义返回给调用方，同时为 AI API 保留流式透传边界。后端已归档提案 `2026-04-19-unified-access-entry-routing` 与 `2026-04-19-unified-access-upstream-proxy` 已经明确：

- 统一入口契约以 `docs/api/unified-access.yaml` 为唯一顶层 API 文档，成功响应不使用 `TML-SDK Result` 包装；
- 平台前置失败通过结构化 JSON 返回，并以 `failureType` 给出明确分类；
- 统一接入成功响应应尽量保留上游状态码、关键响应头与响应体语义，且不阻断流式能力边界。

前端侧的 `aether-console` 已经有面向开发者的 API 市场浏览与 API Key 管理工作流（对应归档提案 `2026-04-16-add-api-catalog-console-pages`、`2026-04-17-add-api-key-console-workspace`），但缺少一个将“已启用 API 资产 + API Key + 统一接入调用契约”串起来的调用工作台。

本设计文档在以下约束下提出实现路径：

- 技术栈与分层约束遵循 `docs/spec/AetherAPI 前端技术栈与开发规范文档.md`：Vue 3 + TypeScript + Vite、Tailwind、shadcn-vue、文件式路由、axios 统一请求入口、i18n 不允许硬编码用户可见文案。
- 视觉与交互语义遵循 `aether-console/DESIGN.md` 中的控制台语义角色、Notice Banner、状态反馈与页面布局规则。
- 不发明新的后端契约；统一接入调用以 `docs/api/unified-access.yaml` 为权威来源，目标 API 列表与详情以 `docs/api/api-catalog-discovery.yaml` 为权威来源。

## 目标 / 非目标

**目标：**

- 提供一个 `aether-console` 内的统一接入调用工作台，使开发者能够以 `apiCode` 为入口执行一次真实调用，并以结构化方式查看成功透传结果或平台前置失败信息。
- 与 API 市场浏览能力联动，基于 Discovery 的资产列表与详情预填 `apiCode`、推荐方法、请求模板与示例快照，降低手工输入成本。
- 明确 API Key 的安全交互，不暗示平台可找回明文 Key，不默认持久化明文 Key，并提供清晰的安全提示与最小必要的临时输入体验。
- 对齐控制台分层，页面只负责编排与状态组合；请求封装位于 `src/api`，复杂交互编排位于 `src/composables` 或 `src/features`。

**非目标：**

- 不新增或修改 `docs/api/` 中的任何契约文件，不引入新的后端管理接口或“统一接入配置管理界面”。
- 不在本提案中实现 Observability 的调用日志查询与分析页面，该能力应在对应领域的权威契约与提案下单独交付。
- 不承诺在首期前端实现中完整支持所有流式协议细节（例如 SSE 事件解析、增量渲染语义化等），仅在不违反前端规范与现有契约的前提下提供“尽量可用”的展示策略。

## 关键决策

1. **信息架构落点：作为开发者控制台工作台的一部分提供调用工作台**
   - 方案：在 `aether-console` 的受保护区域新增入口，与既有 `credentials`、workspace 模式保持一致，页面主体遵循控制台语义角色与布局规则。
   - 理由：统一接入调用与 API Key、已启用资产紧密相关，属于开发者控制台的“可执行调用”体验，而不是官网或营销页能力。
   - 备选：放在 API 市场详情页内作为“Try it”面板。该方案虽然可行，但更容易与匿名浏览边界冲突，因此首选控制台工作台入口。

2. **目标 API 选择：以 Discovery 为权威来源，并保留手动输入 `apiCode` 兜底**
   - 方案：默认从 `/api/v1/discovery/assets` 获取已启用资产列表，选择后再按需请求 `/api/v1/discovery/assets/{apiCode}` 获取详情用于预填，同时保留手动输入 `apiCode` 的能力，用于灰度或联调场景。
   - 理由：避免前端硬编码 API 列表，同时充分利用权威契约中已经可展示的字段，例如 `requestTemplate`、`exampleSnapshot`、`streamingSupported`。

3. **调用执行与分层：所有调用逻辑下沉到 API 层与 composable，不在页面写裸请求**
   - 方案：新增 `unified-access.api.ts`（或等价命名）的 API 模块，封装 `/api/v1/access/{apiCode}` 的多方法调用；页面层通过 `useUnifiedAccessPlayground()` 之类的组合式能力统一管理表单状态、请求执行状态和响应展示状态。
   - 理由：严格遵守前端规范的分层约束，便于复用、测试和后续扩展。

4. **API Key 安全策略：默认不持久化明文，输入控件默认遮罩，并提供显式清空**
   - 方案：调用工作台将 API Key 视为敏感输入，默认使用密码型输入控件；不默认写入 `localStorage`；是否允许会话级暂存（如 `sessionStorage`）作为后续实现中的显式开关，并配套安全提示与“清空”动作。
   - 理由：`docs/api/api-credential.yaml` 明确了“明文仅展示一次”的安全语义，调用工作台不应绕过或弱化这一心智。
   - 备选：与“当前用户 API Key 管理”联动，在创建成功当次将明文 Key 临时传递给调用工作台，仅保留内存态，刷新即失效。该方案可提升首次使用体验，但需要在实现阶段评估路由状态传递方式。

5. **响应展示策略：优先支持 JSON / 文本的可读展示，二进制以下载方式兜底**
   - 方案：根据响应 `Content-Type` 选择展示策略：
     - `application/json`：格式化 JSON 展示；若是平台前置失败，则按 `UnifiedAccessPlatformFailureResp` 结构化呈现；
     - `text/*`：按纯文本展示；
     - 其他类型：以 Blob 下载或预览提示兜底。
   - 理由：`docs/api/unified-access.yaml` 已允许 JSON、文本与二进制响应，且成功响应不受 TML 包装约束，前端不应假设固定字段结构。

6. **流式能力取舍：以“不阻断”为目标，具体协议支持作为实现期决策点**
   - 约束来源：后端提案 `unified-access-upstream-proxy` 强调目标 API 支持流式时不应阻断该能力；但 `docs/api/unified-access.yaml` 当前未显式声明 `text/event-stream` 等流式响应媒体类型。
   - 方案：设计上为“可能存在流式返回”预留 UI 与状态机空间，例如响应区支持增量追加与取消；但具体是否采用 `fetch + ReadableStream`，或继续通过统一 API 层做兼容兜底，留待实现阶段结合现有工程封装能力与团队规范评审后确定。

## 风险 / 权衡

- [API Key 泄漏风险] → 默认遮罩输入、不默认持久化，提供明确安全提示与一键清空；若后续实现生成 cURL 或示例代码，默认隐藏 Key，需用户显式选择才包含。
- [成功响应语义不可预测] → 成功场景不做强类型绑定，按 `Content-Type` 分类展示，并保留“原始响应”查看入口。
- [流式支持可能与现有前端规范冲突] → 先在设计与 spec 中声明“流式为可选增强与实现期决策点”；如需在 API 层内引入专门的流式适配，先补齐顶层规范说明再进入实现。
- [鉴权语义容易混淆] → 在调用工作台中清晰区分控制台登录态（bearerAuth，用于管理接口）与统一接入调用凭证（`X-Aether-Api-Key`，用于统一接入接口），避免 UI 文案误导。
