export type ConsoleRouteName = 'console-home' | 'console-workspace' | 'console-playground'

export type ConsoleNavId =
  | 'catalog-browse'
  | 'catalog-manage'
  | 'category-manage'
  | 'unified-access-playground'
  | 'credentials'
  | 'usage'
  | 'orders'
  | 'billing'
  | 'docs'

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
      {
        id: 'category-manage',
        labelKey: 'console.navigation.categoryManage',
        routeName: 'console-workspace',
        hash: '#category-manage',
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
        id: 'usage',
        labelKey: 'console.navigation.usage',
        routeName: 'console-workspace',
        hash: '#usage',
      },
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
    ],
  },
]

export const consoleTopUtilities: ConsoleTopUtility[] = [
  { id: 'new', labelKey: 'console.topbar.new' },
  { id: 'messages', labelKey: 'console.topbar.messages', badge: '2' },
  { id: 'docs', labelKey: 'console.topbar.docs' },
  { id: 'workorder', labelKey: 'console.topbar.workorder' },
  { id: 'usage', labelKey: 'console.topbar.usage' },
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
    id: 'category-manage',
    titleKey: 'console.workspace.categoryTitle',
    descriptionKey: 'console.workspace.categoryDescription',
  },
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
