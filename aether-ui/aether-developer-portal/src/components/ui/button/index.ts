import type { VariantProps } from 'class-variance-authority'
import { cva } from 'class-variance-authority'

export { default as Button } from './Button.vue'

export const buttonVariants = cva(
  'rounded-xl border border-transparent text-sm font-semibold shadow-[0_18px_40px_-26px_rgba(25,28,30,0.45)] [&_svg:not([class*=size-])]:size-4 group/button inline-flex shrink-0 items-center justify-center whitespace-nowrap transition-all outline-none select-none disabled:pointer-events-none disabled:opacity-50 [&_svg]:pointer-events-none [&_svg]:shrink-0 focus-visible:ring-2 focus-visible:ring-ring/30 active:not-aria-[haspopup]:translate-y-px',
  {
    variants: {
      variant: {
        default:
          'bg-[linear-gradient(135deg,var(--primary),#2563eb)] text-primary-foreground hover:brightness-110',
        outline:
          'bg-card text-foreground shadow-none hover:bg-secondary/90 aria-expanded:bg-secondary/90 aria-expanded:text-foreground',
        secondary:
          'bg-secondary text-secondary-foreground shadow-none hover:bg-[color-mix(in_srgb,var(--secondary)_84%,white)] aria-expanded:bg-secondary aria-expanded:text-secondary-foreground',
        ghost:
          'bg-transparent text-primary shadow-none hover:bg-accent/70 hover:text-primary aria-expanded:bg-accent/70 aria-expanded:text-primary',
        destructive:
          'bg-[color-mix(in_srgb,var(--destructive)_14%,white)] text-destructive shadow-none hover:bg-[color-mix(in_srgb,var(--destructive)_20%,white)] focus-visible:ring-destructive/20',
        link: 'text-primary underline-offset-4 hover:underline',
      },
      size: {
        default:
          'h-11 gap-2 px-5 has-data-[icon=inline-end]:pr-4 has-data-[icon=inline-start]:pl-4',
        xs: 'h-8 gap-1.5 rounded-lg px-3 text-xs shadow-none in-data-[slot=button-group]:rounded-lg has-data-[icon=inline-end]:pr-2.5 has-data-[icon=inline-start]:pl-2.5 [&_svg:not([class*=size-])]:size-3',
        sm: 'h-9 gap-1.5 rounded-xl px-4 text-sm shadow-none in-data-[slot=button-group]:rounded-xl has-data-[icon=inline-end]:pr-3.5 has-data-[icon=inline-start]:pl-3.5 [&_svg:not([class*=size-])]:size-3.5',
        lg: 'h-12 gap-2 px-6 text-base has-data-[icon=inline-end]:pr-5 has-data-[icon=inline-start]:pl-5',
        icon: 'size-11',
        'icon-xs':
          'size-8 rounded-lg shadow-none in-data-[slot=button-group]:rounded-lg [&_svg:not([class*=size-])]:size-3',
        'icon-sm':
          'size-9 rounded-xl shadow-none in-data-[slot=button-group]:rounded-xl',
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
