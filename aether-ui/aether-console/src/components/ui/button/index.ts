import type { VariantProps } from 'class-variance-authority'
import { cva } from 'class-variance-authority'

export { default as Button } from './Button.vue'

export const buttonVariants = cva(
  'inline-flex shrink-0 items-center justify-center whitespace-nowrap rounded-lg border border-transparent text-sm font-medium transition-[transform,box-shadow,background-color,color,border-color] duration-200 outline-none select-none disabled:pointer-events-none disabled:opacity-50 focus-visible:ring-2 focus-visible:ring-ring/20 active:scale-[0.98] [&_svg]:pointer-events-none [&_svg]:shrink-0',
  {
    variants: {
      variant: {
        default:
          'bg-[#222222] text-white shadow-console hover:bg-primary hover:shadow-console-hover',
        outline:
          'border-[rgb(34_34_34_/_0.08)] bg-white text-foreground shadow-console hover:-translate-y-px hover:shadow-console-hover',
        secondary:
          'bg-secondary text-secondary-foreground shadow-none hover:bg-[color-mix(in_srgb,var(--secondary)_88%,white)]',
        ghost:
          'bg-transparent text-foreground shadow-none hover:bg-secondary hover:text-foreground',
        destructive:
          'bg-destructive text-white shadow-console hover:brightness-105',
        link: 'rounded-none bg-transparent px-0 text-foreground underline-offset-4 hover:underline',
      },
      size: {
        default: 'h-11 gap-2 px-5',
        xs: 'h-8 gap-1.5 rounded-full px-3 text-xs',
        sm: 'h-9 gap-1.5 px-4 text-sm',
        lg: 'h-12 gap-2 px-6 text-base',
        icon: 'size-11 rounded-full',
        'icon-xs': 'size-8 rounded-full',
        'icon-sm': 'size-9 rounded-full',
        'icon-lg': 'size-12 rounded-full',
      },
    },
    defaultVariants: {
      variant: 'default',
      size: 'default',
    },
  },
)

export type ButtonVariants = VariantProps<typeof buttonVariants>
