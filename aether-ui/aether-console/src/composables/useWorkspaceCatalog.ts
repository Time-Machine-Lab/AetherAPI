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
  deleteAsset,
  getAsset,
  listAssets,
  publishAsset,
  registerAsset,
  reviseAsset,
  unpublishAsset,
} from '@/api/catalog/asset.api'
import type { ApiAsset, ApiAssetSummary, ApiCategory } from '@/api/catalog/catalog.types'
import type {
  BindAiProfileBody,
  ListAssetsQuery,
  RegisterAssetBody,
  ReviseAssetBody,
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
  reviseAsset: typeof reviseAsset
  publishAsset: typeof publishAsset
  unpublishAsset: typeof unpublishAsset
  deleteAsset: typeof deleteAsset
  bindAiProfile: typeof bindAiProfile
  getRecentAssets: typeof getRecentAssets
  onAssetSelected?: (asset: ApiAsset) => void | Promise<void>
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
    reviseAsset,
    publishAsset,
    unpublishAsset,
    deleteAsset,
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
    assetName: '',
    assetType: 'STANDARD_API',
  })

  const aiProfileForm = ref<BindAiProfileBody>({
    provider: '',
    model: '',
    streamingSupported: false,
    capabilityTags: [],
  })
  const assetConfigForm = ref<{
    displayName: string
    categoryCode: string
    requestMethod: NonNullable<ReviseAssetBody['requestMethod']> | ''
    upstreamUrl: string
    authScheme: string
    requestTemplate: string
    requestExample: string
    responseExample: string
  }>({
    displayName: '',
    categoryCode: '',
    requestMethod: '',
    upstreamUrl: '',
    authScheme: '',
    requestTemplate: '',
    requestExample: '',
    responseExample: '',
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

  function syncAssetConfigForm(asset: ApiAsset | null) {
    assetConfigForm.value = {
      displayName: asset?.displayName ?? '',
      categoryCode: asset?.categoryCode ?? '',
      requestMethod: asset?.requestMethod ?? '',
      upstreamUrl: asset?.upstreamUrl ?? '',
      authScheme: asset?.authScheme ?? '',
      requestTemplate: asset?.requestTemplate ?? '',
      requestExample: asset?.requestExample ?? '',
      responseExample: asset?.responseExample ?? '',
    }
  }

  async function setCurrentAsset(asset: ApiAsset, notifySelection = false) {
    currentAsset.value = asset
    syncAssetConfigForm(asset)
    if (notifySelection) {
      await deps.onAssetSelected?.(asset)
    }
  }

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
      await setCurrentAsset(await deps.getAsset(apiCode), true)
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
      await setCurrentAsset(await deps.getAsset(assetCodeInput.value.trim()), true)
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
      await setCurrentAsset(await deps.registerAsset(registerForm.value), true)
      registerForm.value = {
        apiCode: '',
        assetName: '',
        assetType: 'STANDARD_API',
      }
    } catch {
      assetError.value = deps.t('console.workspace.registerFailed')
    } finally {
      assetLoading.value = false
    }
  }

  async function handleToggleAsset() {
    if (!currentAsset.value) return
    assetLoading.value = true
    assetError.value = ''
    const isPublished = currentAsset.value.status === 'PUBLISHED'
    try {
      const updated = isPublished
        ? await deps.unpublishAsset(currentAsset.value.apiCode)
        : await deps.publishAsset(currentAsset.value.apiCode)
      await setCurrentAsset(updated)
      const idx = assetListItems.value.findIndex((item) => item.apiCode === updated.apiCode)
      if (idx !== -1) {
        assetListItems.value[idx] = {
          ...assetListItems.value[idx],
          status: updated.status,
          publisherDisplayName: updated.publisherDisplayName ?? null,
          publishedAt: updated.publishedAt ?? null,
          updatedAt: updated.updatedAt ?? assetListItems.value[idx].updatedAt,
        }
      }
    } catch {
      assetError.value = deps.t(
        isPublished ? 'console.workspace.assetUnpublishFailed' : 'console.workspace.assetPublishFailed',
      )
    } finally {
      assetLoading.value = false
    }
  }

  async function handleDeleteAsset() {
    if (!currentAsset.value) return
    assetLoading.value = true
    assetError.value = ''
    try {
      const deleted = await deps.deleteAsset(currentAsset.value.apiCode)
      assetListItems.value = assetListItems.value.filter((item) => item.apiCode !== deleted.apiCode)
      assetListTotal.value = Math.max(0, assetListTotal.value - 1)
      currentAsset.value = null
      syncAssetConfigForm(null)
    } catch {
      assetError.value = deps.t('console.workspace.assetDeleteFailed')
    } finally {
      assetLoading.value = false
    }
  }

  async function handleBindAiProfile() {
    if (!currentAsset.value) return
    await setCurrentAsset(await deps.bindAiProfile(currentAsset.value.apiCode, aiProfileForm.value))
  }

  function normalizeOptionalText(value: string) {
    const trimmed = value.trim()
    return trimmed ? trimmed : null
  }

  async function handleSaveAssetConfig() {
    if (!currentAsset.value) return
    assetLoading.value = true
    assetError.value = ''
    try {
      await setCurrentAsset(
        await deps.reviseAsset(currentAsset.value.apiCode, {
          displayName: normalizeOptionalText(assetConfigForm.value.displayName),
          categoryCode: normalizeOptionalText(assetConfigForm.value.categoryCode),
          requestMethod: assetConfigForm.value.requestMethod || null,
          upstreamUrl: normalizeOptionalText(assetConfigForm.value.upstreamUrl),
          authScheme: assetConfigForm.value.authScheme || null,
          requestTemplate: normalizeOptionalText(assetConfigForm.value.requestTemplate),
          requestExample: normalizeOptionalText(assetConfigForm.value.requestExample),
          responseExample: normalizeOptionalText(assetConfigForm.value.responseExample),
        }),
      )
    } catch {
      assetError.value = deps.t('console.workspace.assetConfigSaveFailed')
    } finally {
      assetLoading.value = false
    }
  }

  function addAiTag() {
    const tag = aiTagInput.value.trim()
    if (tag && !aiProfileForm.value.capabilityTags.includes(tag)) {
      aiProfileForm.value.capabilityTags.push(tag)
    }
    aiTagInput.value = ''
  }

  if (deps.autoLoad) {
    onMounted(() => {
      void loadCategories()
      void handleListAssets(1)
    })
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
    assetConfigForm,
    aiProfileForm,
    aiTagInput,
    handleLoadAsset,
    handleRegisterAsset,
    handleSaveAssetConfig,
    handleToggleAsset,
    handleDeleteAsset,
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
