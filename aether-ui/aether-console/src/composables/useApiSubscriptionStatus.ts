import { computed, ref } from 'vue'
import {
  cancelCurrentUserApiSubscription,
  getCurrentUserApiSubscriptionStatus,
  subscribeCurrentUserApi,
} from '@/api/subscription/subscription.api'
import type { ApiSubscription, ApiSubscriptionStatus } from '@/api/subscription/subscription.types'

interface ApiSubscriptionStatusDeps {
  getStatus: typeof getCurrentUserApiSubscriptionStatus
  subscribe: typeof subscribeCurrentUserApi
  cancel: typeof cancelCurrentUserApiSubscription
}

const defaultDeps: ApiSubscriptionStatusDeps = {
  getStatus: getCurrentUserApiSubscriptionStatus,
  subscribe: subscribeCurrentUserApi,
  cancel: cancelCurrentUserApiSubscription,
}

function messageFromError(error: unknown, fallback: string) {
  if (error && typeof error === 'object' && 'message' in error) {
    return String((error as { message?: unknown }).message ?? fallback)
  }
  return fallback
}

export function statusFromSubscription(subscription: ApiSubscription): ApiSubscriptionStatus {
  if (subscription.ownerAccess || subscription.subscriptionStatus === 'OWNER') {
    return {
      apiCode: subscription.apiCode,
      accessStatus: 'OWNER',
      subscriptionId: null,
      subscriptionStatus: null,
      subscribed: false,
      ownerAccess: true,
    }
  }

  return {
    apiCode: subscription.apiCode,
    accessStatus: subscription.subscribed ? 'SUBSCRIBED' : 'NOT_SUBSCRIBED',
    subscriptionId: subscription.subscriptionId ?? null,
    subscriptionStatus: subscription.subscriptionStatus,
    subscribed: subscription.subscribed,
    ownerAccess: false,
  }
}

export function useApiSubscriptionStatus(deps: ApiSubscriptionStatusDeps = defaultDeps) {
  const status = ref<ApiSubscriptionStatus | null>(null)
  const statusLoading = ref(false)
  const statusError = ref('')
  const actionLoading = ref(false)
  const actionError = ref('')

  const canSubscribe = computed(() => status.value?.accessStatus === 'NOT_SUBSCRIBED')
  const canCancel = computed(
    () =>
      status.value?.accessStatus === 'SUBSCRIBED' &&
      status.value.subscriptionStatus === 'ACTIVE' &&
      Boolean(status.value.subscriptionId),
  )

  async function loadStatus(apiCode: string) {
    const normalizedApiCode = apiCode.trim()
    status.value = null
    statusError.value = ''
    if (!normalizedApiCode) return
    statusLoading.value = true
    try {
      status.value = await deps.getStatus(normalizedApiCode)
    } catch (error) {
      statusError.value = messageFromError(error, 'Failed to load subscription status')
    } finally {
      statusLoading.value = false
    }
  }

  async function subscribe(apiCode: string) {
    const normalizedApiCode = apiCode.trim()
    if (!normalizedApiCode) return null
    actionLoading.value = true
    actionError.value = ''
    try {
      const subscription = await deps.subscribe(normalizedApiCode)
      status.value = statusFromSubscription(subscription)
      await loadStatus(normalizedApiCode)
      return subscription
    } catch (error) {
      actionError.value = messageFromError(error, 'Failed to subscribe')
      return null
    } finally {
      actionLoading.value = false
    }
  }

  async function cancel(subscriptionId?: string | null) {
    if (!subscriptionId || !status.value?.apiCode) return null
    actionLoading.value = true
    actionError.value = ''
    const apiCode = status.value.apiCode
    try {
      const subscription = await deps.cancel(subscriptionId)
      status.value = statusFromSubscription(subscription)
      await loadStatus(apiCode)
      return subscription
    } catch (error) {
      actionError.value = messageFromError(error, 'Failed to cancel subscription')
      return null
    } finally {
      actionLoading.value = false
    }
  }

  function resetStatus() {
    status.value = null
    statusError.value = ''
    actionError.value = ''
  }

  return {
    status,
    statusLoading,
    statusError,
    actionLoading,
    actionError,
    canSubscribe,
    canCancel,
    loadStatus,
    subscribe,
    cancel,
    resetStatus,
  }
}
