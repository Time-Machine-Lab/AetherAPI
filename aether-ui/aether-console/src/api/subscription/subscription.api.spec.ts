import { beforeEach, describe, expect, it, vi } from 'vitest'

vi.mock('@/api/http', () => ({
  http: {
    get: vi.fn(),
    post: vi.fn(),
    patch: vi.fn(),
  },
}))

import { http } from '@/api/http'
import {
  cancelCurrentUserApiSubscription,
  getCurrentUserApiSubscriptionStatus,
  listCurrentUserApiSubscriptions,
  subscribeCurrentUserApi,
} from './subscription.api'

const mockedGet = vi.mocked(http.get)
const mockedPost = vi.mocked(http.post)
const mockedPatch = vi.mocked(http.patch)

describe('subscription api', () => {
  beforeEach(() => {
    mockedGet.mockReset()
    mockedPost.mockReset()
    mockedPatch.mockReset()
  })

  it('subscribes to a published API using the current-user contract', async () => {
    mockedPost.mockResolvedValueOnce({
      data: {
        code: 'SUCCESS',
        data: {
          subscriptionId: 'sub-1',
          apiCode: 'weather-api',
          assetName: 'Weather API',
          subscriptionStatus: 'ACTIVE',
          subscribed: true,
          ownerAccess: false,
        },
      },
    })

    const result = await subscribeCurrentUserApi('weather-api')

    expect(mockedPost).toHaveBeenCalledWith('v1/current-user/api-subscriptions', {
      apiCode: 'weather-api',
    })
    expect(result).toMatchObject({
      subscriptionId: 'sub-1',
      apiCode: 'weather-api',
      subscriptionStatus: 'ACTIVE',
      subscribed: true,
    })
  })

  it('lists current-user subscriptions and maps page fields', async () => {
    mockedGet.mockResolvedValueOnce({
      data: {
        data: {
          items: [
            {
              subscriptionId: 'sub-1',
              apiCode: 'weather-api',
              assetName: 'Weather API',
              subscriptionStatus: 'ACTIVE',
              subscribed: true,
              ownerAccess: false,
              createdAt: '2026-05-01T00:00:00Z',
            },
          ],
          page: 1,
          size: 20,
          total: 1,
        },
      },
    })

    const result = await listCurrentUserApiSubscriptions({ page: 1, size: 20 })

    expect(mockedGet).toHaveBeenCalledWith('v1/current-user/api-subscriptions', {
      params: { page: 1, size: 20 },
    })
    expect(result).toMatchObject({
      page: 1,
      pageSize: 20,
      total: 1,
      items: [{ apiCode: 'weather-api', subscriptionStatus: 'ACTIVE' }],
    })
  })

  it('queries current-user subscription status by apiCode', async () => {
    mockedGet.mockResolvedValueOnce({
      data: {
        data: {
          apiCode: 'deepseek-v3',
          accessStatus: 'OWNER',
          subscriptionId: null,
          subscriptionStatus: null,
          subscribed: false,
          ownerAccess: true,
        },
      },
    })

    const result = await getCurrentUserApiSubscriptionStatus('deepseek-v3')

    expect(mockedGet).toHaveBeenCalledWith('v1/current-user/api-subscriptions/status', {
      params: { apiCode: 'deepseek-v3' },
    })
    expect(result).toMatchObject({
      apiCode: 'deepseek-v3',
      accessStatus: 'OWNER',
      ownerAccess: true,
    })
  })

  it('cancels current-user active subscription by id', async () => {
    mockedPatch.mockResolvedValueOnce({
      data: {
        subscriptionId: 'sub-1',
        apiCode: 'weather-api',
        subscriptionStatus: 'CANCELLED',
        subscribed: false,
        ownerAccess: false,
        cancelledAt: '2026-05-02T00:00:00Z',
      },
    })

    const result = await cancelCurrentUserApiSubscription('sub-1')

    expect(mockedPatch).toHaveBeenCalledWith('v1/current-user/api-subscriptions/sub-1/cancel')
    expect(result.subscriptionStatus).toBe('CANCELLED')
  })
})
