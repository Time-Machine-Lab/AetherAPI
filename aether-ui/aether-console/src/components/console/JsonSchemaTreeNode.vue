<script setup lang="ts">
import { computed } from 'vue'
import { useI18n } from 'vue-i18n'
import DisplayTag from '@/components/console/DisplayTag.vue'
import type { SchemaDisplayNode } from '@/utils/schema-visualization'

defineOptions({
  name: 'JsonSchemaTreeNode',
})

const props = withDefaults(
  defineProps<{
    node: SchemaDisplayNode
    level?: number
  }>(),
  {
    level: 0,
  },
)

const { t, te } = useI18n()

const expandable = computed(() => props.node.children.length > 0)
const defaultOpen = computed(() => props.level < 2)
const localizedType = computed(() => formatTypeLabel(props.node.typeLabel))
const localizedLabel = computed(() => {
  if (props.node.label === 'items') {
    return t('console.shared.schemaItems')
  }
  if (props.node.label === 'additionalProperties') {
    return t('console.shared.schemaAdditionalProperties')
  }
  return props.node.label
})
const showRequired = computed(() => props.node.relation === 'property')

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
  <details
    v-if="expandable"
    data-console-schema-tree-node
    :open="defaultOpen"
    class="rounded-[16px] border border-[rgb(34_34_34_/_0.06)] bg-white/90"
  >
    <summary
      class="cursor-pointer list-none px-4 py-3 [&::-webkit-details-marker]:hidden"
      :class="level > 0 ? 'ml-3' : ''"
    >
      <div class="flex flex-wrap items-center justify-between gap-3">
        <div class="min-w-0 space-y-1">
          <p class="text-sm font-semibold text-foreground">{{ localizedLabel }}</p>
          <p class="text-xs text-muted-foreground">{{ node.path }}</p>
        </div>
        <div class="flex flex-wrap items-center gap-2">
          <DisplayTag tone="info" :label="localizedType" />
          <DisplayTag
            v-if="showRequired"
            :tone="node.required ? 'danger' : 'neutral'"
            :label="node.required ? t('console.shared.required') : t('console.shared.optional')"
          />
          <DisplayTag
            v-if="node.enumValues.length > 0"
            tone="warning"
            :label="t('console.shared.schemaEnumCount', { count: node.enumValues.length })"
          />
        </div>
      </div>
    </summary>

    <div
      class="space-y-3 border-t border-[rgb(34_34_34_/_0.06)] px-4 py-4"
      :class="level > 0 ? 'ml-3' : ''"
    >
      <p v-if="node.description" class="text-sm leading-6 text-muted-foreground">
        {{ node.description }}
      </p>

      <div
        v-if="node.format || node.defaultValue !== undefined || node.nullable !== undefined"
        class="grid gap-3 text-xs text-muted-foreground sm:grid-cols-3"
      >
        <div v-if="node.format" class="space-y-1">
          <p class="font-semibold text-foreground">{{ t('console.shared.schemaFormatLabel') }}</p>
          <p>{{ node.format }}</p>
        </div>
        <div v-if="node.defaultValue !== undefined" class="space-y-1">
          <p class="font-semibold text-foreground">{{ t('console.shared.schemaDefaultLabel') }}</p>
          <p class="font-mono text-[11px]">{{ node.defaultValue }}</p>
        </div>
        <div v-if="node.nullable !== undefined" class="space-y-1">
          <p class="font-semibold text-foreground">{{ t('console.shared.schemaNullableLabel') }}</p>
          <p>{{ node.nullable ? t('console.shared.yes') : t('console.shared.no') }}</p>
        </div>
      </div>

      <div v-if="node.enumValues.length > 0" class="space-y-2">
        <p class="text-xs font-semibold text-foreground">
          {{ t('console.shared.schemaEnumValues') }}
        </p>
        <div class="flex flex-wrap gap-2">
          <span
            v-for="enumValue in node.enumValues"
            :key="enumValue"
            class="rounded-full border border-[rgb(34_34_34_/_0.08)] bg-secondary px-2.5 py-1 font-mono text-[11px] text-foreground"
          >
            {{ enumValue }}
          </span>
        </div>
      </div>

      <div class="space-y-3">
        <JsonSchemaTreeNode
          v-for="child in node.children"
          :key="child.id"
          :node="child"
          :level="level + 1"
        />
      </div>
    </div>
  </details>

  <div
    v-else
    data-console-schema-tree-node
    class="rounded-[16px] border border-[rgb(34_34_34_/_0.06)] bg-white/90 px-4 py-3"
    :class="level > 0 ? 'ml-3' : ''"
  >
    <div class="flex flex-wrap items-start justify-between gap-3">
      <div class="min-w-0 space-y-1">
        <p class="text-sm font-semibold text-foreground">{{ localizedLabel }}</p>
        <p class="text-xs text-muted-foreground">{{ node.path }}</p>
      </div>
      <div class="flex flex-wrap items-center gap-2">
        <DisplayTag tone="info" :label="localizedType" />
        <DisplayTag
          v-if="showRequired"
          :tone="node.required ? 'danger' : 'neutral'"
          :label="node.required ? t('console.shared.required') : t('console.shared.optional')"
        />
      </div>
    </div>

    <p v-if="node.description" class="mt-3 text-sm leading-6 text-muted-foreground">
      {{ node.description }}
    </p>

    <div
      v-if="node.format || node.defaultValue !== undefined || node.nullable !== undefined"
      class="mt-3 grid gap-3 text-xs text-muted-foreground sm:grid-cols-3"
    >
      <div v-if="node.format" class="space-y-1">
        <p class="font-semibold text-foreground">{{ t('console.shared.schemaFormatLabel') }}</p>
        <p>{{ node.format }}</p>
      </div>
      <div v-if="node.defaultValue !== undefined" class="space-y-1">
        <p class="font-semibold text-foreground">{{ t('console.shared.schemaDefaultLabel') }}</p>
        <p class="font-mono text-[11px]">{{ node.defaultValue }}</p>
      </div>
      <div v-if="node.nullable !== undefined" class="space-y-1">
        <p class="font-semibold text-foreground">{{ t('console.shared.schemaNullableLabel') }}</p>
        <p>{{ node.nullable ? t('console.shared.yes') : t('console.shared.no') }}</p>
      </div>
    </div>

    <div v-if="node.enumValues.length > 0" class="mt-3 space-y-2">
      <p class="text-xs font-semibold text-foreground">
        {{ t('console.shared.schemaEnumValues') }}
      </p>
      <div class="flex flex-wrap gap-2">
        <span
          v-for="enumValue in node.enumValues"
          :key="enumValue"
          class="rounded-full border border-[rgb(34_34_34_/_0.08)] bg-secondary px-2.5 py-1 font-mono text-[11px] text-foreground"
        >
          {{ enumValue }}
        </span>
      </div>
    </div>
  </div>
</template>
