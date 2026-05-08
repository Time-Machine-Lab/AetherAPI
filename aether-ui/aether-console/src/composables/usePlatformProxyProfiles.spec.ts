import { describe, expect, it, vi } from 'vitest'
import { usePlatformProxyProfiles } from './usePlatformProxyProfiles'
import type {
  AssetProxyBinding,
  PlatformProxyProfile,
  PlatformProxyProfilePage,
} from '@/api/platform-proxy-profile/platform-proxy-profile.types'

vi.mock('@/api/platform-proxy-profile/platform-proxy-profile.api', () => ({
  listPlatformProxyProfiles: vi.fn(),
  getPlatformProxyProfile: vi.fn(),
  createPlatformProxyProfile: vi.fn(),
  updatePlatformProxyProfile: vi.fn(),
  enablePlatformProxyProfile: vi.fn(),
  disablePlatformProxyProfile: vi.fn(),
  deletePlatformProxyProfile: vi.fn(),
  bindProxyProfileToAsset: vi.fn(),
  unbindProxyProfileFromAsset: vi.fn(),
}))

function t(key: string) {
  return key
}

function profile(overrides: Partial<PlatformProxyProfile> = {}): PlatformProxyProfile {
  return {
    id: 'proxy-1',
    profileCode: 'corp-egress',
    profileName: 'Corporate egress',
    proxyType: 'HTTP',
    proxyHost: 'proxy.internal',
    proxyPort: 8080,
    username: 'operator',
    credentialConfigured: true,
    enabled: true,
    deleted: false,
    createdAt: '2026-05-08T00:00:00Z',
    updatedAt: '2026-05-08T00:00:00Z',
    ...overrides,
  }
}

function page(items: PlatformProxyProfile[]): PlatformProxyProfilePage {
  return { items, total: items.length, page: 1, pageSize: 20 }
}

function binding(overrides: Partial<AssetProxyBinding> = {}): AssetProxyBinding {
  return {
    apiCode: 'weather-api',
    proxyProfileId: 'proxy-1',
    proxyProfileCode: 'corp-egress',
    proxyProfileName: 'Corporate egress',
    ...overrides,
  }
}

