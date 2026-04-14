import type { Component } from 'vue'
import { appConfig } from '@/app/app-config'
import PortalLayout from '@/layouts/PortalLayout.vue'

export const layoutRegistry: Record<string, Component> = {
  PortalLayout,
}

export const fallbackLayout = layoutRegistry[appConfig.defaultLayout]
