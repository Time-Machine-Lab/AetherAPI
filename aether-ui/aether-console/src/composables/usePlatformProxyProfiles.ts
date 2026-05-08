import { computed, onMounted, ref, unref, type Ref } from 'vue'
import {
  bindProxyProfileToAsset,
  createPlatformProxyProfile,
  deletePlatformProxyProfile,
  disablePlatformProxyProfile,
  enablePlatformProxyProfile,
  getPlatformProxyProfile,
  listPlatformProxyProfiles,
  unbindProxyProfileFromAsset,
  updatePlatformProxyProfile,
} from '@/api/platform-proxy-profile/platform-proxy-profile.api'
import type {
  AssetProxyBinding,
  ListPlatformProxyProfilesQuery,
  PlatformProxyProfile,
  SavePlatformProxyProfileBody,
} from '@/api/platform-proxy-profile/platform-proxy-profile.types'
import { isPlatformAdminRole } from '@/features/console/platform-admin'

interface PlatformProxyDeps {
  t: (key: string) => string
  currentUserRole?: string | null | Ref<string | null | undefined>
  isPlatformAdminRole: typeof isPlatformAdminRole
  listPlatformProxyProfiles: typeof listPlatformProxyProfiles
  getPlatformProxyProfile: typeof getPlatformProxyProfile
  createPlatformProxyProfile: typeof createPlatformProxyProfile
  updatePlatformProxyProfile: typeof updatePlatformProxyProfile
  enablePlatformProxyProfile: typeof enablePlatformProxyProfile
  disablePlatformProxyProfile: typeof disablePlatformProxyProfile
  deletePlatformProxyProfile: typeof deletePlatformProxyProfile
  bindProxyProfileToAsset: typeof bindProxyProfileToAsset
  unbindProxyProfileFromAsset: typeof unbindProxyProfileFromAsset
  autoLoad?: boolean
}

type PlatformProxyOptions = Partial<Omit<PlatformProxyDeps, 't'>> & Pick<PlatformProxyDeps, 't'>
type EnabledFilter = '' | 'enabled' | 'disabled'

function buildDeps(options: PlatformProxyOptions): PlatformProxyDeps {
  return {
    isPlatformAdminRole,
    listPlatformProxyProfiles,
    getPlatformProxyProfile,
    createPlatformProxyProfile,
    updatePlatformProxyProfile,
    enablePlatformProxyProfile,
    disablePlatformProxyProfile,
    deletePlatformProxyProfile,
    bindProxyProfileToAsset,
    unbindProxyProfileFromAsset,
    autoLoad: true,
    ...options,
  }
}

function normalizeOptionalText(value: string) {
  const trimmed = value.trim()
  return trimmed ? trimmed : null
}

function errorStatus(error: unknown) {
  return typeof error === 'object' && error !== null && 'status' in error
    ? Number((error as { status?: number }).status)
    : 0
}

