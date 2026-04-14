import { readFileSync } from 'node:fs'
import { dirname, resolve } from 'node:path'
import { fileURLToPath } from 'node:url'
import { mount } from '@vue/test-utils'
import { describe, expect, it } from 'vitest'
import { Badge } from '@/components/ui/badge'
import { buttonVariants } from '@/components/ui/button'
import Card from '@/components/ui/card/Card.vue'
import Input from '@/components/ui/input/Input.vue'

const currentDir = dirname(fileURLToPath(import.meta.url))
const styleSheet = readFileSync(resolve(currentDir, '../../../style.css'), 'utf8')

describe('design system foundation', () => {
  it('defines the luminous exchange surface and typography tokens', () => {
    expect(styleSheet).toContain('--surface: #f5f6f7;')
    expect(styleSheet).toContain('--surface-container-lowest: #ffffff;')
    expect(styleSheet).toContain('--primary: #a0383b;')
    expect(styleSheet).toContain('--primary-container: #fe7f7f;')
    expect(styleSheet).toContain('--primary-fixed-dim: #ee7373;')
    expect(styleSheet).toContain('--secondary-container: #ffc882;')
    expect(styleSheet).toContain('--on-surface-variant: #595c5d;')
    expect(styleSheet).toContain('--font-heading: "Manrope", "PingFang SC", sans-serif;')
    expect(styleSheet).toContain('--font-body: "Inter", "PingFang SC", sans-serif;')
  })

  it('uses luminous button variants instead of the default shadcn button skin', () => {
    expect(buttonVariants()).toContain('btn-ethereal')
    expect(buttonVariants()).toContain('btn-ethereal-primary')
    expect(buttonVariants({ variant: 'secondary' })).toContain('btn-ethereal-secondary')
    expect(buttonVariants({ variant: 'ghost' })).toContain('btn-ethereal-ghost')
    expect(styleSheet).toContain('border-radius: 1.5rem;')
    expect(styleSheet).toContain(
      'background: linear-gradient(135deg, var(--primary) 0%, var(--primary-container) 100%);',
    )
  })

  it('renders badges as pill-shaped category chips', () => {
    const wrapper = mount(Badge, {
      slots: { default: 'API' },
    })

    expect(wrapper.attributes('class')).toContain('badge-ethereal')
    expect(styleSheet).toContain('border-radius: 9999px;')
    expect(styleSheet).toContain('background: var(--secondary-container);')
  })

  it('renders cards with tonal depth instead of divider borders', () => {
    const wrapper = mount(Card, {
      slots: { default: 'Card content' },
    })

    expect(wrapper.attributes('class')).toContain('card-ethereal')
    expect(wrapper.attributes('class')).not.toContain('ring-1')
    expect(wrapper.attributes('class')).not.toContain('border')
    expect(styleSheet).toContain('.card-ethereal:hover')
    expect(styleSheet).toContain('background: var(--surface-bright);')
  })

  it('renders inputs with luminous ghost borders and a soft focus halo', () => {
    const wrapper = mount(Input)

    expect(wrapper.attributes('class')).toContain('input-ethereal')
    expect(wrapper.attributes('class')).not.toContain('border-input')
    expect(styleSheet).toContain('.input-ethereal:focus')
    expect(styleSheet).toContain('background: var(--surface-container-lowest);')
    expect(styleSheet).toContain('0 0 0 4px rgb(from var(--primary) r g b / 0.1)')
    expect(styleSheet).toContain('rgb(from var(--primary) r g b / 0.4)')
  })

  it('uses luminous glass and light code surfaces instead of dark hero panels', () => {
    expect(styleSheet).toContain('backdrop-filter: blur(24px);')
    expect(styleSheet).toContain('.architect-code')
    expect(styleSheet).toContain('background: var(--surface-container-highest);')
    expect(styleSheet).not.toContain('background: var(--primary-container);')
  })
})
