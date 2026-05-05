export type ConsoleRouteName = 'console-home' | 'console-workspace' | 'console-playground'

export type ConsoleVisibleNavId =
  | 'catalog-browse'
  | 'catalog-manage'
  | 'unified-access-playground'
  | 'credentials'
  | 'api-subscriptions'
  | 'api-call-logs'
export type ConsoleHiddenNavId = 'category-manage' | 'usage' | 'orders' | 'billing' | 'docs'
export type ConsoleNavId = ConsoleVisibleNavId | ConsoleHiddenNavId

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

export interface ConsoleWorkspacePanel {
  id: string
  titleKey: string
  descriptionKey: string
}

export const hiddenConsoleNavIds = [
  'category-manage',
  'usage',
  'orders',
  'billing',
  'docs',
] as const

const visibleConsoleWorkspaceNavIds = [
  'catalog-manage',
  'credentials',
  'api-subscriptions',
  'api-call-logs',
] as const

export const defaultConsoleWorkspaceHash = '#catalog-manage'

export function isHiddenConsoleNavId(navId: string): navId is ConsoleHiddenNavId {
  return hiddenConsoleNavIds.includes(navId as ConsoleHiddenNavId)
}

export function normalizeConsoleWorkspaceNavId(hash: string): ConsoleVisibleNavId {
  const navId = hash.replace('#', '')

  if (
    visibleConsoleWorkspaceNavIds.includes(navId as (typeof visibleConsoleWorkspaceNavIds)[number])
  ) {
    return navId as ConsoleVisibleNavId
  }

  return 'catalog-manage'
}

export const consoleSidebarGroups: ConsoleSidebarGroup[] = [
  {
    id: 'api-catalog',
    titleKey: 'console.sidebar.apiCatalog',
    items: [
      {
        id: 'catalog-browse',
        labelKey: 'console.navigation.catalogBrowse',
        routeName: 'console-home',
      },
      {
        id: 'catalog-manage',
        labelKey: 'console.navigation.catalogManage',
        routeName: 'console-workspace',
        hash: '#catalog-manage',
      },
    ],
  },
  {
    id: 'unified-access',
    titleKey: 'console.sidebar.unifiedAccess',
    items: [
      {
        id: 'unified-access-playground',
        labelKey: 'console.navigation.playground',
        routeName: 'console-playground',
      },
    ],
  },
  {
    id: 'operations',
    titleKey: 'console.sidebar.operations',
    items: [
      {
        id: 'credentials',
        labelKey: 'console.navigation.credentials',
        routeName: 'console-workspace',
        hash: '#credentials',
      },
      {
        id: 'api-subscriptions',
        labelKey: 'console.navigation.apiSubscriptions',
        routeName: 'console-workspace',
        hash: '#api-subscriptions',
      },
      {
        id: 'api-call-logs',
        labelKey: 'console.navigation.apiCallLogs',
        routeName: 'console-workspace',
        hash: '#api-call-logs',
      },
    ],
  },
]

export const consoleTopUtilities: ConsoleTopUtility[] = [
  { id: 'new', labelKey: 'console.topbar.new' },
  { id: 'messages', labelKey: 'console.topbar.messages', badge: '2' },
  { id: 'workorder', labelKey: 'console.topbar.workorder' },
]

export const consoleNotices: ConsoleNotice[] = [
  {
    id: 'catalog-launch',
    labelKey: 'console.notices.catalogLaunch',
    tone: 'info',
  },
]

export const consoleWorkspacePanels: ConsoleWorkspacePanel[] = [
  {
    id: 'asset-manage',
    titleKey: 'console.workspace.assetTitle',
    descriptionKey: 'console.workspace.assetDescription',
  },
  {
    id: 'recent-assets',
    titleKey: 'console.workspace.recentTitle',
    descriptionKey: 'console.workspace.recentNote',
  },
]
