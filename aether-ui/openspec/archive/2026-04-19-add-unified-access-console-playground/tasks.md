## 1. 前置对齐

- [x] 1.1 在开始实现前重新阅读 `docs/spec/AetherAPI 前端技术栈与开发规范文档.md`、`aether-console/DESIGN.md`、`docs/api/unified-access.yaml` 与 `docs/api/api-catalog-discovery.yaml`，确认调用工作台仍与现有前端分层、控制台语义和接口契约完全对齐。
- [x] 1.2 决定是否需要 API Key 的会话级临时存储；如果所选方案需要新增全局安全说明或修改顶层视觉/交互规则，先更新对应权威文档，再开始功能代码实现。
- [x] 1.3 决定首期实现是否支持超出原始透传展示之外的流式响应体验；如果该决策需要扩展统一前端请求规范或目标应用 `DESIGN.md`，先完成文档更新。

## 2. 路由与 API 层

- [x] 2.1 在 `aether-console` 中新增统一接入调用工作台的路由、导航入口与页面骨架，并放入受保护的控制台工作区。
- [x] 2.2 在 `src/api` 中创建 Unified Access API 模块，通过统一请求入口封装 `/api/v1/access/{apiCode}` 的多方法调用。
- [x] 2.3 复用或补充 Discovery API 辅助方法，用于加载已启用资产列表与资产详情预填，确保请求逻辑不回流到页面层。

## 3. 调用工作台体验

- [x] 3.1 在 `src/features` 和/或 `src/composables` 中实现调用工作台的表单状态与编排逻辑，覆盖 `apiCode`、方法、API Key、可选请求头、请求体、提交、重置与安全清空动作。
- [x] 3.2 实现基于 Discovery 的目标 API 辅助选择能力，可预填 `apiCode` 与契约允许展示的辅助字段，同时保留手动输入 `apiCode` 的一等路径。
- [x] 3.3 实现响应展示能力，覆盖平台前置失败、JSON 成功响应、文本成功响应，以及二进制下载或原始处理路径，且不假设成功响应存在 TML 包装。
- [x] 3.4 增加请求/响应辅助面板，用于展示使用说明、原始响应查看、失败类型解释，以及基于 `traceId` 的联调排查提示。

## 4. 安全、文案与质量校验

- [x] 4.1 为调用工作台、说明面板、失败解释和安全提示补齐全部 i18n 文案资源。
- [x] 4.2 确保 API Key 输入框默认遮罩，不暗示服务端可以再次返回明文 Key，并提供显式清空或移除能力。
- [x] 4.3 校验页面是否符合 `ConsoleLayout` 语义与 `aether-console/DESIGN.md` 中关于 surface、field、notice、status、间距以及空/加载/错误状态的规则。
- [x] 4.4 运行并通过该前端应用要求的质量检查（`lint`、`format --check`、`type-check`、`build`），并手工冒烟验证 Discovery 预填、失败调用、成功透传展示与 API Key 安全行为。
