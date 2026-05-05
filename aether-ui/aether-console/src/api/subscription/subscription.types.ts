export type ApiSubscriptionRecordStatus = 'ACTIVE' | 'CANCELLED' | 'OWNER'
export type ApiSubscriptionAccessStatus = 'SUBSCRIBED' | 'NOT_SUBSCRIBED' | 'OWNER'

export interface ApiSubscription {
  subscriptionId?: string | null
  apiCode: string
  assetName?: string | null
  assetOwnerUserId?: string | null
  subscriptionStatus: ApiSubscriptionRecordStatus
  subscribed: boolean
  ownerAccess: boolean
  createdAt?: string | null
  updatedAt?: string | null
  cancelledAt?: string | null
}

export interface ApiSubscriptionStatus {
  apiCode: string
  accessStatus: ApiSubscriptionAccessStatus
  subscriptionId?: string | null
  subscriptionStatus?: Exclude<ApiSubscriptionRecordStatus, 'OWNER'> | null
  subscribed: boolean
  ownerAccess: boolean
}

export interface ApiSubscriptionPage {
  items: ApiSubscription[]
  page: number
  pageSize: number
  total: number
}
