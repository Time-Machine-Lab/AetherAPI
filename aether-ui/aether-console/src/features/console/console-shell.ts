export type ConsoleRouteName = 'console-home' | 'console-workspace'

export type ConsoleNavId =
  | 'overview'
  | 'marketplace'
  | 'agents'
  | 'credentials'
  | 'usage'
  | 'orders'
  | 'billing'
  | 'docs'

export type ConsoleCardFlag = 'new' | 'free' | 'vision' | 'reasoning'

export interface ConsoleSidebarItem {
  id: ConsoleNavId
  labelKey: string
  routeName: ConsoleRouteName
  hash?: string
  badge?: string
}

export interface ConsoleSidebarGroup {
  id: string
  titleKey: string
  items: ConsoleSidebarItem[]
}

export interface ConsoleTopUtility {
  id: string
  labelKey: string
  badge?: string
}

export interface ConsoleNotice {
  id: string
  labelKey: string
  tone: 'info' | 'success'
}

export interface ConsoleFilter {
  id: string
  labelKey: string
}

export interface ConsoleMarketplaceMetric {
  id: string
  labelKey: string
  value: string
  hintKey: string
}

export interface ConsoleMarketplaceCard {
  id: string
  name: string
  vendor: string
  description: string
  status: string
  tags: string[]
  flags: ConsoleCardFlag[]
  priceInput: string
  priceOutput: string
}

export interface ConsoleWorkspacePanel {
  id: string
  titleKey: string
  descriptionKey: string
  metric: string
  statusKey: string
}

export interface ConsoleTimelineItem {
  id: string
  titleKey: string
  descriptionKey: string
}

export const consoleSidebarGroups: ConsoleSidebarGroup[] = [
  {
    id: 'model-suite',
    titleKey: 'console.sidebar.modelSuite',
    items: [
      {
        id: 'marketplace',
        labelKey: 'console.navigation.marketplace',
        routeName: 'console-home',
      },
      {
        id: 'agents',
        labelKey: 'console.navigation.agents',
        routeName: 'console-workspace',
        hash: '#agents',
        badge: 'Beta',
      },
      {
        id: 'credentials',
        labelKey: 'console.navigation.credentials',
        routeName: 'console-workspace',
        hash: '#credentials',
      },
      {
        id: 'usage',
        labelKey: 'console.navigation.usage',
        routeName: 'console-workspace',
        hash: '#usage',
      },
    ],
  },
  {
    id: 'operations',
    titleKey: 'console.sidebar.operations',
    items: [
      {
        id: 'orders',
        labelKey: 'console.navigation.orders',
        routeName: 'console-workspace',
        hash: '#orders',
      },
      {
        id: 'billing',
        labelKey: 'console.navigation.billing',
        routeName: 'console-workspace',
        hash: '#billing',
      },
      {
        id: 'docs',
        labelKey: 'console.navigation.docs',
        routeName: 'console-workspace',
        hash: '#docs',
      },
      {
        id: 'overview',
        labelKey: 'console.navigation.overview',
        routeName: 'console-workspace',
      },
    ],
  },
]

export const consoleTopUtilities: ConsoleTopUtility[] = [
  { id: 'new', labelKey: 'console.topbar.new' },
  { id: 'messages', labelKey: 'console.topbar.messages', badge: '2' },
  { id: 'docs', labelKey: 'console.topbar.docs' },
  { id: 'workorder', labelKey: 'console.topbar.workorder' },
  { id: 'usage', labelKey: 'console.topbar.usage' },
  { id: 'cloud', labelKey: 'console.topbar.cloud' },
]

export const consoleNotices: ConsoleNotice[] = [
  {
    id: 'token-campaign',
    labelKey: 'console.notices.tokenCampaign',
    tone: 'info',
  },
  {
    id: 'agent-trial',
    labelKey: 'console.notices.agentTrial',
    tone: 'success',
  },
]

