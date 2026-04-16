import axios, { AxiosError, type AxiosRequestConfig } from 'axios'
import { appConfig } from '@/app/app-config'
import { useAuthStore } from '@/stores/useAuthStore'
import { env } from '@/utils/env'
import { mockAdapter } from '@/api/catalog/catalog.mock'

const isMock = import.meta.env.VITE_MOCK === 'true'

interface HttpErrorPayload {
  code?: string
  message?: string
  traceId?: string
}

interface RetryableRequestConfig extends AxiosRequestConfig {
  __retryCount?: number
  retry?: number
}

export interface NormalizedHttpError {
  status: number
  code?: string
  message: string
  traceId?: string
}

function normalizeHttpError(error: AxiosError<HttpErrorPayload>): NormalizedHttpError {
  return {
    status: error.response?.status ?? 0,
    code: error.response?.data?.code,
    message: error.response?.data?.message ?? error.message,
    traceId: error.response?.data?.traceId,
  }
}

function shouldRetry(error: AxiosError, config?: RetryableRequestConfig) {
  if (!config) {
    return false
  }

  if ((config.__retryCount ?? 0) >= (config.retry ?? 1)) {
    return false
  }

  if ((config.method ?? 'GET').toUpperCase() !== 'GET') {
    return false
  }

  return !error.response || error.response.status >= 500
}

export const http = axios.create({
  baseURL: env.apiBaseUrl,
  timeout: env.requestTimeoutMs,
  headers: {
    'X-App-Id': appConfig.appId,
    'X-Requested-With': 'XMLHttpRequest',
  },
  ...(isMock ? { adapter: mockAdapter } : {}),
})

http.interceptors.request.use((config) => {
  const authStore = useAuthStore()

  if (authStore.token) {
    config.headers = config.headers ?? {}
    config.headers.Authorization = `Bearer ${authStore.token}`
  }

  return config
})

http.interceptors.response.use(
  (response) => response,
  async (error: AxiosError<HttpErrorPayload>) => {
    const config = error.config as RetryableRequestConfig | undefined

    if (shouldRetry(error, config) && config) {
      config.__retryCount = (config.__retryCount ?? 0) + 1
      return http(config)
    }

    return Promise.reject(normalizeHttpError(error))
  },
)