export function usePlatformProxyProfiles(options: PlatformProxyOptions) {
  const deps = buildDeps(options)
  const canUsePlatformProxy = computed(() => deps.isPlatformAdminRole(unref(deps.currentUserRole)))

  const profiles = ref<PlatformProxyProfile[]>([])
  const listLoading = ref(false)
  const listError = ref(false)
  const accessDenied = ref(false)
  const filterKeyword = ref('')
  const filterEnabled = ref<EnabledFilter>('')
  const page = ref(1)
  const pageSize = 20
  const total = ref(0)

  const selectedProfile = ref<PlatformProxyProfile | null>(null)
  const detailLoading = ref(false)
  const operationLoading = ref(false)
  const operationError = ref('')

  const formOpen = ref(false)
  const formMode = ref<'create' | 'edit'>('create')
  const profileForm = ref({
    id: '',
    profileCode: '',
    profileName: '',
    proxyType: 'HTTP' as const,
    proxyHost: '',
    proxyPort: 8080,
    username: '',
    password: '',
    enabled: true,
  })

  const bindingApiCode = ref('')
  const bindingProfileId = ref('')
  const bindingResult = ref<AssetProxyBinding | null>(null)
  const bindingLoading = ref(false)
  const bindingError = ref('')

  const enabledProfiles = computed(() =>
    profiles.value.filter((profile) => profile.enabled && !profile.deleted),
  )

  const selectedBindingProfile = computed(
    () => profiles.value.find((profile) => profile.id === bindingProfileId.value) ?? null,
  )

  const canBind = computed(() => {
    const profile = selectedBindingProfile.value
    return (
      canUsePlatformProxy.value &&
      Boolean(bindingApiCode.value.trim()) &&
      Boolean(profile?.enabled) &&
      !profile?.deleted
    )
  })

  function handleAccessError(error: unknown) {
    if (errorStatus(error) === 401 || errorStatus(error) === 403) {
      accessDenied.value = true
    }
  }

  function totalPages() {
    return Math.max(1, Math.ceil(total.value / pageSize))
  }

  function buildListQuery(nextPage: number): ListPlatformProxyProfilesQuery {
    return {
      page: nextPage,
      size: pageSize,
      ...(filterKeyword.value.trim() && { keyword: filterKeyword.value.trim() }),
      ...(filterEnabled.value === 'enabled' && { enabled: true }),
      ...(filterEnabled.value === 'disabled' && { enabled: false }),
    }
  }

  function replaceProfile(updated: PlatformProxyProfile) {
    const index = profiles.value.findIndex((profile) => profile.id === updated.id)
    if (index === -1) {
      profiles.value.unshift(updated)
    } else {
      profiles.value[index] = updated
    }
    selectedProfile.value = updated
  }

  function resetForm(profile?: PlatformProxyProfile | null) {
    profileForm.value = {
      id: profile?.id ?? '',
      profileCode: profile?.profileCode ?? '',
      profileName: profile?.profileName ?? '',
      proxyType: profile?.proxyType ?? 'HTTP',
      proxyHost: profile?.proxyHost ?? '',
      proxyPort: profile?.proxyPort || 8080,
      username: profile?.username ?? '',
      password: '',
      enabled: profile?.enabled ?? true,
    }
  }

  function openCreateForm() {
    formMode.value = 'create'
    resetForm(null)
    operationError.value = ''
    formOpen.value = true
  }

  function openEditForm(profile = selectedProfile.value) {
    if (!profile) return
    formMode.value = 'edit'
    resetForm(profile)
    operationError.value = ''
    formOpen.value = true
  }

  function closeForm() {
    formOpen.value = false
    operationError.value = ''
  }

  async function loadProfiles(nextPage = 1) {
    if (!canUsePlatformProxy.value) {
      accessDenied.value = true
      return false
    }
    listLoading.value = true
    listError.value = false
    accessDenied.value = false
    try {
      const result = await deps.listPlatformProxyProfiles(buildListQuery(nextPage))
      profiles.value = result.items
      total.value = result.total
      page.value = result.page
      return true
    } catch (error) {
      handleAccessError(error)
      listError.value = true
      return false
    } finally {
      listLoading.value = false
    }
  }

  async function selectProfile(profileId: string) {
    if (!canUsePlatformProxy.value) {
      accessDenied.value = true
      return false
    }
    detailLoading.value = true
    operationError.value = ''
    try {
      selectedProfile.value = await deps.getPlatformProxyProfile(profileId)
      return true
    } catch (error) {
      handleAccessError(error)
      operationError.value = deps.t('console.platformProxy.errors.loadDetail')
      return false
    } finally {
      detailLoading.value = false
    }
  }

  function buildSaveBody(): SavePlatformProxyProfileBody {
    const password = normalizeOptionalText(profileForm.value.password)
    return {
      profileCode: profileForm.value.profileCode.trim(),
      profileName: profileForm.value.profileName.trim(),
      proxyType: profileForm.value.proxyType,
      proxyHost: profileForm.value.proxyHost.trim(),
      proxyPort: Number(profileForm.value.proxyPort),
      username: normalizeOptionalText(profileForm.value.username),
      password: password ?? undefined,
      enabled: profileForm.value.enabled,
    }
  }

  async function saveProfile() {
    if (!canUsePlatformProxy.value) {
      accessDenied.value = true
      return false
    }
    operationLoading.value = true
    operationError.value = ''
    try {
      const updated =
        formMode.value === 'edit' && profileForm.value.id
          ? await deps.updatePlatformProxyProfile(profileForm.value.id, buildSaveBody())
          : await deps.createPlatformProxyProfile(buildSaveBody())
      replaceProfile(updated)
      formOpen.value = false
      await loadProfiles(page.value)
      return true
    } catch (error) {
      handleAccessError(error)
      operationError.value = deps.t('console.platformProxy.errors.save')
      return false
    } finally {
      operationLoading.value = false
    }
  }

  async function toggleProfile(profile: PlatformProxyProfile) {
    operationLoading.value = true
    operationError.value = ''
    try {
      const updated = profile.enabled
        ? await deps.disablePlatformProxyProfile(profile.id)
        : await deps.enablePlatformProxyProfile(profile.id)
      replaceProfile(updated)
      return true
    } catch (error) {
      handleAccessError(error)
      operationError.value = deps.t('console.platformProxy.errors.toggle')
      return false
    } finally {
      operationLoading.value = false
    }
  }

  async function deleteProfile(profile: PlatformProxyProfile) {
    operationLoading.value = true
    operationError.value = ''
    try {
      const deleted = await deps.deletePlatformProxyProfile(profile.id)
      profiles.value = profiles.value.filter((item) => item.id !== deleted.id)
      total.value = Math.max(0, total.value - 1)
      if (selectedProfile.value?.id === deleted.id) selectedProfile.value = null
      if (bindingProfileId.value === deleted.id) bindingProfileId.value = ''
      return true
    } catch (error) {
      handleAccessError(error)
      operationError.value = deps.t('console.platformProxy.errors.delete')
      return false
    } finally {
      operationLoading.value = false
    }
  }

  async function bindProfile() {
    if (!canBind.value) return false
    bindingLoading.value = true
    bindingError.value = ''
    try {
      bindingResult.value = await deps.bindProxyProfileToAsset(bindingApiCode.value.trim(), {
        profileId: bindingProfileId.value,
      })
      return true
    } catch (error) {
      handleAccessError(error)
      bindingError.value = deps.t('console.platformProxy.errors.bind')
      return false
    } finally {
      bindingLoading.value = false
    }
  }

  async function unbindProfile() {
    const apiCode = bindingApiCode.value.trim()
    if (!canUsePlatformProxy.value || !apiCode) return false
    bindingLoading.value = true
    bindingError.value = ''
    try {
      bindingResult.value = await deps.unbindProxyProfileFromAsset(apiCode)
      bindingProfileId.value = ''
      return true
    } catch (error) {
      handleAccessError(error)
      bindingError.value = deps.t('console.platformProxy.errors.unbind')
      return false
    } finally {
      bindingLoading.value = false
    }
  }

  if (deps.autoLoad) {
    onMounted(() => {
      void loadProfiles(1)
    })
  }

  return {
    canUsePlatformProxy,
    profiles,
    listLoading,
    listError,
    accessDenied,
    filterKeyword,
    filterEnabled,
    page,
    pageSize,
    total,
    selectedProfile,
    detailLoading,
    operationLoading,
    operationError,
    formOpen,
    formMode,
    profileForm,
    bindingApiCode,
    bindingProfileId,
    bindingResult,
    bindingLoading,
    bindingError,
    enabledProfiles,
    selectedBindingProfile,
    canBind,
    totalPages,
    loadProfiles,
    selectProfile,
    openCreateForm,
    openEditForm,
    closeForm,
    saveProfile,
    toggleProfile,
    deleteProfile,
    bindProfile,
    unbindProfile,
    resetForm,
  }
}