export const consoleMarketplaceFilters: ConsoleFilter[] = [
  { id: 'all', labelKey: 'console.filters.all' },
  { id: 'new', labelKey: 'console.filters.new' },
  { id: 'free', labelKey: 'console.filters.free' },
  { id: 'vision', labelKey: 'console.filters.vision' },
  { id: 'reasoning', labelKey: 'console.filters.reasoning' },
]

export const consoleMarketplaceMetrics: ConsoleMarketplaceMetric[] = [
  {
    id: 'models',
    labelKey: 'console.metrics.models',
    value: '128',
    hintKey: 'console.metricsHints.models',
  },
  {
    id: 'vendors',
    labelKey: 'console.metrics.vendors',
    value: '23',
    hintKey: 'console.metricsHints.vendors',
  },
  {
    id: 'new',
    labelKey: 'console.metrics.new',
    value: '12',
    hintKey: 'console.metricsHints.new',
  },
]

export const consoleMarketplaceCards: ConsoleMarketplaceCard[] = [
  {
    id: 'deepseek-v3-2',
    name: 'DeepSeek/DeepSeek-V3.2',
    vendor: 'DeepSeek',
    description: '适用于 Agent 和推理型工作流，兼顾速度、成本与通用文本能力。',
    status: '上新',
    tags: ['工具调用', '推理增强'],
    flags: ['new', 'reasoning'],
    priceInput: '0.0027 元 / K',
    priceOutput: '0.0037 元 / K',
  },
  {
    id: 'moonshot-kimi-k2-5',
    name: 'Moonshot/Kimi-K2.5',
    vendor: 'Moonshot',
    description: '覆盖长上下文、Agent 和代码场景，适合作为综合型旗舰模型。',
    status: '上新',
    tags: ['推理增强', '长上下文'],
    flags: ['new', 'vision', 'reasoning'],
    priceInput: '0.0040 元 / K',
    priceOutput: '0.0210 元 / K',
  },
  {
    id: 'minimax-m2-5',
    name: 'Minimax/Minimax-M2.5',
    vendor: 'Minimax',
    description: '面向 Agent 与多步骤协作流程，适合代码、分析和自动化任务。',
    status: '上新',
    tags: ['AI 编程', 'Agent'],
    flags: ['new', 'reasoning'],
    priceInput: '0.0021 元 / K',
    priceOutput: '0.0084 元 / K',
  },
  {
    id: 'zai-glm-5',
    name: 'Z-AI/GLM 5',
    vendor: 'Z-AI',
    description: '工具调用与通用推理兼备，适合从轻量问答到复杂编排的多类场景。',
    status: '上新',
    tags: ['工具调用'],
    flags: ['new'],
    priceInput: '0.0047 元 / K',
    priceOutput: '0.0187 元 / K',
  },
  {
    id: 'minimax-m1',
    name: 'Minimax/Minimax-M1',
    vendor: 'Minimax',
    description: '上一代旗舰模型，适合语义理解、代写和中等规模自动化任务。',
    status: '热门',
    tags: ['工具调用', '文本'],
    flags: [],
    priceInput: '0.0021 元 / K',
    priceOutput: '0.0084 元 / K',
  },
  {
    id: 'xiaomi-v2-flash',
    name: 'Xiaomi/Mimo-V2-Flash',
    vendor: 'Xiaomi',
    description: '高性价比轻量模型，适合移动端与高并发响应场景的接入。',
    status: '上新',
    tags: ['轻量', '高并发'],
    flags: ['new'],
    priceInput: '0.0007 元 / K',
    priceOutput: '0.0021 元 / K',
  },
  {
    id: 'kimi-k2-thinking',
    name: 'Kimi K2 Thinking',
    vendor: 'Moonshot',
    description: '偏推理链路和多轮拆解，适合复杂任务分步执行与分析型 Agent。',
    status: '热门',
    tags: ['推理增强', 'Agent'],
    flags: ['reasoning'],
    priceInput: '0.0040 元 / K',
    priceOutput: '0.0160 元 / K',
  },
  {
    id: 'longcat-flash-lite',
    name: 'Longcat-Flash-Lite',
    vendor: 'Meituan',
    description: '面向轻量场景的高响应模型，适合 FAQ、流程咨询与基础编排。',
    status: '限时免费',
    tags: ['轻量', '问答'],
    flags: ['free'],
    priceInput: '0.0000 元 / K',
    priceOutput: '0.0000 元 / K',
  },
  {
    id: 'baidu-search-api',
    name: 'Baidu Search API',
    vendor: 'Baidu',
    description: '搜索增强接口，适合需要联网检索与引用外部资料的工作流。',
    status: '工具',
    tags: ['搜索增强'],
    flags: ['vision'],
    priceInput: '按次',
    priceOutput: '0.036 元 / 次',
  },
  {
    id: 'gpt-oss-20b',
    name: 'gpt-oss-20b',
    vendor: 'OpenAI OSS',
    description: '适合开源私有化部署场景，强调基础对话、工具调度和轻量推理。',
    status: '开源',
    tags: ['开源', '工具调用'],
    flags: [],
    priceInput: '0.0027 元 / K',
    priceOutput: '0.0036 元 / K',
  },
]

