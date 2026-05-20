## Context

`aether-console` 的 Import Agent 工作台已经通过 API 层、composable 和 `ImportAgentWorkspace.vue` 渲染导入会话、计划卡片和结构化 `clarificationItems`。`aether-console/DESIGN.md` 要求当前计划卡片清晰分离 summary、guided clarification controls、legacy questions、category actions 和 asset actions；前端统一规范要求请求走 API 层、用户可见文案走 i18n。

后端本次将为结构化澄清项增加可选推荐默认值字段。前端设计目标不是替用户自动提交，而是在计划卡片里让用户看到“Agent 建议值”，并能一键采用或编辑后提交。

## Goals / Non-Goals

**Goals:**

- 支持渲染 `defaultValue`、`defaultLabel`、`defaultSource`、`defaultConfidence` 等可选字段。
- 对有默认值的问题提供明确的“采用推荐值”或预填后编辑体验。
- 保持旧响应兼容：没有默认值字段时继续使用当前结构化问题 UI。
- 所有新增文案走 i18n，所有请求仍通过 `src/api/import-agent` 和 `useImportAgentWorkspace`。

**Non-Goals:**

- 不新增 Import Agent 页面或路由。
- 不修改后端推断规则。
- 不在前端保存密钥或做密钥脱敏。
- 不把默认值静默提交为答案。

## Decisions

### 决策 1：默认值显示在结构化澄清控件内部

带默认值的问题仍按当前 `clarificationItems` 分组渲染在计划卡片里。推荐值应作为该字段控件的一部分展示，例如在输入框初始草稿值、选择框推荐项、或控件下方辅助文案中呈现来源与置信度。

备选方案是单独做一个“Agent 推荐”面板。拒绝原因是会把问题和答案入口拆开，用户需要来回对照，反而增加负担。

### 决策 2：默认值进入本地草稿，不直接进入提交答案

前端可以把 `defaultValue` 用作本地控件草稿值，或提供“采用推荐值”按钮把它填入草稿；但只有用户点击提交/继续后，才构造 `clarificationAnswers` 发送给后端。

这能避免“页面加载即确认默认值”的隐式行为，也能保留用户编辑空间。

### 决策 3：来源与置信度轻量展示

`defaultSource` 和 `defaultConfidence` 不应变成大段解释文本。UI 可以用短标签或辅助文案表达，例如“推荐：来自文档”“置信度：高”。如果字段不存在，则不展示该辅助信息。

### 决策 4：类型扩展从 API DTO 到领域类型单向流动

先对齐 `docs/api/api-import-agent.yaml`，再更新前端 DTO/types 和 mapper。页面组件只消费领域类型，不直接耦合原始 DTO 字段命名变化。旧字段缺失时使用 `undefined/null` 兼容。

## Risks / Trade-offs

- [Risk] 用户误以为默认值已提交。 -> Mitigation：按钮和提交状态文案明确“采用推荐值”与“提交答案”的区别，不在加载时自动调用 API。
- [Risk] 默认值过长，影响计划卡片布局。 -> Mitigation：长文本使用 multiline 控件、等宽预览或折叠显示，保持卡片内文本不溢出。
- [Risk] 后端字段尚未部署时前端报错。 -> Mitigation：新增字段全部按 optional 处理，测试覆盖旧响应。
- [Risk] 来源/置信度标签干扰主要操作。 -> Mitigation：作为次级辅助信息呈现，不抢占主按钮层级。

## Migration Plan

1. 等后端更新 `../docs/api/api-import-agent.yaml` 后，同步前端 DTO/types。
2. 更新 `useImportAgentWorkspace` 的澄清答案草稿逻辑，让 defaultValue 可被采用或预填。
3. 更新 `ImportAgentWorkspace.vue` 的结构化澄清区渲染和 i18n 文案。
4. 补充 API 映射、composable 和组件测试，覆盖有默认值、无默认值、旧响应兼容和用户编辑后提交。
5. 回滚方式：忽略新增默认值字段，前端退回当前结构化澄清体验。

## Open Questions

- SELECT 类型是否默认高亮推荐项但不自动选中，还是直接预选为草稿值？本提案建议预填草稿，但提交必须由用户触发。
- `defaultConfidence` 是否需要在低置信度时隐藏“一键采用”？本提案建议低置信度仍可显示，但文案弱化。
