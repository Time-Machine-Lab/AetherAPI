type SchemaKeyword = Record<string, unknown>

export type SchemaNodeRelation = 'root' | 'property' | 'items' | 'variant' | 'additionalProperties'

export interface SchemaDisplayNode {
  id: string
  label: string
  path: string
  relation: SchemaNodeRelation
  typeLabel: string
  required: boolean
  enumValues: string[]
  description?: string
  format?: string
  defaultValue?: string
  nullable?: boolean
  children: SchemaDisplayNode[]
}

export interface ParsedSchemaDocument {
  raw: string
  validJson: boolean
  visualizable: boolean
  tree: SchemaDisplayNode | null
}

export interface SchemaDisplaySummary {
  typeLabel: string
  fieldCount: number
  requiredCount: number
  enumCount: number
}

export function parseSchemaDocument(value?: string | null): ParsedSchemaDocument {
  const raw = value?.trim() ?? ''
  if (!raw) {
    return {
      raw,
      validJson: false,
      visualizable: false,
      tree: null,
    }
  }

  try {
    const parsed = JSON.parse(raw)
    if (!isSchemaKeyword(parsed)) {
      return {
        raw,
        validJson: true,
        visualizable: false,
        tree: null,
      }
    }

    const tree = buildSchemaNode('root', parsed, false, '$', 'root')
    return {
      raw,
      validJson: true,
      visualizable: true,
      tree,
    }
  } catch {
    return {
      raw,
      validJson: false,
      visualizable: false,
      tree: null,
    }
  }
}

export function summarizeSchemaTree(tree: SchemaDisplayNode | null): SchemaDisplaySummary | null {
  if (!tree) {
    return null
  }

  const directFields = tree.children.filter((child) => child.relation === 'property')
  return {
    typeLabel: tree.typeLabel,
    fieldCount: directFields.length,
    requiredCount: directFields.filter((child) => child.required).length,
    enumCount: directFields.filter((child) => child.enumValues.length > 0).length,
  }
}

function buildSchemaNode(
  label: string,
  schema: SchemaKeyword,
  required: boolean,
  path: string,
  relation: SchemaNodeRelation,
): SchemaDisplayNode {
  const children: SchemaDisplayNode[] = []
  const requiredSet = new Set(toStringArray(schema.required))

  const properties = schema.properties
  if (isSchemaKeyword(properties)) {
    for (const [propertyName, propertySchema] of Object.entries(properties)) {
      if (!isSchemaKeyword(propertySchema)) {
        continue
      }
      children.push(
        buildSchemaNode(
          propertyName,
          propertySchema,
          requiredSet.has(propertyName),
          `${path}.${propertyName}`,
          'property',
        ),
      )
    }
  }

  const items = schema.items
  if (isSchemaKeyword(items)) {
    children.push(buildSchemaNode('items', items, false, `${path}[]`, 'items'))
  } else if (Array.isArray(items)) {
    items.forEach((itemSchema, index) => {
      if (!isSchemaKeyword(itemSchema)) {
        return
      }
      children.push(
        buildSchemaNode(`item ${index + 1}`, itemSchema, false, `${path}[${index}]`, 'items'),
      )
    })
  }

  appendVariantChildren(children, schema, 'oneOf', path)
  appendVariantChildren(children, schema, 'anyOf', path)
  appendVariantChildren(children, schema, 'allOf', path)

  const additionalProperties = schema.additionalProperties
  if (isSchemaKeyword(additionalProperties)) {
    children.push(
      buildSchemaNode(
        'additionalProperties',
        additionalProperties,
        false,
        `${path}.*`,
        'additionalProperties',
      ),
    )
  }

  return {
    id: path,
    label,
    path,
    relation,
    typeLabel: resolveTypeLabel(schema),
    required,
    enumValues: toEnumValues(schema.enum),
    description: toOptionalString(schema.description),
    format: toOptionalString(schema.format),
    defaultValue: formatLiteral(schema.default),
    nullable: resolveNullable(schema),
    children,
  }
}

function appendVariantChildren(
  children: SchemaDisplayNode[],
  schema: SchemaKeyword,
  keyword: 'oneOf' | 'anyOf' | 'allOf',
  path: string,
) {
  const variants = schema[keyword]
  if (!Array.isArray(variants)) {
    return
  }

  variants.forEach((variant, index) => {
    if (!isSchemaKeyword(variant)) {
      return
    }
    children.push(
      buildSchemaNode(
        `${keyword} ${index + 1}`,
        variant,
        false,
        `${path}.${keyword}[${index}]`,
        'variant',
      ),
    )
  })
}

function resolveTypeLabel(schema: SchemaKeyword): string {
  const schemaType = schema.type
  if (typeof schemaType === 'string' && schemaType) {
    return schemaType
  }

  if (Array.isArray(schemaType)) {
    const types = schemaType.filter((value): value is string => typeof value === 'string')
    if (types.length > 0) {
      return types.join(' | ')
    }
  }

  if (Array.isArray(schema.enum) && schema.enum.length > 0) {
    return 'enum'
  }

  if (Array.isArray(schema.oneOf) && schema.oneOf.length > 0) {
    return 'oneOf'
  }

  if (Array.isArray(schema.anyOf) && schema.anyOf.length > 0) {
    return 'anyOf'
  }

  if (Array.isArray(schema.allOf) && schema.allOf.length > 0) {
    return 'allOf'
  }

  if (isSchemaKeyword(schema.properties)) {
    return 'object'
  }

  if (schema.items !== undefined) {
    return 'array'
  }

  return 'unknown'
}

function resolveNullable(schema: SchemaKeyword): boolean {
  if (schema.nullable === true) {
    return true
  }

  return Array.isArray(schema.type) && schema.type.includes('null')
}

function toEnumValues(value: unknown): string[] {
  if (!Array.isArray(value)) {
    return []
  }

  return value.map((item) => formatLiteral(item) ?? '').filter(Boolean)
}

function toStringArray(value: unknown): string[] {
  if (!Array.isArray(value)) {
    return []
  }

  return value.filter((item): item is string => typeof item === 'string')
}

function toOptionalString(value: unknown): string | undefined {
  return typeof value === 'string' && value.trim() ? value.trim() : undefined
}

function formatLiteral(value: unknown): string | undefined {
  if (value === undefined) {
    return undefined
  }
  if (typeof value === 'string') {
    return value
  }
  return JSON.stringify(value)
}

function isSchemaKeyword(value: unknown): value is SchemaKeyword {
  return typeof value === 'object' && value !== null && !Array.isArray(value)
}
