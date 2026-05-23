package io.github.timemachinelab.infrastructure.importagent.planner.contract;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

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

    static boolean isValid(String value) {
        return normalize(value) != null;
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
}

