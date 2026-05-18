<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { X } from 'lucide-vue-next'
import { useI18n } from 'vue-i18n'
import CodeBlock from '@/components/console/CodeBlock.vue'
import DisplayTag from '@/components/console/DisplayTag.vue'
import FieldGroup from '@/components/console/FieldGroup.vue'
import JsonSchemaTreeNode from '@/components/console/JsonSchemaTreeNode.vue'
import StateBlock from '@/components/console/StateBlock.vue'
import { Button } from '@/components/ui/button'
import {
  parseSchemaDocument,
  summarizeSchemaTree,
} from '@/utils/schema-visualization'

const props = withDefaults(
  defineProps<{
    value?: string | null
    label: string
    emptyTitle?: string
    emptyDescription?: string
    maxHeightClass?: string
    presentation?: 'code' | 'overlay'
  }>(),
  {
    value: null,
    maxHeightClass: 'max-h-[300px]',
    presentation: 'code',
  },
)

const { t, te } = useI18n()
const schema = computed(() => props.value?.trim() ?? '')
const schemaDocument = computed(() => parseSchemaDocument(props.value))
const schemaTree = computed(() => schemaDocument.value.tree)
const schemaSummary = computed(() => summarizeSchemaTree(schemaTree.value))
const overlayOpen = ref(false)
const activeTab = ref<'visual' | 'raw'>('raw')

watch(
  schemaDocument,
  (document) => {
    activeTab.value = document.visualizable ? 'visual' : 'raw'
  },
  { immediate: true },
)

const summaryText = computed(() => {
  if (!schemaSummary.value) {
    return t('console.shared.schemaRawOnly')
  }

  const parts = [formatTypeLabel(schemaSummary.value.typeLabel)]
  if (schemaSummary.value.fieldCount > 0) {
    parts.push(t('console.shared.schemaFieldCount', { count: schemaSummary.value.fieldCount }))
  }
  if (schemaSummary.value.requiredCount > 0) {
    parts.push(
      t('console.shared.schemaRequiredCount', { count: schemaSummary.value.requiredCount }),
    )
  }
  if (schemaSummary.value.enumCount > 0) {
    parts.push(t('console.shared.schemaEnumCount', { count: schemaSummary.value.enumCount }))
  }
  return parts.join(' · ')
})

const rootTypeLabel = computed(() => {
  if (!schemaSummary.value) {
    return null
  }
  return formatTypeLabel(schemaSummary.value.typeLabel)
})

function openOverlay() {
  activeTab.value = schemaDocument.value.visualizable ? 'visual' : 'raw'
  overlayOpen.value = true
}

function closeOverlay() {
  overlayOpen.value = false
}

function formatTypeLabel(typeLabel: string): string {
  return typeLabel
    .split(' | ')
    .map((part) => {
      const normalized = part.trim()
      const key = `console.shared.schemaType.${normalized}`
      return te(key) ? t(key) : normalized
    })
    .join(' | ')
}
</script>