export const consoleWorkspacePanels: ConsoleWorkspacePanel[] = [
  {
    id: 'agents',
    titleKey: 'console.panels.agents.title',
    descriptionKey: 'console.panels.agents.description',
    metric: '8 条工作流模板',
    statusKey: 'console.panelStatus.beta',
  },
  {
    id: 'credentials',
    titleKey: 'console.panels.credentials.title',
    descriptionKey: 'console.panels.credentials.description',
    metric: '14 个活动密钥',
    statusKey: 'console.panelStatus.ready',
  },
  {
    id: 'usage',
    titleKey: 'console.panels.usage.title',
    descriptionKey: 'console.panels.usage.description',
    metric: '24h 调用 82.4 万次',
    statusKey: 'console.panelStatus.stable',
  },
  {
    id: 'orders',
    titleKey: 'console.panels.orders.title',
    descriptionKey: 'console.panels.orders.description',
    metric: '6 笔待处理结算',
    statusKey: 'console.panelStatus.attention',
  },
  {
    id: 'billing',
    titleKey: 'console.panels.billing.title',
    descriptionKey: 'console.panels.billing.description',
    metric: '3 套价格策略草稿',
    statusKey: 'console.panelStatus.planned',
  },
  {
    id: 'docs',
    titleKey: 'console.panels.docs.title',
    descriptionKey: 'console.panels.docs.description',
    metric: '12 份文档待发布',
    statusKey: 'console.panelStatus.inProgress',
  },
]

export const consoleTimeline: ConsoleTimelineItem[] = [
  {
    id: 'launch',
    titleKey: 'console.timeline.launch.title',
    descriptionKey: 'console.timeline.launch.description',
  },
  {
    id: 'pricing',
    titleKey: 'console.timeline.pricing.title',
    descriptionKey: 'console.timeline.pricing.description',
  },
  {
    id: 'docs',
    titleKey: 'console.timeline.docs.title',
    descriptionKey: 'console.timeline.docs.description',
  },
]

export function summarizeConsoleSkeleton() {
  const sidebarItemCount = consoleSidebarGroups.reduce(
    (total, group) => total + group.items.length,
    0,
  )

  return {
    navIds: consoleSidebarGroups.flatMap((group) => group.items.map((item) => item.id)),
    moduleIds: consoleWorkspacePanels.map((item) => item.id),
    quickActionIds: consoleTopUtilities.map((item) => item.id),
    readyCount: consoleWorkspacePanels.length,
    plannedCount: consoleTimeline.length,
    sidebarGroupCount: consoleSidebarGroups.length,
    sidebarItemCount,
    topUtilityCount: consoleTopUtilities.length,
    marketplaceCardCount: consoleMarketplaceCards.length,
  }
}
