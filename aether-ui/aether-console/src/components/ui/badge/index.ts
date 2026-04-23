import type { VariantProps } from 'class-variance-authority'
import { cva } from 'class-variance-authority'

export { default as Badge } from './Badge.vue'

export const badgeVariants = cva(
  'inline-flex w-fit shrink-0 items-center justify-center overflow-hidden rounded-full border px-3 py-1 text-[0.69rem] font-semibold uppercase tracking-[0.22em] whitespace-nowrap transition-colors [&>svg]:pointer-events-none [&>svg]:size-3',
  {
    variants: {
      variant: {
        default: 'border-transparent bg-[color-mix(in_srgb,var(--primary)_12%,white)] text-primary',
        secondary: 'border-transparent bg-secondary text-secondary-foreground',
        destructive:
          'border-transparent bg-[color-mix(in_srgb,var(--destructive)_12%,white)] text-destructive',
        outline: 'border-[rgb(34_34_34_/_0.08)] bg-white text-foreground shadow-console',
        ghost: 'border-transparent bg-transparent text-muted-foreground',
        link: 'border-transparent bg-transparent px-0 text-foreground underline-offset-4 hover:underline',
        'status-enabled':
          'border-transparent bg-[color-mix(in_srgb,var(--primary)_10%,white)] text-primary cursor-default',
        'status-disabled':
          'border-transparent bg-secondary text-secondary-foreground cursor-default',
        'type-ai':
          'border-transparent bg-[color-mix(in_srgb,var(--chart-3)_10%,white)] text-[var(--chart-3)] cursor-default',
        'type-api':
          'border-transparent bg-[color-mix(in_srgb,var(--primary)_10%,white)] text-primary cursor-default',
      },
    },
    defaultVariants: {
      variant: 'default',
    },
  },
)

export type BadgeVariants = VariantProps<typeof badgeVariants>
