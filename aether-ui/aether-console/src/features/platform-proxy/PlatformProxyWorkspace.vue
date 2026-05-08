<script setup lang="ts">
import { computed } from 'vue'
import { storeToRefs } from 'pinia'
import { useI18n } from 'vue-i18n'
import { CalendarClock, KeyRound, Network, Search, ShieldCheck, Unplug } from 'lucide-vue-next'
import { useAuthStore } from '@/stores/useAuthStore'
import { usePlatformProxyProfiles } from '@/composables/usePlatformProxyProfiles'
import { Button } from '@/components/ui/button'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { Input } from '@/components/ui/input'
import DataListRow from '@/components/console/DataListRow.vue'
import DisplayTag from '@/components/console/DisplayTag.vue'
import FieldGroup from '@/components/console/FieldGroup.vue'
import FieldLabel from '@/components/console/FieldLabel.vue'
import MetaItem from '@/components/console/MetaItem.vue'
import StateBlock from '@/components/console/StateBlock.vue'
import type {
  PlatformProxyAssetCandidate,
  PlatformProxyAssetStatus,
  PlatformProxyProfile,
} from '@/api/platform-proxy-profile/platform-proxy-profile.types'
import { assetTypeTone, type DisplayTone } from '@/utils/visual-system'

const { t } = useI18n()
const authStore = useAuthStore()
const { currentUser } = storeToRefs(authStore)
const currentUserRole = computed(() => currentUser.value?.role)

const proxy = usePlatformProxyProfiles({
  t,
  currentUserRole,
})

function enabledLabel(profile: PlatformProxyProfile) {
  if (profile.deleted) return t('console.platformProxy.statusDeleted')
  return profile.enabled
    ? t('console.platformProxy.statusEnabled')
    : t('console.platformProxy.statusDisabled')
}

function enabledTone(profile: PlatformProxyProfile): DisplayTone {
  if (profile.deleted) return 'danger'
  return profile.enabled ? 'success' : 'neutral'
}

function credentialLabel(profile: PlatformProxyProfile) {
  return profile.credentialConfigured
    ? t('console.platformProxy.credentialConfigured')
    : t('console.platformProxy.credentialNotConfigured')
}

function credentialTone(profile: PlatformProxyProfile): DisplayTone {
  return profile.credentialConfigured ? 'info' : 'neutral'
}

function assetStatusLabel(status: PlatformProxyAssetStatus) {
  if (status === 'PUBLISHED') return t('console.workspace.published')
  if (status === 'UNPUBLISHED') return t('console.workspace.unpublished')
  return t('console.workspace.draft')
}

function assetStatusTone(status: PlatformProxyAssetStatus): DisplayTone {
  if (status === 'PUBLISHED') return 'success'
  if (status === 'UNPUBLISHED') return 'warning'
  return 'neutral'
}

function candidateBindingLabel(candidate: PlatformProxyAssetCandidate) {
  return (
    candidate.proxyProfileName ||
    candidate.proxyProfileCode ||
    t('console.platformProxy.assetCandidateUnbound')
  )
}

function formatDateTime(value?: string | null) {
  if (!value) return ''
  try {
    return new Date(value).toLocaleString()
  } catch {
    return value
  }
}

async function submitSearch() {
  await proxy.loadProfiles(1)
}

async function submitAssetCandidateSearch() {
  await proxy.loadAssetCandidates(1)
}

async function selectProfile(profile: PlatformProxyProfile) {
  await proxy.selectProfile(profile.id)
}

function startCreate() {
  proxy.openCreateForm()
}

function startEdit(profile: PlatformProxyProfile) {
  proxy.openEditForm(profile)
}

async function confirmDelete(profile: PlatformProxyProfile) {
  if (!window.confirm(t('console.platformProxy.deleteConfirm'))) return
  await proxy.deleteProfile(profile)
}
</script>

