import type { Component } from 'vue'
import { appConfig } from '@/app/app-config'
import ConsoleLayout from '@/layouts/ConsoleLayout.vue'

export const layoutRegistry: Record<string, Component> = {
  ConsoleLayout,
}

export const fallbackLayout = layoutRegistry[appConfig.defaultLayout]
