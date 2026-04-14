import type { VariantProps } from 'class-variance-authority'
import { cva } from 'class-variance-authority'

export { default as Button } from './Button.vue'

export const buttonVariants = cva(
  'btn-ethereal group/button shrink-0 [&_svg:not([class*=size-])]:size-4 [&_svg]:pointer-events-none [&_svg]:shrink-0',
  {
    variants: {
      variant: {
        default: 'btn-ethereal-primary',
        outline: 'btn-ethereal-secondary',
        secondary: 'btn-ethereal-secondary',
        ghost: 'btn-ethereal-ghost',
        destructive: 'btn-ethereal-destructive',
        link: 'btn-ethereal-link',
      },
      size: {
        default: 'min-h-10 px-4 text-sm',
        xs: 'min-h-8 px-3 text-xs [&_svg:not([class*=size-])]:size-3',
        sm: 'min-h-9 px-3.5 text-[0.82rem] [&_svg:not([class*=size-])]:size-3.5',
        lg: 'min-h-12 px-5 text-sm',
        icon: 'size-10',
        'icon-xs': 'size-8 [&_svg:not([class*=size-])]:size-3',
        'icon-sm': 'size-9',
        'icon-lg': 'size-12',
      },
    },
    defaultVariants: {
      variant: 'default',
      size: 'default',
    },
  },
)
export type ButtonVariants = VariantProps<typeof buttonVariants>
