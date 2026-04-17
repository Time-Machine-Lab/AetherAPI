## 1. Design Authority Sync

- [x] 1.1 更新 `aether-console/DESIGN.md`，补充控制台语义角色、公告 banner、搜索层级、表单对齐与状态反馈规则
- [x] 1.2 基于更新后的 `DESIGN.md` 复核本次变更边界，确认无需变更 `docs/api/*.yaml` 和其他顶层契约文档

## 2. Semantic Component Foundation

- [x] 2.1 调整 `src/components/ui/button`、`src/components/ui/badge`、`src/components/ui/input` 及相关 surface 样式，使 action/status/field 语义具备清晰区分
- [x] 2.2 在 `src/layouts/ConsoleLayout.vue` 和相关 shell 配置中引入独立的 notice banner 表现，弱化 shell 搜索的视觉权重并保留其功能入口

## 3. Page Hierarchy Refresh

- [x] 3.1 重构 `src/pages/index.vue` 的市场页层级，统一页面搜索、资产卡片、详情面板和空/错/加载状态的对齐与权重
- [x] 3.2 重构 `src/pages/workspace.vue` 的工作台层级，统一分类创建、资产查询、行内重命名、状态切换和最近访问列表的高度与基线

## 4. Verification

- [x] 4.1 同步调整必要的文案或 i18n 资源，确保公告、辅助说明和状态语义与新的视觉分层一致
- [x] 4.2 在桌面端和窄屏布局下验证按钮、badge、公告、输入框和页面对齐表现，确认提案中的关键问题已被覆盖

## 5. Workspace Visual & Interaction Refinement

- [x] 5.1 将分类列表项左侧装饰条从 `border-l`（随圆角弯曲）改为绝对定位内元素直线色条
- [x] 5.2 重构 API 资产"注册新资产"表单布局：apiCode/categoryCode 双列排布、displayName 独占一行、select 统一 Input 风格、注册按钮右对齐、间距放大至 `space-y-3`
- [x] 5.3 为最近访问列表项补充 `→` 箭头指示器、hover 上浮动效和 active 按压感
- [x] 5.4 分类列表项增加 hover 上浮 (`-translate-y-px`) 和统一 `transition-[box-shadow,transform]` 过渡
- [x] 5.5 按钮基类新增 `cursor-pointer`，确保所有 Button hover 时显示手形光标
- [x] 5.6 原生 `<select>` 统一为与 Input 组件一致的高度、边框、圆角和 focus 高亮样式

## 6. Marketplace Visual & Interaction Refinement

- [x] 6.1 新增 `type-ai`（紫色）和 `type-api`（品牌红）Badge 变体，替换市场页卡片中统一使用的 `status-enabled`
- [x] 6.2 增强资产卡片选中态高亮：`ring-primary/40 border-primary/25 shadow-console-hover`
- [x] 6.3 为资产卡片补充 `hover:-translate-y-px` 上浮微动效和 `transition-[box-shadow,transform]` 过渡
- [x] 6.4 降低页面内搜索框视觉层级：去掉外层胶囊 `shadow-console`，改为 subtle 边框，focus 时提升阴影
- [x] 6.5 详情面板内容区包裹 `<Transition name="fade" mode="out-in">`，180ms 淡入淡出
- [x] 6.6 详情面板空状态改为 `MousePointerClick` 图标 + 提示文案的上下布局
- [x] 6.7 请求方法 pill 改为方角 `rounded-[8px]` + 细边框 + `font-mono`，与 AI 能力标签形态区分
- [x] 6.8 AI 能力标签使用紫色文字 `text-[var(--chart-3)]`，与请求方法 pill 在色彩上区分
- [x] 6.9 代码示例 `<pre>` 块改为 `bg-[#fafafa]` + 细边框 + `leading-5`，与灰色信息区块拉开差异
- [x] 6.10 卡片网格区添加 `min-h-[200px]`，减少少量卡片时下方空白的失衡感
