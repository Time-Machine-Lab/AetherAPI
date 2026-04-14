<script setup lang="ts">
import type { HTMLAttributes } from 'vue'

const props = defineProps<{
  class?: HTMLAttributes['class']
}>()
</script>

<template>
  <svg viewBox="0 0 64 64" fill="none" xmlns="http://www.w3.org/2000/svg" :class="props.class">
    <rect x="6" y="6" width="52" height="52" rx="18" fill="url(#brand-gradient)" />
    <path
      d="M22 39.5L31.2 18H35.8L45 39.5H39.9L37.9 34.5H28.9L26.9 39.5H22ZM30.6 30.8H36.2L33.4 23.8L30.6 30.8Z"
      fill="white"
    />
    <defs>
      <linearGradient
        id="brand-gradient"
        x1="8"
        y1="10"
        x2="56"
        y2="54"
        gradientUnits="userSpaceOnUse"
      >
        <stop stop-color="#A0383B" />
        <stop offset="1" stop-color="#FE7F7F" />
      </linearGradient>
    </defs>
  </svg>
</template>
