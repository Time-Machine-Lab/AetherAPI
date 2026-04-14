import type { Component } from 'vue'
import { appConfig } from '@/app/app-config'
import MarketingLayout from '@/layouts/MarketingLayout.vue'

export const layoutRegistry: Record<string, Component> = {
  MarketingLayout,
}

export const fallbackLayout = layoutRegistry[appConfig.defaultLayout]
