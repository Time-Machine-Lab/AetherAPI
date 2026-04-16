<script setup lang="ts">
import { useI18n } from 'vue-i18n'
import { Button } from '@/components/ui/button'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { useAppInfo } from '@/composables/useAppInfo'
import {
  consoleTimeline,
  consoleWorkspacePanels,
  summarizeConsoleSkeleton,
} from '@/features/console/console-shell'

const { env } = useAppInfo()
const { t } = useI18n()
const summary = summarizeConsoleSkeleton()
</script>

<route lang="json5">
{
  name: 'console-workspace',
  meta: {
    layout: 'ConsoleLayout',
    titleKey: 'console.workspace.metaTitle',
    requiresAuth: true,
  },
}
</route>

<template>
  <div class="space-y-6">
    <section class="flex flex-col gap-3 lg:flex-row lg:items-end lg:justify-between">
      <div>
        <p class="console-kicker">{{ t('console.navigation.overview') }}</p>
        <h2 class="console-display mt-3 text-[1.75rem] font-bold text-foreground">
          {{ t('console.workspace.title') }}
        </h2>
        <p class="mt-3 text-sm leading-6 text-muted-foreground">{{ t('console.workspace.description') }}</p>
      </div>
      <div class="flex flex-wrap gap-2">
        <Button size="sm">{{ t('console.workspace.primaryAction') }}</Button>
        <Button variant="outline" size="sm">{{ t('console.workspace.secondaryAction') }}</Button>
      </div>
    </section>

    <section class="grid gap-4 xl:grid-cols-4">
      <Card>
        <CardContent class="p-5">
          <p class="text-sm text-muted-foreground">{{ t('console.workspace.statLabel') }}</p>
          <p class="console-display mt-3 text-[2rem] font-bold text-foreground">
            {{ summary.sidebarItemCount }}
          </p>
          <p class="mt-2 text-xs text-muted-foreground">{{ t('console.workspace.statHint') }}</p>
        </CardContent>
      </Card>
      <Card>
        <CardContent class="p-5">
          <p class="text-sm text-muted-foreground">{{ t('console.workspace.readyLabel') }}</p>
          <p class="console-display mt-3 text-[2rem] font-bold text-foreground">
            {{ summary.readyCount }}
          </p>
          <p class="mt-2 text-xs text-muted-foreground">{{ t('console.workspace.readyHint') }}</p>
        </CardContent>
      </Card>
      <Card>
        <CardContent class="p-5">
          <p class="text-sm text-muted-foreground">{{ t('console.workspace.timelineLabel') }}</p>
          <p class="console-display mt-3 text-[2rem] font-bold text-foreground">
            {{ summary.plannedCount }}
          </p>
          <p class="mt-2 text-xs text-muted-foreground">{{ t('console.workspace.timelineHint') }}</p>
        </CardContent>
      </Card>
      <Card>
        <CardContent class="p-5">
          <p class="text-sm text-muted-foreground">{{ t('console.workspace.envLabel') }}</p>
          <p class="mt-2 truncate text-base font-semibold text-foreground">{{ env.apiBaseUrl }}</p>
          <p class="mt-2 text-xs text-muted-foreground">{{ env.appName }}</p>
        </CardContent>
      </Card>
    </section>

    <section class="grid gap-4 2xl:grid-cols-[minmax(0,1fr)_320px]">
      <div class="space-y-4">
        <Card>
          <CardHeader>
            <CardTitle>{{ t('console.workspace.panelsTitle') }}</CardTitle>
            <CardDescription>{{ t('console.workspace.panelsDescription') }}</CardDescription>
          </CardHeader>
          <CardContent class="grid gap-4 lg:grid-cols-2">
            <Card
              v-for="panel in consoleWorkspacePanels"
              :id="panel.id"
              :key="panel.id"
              class="scroll-mt-24"
            >
              <CardContent class="p-5">
                <div class="flex items-start justify-between gap-3">
                  <div>
                    <p class="text-base font-semibold text-foreground">{{ t(panel.titleKey) }}</p>
                    <p class="mt-2 text-sm leading-6 text-muted-foreground">
                      {{ t(panel.descriptionKey) }}
                    </p>
                  </div>
                  <span
                    class="rounded-full bg-[color-mix(in_srgb,var(--primary)_10%,white)] px-2.5 py-1 text-[11px] font-medium text-foreground"
                  >
                    {{ t(panel.statusKey) }}
                  </span>
                </div>
                <div class="mt-4 rounded-[14px] bg-secondary px-3 py-3 text-sm font-medium text-foreground">
                  {{ panel.metric }}
                </div>
              </CardContent>
            </Card>
          </CardContent>
        </Card>
      </div>

      <div class="space-y-4">
        <Card>
          <CardHeader>
            <CardTitle>{{ t('console.workspace.timelineTitle') }}</CardTitle>
            <CardDescription>{{ t('console.workspace.timelineDescription') }}</CardDescription>
          </CardHeader>
          <CardContent class="space-y-4">
            <div
              v-for="item in consoleTimeline"
              :key="item.id"
              class="rounded-[20px] bg-secondary px-4 py-4"
            >
              <p class="text-sm font-semibold text-foreground">{{ t(item.titleKey) }}</p>
              <p class="mt-2 text-sm leading-6 text-muted-foreground">
                {{ t(item.descriptionKey) }}
              </p>
            </div>
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <CardTitle>{{ t('console.workspace.environmentTitle') }}</CardTitle>
          </CardHeader>
          <CardContent class="space-y-3 text-sm">
            <div class="rounded-[14px] bg-secondary px-4 py-3">
              <p class="text-xs text-muted-foreground">APP ID</p>
              <p class="mt-1 font-medium text-foreground">{{ env.appId }}</p>
            </div>
            <div class="rounded-[14px] bg-secondary px-4 py-3">
              <p class="text-xs text-muted-foreground">APP NAME</p>
              <p class="mt-1 font-medium text-foreground">{{ env.appName }}</p>
            </div>
            <div class="rounded-[14px] bg-secondary px-4 py-3">
              <p class="text-xs text-muted-foreground">API BASE</p>
              <p class="mt-1 break-all font-medium text-foreground">{{ env.apiBaseUrl }}</p>
            </div>
          </CardContent>
        </Card>
      </div>
    </section>
  </div>
</template>
