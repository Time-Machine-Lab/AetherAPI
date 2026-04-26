import { describe, expect, it, vi } from 'vitest'

vi.mock('vue', async (importOriginal) => {
  const actual = await importOriginal<typeof import('vue')>()
  return {
    ...actual,
    onMounted: (callback: () => void | Promise<void>) => {
      void callback()
    },
  }
})

vi.mock('@/api/catalog/category.api', () => ({
  listCategories: vi.fn(),
  createCategory: vi.fn(),
  renameCategory: vi.fn(),
  enableCategory: vi.fn(),
  disableCategory: vi.fn(),
}))

vi.mock('@/api/catalog/asset.api', () => ({
  getAsset: vi.fn(),
  listAssets: vi.fn(),
  registerAsset: vi.fn(),
  reviseAsset: vi.fn(),
  publishAsset: vi.fn(),
  unpublishAsset: vi.fn(),
  deleteAsset: vi.fn(),
  bindAiProfile: vi.fn(),
}))

import { useWorkspaceCatalog } from './useWorkspaceCatalog'

describe('useWorkspaceCatalog auto load', () => {
  it('loads asset list on mount by default', async () => {
    const listAssets = vi.fn().mockResolvedValueOnce({
      items: [],
      total: 0,
      page: 1,
      pageSize: 20,
    })

    useWorkspaceCatalog({
      t: (key) => key,
      listCategories: vi.fn().mockResolvedValueOnce({
        items: [],
        total: 0,
        page: 1,
        pageSize: 20,
      }),
      listAssets,
      getRecentAssets: vi.fn().mockReturnValue([]),
    })

    await Promise.resolve()

    expect(listAssets).toHaveBeenCalledWith({ page: 1, size: 20 })
  })
})