<template>
  <div data-console-json-schema-viewer>
    <template v-if="schema && presentation === 'overlay'">
      <button
        type="button"
        class="group w-full cursor-pointer rounded-[14px] border border-[rgb(34_34_34_/_0.06)] bg-white p-4 text-left shadow-console transition-[transform,box-shadow] duration-200 hover:-translate-y-px hover:shadow-console-hover focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-[rgb(34_34_34_/_0.14)] focus-visible:ring-offset-2"
        @click="openOverlay"
      >
        <div class="flex items-start justify-between gap-3">
          <div class="min-w-0 space-y-2">
            <p class="text-sm font-semibold text-foreground">{{ label }}</p>
            <p class="text-xs leading-5 text-muted-foreground">{{ summaryText }}</p>
          </div>
          <span
            class="inline-flex h-8 shrink-0 cursor-pointer items-center justify-center rounded-full border border-[rgb(34_34_34_/_0.08)] bg-white px-3 text-xs font-medium text-foreground shadow-console transition-colors duration-200 group-hover:border-[rgb(34_34_34_/_0.14)] group-hover:bg-secondary group-focus-visible:border-[rgb(34_34_34_/_0.14)] group-focus-visible:bg-secondary"
          >
            {{ t('console.shared.schemaInspect') }}
          </span>
        </div>
      </button>

      <Teleport to="body">
        <div
          v-if="overlayOpen"
          class="fixed inset-0 z-50 bg-black/45 px-3 py-3 sm:flex sm:items-center sm:justify-center sm:px-6 sm:py-8"
        >
          <div class="absolute inset-0" @click="closeOverlay" />

          <section
            role="dialog"
            :aria-label="label"
            aria-modal="true"
            class="relative mt-10 flex h-[calc(100vh-2rem)] w-full flex-col overflow-hidden rounded-t-[24px] rounded-b-[20px] border border-[rgb(34_34_34_/_0.08)] bg-white shadow-console-hover sm:mt-0 sm:h-[min(88vh,760px)] sm:max-w-5xl sm:rounded-[24px]"
          >
            <div class="border-b border-[rgb(34_34_34_/_0.06)] px-4 py-4 sm:px-6">
              <div class="flex items-start justify-between gap-3">
                <div class="min-w-0 space-y-2">
                  <p class="text-xs font-semibold tracking-[0.18em] text-muted-foreground uppercase">
                    {{ label }}
                  </p>
                  <div class="flex flex-wrap gap-2">
                    <DisplayTag v-if="rootTypeLabel" tone="info" :label="rootTypeLabel" />
                    <DisplayTag
                      v-if="schemaSummary?.fieldCount"
                      tone="neutral"
                      :label="t('console.shared.schemaFieldCount', { count: schemaSummary.fieldCount })"
                    />
                    <DisplayTag
                      v-if="schemaSummary?.requiredCount"
                      tone="danger"
                      :label="t('console.shared.schemaRequiredCount', { count: schemaSummary.requiredCount })"
                    />
                  </div>
                </div>

                <Button type="button" size="icon-sm" variant="ghost" :title="t('console.shared.close')" @click="closeOverlay">
                  <X class="size-4" />
                </Button>
              </div>

              <div class="mt-4 flex flex-wrap gap-2">
                <Button
                  type="button"
                  size="xs"
                  :variant="activeTab === 'visual' ? 'default' : 'ghost'"
                  :disabled="!schemaDocument.visualizable"
                  @click="activeTab = 'visual'"
                >
                  {{ t('console.shared.schemaVisual') }}
                </Button>
                <Button
                  type="button"
                  size="xs"
                  :variant="activeTab === 'raw' ? 'outline' : 'ghost'"
                  @click="activeTab = 'raw'"
                >
                  {{ t('console.shared.schemaRaw') }}
                </Button>
              </div>
            </div>

            <div class="flex-1 overflow-auto px-4 py-4 sm:px-6 sm:py-5">
              <div v-if="activeTab === 'visual' && schemaTree" class="space-y-4">
                <FieldGroup :title="label" :description="schemaTree.description">
                  <div class="flex flex-wrap gap-2">
                    <DisplayTag v-if="rootTypeLabel" tone="info" :label="rootTypeLabel" />
                    <DisplayTag
                      v-if="schemaTree.nullable"
                      tone="warning"
                      :label="t('console.shared.schemaNullableLabel')"
                    />
                    <DisplayTag
                      v-if="schemaTree.enumValues.length > 0"
                      tone="warning"
                      :label="t('console.shared.schemaEnumCount', { count: schemaTree.enumValues.length })"
                    />
                  </div>

                  <div
                    v-if="schemaTree.format || schemaTree.defaultValue !== undefined"
                    class="grid gap-3 text-xs text-muted-foreground sm:grid-cols-2"
                  >
                    <div v-if="schemaTree.format" class="space-y-1">
                      <p class="font-semibold text-foreground">{{ t('console.shared.schemaFormatLabel') }}</p>
                      <p>{{ schemaTree.format }}</p>
                    </div>
                    <div v-if="schemaTree.defaultValue !== undefined" class="space-y-1">
                      <p class="font-semibold text-foreground">{{ t('console.shared.schemaDefaultLabel') }}</p>
                      <p class="font-mono text-[11px]">{{ schemaTree.defaultValue }}</p>
                    </div>
                  </div>

                  <div v-if="schemaTree.enumValues.length > 0" class="space-y-2">
                    <p class="text-xs font-semibold text-foreground">{{ t('console.shared.schemaEnumValues') }}</p>
                    <div class="flex flex-wrap gap-2">
                      <span
                        v-for="enumValue in schemaTree.enumValues"
                        :key="enumValue"
                        class="rounded-full border border-[rgb(34_34_34_/_0.08)] bg-secondary px-2.5 py-1 font-mono text-[11px] text-foreground"
                      >
                        {{ enumValue }}
                      </span>
                    </div>
                  </div>
                </FieldGroup>

                <div v-if="schemaTree.children.length > 0" class="space-y-3">
                  <JsonSchemaTreeNode
                    v-for="child in schemaTree.children"
                    :key="child.id"
                    :node="child"
                  />
                </div>
              </div>

              <div v-else-if="activeTab === 'visual'" class="space-y-4">
                <StateBlock
                  tone="unavailable"
                  :title="t('console.shared.schemaVisualUnavailableTitle')"
                  :description="t('console.shared.schemaVisualUnavailableDescription')"
                />
                <CodeBlock
                  :label="label"
                  :value="schema"
                  max-height-class="max-h-[55vh]"
                  collapsible
                />
              </div>

              <CodeBlock
                v-else
                :label="label"
                :value="schema"
                max-height-class="max-h-[55vh]"
                collapsible
              />
            </div>
          </section>
        </div>
      </Teleport>
    </template>

    <CodeBlock
      v-else-if="schema"
      :label="label"
      :value="schema"
      :max-height-class="maxHeightClass"
      collapsible
    />
    <StateBlock
      v-else
      tone="empty"
      :title="emptyTitle ?? t('console.shared.schemaEmpty')"
      :description="emptyDescription"
    />
  </div>
</template>
