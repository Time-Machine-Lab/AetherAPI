## Why

`aether-console` 已经接入 API Catalog 的核心页面，但当前视觉呈现仍停留在“可用骨架”阶段：状态 badge、次级按钮、公告条、卡片和输入框之间缺少清晰的语义分层，导致用户难以快速区分“信息展示”“状态反馈”和“可执行操作”。在 API 市场页与 API 资产管理页中，这种语义混叠进一步放大为布局对齐不稳、控件高度不一致、焦点与 hover 反馈过弱、页面重心失衡等问题，削弱了控制台应有的专业感与可操作性。

现在提出这项变更，是为了在不改动现有 API 契约和整体产品边界的前提下，补齐 `aether-console` 的视觉层级与交互细节规范，让它真正符合 `aether-console/DESIGN.md` 中既定的 Airbnb 风格浏览体验，同时形成一套可复用的控制台组件语义。

## What Changes

- 梳理 `aether-console` 中按钮、badge/tag、状态开关、公告条、输入框、卡片等高频组件的语义分工，明确不同信息层与操作层的外观差异、边框策略、颜色角色、焦点反馈与 hover 反馈。
- 重构 API 市场页的页面层级，修正顶部全局搜索、页面内搜索、资产卡片和详情面板之间的视觉重心冲突，消除局部区域“凸出来”或权重失衡的问题。
- 优化 API 资产管理页和分类管理页的布局节奏，统一输入框、按钮、列表项和编辑态表单的高度、对齐线和操作区密度，修复重命名输入框过矮、行内编辑突兀的问题。
- 将顶部公告区域从“复用普通胶囊组件”调整为具备公告感知的独立样式，补足图标、背景、边框/强调色和信息层级，让系统消息与普通筛选/状态元素不再混淆。
- 补充空状态、选中态、禁用态和局部错误态的视觉规范，增强工作台在浏览、筛选、选择、编辑和切换状态时的可感知反馈。
- 在实施前同步更新目标应用的 `aether-console/DESIGN.md`，把本次新增的组件语义、公告样式、布局对齐与交互反馈规则沉淀为该应用的权威设计约束。
- 将分类列表项左侧状态装饰条从 `border-l`（跟随 `border-radius` 弯曲）替换为绝对定位的 `::before` 内元素，实现直线色条视觉。
- 重构 API 资产"注册新资产"表单的布局节奏：`apiCode` / `categoryCode` 双列排布、`displayName` 独占一行、原生 `<select>` 统一为 Input 等高风格、提交按钮右对齐、整体间距从 `space-y-2` 放大至 `space-y-3`。
- 为最近访问的资产列表项补充方向箭头指示器 `→`、hover 上浮（`-translate-y-px`）和 active 按压（`scale-[0.995]`），提供明确的可点击暗示。
- 在按钮基类中增加 `cursor-pointer`，确保所有 Button 在 hover 时显示手形光标；分类列表项和最近访问列表项同步补充 hover 上浮微动效和 `transition-[box-shadow,transform]` 过渡声明。
- 新增 `type-ai` 和 `type-api` 两个 Badge 变体，分别使用紫色（`chart-3`）和品牌红区分 AI 类资产与标准 API 资产，替换市场页卡片中统一使用 `status-enabled` 的做法。
- 增强市场页资产卡片的选中态：从 `ring-primary/30` 提升为 `ring-primary/40 border-primary/25 shadow-console-hover`，在白底上形成明确的视觉锚点。
- 为市场页资产卡片补充 `hover:-translate-y-px` 上浮微动效和 `transition-[box-shadow,transform]` 过渡，与工作台列表项交互质感一致。
- 降低页面内搜索框的视觉层级：去掉外层胶囊容器的 `shadow-console` 和 `bg-white`，改为 subtle 边框样式，focus 时再提升阴影，与 shell 搜索拉开主次。
- 详情面板内容区包裹 `<Transition name="fade" mode="out-in">`，切换资产时带 180ms 淡入淡出过渡，消除内容硬切感。
- 详情面板空状态从纯文字改为 `MousePointerClick` 图标 + 提示文案的上下布局，提供更明确的操作引导。
- 请求方法 pill（如 `POST /chat/completions`）改为方角 `rounded-[8px]` + 细边框 + `font-mono`，与 AI 能力标签（`rounded-full` + 紫色文字 + `shadow-console`）在形态和色彩上形成区分。
- 代码示例 `<pre>` 块从 `bg-secondary` 改为 `bg-[#fafafa]` + 细边框 + `leading-5`，与上方灰色信息区块拉开差异。

## Capabilities

### New Capabilities
- `console-visual-hierarchy`: 规范 API 市场页与工作台页的布局层级、搜索区权重、卡片与详情面板关系、表单对齐和空状态呈现。
- `console-component-semantics`: 规范按钮、badge/tag、状态标识、公告条、输入框和行内编辑态的语义外观、颜色分工与交互反馈。

### Modified Capabilities

无。

## Impact

- 受影响应用：`aether-console`
- 受影响区域：`src/layouts/ConsoleLayout.vue`、`src/pages/index.vue`、`src/pages/workspace.vue`、`src/style.css`、`src/components/ui/{button,badge,input,card}` 及相关组合样式
- 受影响文档：`aether-console/DESIGN.md` 需要先同步补充组件语义与页面层级规则，再进入实现
- 后端与 API 契约：`docs/api/*.yaml` 无需变更
- 风险与边界：本提案只调整视觉层级、交互反馈和页面编排，不改变既有业务流程、路由边界或鉴权方式
- 组件级变更：`buttonVariants` 基类新增 `cursor-pointer`；`badgeVariants` 中 `status-enabled` / `status-disabled` 保持 `cursor-default` 以区分非操作元素
