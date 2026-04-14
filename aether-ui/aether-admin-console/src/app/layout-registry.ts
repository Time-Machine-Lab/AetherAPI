import type { Component } from 'vue'
import { appConfig } from '@/app/app-config'
import AdminLayout from '@/layouts/AdminLayout.vue'

export const layoutRegistry: Record<string, Component> = {
  AdminLayout,
}

export const fallbackLayout = layoutRegistry[appConfig.defaultLayout]
