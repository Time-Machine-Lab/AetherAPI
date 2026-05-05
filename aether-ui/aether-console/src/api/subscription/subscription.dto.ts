export type ApiSubscriptionRecordStatusDto = 'ACTIVE' | 'CANCELLED' | 'OWNER'
export type ApiSubscriptionAccessStatusDto = 'SUBSCRIBED' | 'NOT_SUBSCRIBED' | 'OWNER'

export interface SubscribeApiBody {
  apiCode: string
}

export interface ApiSubscriptionDto {
  subscriptionId?: string | null
  apiCode: string
  assetName?: string | null
  assetOwnerUserId?: string | null
  subscriptionStatus: ApiSubscriptionRecordStatusDto
  subscribed: boolean
  ownerAccess: boolean
  createdAt?: string | null
  updatedAt?: string | null
  cancelledAt?: string | null
}

export interface ApiSubscriptionStatusDto {
  apiCode: string
  accessStatus: ApiSubscriptionAccessStatusDto
  subscriptionId?: string | null
  subscriptionStatus?: Exclude<ApiSubscriptionRecordStatusDto, 'OWNER'> | null
  subscribed: boolean
  ownerAccess: boolean
}

export interface ApiSubscriptionPageDto {
  items: ApiSubscriptionDto[]
  page: number
  size: number
  total: number
}

export interface TmlResultDto<T> {
  code?: string
  message?: string
  traceId?: string | null
  data: T
}

export interface ListApiSubscriptionsQuery {
  page?: number
  size?: number
}