describe('usePlatformProxyProfiles', () => {
  it('loads list with filter and paging state for administrator roles', async () => {
    const listPlatformProxyProfiles = vi.fn().mockResolvedValueOnce(page([profile()]))
    const workspace = usePlatformProxyProfiles({
      t,
      autoLoad: false,
      currentUserRole: 'OWNER',
      listPlatformProxyProfiles,
    })

    workspace.filterKeyword.value = ' corp '
    workspace.filterEnabled.value = 'enabled'
    await workspace.loadProfiles(1)

    expect(listPlatformProxyProfiles).toHaveBeenCalledWith({
      keyword: 'corp',
      enabled: true,
      page: 1,
      size: 20,
    })
    expect(workspace.profiles.value).toHaveLength(1)
    expect(workspace.accessDenied.value).toBe(false)
  })

  it('blocks non-administrator sessions before loading or mutating', async () => {
    const listPlatformProxyProfiles = vi.fn()
    const workspace = usePlatformProxyProfiles({
      t,
      autoLoad: false,
      currentUserRole: 'USER',
      listPlatformProxyProfiles,
    })

    const result = await workspace.loadProfiles(1)

    expect(result).toBe(false)
    expect(workspace.accessDenied.value).toBe(true)
    expect(listPlatformProxyProfiles).not.toHaveBeenCalled()
  })

  it('hydrates edit form without prefilled password', async () => {
    const getPlatformProxyProfile = vi.fn().mockResolvedValueOnce(
      profile({
        id: 'proxy-1',
        username: 'operator',
        credentialConfigured: true,
      }),
    )
    const workspace = usePlatformProxyProfiles({
      t,
      autoLoad: false,
      currentUserRole: 'ADMIN',
      getPlatformProxyProfile,
    })

    await workspace.selectProfile('proxy-1')
    workspace.openEditForm()

    expect(workspace.profileForm.value.username).toBe('operator')
    expect(workspace.profileForm.value.password).toBe('')
    expect(workspace.profileForm.value.profileCode).toBe('corp-egress')
  })

  it('creates and updates profiles with contract-backed body only', async () => {
    const createPlatformProxyProfile = vi.fn().mockResolvedValueOnce(profile({ id: 'proxy-new' }))
    const updatePlatformProxyProfile = vi
      .fn()
      .mockResolvedValueOnce(profile({ profileName: 'New' }))
    const listPlatformProxyProfiles = vi.fn().mockResolvedValue(page([]))
    const workspace = usePlatformProxyProfiles({
      t,
      autoLoad: false,
      currentUserRole: 'PLATFORM_ADMIN',
      createPlatformProxyProfile,
      updatePlatformProxyProfile,
      listPlatformProxyProfiles,
    })

    workspace.openCreateForm()
    workspace.profileForm.value = {
      id: '',
      profileCode: 'new-egress',
      profileName: 'New egress',
      proxyType: 'HTTP',
      proxyHost: 'proxy.example.com',
      proxyPort: 8080,
      username: ' operator ',
      password: ' secret ',
      enabled: true,
    }
    await workspace.saveProfile()

    expect(createPlatformProxyProfile).toHaveBeenCalledWith({
      profileCode: 'new-egress',
      profileName: 'New egress',
      proxyType: 'HTTP',
      proxyHost: 'proxy.example.com',
      proxyPort: 8080,
      username: 'operator',
      password: 'secret',
      enabled: true,
    })

    workspace.selectedProfile.value = profile()
    workspace.openEditForm()
    workspace.profileForm.value.password = '   '
    await workspace.saveProfile()

    expect(updatePlatformProxyProfile).toHaveBeenCalledWith(
      'proxy-1',
      expect.objectContaining({ password: undefined }),
    )
  })

  it('toggles and deletes profiles while updating local state', async () => {
    const disabled = profile({ enabled: false })
    const disablePlatformProxyProfile = vi.fn().mockResolvedValueOnce(disabled)
    const deletePlatformProxyProfile = vi.fn().mockResolvedValueOnce({ ...disabled, deleted: true })
    const workspace = usePlatformProxyProfiles({
      t,
      autoLoad: false,
      currentUserRole: 'OWNER',
      disablePlatformProxyProfile,
      deletePlatformProxyProfile,
    })
    workspace.profiles.value = [profile()]
    workspace.total.value = 1

    await workspace.toggleProfile(workspace.profiles.value[0])
    expect(disablePlatformProxyProfile).toHaveBeenCalledWith('proxy-1')
    expect(workspace.profiles.value[0].enabled).toBe(false)

    await workspace.deleteProfile(workspace.profiles.value[0])
    expect(deletePlatformProxyProfile).toHaveBeenCalledWith('proxy-1')
    expect(workspace.profiles.value).toHaveLength(0)
    expect(workspace.total.value).toBe(0)
  })

  it('binds and unbinds enabled profiles without overwriting failed result', async () => {
    const bindProxyProfileToAsset = vi.fn().mockResolvedValueOnce(binding())
    const unbindProxyProfileFromAsset = vi
      .fn()
      .mockRejectedValueOnce({ status: 400, message: 'invalid' })
    const workspace = usePlatformProxyProfiles({
      t,
      autoLoad: false,
      currentUserRole: 'ADMIN',
      bindProxyProfileToAsset,
      unbindProxyProfileFromAsset,
    })
    workspace.profiles.value = [profile(), profile({ id: 'proxy-disabled', enabled: false })]
    workspace.bindingApiCode.value = ' weather-api '
    workspace.bindingProfileId.value = 'proxy-1'

    await workspace.bindProfile()

    expect(bindProxyProfileToAsset).toHaveBeenCalledWith('weather-api', { profileId: 'proxy-1' })
    expect(workspace.bindingResult.value?.proxyProfileId).toBe('proxy-1')

    workspace.bindingProfileId.value = 'proxy-disabled'
    expect(workspace.canBind.value).toBe(false)

    await workspace.unbindProfile()
    expect(workspace.bindingError.value).toBe('console.platformProxy.errors.unbind')
    expect(workspace.bindingResult.value?.proxyProfileId).toBe('proxy-1')
  })

  it('marks backend 401 or 403 as access denied fallback', async () => {
    const workspace = usePlatformProxyProfiles({
      t,
      autoLoad: false,
      currentUserRole: 'OWNER',
      listPlatformProxyProfiles: vi.fn().mockRejectedValueOnce({ status: 403 }),
    })

    await workspace.loadProfiles(1)

    expect(workspace.accessDenied.value).toBe(true)
    expect(workspace.listError.value).toBe(true)
  })
})
