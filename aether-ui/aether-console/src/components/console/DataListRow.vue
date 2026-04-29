<script setup lang="ts">
const props = withDefaults(
  defineProps<{
    as?: 'button' | 'div'
    selected?: boolean
  }>(),
  {
    as: 'div',
    selected: false,
  },
)

defineEmits<{
  click: [MouseEvent]
}>()
</script>

<template>
  <component
    :is="props.as"
    data-console-data-list-row
    class="relative min-h-[72px] w-full rounded-[14px] border bg-white px-4 py-3 text-left shadow-console transition-[border-color,box-shadow,transform] duration-200"
    :class="[
      selected ? 'border-primary/35 ring-2 ring-primary/20' : 'border-[rgb(34_34_34_/_0.06)]',
      props.as === 'button'
        ? 'cursor-pointer hover:-translate-y-px hover:shadow-console-hover active:scale-[0.995]'
        : '',
    ]"
    @click="$emit('click', $event)"
  >
    <div class="flex flex-col gap-3 sm:flex-row sm:items-start sm:justify-between">
      <div class="min-w-0 flex-1 space-y-1">
        <slot name="title" />
        <slot name="description" />
        <div v-if="$slots.meta" class="flex flex-wrap gap-1.5 pt-1">
          <slot name="meta" />
        </div>
      </div>
      <div
        v-if="$slots.tags || $slots.actions"
        class="flex w-full flex-wrap items-center justify-start gap-2 sm:w-auto sm:shrink-0 sm:justify-end"
      >
        <slot name="tags" />
        <slot name="actions" />
      </div>
    </div>
  </component>
</template>
