import { describe, expect, it, vi } from 'vitest'

vi.mock('@/api/subscription/subscription.api', () => ({
  cancelCurrentUserApiSubscription: vi.fn(),
  getCurrentUserApiSubscriptionStatus: vi.fn(),
  subscribeCurrentUserApi: vi.fn(),
}))

import { useApiSubscriptionStatus } from './useApiSubscriptionStatus'
import type { ApiSubscription, ApiSubscriptionStatus } from '@/api/subscription/subscription.types'

function activeSubscription(overrides: Partial<ApiSubscription> = {}): ApiSubscription {
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

function status(overrides: Partial<ApiSubscriptionStatus> = {}): ApiSubscriptionStatus {
  return {
    apiCode: 'weather-api',
    accessStatus: 'NOT_SUBSCRIBED',
    subscriptionId: null,
    subscriptionStatus: null,
    subscribed: false,
    ownerAccess: false,
    ...overrides,
  }
}

describe('useApiSubscriptionStatus', () => {
  it('loads not-subscribed status and exposes subscribe availability', async () => {
    const deps = {
      getStatus: vi.fn().mockResolvedValueOnce(status()),
      subscribe: vi.fn(),
      cancel: vi.fn(),
    }
    const state = useApiSubscriptionStatus(deps)

    await state.loadStatus(' weather-api ')

    expect(deps.getStatus).toHaveBeenCalledWith('weather-api')
    expect(state.status.value?.accessStatus).toBe('NOT_SUBSCRIBED')
    expect(state.canSubscribe.value).toBe(true)
  })

  it('refreshes status after subscribing to avoid stale local state', async () => {
    const deps = {
      getStatus: vi
        .fn()
        .mockResolvedValueOnce(status({ accessStatus: 'NOT_SUBSCRIBED' }))
        .mockResolvedValueOnce(
          status({
            accessStatus: 'SUBSCRIBED',
            subscriptionId: 'sub-1',
            subscriptionStatus: 'ACTIVE',
            subscribed: true,
          }),
        ),
      subscribe: vi.fn().mockResolvedValueOnce(activeSubscription()),
      cancel: vi.fn(),
    }
    const state = useApiSubscriptionStatus(deps)

    await state.loadStatus('weather-api')
    await state.subscribe('weather-api')

    expect(deps.subscribe).toHaveBeenCalledWith('weather-api')
    expect(deps.getStatus).toHaveBeenCalledTimes(2)
    expect(state.status.value?.accessStatus).toBe('SUBSCRIBED')
    expect(state.canCancel.value).toBe(true)
  })

  it('renders owner access as non-cancellable and non-subscribable', async () => {
    const deps = {
      getStatus: vi.fn().mockResolvedValueOnce(
        status({
          accessStatus: 'OWNER',
          ownerAccess: true,
        }),
      ),
      subscribe: vi.fn(),
      cancel: vi.fn(),
    }
    const state = useApiSubscriptionStatus(deps)

    await state.loadStatus('kimi-k2')

    expect(state.status.value?.accessStatus).toBe('OWNER')
    expect(state.canSubscribe.value).toBe(false)
    expect(state.canCancel.value).toBe(false)
  })

  it('refreshes status after cancelling an active subscription', async () => {
    const deps = {
      getStatus: vi
        .fn()
        .mockResolvedValueOnce(
          status({
            accessStatus: 'SUBSCRIBED',
            subscriptionId: 'sub-1',
            subscriptionStatus: 'ACTIVE',
            subscribed: true,
          }),
        )
        .mockResolvedValueOnce(status({ accessStatus: 'NOT_SUBSCRIBED' })),
      subscribe: vi.fn(),
      cancel: vi.fn().mockResolvedValueOnce(
        activeSubscription({
          subscriptionStatus: 'CANCELLED',
          subscribed: false,
          cancelledAt: '2026-05-02T00:00:00Z',
        }),
      ),
    }
    const state = useApiSubscriptionStatus(deps)

    await state.loadStatus('weather-api')
    await state.cancel('sub-1')

    expect(deps.cancel).toHaveBeenCalledWith('sub-1')
    expect(state.status.value?.accessStatus).toBe('NOT_SUBSCRIBED')
  })
})
