export type DisplayTone = 'neutral' | 'success' | 'warning' | 'danger' | 'info' | 'ai' | 'api'

export type ConsoleMethod = 'GET' | 'POST' | 'PUT' | 'PATCH' | 'DELETE'

export function methodTone(method?: string | null): DisplayTone {
  if (method === 'GET') return 'success'
  if (method === 'POST') return 'info'
  if (method === 'PUT' || method === 'PATCH') return 'warning'
  if (method === 'DELETE') return 'danger'
  return 'neutral'
}

export function assetTypeTone(assetType?: string | null): DisplayTone {
  return assetType === 'AI_API' ? 'ai' : 'api'
}

export function successTone(success?: boolean | null): DisplayTone {
  if (success === true) return 'success'
  if (success === false) return 'danger'
  return 'neutral'
}

export function displayToneClasses(tone: DisplayTone): string {
  const classes: Record<DisplayTone, string> = {
    neutral: 'bg-secondary text-secondary-foreground',
    success: 'bg-[color-mix(in_srgb,var(--primary)_10%,white)] text-primary',
    warning: 'bg-amber-50 text-amber-700',
    danger: 'bg-[color-mix(in_srgb,var(--destructive)_10%,white)] text-destructive',
    info: 'bg-[color-mix(in_srgb,var(--palette-text-legal)_10%,white)] text-[var(--palette-text-legal)]',
    ai: 'bg-[color-mix(in_srgb,var(--chart-3)_10%,white)] text-[var(--chart-3)]',
    api: 'bg-[color-mix(in_srgb,var(--primary)_10%,white)] text-primary',
  }
  return classes[tone]
}
