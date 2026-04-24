import { onMounted, ref } from 'vue'
import {
  createCategory,
  disableCategory,
  enableCategory,
  listCategories,
  renameCategory,
} from '@/api/catalog/category.api'
import {
  bindAiProfile,
  disableAsset,
  enableAsset,
  getAsset,
  listAssets,
  registerAsset,
} from '@/api/catalog/asset.api'
import type { ApiAsset, ApiAssetSummary, ApiCategory } from '@/api/catalog/catalog.types'
import type {
  BindAiProfileBody,
  ListAssetsQuery,
  RegisterAssetBody,
} from '@/api/catalog/catalog.dto'
import { getRecentAssets } from '@/features/catalog/catalog-helpers'

interface WorkspaceCatalogDeps {
  t: (key: string) => string
  listCategories: typeof listCategories
  createCategory: typeof createCategory
  renameCategory: typeof renameCategory
  enableCategory: typeof enableCategory
  disableCategory: typeof disableCategory
  getAsset: typeof getAsset
  listAssets: typeof listAssets
  registerAsset: typeof registerAsset
  enableAsset: typeof enableAsset
  disableAsset: typeof disableAsset
  bindAiProfile: typeof bindAiProfile
  getRecentAssets: typeof getRecentAssets
  autoLoad?: boolean
}

type WorkspaceCatalogOptions = Partial<Omit<WorkspaceCatalogDeps, 't'>> &
  Pick<WorkspaceCatalogDeps, 't'>

function buildDeps(options: WorkspaceCatalogOptions): WorkspaceCatalogDeps {
  return {
    listCategories,
    createCategory,
    renameCategory,
    enableCategory,
    disableCategory,
    getAsset,
    listAssets,
    registerAsset,
    enableAsset,
    disableAsset,
    bindAiProfile,
    getRecentAssets,
    autoLoad: true,
    ...options,
  }
}

