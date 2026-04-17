## 1. 权威文档对齐

- [ ] 1.1 在开始实现前，先阅读 `docs/spec/AetherAPI 前端技术栈与开发规范文档.md`、`aether-console/DESIGN.md`、`docs/api/api-credential.yaml` 以及 Consumer & Auth 领域设计文档。
- [ ] 1.2 如果实现过程中需要新增统一接入调用示例、补充 API 字段或扩展凭证专属视觉规则，而现有文档尚未覆盖，必须先更新对应权威文档（适用时通过 `tml-docs-spec-generate` 更新 `docs/api/*.yaml`，或更新 `aether-console/DESIGN.md`），再开始写功能代码。

## 2. 凭证 API 集成

- [ ] 2.1 在 `aether-console` 中新增与 `docs/api/api-credential.yaml` 对齐的凭证领域 API 模块、DTO 映射和错误归一化处理。
- [ ] 2.2 增加页面级组合逻辑，用于凭证筛选、选中、刷新、生命周期操作和一次性明文展示处理，同时不引入新的全局业务 Store。

## 3. 凭证工作区界面

- [ ] 3.1 将 `#credentials` 占位内容替换为真实的受保护工作区分区，支持 API Key 列表浏览、详情查看以及 empty / loading / error 状态。
- [ ] 3.2 实现 API Key 创建流程，包含一次性明文展示、安全提醒文案以及后续仅展示掩码的行为。
- [ ] 3.3 实现启用、停用和吊销操作，并提供与当前状态匹配的可用性控制和操作后刷新。
- [ ] 3.4 渲染状态、过期、吊销和 `lastUsedSnapshot` 信息，并且不伪造本地使用历史。

## 4. 说明文案与国际化

- [ ] 4.1 增加凭证说明内容，解释当前用户 API Key 模型、隐藏 `Consumer` 的边界，以及官方调用文档所在位置。
- [ ] 4.2 新增或更新 `zh-CN` 和 `en-US` 的 locale 资源，覆盖凭证导航、表单、通知、说明文案、空状态和可恢复错误提示。

## 5. 验证

- [ ] 5.1 增加测试，覆盖凭证工作区渲染、一次性明文展示行为、生命周期操作状态切换，以及基于 locale 的说明文案。
- [ ] 5.2 对 `aether-console` 执行 `lint`、`type-check` 和 `build`，如果完整契约尚未就绪，则记录仍然依赖后端联调的验证缺口。
