package io.github.timemachinelab.infrastructure.importagent.planner;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

final class ImportAgentPlannerSubagentSupport {

    private ImportAgentPlannerSubagentSupport() {
    }

    static ArrayNode ensureArray(ObjectNode root, String fieldName) {
        JsonNode existing = root.path(fieldName);
        if (existing.isArray()) {
            return (ArrayNode) existing;
        }
        return root.putArray(fieldName);
    }

    static ObjectNode ensureObject(ObjectNode root, String fieldName) {
        JsonNode existing = root.path(fieldName);
        if (existing.isObject()) {
            return (ObjectNode) existing;
        }
        return root.putObject(fieldName);
    }

    static ObjectNode findAssetByApiCode(ArrayNode assetPlans, String apiCode) {
        if (assetPlans == null || apiCode == null || apiCode.isBlank()) {
            return null;
        }
        for (JsonNode assetNode : assetPlans) {
            if (assetNode.isObject() && apiCode.equals(assetNode.path("apiCode").asText(null))) {
                return (ObjectNode) assetNode;
            }
        }
        return null;
    }

    static ObjectNode findOrCreateAsset(ArrayNode assetPlans, String apiCode) {
        ObjectNode existing = findAssetByApiCode(assetPlans, apiCode);
        if (existing != null) {
            return existing;
        }
        ObjectNode created = assetPlans.addObject();
        if (apiCode != null && !apiCode.isBlank()) {
            created.put("apiCode", apiCode);
        }
        return created;
    }

    static void applyStringField(
            ObjectNode target,
            String fieldName,
            JsonNode source,
            String sourceFieldName,
            ImportAgentPlannerSubagentContext context,
            String conflictMessage) {
        if (target == null || source == null) {
            return;
        }
        String incoming = textValue(source, sourceFieldName);
        if (incoming == null || incoming.isBlank()) {
            return;
        }
        String current = textValue(target, fieldName);
        if (current == null || current.isBlank()) {
            target.put(fieldName, incoming);
            return;
        }
        if (!current.equals(incoming)) {
            context.addClarificationQuestion(conflictMessage);
        }
    }

    static void applyBooleanField(
            ObjectNode target,
            String fieldName,
            JsonNode source,
            String sourceFieldName) {
        if (target == null || source == null) {
            return;
        }
        JsonNode incoming = source.path(sourceFieldName);
        if (!incoming.isBoolean()) {
            return;
        }
        if (!target.has(fieldName)) {
            target.put(fieldName, incoming.asBoolean());
        }
    }

    static String textValue(JsonNode node, String fieldName) {
        if (node == null || fieldName == null) {
            return null;
        }
        JsonNode child = node.path(fieldName);
        if (!child.isValueNode()) {
            return null;
        }
        String value = child.asText(null);
        return value == null || value.isBlank() ? null : value.trim();
    }
}