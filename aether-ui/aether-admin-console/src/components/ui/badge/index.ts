import type { VariantProps } from 'class-variance-authority'
import { cva } from 'class-variance-authority'

export { default as Badge } from './Badge.vue'

export const badgeVariants = cva(
  'h-7 gap-1 rounded-full border border-transparent px-3 py-1 text-[0.7rem] font-semibold uppercase tracking-[0.22em] transition-all has-data-[icon=inline-end]:pr-2.5 has-data-[icon=inline-start]:pl-2.5 [&>svg]:size-3! group/badge inline-flex w-fit shrink-0 items-center justify-center overflow-hidden whitespace-nowrap focus-visible:ring-2 focus-visible:ring-ring/30 [&>svg]:pointer-events-none',
  {
    variants: {
      variant: {
        default: 'bg-[linear-gradient(135deg,var(--primary),#2563eb)] text-primary-foreground',
        secondary: 'bg-secondary text-secondary-foreground',
        destructive:
          'bg-[color-mix(in_srgb,var(--destructive)_14%,white)] text-destructive focus-visible:ring-destructive/20',
        outline: 'bg-card text-foreground shadow-[inset_0_0_0_1px_rgb(25_28_30_/_0.08)]',
        ghost: 'bg-transparent text-primary',
        link: 'text-primary underline-offset-4 hover:underline',
      },
    },
    defaultVariants: {
      variant: 'default',
    },
  },
)
export type BadgeVariants = VariantProps<typeof badgeVariants>
