export interface FormattedCodeContent {
  display: string
  source: string
  language: 'json' | 'text'
  formatted: boolean
}

function stringifySource(value: unknown): string {
  if (value === null || value === undefined) {
    return ''
  }

  if (typeof value === 'string') {
    return value
  }

  return JSON.stringify(value)
}

export function formatCodeContent(value: unknown): FormattedCodeContent {
  const source = stringifySource(value)

  if (!source.trim()) {
    return {
      display: '',
      source: '',
      language: 'text',
      formatted: false,
    }
  }

  if (typeof value !== 'string') {
    return {
      display: JSON.stringify(value, null, 2),
      source: JSON.stringify(value),
      language: 'json',
      formatted: true,
    }
  }

  try {
    return {
      display: JSON.stringify(JSON.parse(source), null, 2),
      source,
      language: 'json',
      formatted: true,
    }
  } catch {
    return {
      display: source,
      source,
      language: 'text',
      formatted: false,
    }
  }
}

export async function copyTextToClipboard(text: string): Promise<boolean> {
  if (!navigator?.clipboard?.writeText) {
    return false
  }

  try {
    await navigator.clipboard.writeText(text)
    return true
  } catch {
    return false
  }
}
