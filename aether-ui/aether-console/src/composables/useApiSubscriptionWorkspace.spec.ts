import { describe, expect, it, vi } from 'vitest'

vi.mock('@/api/subscription/subscription.api', () => ({
  cancelCurrentUserApiSubscription: vi.fn(),
  listCurrentUserApiSubscriptions: vi.fn(),
}))

import { canCancelSubscription, useApiSubscriptionWorkspace } from './useApiSubscriptionWorkspace'
import type { ApiSubscription } from '@/api/subscription/subscription.types'

function subscription(overrides: Partial<ApiSubscription> = {}): ApiSubscription {
  return {
    subscriptionId: 'sub-1',
    apiCode: 'weather-api',
    assetName: 'Weather API',
    subscriptionStatus: 'ACTIVE',
    subscribed: true,
    ownerAccess: false,
    ...overrides,
  }
}

describe('useApiSubscriptionWorkspace', () => {
  it('loads current-user subscription list', async () => {
    const deps = {
      list: vi.fn().mockResolvedValueOnce({
        items: [subscription()],
        page: 1,
        pageSize: 20,
        total: 1,
      }),
      cancel: vi.fn(),
    }
    const workspace = useApiSubscriptionWorkspace(deps)

    await workspace.loadSubscriptions(1)

    expect(deps.list).toHaveBeenCalledWith({ page: 1, size: 20 })
    expect(workspace.items.value).toHaveLength(1)
    expect(workspace.totalPages.value).toBe(1)
  })

  it('does not cancel rows that are already cancelled or lack an id', async () => {
    expect(
      canCancelSubscription(subscription({ subscriptionStatus: 'CANCELLED', subscribed: false })),
    ).toBe(false)
    expect(canCancelSubscription(subscription({ subscriptionId: null }))).toBe(false)
  })

  it('cancels active rows and reloads the list', async () => {
    const deps = {
      list: vi
        .fn()
        .mockResolvedValueOnce({ items: [subscription()], page: 1, pageSize: 20, total: 1 })
        .mockResolvedValueOnce({
          items: [subscription({ subscriptionStatus: 'CANCELLED', subscribed: false })],
          page: 1,
          pageSize: 20,
          total: 1,
        }),
      cancel: vi.fn().mockResolvedValueOnce(
        subscription({
          subscriptionStatus: 'CANCELLED',
          subscribed: false,
        }),
      ),
    }
    const workspace = useApiSubscriptionWorkspace(deps)

    await workspace.loadSubscriptions(1)
    await workspace.cancelSubscription(workspace.items.value[0])

    expect(deps.cancel).toHaveBeenCalledWith('sub-1')
    expect(deps.list).toHaveBeenCalledTimes(2)
    expect(workspace.items.value[0].subscriptionStatus).toBe('CANCELLED')
  })
})
