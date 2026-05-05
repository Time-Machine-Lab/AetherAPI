import { http } from '@/api/http'
import type {
  ApiSubscriptionDto,
  ApiSubscriptionPageDto,
  ApiSubscriptionStatusDto,
  ListApiSubscriptionsQuery,
  TmlResultDto,
} from './subscription.dto'
import type {
  ApiSubscription,
  ApiSubscriptionPage,
  ApiSubscriptionStatus,
} from './subscription.types'

function unwrap<T>(payload: T | TmlResultDto<T>): T {
  if (payload && typeof payload === 'object' && 'data' in payload) {
    return (payload as TmlResultDto<T>).data
  }
  return payload as T
}

function mapSubscription(dto: ApiSubscriptionDto): ApiSubscription {
  return {
    subscriptionId: dto.subscriptionId ?? null,
    apiCode: dto.apiCode,
    assetName: dto.assetName ?? null,
    assetOwnerUserId: dto.assetOwnerUserId ?? null,
    subscriptionStatus: dto.subscriptionStatus,
    subscribed: dto.subscribed,
    ownerAccess: dto.ownerAccess,
    createdAt: dto.createdAt ?? null,
    updatedAt: dto.updatedAt ?? null,
    cancelledAt: dto.cancelledAt ?? null,
  }
}

function mapSubscriptionStatus(dto: ApiSubscriptionStatusDto): ApiSubscriptionStatus {
  return {
    apiCode: dto.apiCode,
    accessStatus: dto.accessStatus,
    subscriptionId: dto.subscriptionId ?? null,
    subscriptionStatus: dto.subscriptionStatus ?? null,
    subscribed: dto.subscribed,
    ownerAccess: dto.ownerAccess,
  }
}

export async function subscribeCurrentUserApi(apiCode: string): Promise<ApiSubscription> {
  const { data } = await http.post<ApiSubscriptionDto | TmlResultDto<ApiSubscriptionDto>>(
    'v1/current-user/api-subscriptions',
    { apiCode },
  )
  return mapSubscription(unwrap(data))
}

export async function listCurrentUserApiSubscriptions(
  query?: ListApiSubscriptionsQuery,
): Promise<ApiSubscriptionPage> {
  const { data } = await http.get<ApiSubscriptionPageDto | TmlResultDto<ApiSubscriptionPageDto>>(
    'v1/current-user/api-subscriptions',
    { params: query },
  )
  const page = unwrap(data)
  return {
    items: page.items.map(mapSubscription),
    page: page.page,
    pageSize: page.size,
    total: page.total,
  }
}

export async function getCurrentUserApiSubscriptionStatus(
  apiCode: string,
): Promise<ApiSubscriptionStatus> {
  const { data } = await http.get<
    ApiSubscriptionStatusDto | TmlResultDto<ApiSubscriptionStatusDto>
  >('v1/current-user/api-subscriptions/status', { params: { apiCode } })
  return mapSubscriptionStatus(unwrap(data))
}

export async function cancelCurrentUserApiSubscription(
  subscriptionId: string,
): Promise<ApiSubscription> {
  const { data } = await http.patch<ApiSubscriptionDto | TmlResultDto<ApiSubscriptionDto>>(
    `v1/current-user/api-subscriptions/${subscriptionId}/cancel`,
  )
  return mapSubscription(unwrap(data))
}
