import type { VariantProps } from 'class-variance-authority'
import { cva } from 'class-variance-authority'

export { default as Badge } from './Badge.vue'

export const badgeVariants = cva(
  'badge-ethereal group/badge w-fit shrink-0 overflow-hidden whitespace-nowrap [&>svg]:pointer-events-none [&>svg]:size-3!',
  {
    variants: {
      variant: {
        default: 'badge-ethereal-default',
        secondary: 'badge-ethereal-secondary',
        destructive: 'badge-ethereal-destructive',
        outline: 'badge-ethereal-outline',
        ghost: 'badge-ethereal-ghost',
        link: 'badge-ethereal-link',
      },
    },
    defaultVariants: {
      variant: 'default',
    },
  },
)
export type BadgeVariants = VariantProps<typeof badgeVariants>