<template>
  <div class="space-y-6">
    <section>
      <p class="console-kicker">{{ t('console.navigation.platformProxyProfiles') }}</p>
      <h2 class="console-display mt-3 text-[1.75rem] font-bold text-foreground">
        {{ t('console.platformProxy.title') }}
      </h2>
      <p class="mt-3 text-sm leading-6 text-muted-foreground">
        {{ t('console.platformProxy.description') }}
      </p>
    </section>

    <StateBlock
      v-if="!proxy.canUsePlatformProxy.value || proxy.accessDenied.value"
      tone="unavailable"
      :title="t('console.platformProxy.accessDeniedTitle')"
      :description="t('console.platformProxy.accessDeniedDescription')"
    />

    <div v-else class="grid gap-5 2xl:grid-cols-[1fr_420px]">
      <div class="space-y-5">
        <Card id="platform-proxy-profiles" class="scroll-mt-24">
          <CardHeader>
            <div class="flex flex-wrap items-start justify-between gap-3">
              <div>
                <CardTitle>{{ t('console.platformProxy.listTitle') }}</CardTitle>
                <CardDescription>{{ t('console.platformProxy.listDescription') }}</CardDescription>
              </div>
              <Button size="sm" @click="startCreate">
                <Network class="size-4" />
                {{ t('console.platformProxy.create') }}
              </Button>
            </div>
          </CardHeader>
          <CardContent class="space-y-4">
            <div class="flex flex-wrap gap-2">
              <div class="relative min-w-[180px] flex-1">
                <Search
                  class="pointer-events-none absolute left-4 top-1/2 size-4 -translate-y-1/2 text-muted-foreground"
                />
                <Input
                  v-model="proxy.filterKeyword.value"
                  :placeholder="t('console.platformProxy.filterKeyword')"
                  class="pl-10"
                  @keydown.enter.prevent="submitSearch"
                />
              </div>
              <select
                v-model="proxy.filterEnabled.value"
                class="h-11 cursor-pointer appearance-none rounded-[8px] border border-[rgb(34_34_34_/_0.08)] bg-white px-4 py-3 text-sm text-foreground outline-none transition-[background-color,box-shadow,border-color] focus-visible:border-primary focus-visible:ring-2 focus-visible:ring-primary/15"
              >
                <option value="">{{ t('console.platformProxy.filterAll') }}</option>
                <option value="enabled">{{ t('console.platformProxy.statusEnabled') }}</option>
                <option value="disabled">{{ t('console.platformProxy.statusDisabled') }}</option>
              </select>
              <Button size="sm" :disabled="proxy.listLoading.value" @click="submitSearch">
                {{ t('console.platformProxy.search') }}
              </Button>
            </div>

            <StateBlock
              v-if="proxy.listLoading.value"
              tone="loading"
              :title="t('console.platformProxy.loading')"
            />
            <StateBlock
              v-else-if="proxy.listError.value"
              tone="error"
              :title="t('console.platformProxy.listError')"
            />
            <StateBlock
              v-else-if="proxy.profiles.value.length === 0"
              tone="empty"
              :title="t('console.platformProxy.listEmpty')"
            />
            <div v-else class="space-y-2">
              <DataListRow
                v-for="profile in proxy.profiles.value"
                :key="profile.id"
                :selected="proxy.selectedProfile.value?.id === profile.id"
              >
                <template #title>
                  <p class="truncate text-sm font-medium text-foreground">
                    {{ profile.profileName || profile.profileCode }}
                  </p>
                </template>
                <template #description>
                  <p class="text-xs text-muted-foreground">
                    {{ profile.profileCode }} · {{ profile.proxyHost }}:{{ profile.proxyPort }}
                  </p>
                </template>
                <template #meta>
                  <MetaItem
                    :icon="Network"
                    :label="t('console.platformProxy.proxyType')"
                    :value="profile.proxyType"
                  />
                  <MetaItem
                    :icon="CalendarClock"
                    :label="t('console.platformProxy.updatedAt')"
                    :value="formatDateTime(profile.updatedAt)"
                  />
                </template>
                <template #tags>
                  <DisplayTag :tone="enabledTone(profile)" :label="enabledLabel(profile)" />
                  <DisplayTag :tone="credentialTone(profile)" :label="credentialLabel(profile)" />
                </template>
                <template #actions>
                  <Button size="xs" variant="ghost" @click="selectProfile(profile)">
                    {{ t('console.platformProxy.detail') }}
                  </Button>
                  <Button size="xs" variant="outline" @click="startEdit(profile)">
                    {{ t('console.shared.edit') }}
                  </Button>
                  <Button
                    size="xs"
                    variant="outline"
                    :disabled="proxy.operationLoading.value"
                    @click="proxy.toggleProfile(profile)"
                  >
                    {{
                      profile.enabled
                        ? t('console.workspace.disable')
                        : t('console.workspace.enable')
                    }}
                  </Button>
                  <Button
                    size="xs"
                    variant="destructive"
                    :disabled="proxy.operationLoading.value"
                    @click="confirmDelete(profile)"
                  >
                    {{ t('console.platformProxy.delete') }}
                  </Button>
                </template>
              </DataListRow>

              <div class="flex items-center justify-between pt-2 text-xs text-muted-foreground">
                <span>{{
                  t('console.platformProxy.pageSummary', {
                    page: proxy.page.value,
                    totalPages: proxy.totalPages(),
                    total: proxy.total.value,
                  })
                }}</span>
                <div class="flex gap-1">
                  <Button
                    size="xs"
                    variant="outline"
                    :disabled="proxy.page.value <= 1 || proxy.listLoading.value"
                    @click="proxy.loadProfiles(proxy.page.value - 1)"
                  >
                    {{ t('console.platformProxy.prev') }}
                  </Button>
                  <Button
                    size="xs"
                    variant="outline"
                    :disabled="proxy.page.value >= proxy.totalPages() || proxy.listLoading.value"
                    @click="proxy.loadProfiles(proxy.page.value + 1)"
                  >
                    {{ t('console.platformProxy.next') }}
                  </Button>
                </div>
              </div>
            </div>
          </CardContent>
        </Card>

        <Card v-if="proxy.formOpen.value">
          <CardHeader>
            <CardTitle>
              {{
                proxy.formMode.value === 'edit'
                  ? t('console.platformProxy.editTitle')
                  : t('console.platformProxy.createTitle')
              }}
            </CardTitle>
            <CardDescription>
              {{ t('console.platformProxy.formDescription') }}
            </CardDescription>
          </CardHeader>
          <CardContent>
            <FieldGroup
              :title="t('console.platformProxy.formGroup')"
              :description="t('console.platformProxy.passwordReplacementHint')"
            >
              <div class="grid gap-3 md:grid-cols-2">
                <div class="space-y-2">
                  <FieldLabel :label="t('console.platformProxy.profileCode')" required />
                  <Input v-model="proxy.profileForm.value.profileCode" />
                </div>
                <div class="space-y-2">
                  <FieldLabel :label="t('console.platformProxy.profileName')" required />
                  <Input v-model="proxy.profileForm.value.profileName" />
                </div>
                <div class="space-y-2">
                  <FieldLabel :label="t('console.platformProxy.proxyType')" required />
                  <select
                    v-model="proxy.profileForm.value.proxyType"
                    class="h-11 w-full cursor-pointer appearance-none rounded-[8px] border border-[rgb(34_34_34_/_0.08)] bg-white px-4 py-3 text-sm text-foreground outline-none transition-[background-color,box-shadow,border-color] focus-visible:border-primary focus-visible:ring-2 focus-visible:ring-primary/15"
                  >
                    <option value="HTTP">HTTP</option>
                  </select>
                </div>
                <div class="space-y-2">
                  <FieldLabel :label="t('console.platformProxy.proxyPort')" required />
                  <Input v-model.number="proxy.profileForm.value.proxyPort" type="number" />
                </div>
                <div class="space-y-2 md:col-span-2">
                  <FieldLabel :label="t('console.platformProxy.proxyHost')" required />
                  <Input v-model="proxy.profileForm.value.proxyHost" />
                </div>
                <div class="space-y-2">
                  <FieldLabel :label="t('console.platformProxy.username')" optional />
                  <Input v-model="proxy.profileForm.value.username" />
                </div>
                <div class="space-y-2">
                  <FieldLabel
                    :label="t('console.platformProxy.password')"
                    :hint="t('console.platformProxy.passwordReplacementHint')"
                    optional
                  />
                  <Input v-model="proxy.profileForm.value.password" type="password" />
                </div>
                <label class="flex items-center gap-2 text-sm md:col-span-2">
                  <input v-model="proxy.profileForm.value.enabled" type="checkbox" />
                  {{ t('console.platformProxy.enabledOnSave') }}
                </label>
              </div>
              <p v-if="proxy.operationError.value" class="text-sm text-destructive">
                {{ proxy.operationError.value }}
              </p>
              <div class="flex justify-end gap-2">
                <Button size="sm" variant="outline" @click="proxy.closeForm">
                  {{ t('console.workspace.cancel') }}
                </Button>
                <Button
                  size="sm"
                  :disabled="proxy.operationLoading.value"
                  @click="proxy.saveProfile"
                >
                  <ShieldCheck class="size-4" />
                  {{ t('console.workspace.save') }}
                </Button>
              </div>
            </FieldGroup>
          </CardContent>
        </Card>
      </div>

      <div class="space-y-5">
        <Card>
          <CardHeader>
            <CardTitle>{{ t('console.platformProxy.bindingTitle') }}</CardTitle>
            <CardDescription>{{ t('console.platformProxy.bindingDescription') }}</CardDescription>
          </CardHeader>
          <CardContent class="space-y-4">
            <div class="space-y-3">
              <FieldLabel
                :label="t('console.platformProxy.assetCandidateSearchTitle')"
                :hint="t('console.platformProxy.assetCandidateSearchHint')"
                optional
              />
              <div class="flex flex-wrap gap-2">
                <div class="relative min-w-[180px] flex-1">
                  <Search
                    class="pointer-events-none absolute left-4 top-1/2 size-4 -translate-y-1/2 text-muted-foreground"
                  />
                  <Input
                    v-model="proxy.assetCandidateKeyword.value"
                    :placeholder="t('console.platformProxy.assetCandidateSearchPlaceholder')"
                    class="pl-10"
                    @keydown.enter.prevent="submitAssetCandidateSearch"
                  />
                </div>
                <Button
                  size="sm"
                  :disabled="proxy.assetCandidateLoading.value"
                  @click="submitAssetCandidateSearch"
                >
                  <Search class="size-4" />
                  {{ t('console.platformProxy.assetCandidateSearch') }}
                </Button>
              </div>

              <StateBlock
                v-if="proxy.assetCandidateLoading.value"
                tone="loading"
                :title="t('console.platformProxy.assetCandidateLoading')"
              />
              <StateBlock
                v-else-if="proxy.assetCandidateError.value"
                tone="error"
                :title="proxy.assetCandidateError.value"
              />
              <StateBlock
                v-else-if="
                  proxy.assetCandidateKeyword.value.trim() &&
                  proxy.assetCandidates.value.length === 0
                "
                tone="empty"
                :title="t('console.platformProxy.assetCandidateEmpty')"
              />
              <div v-else-if="proxy.assetCandidates.value.length > 0" class="space-y-2">
                <DataListRow
                  v-for="candidate in proxy.assetCandidates.value"
                  :key="candidate.apiCode"
                  as="button"
                  :selected="proxy.selectedAssetCandidate.value?.apiCode === candidate.apiCode"
                  @click="proxy.selectAssetCandidate(candidate)"
                >
                  <template #title>
                    <p class="truncate text-sm font-medium text-foreground">
                      {{ candidate.assetName || candidate.apiCode }}
                    </p>
                  </template>
                  <template #description>
                    <p class="text-xs text-muted-foreground">
                      {{ candidate.apiCode }}
                    </p>
                  </template>
                  <template #meta>
                    <MetaItem
                      :label="t('console.platformProxy.assetCandidatePublisher')"
                      :value="candidate.publisherDisplayName"
                    />
                    <MetaItem
                      :label="t('console.platformProxy.assetCandidateCurrentBinding')"
                      :value="candidateBindingLabel(candidate)"
                    />
                  </template>
                  <template #tags>
                    <DisplayTag
                      :tone="assetTypeTone(candidate.assetType)"
                      :label="candidate.assetType"
                    />
                    <DisplayTag
                      :tone="assetStatusTone(candidate.status)"
                      :label="assetStatusLabel(candidate.status)"
                    />
                  </template>
                  <template #actions>
                    <Button
                      size="xs"
                      variant="outline"
                      @click.stop="proxy.selectAssetCandidate(candidate)"
                    >
                      {{ t('console.platformProxy.assetCandidateSelect') }}
                    </Button>
                  </template>
                </DataListRow>

                <div class="flex items-center justify-between pt-2 text-xs text-muted-foreground">
                  <span>{{
                    t('console.platformProxy.assetCandidatePageSummary', {
                      page: proxy.assetCandidatePage.value,
                      totalPages: proxy.assetCandidateTotalPages(),
                      total: proxy.assetCandidateTotal.value,
                    })
                  }}</span>
                  <div class="flex gap-1">
                    <Button
                      size="xs"
                      variant="outline"
                      :disabled="
                        proxy.assetCandidatePage.value <= 1 || proxy.assetCandidateLoading.value
                      "
                      @click="proxy.loadAssetCandidates(proxy.assetCandidatePage.value - 1)"
                    >
                      {{ t('console.platformProxy.prev') }}
                    </Button>
                    <Button
                      size="xs"
                      variant="outline"
                      :disabled="
                        proxy.assetCandidatePage.value >= proxy.assetCandidateTotalPages() ||
                        proxy.assetCandidateLoading.value
                      "
                      @click="proxy.loadAssetCandidates(proxy.assetCandidatePage.value + 1)"
                    >
                      {{ t('console.platformProxy.next') }}
                    </Button>
                  </div>
                </div>
              </div>
            </div>

            <div class="space-y-2">
              <FieldLabel :label="t('console.platformProxy.bindingApiCode')" required />
              <Input v-model="proxy.bindingApiCode.value" />
            </div>
            <div class="space-y-2">
              <FieldLabel :label="t('console.platformProxy.bindingProfile')" required />
              <select
                v-model="proxy.bindingProfileId.value"
                class="h-11 w-full cursor-pointer appearance-none rounded-[8px] border border-[rgb(34_34_34_/_0.08)] bg-white px-4 py-3 text-sm text-foreground outline-none transition-[background-color,box-shadow,border-color] focus-visible:border-primary focus-visible:ring-2 focus-visible:ring-primary/15"
              >
                <option value="">{{ t('console.platformProxy.bindingProfilePlaceholder') }}</option>
                <option
                  v-for="profile in proxy.profiles.value"
                  :key="profile.id"
                  :value="profile.id"
                  :disabled="!profile.enabled || profile.deleted"
                >
                  {{ profile.profileName }} ({{ profile.profileCode }})
                </option>
              </select>
            </div>
            <p class="text-xs leading-5 text-muted-foreground">
              {{ t('console.platformProxy.disabledBindingHint') }}
            </p>
            <p v-if="proxy.bindingError.value" class="text-sm text-destructive">
              {{ proxy.bindingError.value }}
            </p>
            <div class="flex flex-wrap gap-2">
              <Button
                size="sm"
                :disabled="!proxy.canBind.value || proxy.bindingLoading.value"
                @click="proxy.bindProfile"
              >
                <KeyRound class="size-4" />
                {{ t('console.platformProxy.bind') }}
              </Button>
              <Button
                size="sm"
                variant="outline"
                :disabled="!proxy.bindingApiCode.value.trim() || proxy.bindingLoading.value"
                @click="proxy.unbindProfile"
              >
                <Unplug class="size-4" />
                {{ t('console.platformProxy.unbind') }}
              </Button>
            </div>
            <div
              v-if="proxy.bindingResult.value"
              class="space-y-2 rounded-[14px] border border-[rgb(34_34_34_/_0.06)] bg-secondary/50 p-4"
            >
              <p class="text-xs font-semibold text-muted-foreground">
                {{ t('console.platformProxy.bindingResult') }}
              </p>
              <p class="text-sm font-medium text-foreground">
                {{ proxy.bindingResult.value.apiCode }}
              </p>
              <DisplayTag
                :tone="proxy.bindingResult.value.proxyProfileId ? 'success' : 'neutral'"
                :label="
                  proxy.bindingResult.value.proxyProfileName ||
                  t('console.platformProxy.bindingUnbound')
                "
              />
            </div>
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <CardTitle>{{ t('console.platformProxy.detailTitle') }}</CardTitle>
            <CardDescription>{{ t('console.platformProxy.detailDescription') }}</CardDescription>
          </CardHeader>
          <CardContent>
            <StateBlock
              v-if="proxy.detailLoading.value"
              tone="loading"
              :title="t('console.platformProxy.loadingDetail')"
            />
            <StateBlock
              v-else-if="!proxy.selectedProfile.value"
              tone="empty"
              :title="t('console.platformProxy.detailEmpty')"
            />
            <div v-else class="space-y-4">
              <div class="flex items-start justify-between gap-3">
                <div>
                  <p class="font-semibold text-foreground">
                    {{ proxy.selectedProfile.value.profileName }}
                  </p>
                  <p class="text-xs text-muted-foreground">
                    {{ proxy.selectedProfile.value.profileCode }}
                  </p>
                </div>
                <DisplayTag
                  :tone="enabledTone(proxy.selectedProfile.value)"
                  :label="enabledLabel(proxy.selectedProfile.value)"
                />
              </div>
              <div class="space-y-2 text-sm">
                <p>
                  <span class="text-muted-foreground"
                    >{{ t('console.platformProxy.proxyHost') }}:</span
                  >
                  <span class="ml-2 text-foreground">{{
                    proxy.selectedProfile.value.proxyHost
                  }}</span>
                </p>
                <p>
                  <span class="text-muted-foreground"
                    >{{ t('console.platformProxy.proxyPort') }}:</span
                  >
                  <span class="ml-2 text-foreground">{{
                    proxy.selectedProfile.value.proxyPort
                  }}</span>
                </p>
                <p>
                  <span class="text-muted-foreground"
                    >{{ t('console.platformProxy.username') }}:</span
                  >
                  <span class="ml-2 text-foreground">{{
                    proxy.selectedProfile.value.username || t('console.platformProxy.notConfigured')
                  }}</span>
                </p>
              </div>
              <DisplayTag
                :tone="credentialTone(proxy.selectedProfile.value)"
                :label="credentialLabel(proxy.selectedProfile.value)"
              />
              <p class="text-xs leading-5 text-muted-foreground">
                {{ t('console.platformProxy.secretSafetyHint') }}
              </p>
            </div>
          </CardContent>
        </Card>
      </div>
    </div>
  </div>
</template>
