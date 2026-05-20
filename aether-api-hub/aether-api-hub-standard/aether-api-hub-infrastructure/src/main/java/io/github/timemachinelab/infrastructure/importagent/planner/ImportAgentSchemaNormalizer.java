package io.github.timemachinelab.infrastructure.importagent.planner;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.Iterator;
import java.util.Set;

final class ImportAgentSchemaNormalizer {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final Set<String> JSON_SCHEMA_TYPES = Set.of(
            "object", "array", "string", "integer", "number", "boolean", "null");

    private ImportAgentSchemaNormalizer() {
    }

    static String normalize(JsonNode node) {
        if (node == null || node.isMissingNode() || node.isNull()) {
            return null;
        }
        if (node.isObject()) {
            return serializeObject((ObjectNode) node);
        }
        if (node.isTextual()) {
            return normalize(node.asText(null));
        }
        return null;
    }

    static String normalize(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            JsonNode node = OBJECT_MAPPER.readTree(value.trim());
            return node.isObject() ? serializeObject((ObjectNode) node) : null;
        } catch (Exception ex) {
            return null;
        }
    }

    static String normalizeOrCurrent(JsonNode node, String currentValue) {
        String normalized = normalize(node);
        if (normalized != null) {
            return normalized;
        }
        return normalize(currentValue);
    }

    static String inferFromExample(String example) {
        if (example == null || example.isBlank()) {
            return null;
        }
        try {
            JsonNode node = OBJECT_MAPPER.readTree(example.trim());
            if (!node.isObject()) {
                return null;
            }
            ObjectNode schema = buildSchema(node);
            return OBJECT_MAPPER.writeValueAsString(schema);
        } catch (Exception ex) {
            return null;
        }
    }

    static boolean isValid(String value) {
        return normalize(value) != null;
    }

    static String inferExampleFromSchema(String schemaValue) {
        String normalized = normalize(schemaValue);
        if (normalized == null) {
            return null;
        }
        try {
            JsonNode schema = OBJECT_MAPPER.readTree(normalized);
            JsonNode example = buildExample(schema, "value");
            return example != null && example.isObject() ? OBJECT_MAPPER.writeValueAsString(example) : null;
        } catch (Exception ex) {
            return null;
        }
    }

    private static String serializeObject(ObjectNode node) {
        if (!hasSupportedType(node)) {
            return null;
        }
        try {
            return OBJECT_MAPPER.writeValueAsString(node);
        } catch (Exception ex) {
            return null;
        }
    }

    private static boolean hasSupportedType(ObjectNode node) {
        JsonNode typeNode = node.path("type");
        if (typeNode.isMissingNode() || typeNode.isNull()) {
            return true;
        }
        if (typeNode.isTextual()) {
            return JSON_SCHEMA_TYPES.contains(typeNode.asText());
        }
        if (!typeNode.isArray()) {
            return false;
        }
        for (JsonNode item : typeNode) {
            if (!item.isTextual() || !JSON_SCHEMA_TYPES.contains(item.asText())) {
                return false;
            }
        }
        return true;
    }

    private static ObjectNode buildSchema(JsonNode node) {
        ObjectNode schema = OBJECT_MAPPER.createObjectNode();
        if (node.isObject()) {
            schema.put("type", "object");
            ObjectNode properties = schema.putObject("properties");
            ArrayNode required = schema.putArray("required");
            node.fieldNames().forEachRemaining(fieldName -> {
                properties.set(fieldName, buildSchema(node.path(fieldName)));
                required.add(fieldName);
            });
            if (required.isEmpty()) {
                schema.remove("required");
            }
            return schema;
        }
        if (node.isArray()) {
            schema.put("type", "array");
            if (!node.isEmpty()) {
                schema.set("items", buildSchema(node.get(0)));
            }
            return schema;
        }
        if (node.isIntegralNumber()) {
            schema.put("type", "integer");
            return schema;
        }
        if (node.isNumber()) {
            schema.put("type", "number");
            return schema;
        }
        if (node.isBoolean()) {
            schema.put("type", "boolean");
            return schema;
        }
        if (node.isNull()) {
            schema.put("type", "null");
            return schema;
        }
        schema.put("type", "string");
        return schema;
    }

    private static JsonNode buildExample(JsonNode schema, String fieldName) {
        if (schema == null || !schema.isObject()) {
            return JsonNodeFactory.instance.textNode(exampleText(fieldName));
        }
        JsonNode explicitExample = firstPresent(schema, "example", "default", "const");
        if (explicitExample != null && !explicitExample.isMissingNode() && !explicitExample.isNull()) {
            return explicitExample;
        }
        JsonNode enumNode = schema.path("enum");
        if (enumNode.isArray() && !enumNode.isEmpty()) {
            return enumNode.get(0);
        }
        String type = primaryType(schema.path("type"));
        if (type == null) {
            if (schema.path("properties").isObject()) {
                type = "object";
            } else if (schema.path("items").isObject()) {
                type = "array";
            } else {
                type = "string";
            }
        }
        return switch (type) {
            case "object" -> buildObjectExample(schema);
            case "array" -> buildArrayExample(schema, fieldName);
            case "integer" -> JsonNodeFactory.instance.numberNode(1);
            case "number" -> JsonNodeFactory.instance.numberNode(1.0);
            case "boolean" -> JsonNodeFactory.instance.booleanNode(true);
            case "null" -> JsonNodeFactory.instance.nullNode();
            default -> JsonNodeFactory.instance.textNode(exampleText(fieldName));
        };
    }

    private static ObjectNode buildObjectExample(JsonNode schema) {
        ObjectNode example = OBJECT_MAPPER.createObjectNode();
        JsonNode properties = schema.path("properties");
        if (!properties.isObject()) {
            return example;
        }
        Iterator<String> fieldNames = properties.fieldNames();
        while (fieldNames.hasNext()) {
            String fieldName = fieldNames.next();
            example.set(fieldName, buildExample(properties.path(fieldName), fieldName));
        }
        return example;
    }

    private static ArrayNode buildArrayExample(JsonNode schema, String fieldName) {
        ArrayNode array = OBJECT_MAPPER.createArrayNode();
        JsonNode items = schema.path("items");
        if (items.isObject()) {
            array.add(buildExample(items, singularName(fieldName)));
        }
        return array;
    }

    private static JsonNode firstPresent(JsonNode schema, String... fieldNames) {
        for (String fieldName : fieldNames) {
            JsonNode value = schema.path(fieldName);
            if (!value.isMissingNode()) {
                return value;
            }
        }
        return null;
    }

    private static String primaryType(JsonNode typeNode) {
        if (typeNode.isTextual()) {
            return typeNode.asText();
        }
        if (!typeNode.isArray()) {
            return null;
        }
        for (JsonNode item : typeNode) {
            if (item.isTextual() && !"null".equals(item.asText())) {
                return item.asText();
            }
        }
        return null;
    }

    private static String exampleText(String fieldName) {
        if (fieldName == null || fieldName.isBlank()) {
            return "string";
        }
        return "example-" + fieldName.replaceAll("[^A-Za-z0-9_-]+", "-");
    }

    private static String singularName(String fieldName) {
        if (fieldName == null || fieldName.length() < 2) {
            return "item";
        }
        return fieldName.endsWith("s") ? fieldName.substring(0, fieldName.length() - 1) : "item";
    }
}
