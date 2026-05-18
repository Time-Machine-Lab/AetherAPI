import { describe, expect, it } from 'vitest'
import { parseSchemaDocument, summarizeSchemaTree } from './schema-visualization'

describe('schema-visualization', () => {
  it('parses object properties, required flags, enums, and nested children', () => {
    const parsed = parseSchemaDocument(`{
      "type": "object",
      "required": ["city", "mode"],
      "properties": {
        "city": { "type": "string", "description": "City name" },
        "mode": { "type": "string", "enum": ["current", "weekly"] },
        "meta": {
          "type": "object",
          "properties": {
            "unit": { "type": ["string", "null"], "nullable": true, "default": "celsius" }
          }
        }
      }
    }`)

    expect(parsed.validJson).toBe(true)
    expect(parsed.visualizable).toBe(true)
    expect(parsed.tree?.typeLabel).toBe('object')
    expect(parsed.tree?.children).toHaveLength(3)
    expect(parsed.tree?.children[0]).toMatchObject({
      label: 'city',
      required: true,
      typeLabel: 'string',
      description: 'City name',
    })
    expect(parsed.tree?.children[1]).toMatchObject({
      label: 'mode',
      required: true,
      enumValues: ['current', 'weekly'],
    })
    expect(parsed.tree?.children[2].children[0]).toMatchObject({
      label: 'unit',
      nullable: true,
      defaultValue: 'celsius',
      typeLabel: 'string | null',
    })

    expect(summarizeSchemaTree(parsed.tree)).toEqual({
      typeLabel: 'object',
      fieldCount: 3,
      requiredCount: 2,
      enumCount: 1,
    })
  })

  it('parses array item schemas and variant branches', () => {
    const parsed = parseSchemaDocument(`{
      "type": "array",
      "items": {
        "type": "object",
        "properties": {
          "status": { "oneOf": [{ "type": "string" }, { "type": "number" }] }
        }
      }
    }`)

    expect(parsed.tree?.typeLabel).toBe('array')
    expect(parsed.tree?.children[0]).toMatchObject({
      label: 'items',
      relation: 'items',
      typeLabel: 'object',
    })
    expect(parsed.tree?.children[0].children[0].children.map((child) => child.label)).toEqual([
      'oneOf 1',
      'oneOf 2',
    ])
  })

  it('falls back when schema text is invalid json', () => {
    const parsed = parseSchemaDocument('{invalid-schema')

    expect(parsed.validJson).toBe(false)
    expect(parsed.visualizable).toBe(false)
    expect(parsed.tree).toBeNull()
  })
})