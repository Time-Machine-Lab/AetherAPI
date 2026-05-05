import { computed, ref } from 'vue'
import {
  cancelCurrentUserApiSubscription,
  listCurrentUserApiSubscriptions,
} from '@/api/subscription/subscription.api'
import type { ApiSubscription } from '@/api/subscription/subscription.types'

interface ApiSubscriptionWorkspaceDeps {
  list: typeof listCurrentUserApiSubscriptions
  cancel: typeof cancelCurrentUserApiSubscription
}

const defaultDeps: ApiSubscriptionWorkspaceDeps = {
  list: listCurrentUserApiSubscriptions,
  cancel: cancelCurrentUserApiSubscription,
}

function messageFromError(error: unknown, fallback: string) {
  if (error && typeof error === 'object' && 'message' in error) {
    return String((error as { message?: unknown }).message ?? fallback)
  }
  return fallback
}

export function canCancelSubscription(subscription: ApiSubscription) {
  return subscription.subscriptionStatus === 'ACTIVE' && Boolean(subscription.subscriptionId)
}

export function useApiSubscriptionWorkspace(deps: ApiSubscriptionWorkspaceDeps = defaultDeps) {
  const items = ref<ApiSubscription[]>([])
  const page = ref(1)
  const pageSize = ref(20)
  const total = ref(0)
  const loading = ref(false)
  const error = ref('')
  const actionError = ref('')
  const cancellingId = ref<string | null>(null)

  const totalPages = computed(() => Math.max(1, Math.ceil(total.value / pageSize.value)))

  async function loadSubscriptions(nextPage = page.value) {
    loading.value = true
    error.value = ''
    try {
      const result = await deps.list({ page: nextPage, size: pageSize.value })
      items.value = result.items
      page.value = result.page
      pageSize.value = result.pageSize
      total.value = result.total
    } catch (err) {
      error.value = messageFromError(err, 'Failed to load subscriptions')
    } finally {
      loading.value = false
    }
  }

  async function cancelSubscription(subscription: ApiSubscription) {
    if (!canCancelSubscription(subscription) || !subscription.subscriptionId) return null
    cancellingId.value = subscription.subscriptionId
    actionError.value = ''
    try {
      const cancelled = await deps.cancel(subscription.subscriptionId)
      await loadSubscriptions(page.value)
      return cancelled
    } catch (err) {
      actionError.value = messageFromError(err, 'Failed to cancel subscription')
      return null
    } finally {
      cancellingId.value = null
    }
  }

  return {
    items,
    page,
    pageSize,
    total,
    loading,
    error,
    actionError,
    cancellingId,
    totalPages,
    loadSubscriptions,
    cancelSubscription,
  }
}
