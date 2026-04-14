import { appConfig } from '@/app/app-config'
import { env } from '@/utils/env'

export function useAppInfo() {
  return {
    appConfig,
    env,
  }
}
