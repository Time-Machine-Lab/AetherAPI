<script setup lang="ts">
import BrandMarkIcon from '@/components/icons/BrandMarkIcon.vue'
import { Badge } from '@/components/ui/badge'
import { Button } from '@/components/ui/button'
import {
  Card,
  CardContent,
  CardDescription,
  CardFooter,
  CardHeader,
  CardTitle,
} from '@/components/ui/card'

const signalKeys = ['throughput', 'latency', 'providers'] as const
const capabilityKeys = ['curation', 'orchestration', 'governance'] as const
const principleKeys = ['editorial', 'integration', 'control'] as const
const ritualKeys = ['discover', 'compose', 'ship'] as const
</script>

<route lang="json5">
{
  name: 'marketing-home',
  meta: {
    layout: 'MarketingLayout',
    titleKey: 'marketing.home.metaTitle',
    requiresAuth: false,
  },
}
</route>

<template>
  <div class="space-y-10 py-8 md:space-y-14 md:py-12">
    <section
      class="architect-hero hero-stage grid gap-8 rounded-[1.5rem] px-6 py-8 md:px-8 md:py-10 lg:grid-cols-[minmax(0,1.02fr)_minmax(24rem,0.98fr)] lg:items-start lg:px-12 lg:py-14"
    >
      <div class="relative z-10 space-y-8 pt-4 md:pt-8 lg:pl-10">
        <div class="space-y-5">
          <Badge class="ghost-outline bg-[color:rgb(from_var(--surface-container-lowest)_r_g_b_/_0.82)]" variant="outline">
            {{ $t('marketing.home.badge') }}
          </Badge>
          <p class="max-w-xl text-[0.75rem] font-semibold uppercase tracking-[0.28em] text-[color:var(--primary-fixed-dim)]">
            {{ $t('marketing.home.kicker') }}
          </p>
          <h1
            class="max-w-5xl font-[var(--font-heading)] text-5xl font-medium leading-[0.9] tracking-[-0.04em] text-foreground md:text-7xl"
          >
            {{ $t('marketing.home.headline') }}
          </h1>
          <p class="max-w-[62ch] text-base leading-8 text-muted-foreground md:text-lg">
            {{ $t('marketing.home.description') }}
          </p>
        </div>

        <div class="flex flex-wrap gap-3">
          <Button as-child size="lg">
            <a href="#platform">{{ $t('marketing.home.primaryAction') }}</a>
          </Button>
          <Button as-child size="lg" variant="secondary">
            <a href="#standards">{{ $t('marketing.home.secondaryAction') }}</a>
          </Button>
        </div>

        <div class="grid gap-4 md:grid-cols-3">
          <div
            v-for="signalKey in signalKeys"
            :key="signalKey"
            class="metric-slab rounded-[1.25rem] px-5 py-5 text-[color:var(--foreground)]"
          >
            <p class="text-[0.6875rem] font-bold uppercase tracking-[0.16em] text-muted-foreground">
              {{ $t(`marketing.home.signalCards.${signalKey}.label`) }}
            </p>
            <p class="mt-3 text-2xl font-semibold tracking-[-0.03em] text-foreground">
              {{ $t(`marketing.home.signalCards.${signalKey}.value`) }}
            </p>
            <p class="mt-3 max-w-[28ch] text-sm leading-7 text-muted-foreground">
              {{ $t(`marketing.home.signalCards.${signalKey}.description`) }}
            </p>
          </div>
        </div>
      </div>

      <div class="relative z-10 space-y-4 lg:-mr-6 lg:mt-2">
        <div class="hero-orbit ml-auto hidden max-w-[18rem] rounded-[1.5rem] px-5 py-5 md:block">
          <p class="text-[0.6875rem] font-bold uppercase tracking-[0.16em] text-[color:var(--primary-fixed-dim)]">
            {{ $t('marketing.home.platformQuote.label') }}
          </p>
          <p class="mt-3 font-[var(--font-heading)] text-2xl font-semibold leading-9 tracking-[-0.03em] text-foreground">
            {{ $t('marketing.home.platformQuote.value') }}
          </p>
        </div>

        <div class="architect-code rounded-[1.5rem] p-3 shadow-[0_24px_64px_-44px_var(--ambient-shadow-strong)]">
          <Card class="min-h-[18rem] bg-transparent text-foreground shadow-none hover:translate-y-0 hover:shadow-none">
            <CardHeader>
              <div class="flex items-start justify-between gap-4">
                <div class="space-y-3">
                  <Badge variant="secondary">{{ $t('marketing.home.heroPanel.badge') }}</Badge>
                  <CardTitle class="max-w-[14ch]">{{ $t('marketing.home.heroPanel.title') }}</CardTitle>
                </div>
                <BrandMarkIcon class="h-14 w-14 shrink-0" />
              </div>
              <CardDescription class="architect-metadata">
                {{ $t('marketing.home.heroPanel.description') }}
              </CardDescription>
            </CardHeader>
            <CardContent class="grid gap-5">
              <div class="grid gap-5 sm:grid-cols-2">
                <div>
                  <p class="architect-keyline text-[0.6875rem] font-bold uppercase tracking-[0.16em]">
                    {{ $t('marketing.home.heroPanel.metrics.curation.label') }}
                  </p>
                  <p class="mt-2 text-3xl font-semibold tracking-[-0.04em] text-foreground">
                    {{ $t('marketing.home.heroPanel.metrics.curation.value') }}
                  </p>
                </div>
                <div>
                  <p class="architect-keyline text-[0.6875rem] font-bold uppercase tracking-[0.16em]">
                    {{ $t('marketing.home.heroPanel.metrics.reliability.label') }}
                  </p>
                  <p class="mt-2 text-3xl font-semibold tracking-[-0.04em] text-foreground">
                    {{ $t('marketing.home.heroPanel.metrics.reliability.value') }}
                  </p>
                </div>
              </div>

              <div class="space-y-3">
                <div
                  v-for="ritualKey in ritualKeys"
                  :key="ritualKey"
                  class="rounded-[1rem] bg-[color:rgb(from_var(--surface-container-lowest)_r_g_b_/_0.76)] px-4 py-4 shadow-[inset_0_0_0_1px_rgb(from_var(--outline-variant)_r_g_b_/_0.15)]"
                >
                  <div class="flex items-baseline justify-between gap-4">
                    <p class="architect-metadata text-[0.6875rem] font-bold uppercase tracking-[0.16em]">
                      {{ $t(`marketing.home.rituals.${ritualKey}.step`) }}
                    </p>
                    <p class="text-sm font-medium text-[color:var(--secondary-fixed-dim)]">
                      {{ $t(`marketing.home.rituals.${ritualKey}.timing`) }}
                    </p>
                  </div>
                  <p class="mt-2 text-base font-semibold text-foreground">
                    {{ $t(`marketing.home.rituals.${ritualKey}.title`) }}
                  </p>
                  <p class="architect-metadata mt-2 max-w-[34ch] text-sm leading-7">
                    {{ $t(`marketing.home.rituals.${ritualKey}.description`) }}
                  </p>
                </div>
              </div>
            </CardContent>
          </Card>
        </div>
      </div>
    </section>

    <section
      id="summary"
      class="grid gap-8 px-1 py-6 lg:grid-cols-[minmax(0,0.8fr)_minmax(0,1.2fr)] lg:items-start"
    >
      <div class="space-y-4 lg:sticky lg:top-28 lg:pl-8">
        <p class="text-[0.75rem] font-semibold uppercase tracking-[0.28em] text-muted-foreground">
          {{ $t('marketing.home.summaryKicker') }}
        </p>
        <h2
          class="max-w-[12ch] font-[var(--font-heading)] text-4xl font-medium tracking-[-0.03em] text-foreground md:text-5xl"
        >
          {{ $t('marketing.home.summaryTitle') }}
        </h2>
        <p class="max-w-[58ch] text-base leading-8 text-muted-foreground">
          {{ $t('marketing.home.summaryDescription') }}
        </p>

        <div class="story-panel hidden rounded-[1.5rem] px-6 py-6 lg:block">
          <p class="text-[0.6875rem] font-bold uppercase tracking-[0.16em] text-[color:var(--primary-fixed-dim)]">
            {{ $t('marketing.home.manifesto.badge') }}
          </p>
          <p class="mt-3 font-[var(--font-heading)] text-2xl font-semibold leading-9 tracking-[-0.03em] text-foreground">
            {{ $t('marketing.home.manifesto.title') }}
          </p>
          <p class="mt-4 text-sm leading-7 text-muted-foreground">
            {{ $t('marketing.home.manifesto.footnote') }}
          </p>
        </div>
      </div>

      <div class="grid gap-4 md:grid-cols-2">
        <Card
          v-for="capabilityKey in capabilityKeys"
          :key="capabilityKey"
          class="architect-block odd:md:translate-y-10"
        >
          <CardHeader>
            <Badge variant="secondary">
              {{ $t(`marketing.home.capabilityCards.${capabilityKey}.badge`) }}
            </Badge>
            <CardTitle>{{ $t(`marketing.home.capabilityCards.${capabilityKey}.title`) }}</CardTitle>
            <CardDescription>
              {{ $t(`marketing.home.capabilityCards.${capabilityKey}.description`) }}
            </CardDescription>
          </CardHeader>
          <CardContent>
            <p class="max-w-[28ch] text-sm leading-7 text-muted-foreground">
              {{ $t(`marketing.home.capabilityCards.${capabilityKey}.detail`) }}
            </p>
          </CardContent>
        </Card>

        <Card class="architect-block md:col-span-2">
          <CardHeader>
            <Badge>{{ $t('marketing.home.manifesto.badge') }}</Badge>
            <CardTitle>{{ $t('marketing.home.manifesto.title') }}</CardTitle>
            <CardDescription>
              {{ $t('marketing.home.manifesto.description') }}
            </CardDescription>
          </CardHeader>
          <CardFooter class="flex-wrap justify-between gap-4">
            <p class="max-w-[46ch] text-sm leading-7 text-muted-foreground">
              {{ $t('marketing.home.manifesto.footnote') }}
            </p>
            <Button as-child variant="ghost">
              <a href="#cta">{{ $t('marketing.home.manifesto.action') }}</a>
            </Button>
          </CardFooter>
        </Card>
      </div>
    </section>

    <section id="platform" class="architect-section space-y-8 rounded-[1.5rem] px-6 py-8 md:px-8 md:py-10 lg:px-12">
      <div class="grid gap-8 lg:grid-cols-[minmax(0,1.05fr)_minmax(20rem,0.95fr)] lg:items-end">
        <div class="space-y-4 lg:pl-8">
          <p class="text-[0.75rem] font-semibold uppercase tracking-[0.28em] text-muted-foreground">
            {{ $t('marketing.home.platformKicker') }}
          </p>
          <h2
            class="max-w-[13ch] font-[var(--font-heading)] text-4xl font-medium tracking-[-0.03em] text-foreground md:text-5xl"
          >
            {{ $t('marketing.home.platformTitle') }}
          </h2>
          <p class="max-w-[64ch] text-base leading-8 text-muted-foreground">
            {{ $t('marketing.home.platformDescription') }}
          </p>
        </div>

        <div class="architect-block rounded-[1.25rem] px-6 py-6">
          <p class="text-[0.6875rem] font-bold uppercase tracking-[0.16em] text-muted-foreground">
            {{ $t('marketing.home.platformQuote.label') }}
          </p>
          <p class="mt-3 max-w-[30ch] font-[var(--font-heading)] text-2xl font-medium leading-10 tracking-[-0.03em] text-foreground">
            {{ $t('marketing.home.platformQuote.value') }}
          </p>
        </div>
      </div>

      <div class="grid gap-5 lg:grid-cols-3">
        <div
          v-for="principleKey in principleKeys"
          :key="principleKey"
          class="architect-block rounded-[1.25rem] px-6 py-6"
        >
          <p class="text-[0.6875rem] font-bold uppercase tracking-[0.16em] text-muted-foreground">
            {{ $t(`marketing.home.principles.${principleKey}.label`) }}
          </p>
          <h3 class="mt-3 font-[var(--font-heading)] text-2xl font-medium tracking-[-0.03em] text-foreground">
            {{ $t(`marketing.home.principles.${principleKey}.title`) }}
          </h3>
          <p class="mt-4 max-w-[30ch] text-sm leading-7 text-muted-foreground">
            {{ $t(`marketing.home.principles.${principleKey}.description`) }}
          </p>
        </div>
      </div>
    </section>

    <section id="standards" class="space-y-8 px-1 py-6">
      <div class="space-y-4 lg:pl-8">
        <p class="text-[0.75rem] font-semibold uppercase tracking-[0.28em] text-muted-foreground">
          {{ $t('marketing.home.standardsKicker') }}
        </p>
        <h2
          class="max-w-[12ch] font-[var(--font-heading)] text-4xl font-medium tracking-[-0.03em] text-foreground md:text-5xl"
        >
          {{ $t('marketing.home.standardsTitle') }}
        </h2>
        <p class="max-w-[64ch] text-base leading-8 text-muted-foreground">
          {{ $t('marketing.home.standardsDescription') }}
        </p>
      </div>

      <div class="architect-section rounded-[1.5rem] p-4 md:p-6">
        <div class="grid gap-4 lg:grid-cols-3">
          <Card
            v-for="ritualKey in ritualKeys"
            :key="`${ritualKey}-standard`"
            size="sm"
            class="architect-block min-h-full bg-[color:var(--surface-container-lowest)]"
          >
            <CardHeader>
              <Badge variant="outline">{{ $t(`marketing.home.rituals.${ritualKey}.step`) }}</Badge>
              <CardTitle class="text-[1.25rem]">
                {{ $t(`marketing.home.standardsCards.${ritualKey}.title`) }}
              </CardTitle>
              <CardDescription>
                {{ $t(`marketing.home.standardsCards.${ritualKey}.description`) }}
              </CardDescription>
            </CardHeader>
          </Card>
        </div>
      </div>
    </section>

    <section id="cta" class="pb-10 md:pb-16">
      <div class="surface-glass cta-stage ghost-outline rounded-[1.5rem] px-6 py-8 md:px-10 md:py-12">
        <div class="grid gap-8 lg:grid-cols-[minmax(0,1fr)_auto] lg:items-end">
          <div class="space-y-4 lg:pl-8">
            <p class="text-[0.75rem] font-semibold uppercase tracking-[0.28em] text-muted-foreground">
              {{ $t('marketing.home.cta.kicker') }}
            </p>
            <h2
              class="max-w-[14ch] font-[var(--font-heading)] text-4xl font-medium tracking-[-0.03em] text-foreground md:text-5xl"
            >
              {{ $t('marketing.home.cta.title') }}
            </h2>
            <p class="max-w-[58ch] text-base leading-8 text-muted-foreground">
              {{ $t('marketing.home.cta.description') }}
            </p>
          </div>

          <div class="flex flex-wrap gap-3 lg:justify-end">
            <Button as-child size="lg">
              <a href="#top">{{ $t('marketing.home.cta.primaryAction') }}</a>
            </Button>
            <Button as-child size="lg" variant="secondary">
              <a href="#summary">{{ $t('marketing.home.cta.secondaryAction') }}</a>
            </Button>
          </div>
        </div>
      </div>
    </section>
  </div>
</template>