export function useWorkspaceCatalog(options: WorkspaceCatalogOptions) {
  const deps = buildDeps(options)

  const categories = ref<ApiCategory[]>([])
  const catLoading = ref(false)
  const catError = ref(false)
  const newCatName = ref('')
  const renamingCat = ref<string | null>(null)
  const renameValue = ref('')

  const assetCodeInput = ref('')
  const currentAsset = ref<ApiAsset | null>(null)
  const assetLoading = ref(false)
  const assetError = ref('')

  const registerForm = ref<RegisterAssetBody>({
    apiCode: '',
    displayName: '',
    assetType: 'STANDARD_API',
    categoryCode: '',
  })

  const aiProfileForm = ref<BindAiProfileBody>({
    provider: '',
    model: '',
    streaming: false,
    tags: [],
  })
  const aiTagInput = ref('')
  const recentAssets = ref(deps.getRecentAssets())

  // ── Asset list ──────────────────────────────────────────────────────────────
  const assetListItems = ref<ApiAssetSummary[]>([])
  const assetListTotal = ref(0)
  const assetListPage = ref(1)
  const assetListPageSize = 20
  const assetListLoading = ref(false)
  const assetListError = ref(false)
  const assetListFilterKeyword = ref('')
  const assetListFilterStatus = ref<ListAssetsQuery['status'] | ''>('')
  const assetListFilterCategory = ref('')

  async function handleListAssets(page = 1) {
    assetListLoading.value = true
    assetListError.value = false
    try {
      const query: ListAssetsQuery = {
        page,
        size: assetListPageSize,
        ...(assetListFilterKeyword.value.trim() && {
          keyword: assetListFilterKeyword.value.trim(),
        }),
        ...(assetListFilterStatus.value && { status: assetListFilterStatus.value }),
        ...(assetListFilterCategory.value.trim() && {
          categoryCode: assetListFilterCategory.value.trim(),
        }),
      }
      const result = await deps.listAssets(query)
      assetListItems.value = result.items
      assetListTotal.value = result.total
      assetListPage.value = result.page
    } catch {
      assetListError.value = true
    } finally {
      assetListLoading.value = false
    }
  }

  async function handleListSelectAsset(apiCode: string) {
    assetLoading.value = true
    assetError.value = ''
    try {
      currentAsset.value = await deps.getAsset(apiCode)
    } catch {
      assetError.value = deps.t('console.workspace.assetNotFound')
    } finally {
      assetLoading.value = false
    }
  }

  function assetListTotalPages() {
    return Math.max(1, Math.ceil(assetListTotal.value / assetListPageSize))
  }

  async function loadCategories() {
    catLoading.value = true
    catError.value = false
    try {
      const result = await deps.listCategories()
      categories.value = result.items
    } catch {
      catError.value = true
    } finally {
      catLoading.value = false
    }
  }

  async function handleCreateCategory() {
    if (!newCatName.value.trim()) return
    const cat = await deps.createCategory({ name: newCatName.value.trim() })
    categories.value.unshift(cat)
    newCatName.value = ''
  }

  async function handleRenameCategory(code: string) {
    if (!renameValue.value.trim()) return
    const updated = await deps.renameCategory(code, { name: renameValue.value.trim() })
    const idx = categories.value.findIndex((c) => c.categoryCode === code)
    if (idx !== -1) categories.value[idx] = updated
    renamingCat.value = null
  }

  async function handleToggleCategory(cat: ApiCategory) {
    const updated =
      cat.status === 'ENABLED'
        ? await deps.disableCategory(cat.categoryCode)
        : await deps.enableCategory(cat.categoryCode)
    const idx = categories.value.findIndex((c) => c.categoryCode === cat.categoryCode)
    if (idx !== -1) categories.value[idx] = updated
  }

  async function handleLoadAsset() {
    if (!assetCodeInput.value.trim()) return
    assetLoading.value = true
    assetError.value = ''
    try {
      currentAsset.value = await deps.getAsset(assetCodeInput.value.trim())
    } catch {
      assetError.value = deps.t('console.workspace.assetNotFound')
    } finally {
      assetLoading.value = false
    }
  }

  async function handleRegisterAsset() {
    assetLoading.value = true
    assetError.value = ''
    try {
      currentAsset.value = await deps.registerAsset(registerForm.value)
      registerForm.value = {
        apiCode: '',
        displayName: '',
        assetType: 'STANDARD_API',
        categoryCode: '',
      }
    } catch {
      assetError.value = deps.t('console.workspace.registerFailed')
    } finally {
      assetLoading.value = false
    }
  }

  async function handleToggleAsset() {
    if (!currentAsset.value) return
    const updated =
      currentAsset.value.status === 'ENABLED'
        ? await deps.disableAsset(currentAsset.value.apiCode)
        : await deps.enableAsset(currentAsset.value.apiCode)
    currentAsset.value = updated
  }

  async function handleBindAiProfile() {
    if (!currentAsset.value) return
    currentAsset.value = await deps.bindAiProfile(currentAsset.value.apiCode, aiProfileForm.value)
  }

  function addAiTag() {
    const tag = aiTagInput.value.trim()
    if (tag && !aiProfileForm.value.tags.includes(tag)) {
      aiProfileForm.value.tags.push(tag)
    }
    aiTagInput.value = ''
  }

  if (deps.autoLoad) {
    onMounted(loadCategories)
  }

  return {
    categories,
    catLoading,
    catError,
    newCatName,
    renamingCat,
    renameValue,
    loadCategories,
    handleCreateCategory,
    handleRenameCategory,
    handleToggleCategory,
    assetCodeInput,
    currentAsset,
    assetLoading,
    assetError,
    registerForm,
    aiProfileForm,
    aiTagInput,
    handleLoadAsset,
    handleRegisterAsset,
    handleToggleAsset,
    handleBindAiProfile,
    addAiTag,
    recentAssets,
    assetListItems,
    assetListTotal,
    assetListPage,
    assetListPageSize,
    assetListLoading,
    assetListError,
    assetListFilterKeyword,
    assetListFilterStatus,
    assetListFilterCategory,
    handleListAssets,
    handleListSelectAsset,
    assetListTotalPages,
  }
}
