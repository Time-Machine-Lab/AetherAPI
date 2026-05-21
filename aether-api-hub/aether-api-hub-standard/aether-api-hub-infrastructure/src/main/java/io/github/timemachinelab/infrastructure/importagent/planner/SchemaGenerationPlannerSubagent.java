package io.github.timemachinelab.infrastructure.importagent.planner;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

@ImportAgentPlannerSubagentSpec(name = "schema_generation", role = ImportAgentPlannerSubagentRole.SCHEMA_GENERATION, order = 35)
public class SchemaGenerationPlannerSubagent implements ImportAgentPlannerSubagent {

    @Override
    public void contribute(ImportAgentPlannerSubagentContext context, ObjectNode candidatePlan) {
        JsonNode assetPlansNode = candidatePlan.path("assetPlans");
        if (!assetPlansNode.isArray()) {
            return;
        }
        ArrayNode assetPlans = (ArrayNode) assetPlansNode;
        applySchemaHints(context, assetPlans, context.getExtractedFacts());
        applySchemaHints(context, assetPlans, context.getSlotPatches());
        inferSchemasFromExamples(assetPlans);
        inferExamplesFromSchemas(assetPlans);
    }

    private void applySchemaHints(
            ImportAgentPlannerSubagentContext context,
            ArrayNode assetPlans,
            JsonNode source) {
        if (source == null || !source.isObject()) {
            return;
        }
        JsonNode schemaHints = source.path("schemaHints");
        if (!schemaHints.isArray()) {
            schemaHints = source.path("assetPlans");
        }
        if (!schemaHints.isArray()) {
            return;
        }
        for (JsonNode hintNode : schemaHints) {
            String apiCode = ImportAgentPlannerSubagentSupport.textValue(hintNode, "apiCode");
            ObjectNode assetNode = ImportAgentPlannerSubagentSupport.findAssetByApiCode(assetPlans, apiCode);
            if (assetNode == null) {
                continue;
            }
            applySchemaField(context, assetNode, hintNode, "requestJsonSchema", "请求体 Schema");
            applySchemaField(context, assetNode, hintNode, "responseJsonSchema", "响应体 Schema");
        }
    }

    private void applySchemaField(
            ImportAgentPlannerSubagentContext context,
            ObjectNode assetNode,
            JsonNode source,
            String fieldName,
            String displayName) {
        if (!source.has(fieldName)) {
            return;
        }
        String normalized = ImportAgentSchemaNormalizer.normalize(source.path(fieldName));
        if (normalized == null) {
            context.addClarificationQuestion("资产计划 " + displayApiCode(assetNode)
                    + " 的" + displayName + "需要是合法 JSON 对象；请提供请求/响应示例或字段说明。");
            return;
        }
        String current = ImportAgentSchemaNormalizer.normalize(ImportAgentPlannerSubagentSupport.textValue(assetNode, fieldName));
        if (current == null) {
            assetNode.put(fieldName, normalized);
            return;
        }
        if (!current.equals(normalized)) {
            context.addClarificationQuestion("资产计划 " + displayApiCode(assetNode)
                    + " 的" + displayName + "存在冲突，请确认应使用哪份结构。");
        }
    }

    private void inferSchemasFromExamples(ArrayNode assetPlans) {
        for (JsonNode node : assetPlans) {
            if (!node.isObject()) {
                continue;
            }
            ObjectNode assetNode = (ObjectNode) node;
            inferSchemaFromExample(assetNode, "requestJsonSchema", "requestExample");
            inferSchemaFromExample(assetNode, "responseJsonSchema", "responseExample");
        }
    }

    private void inferSchemaFromExample(ObjectNode assetNode, String schemaFieldName, String exampleFieldName) {
        if (ImportAgentSchemaNormalizer.normalize(ImportAgentPlannerSubagentSupport.textValue(assetNode, schemaFieldName)) != null) {
            return;
        }
        String inferred = ImportAgentSchemaNormalizer.inferFromExample(
                normalizeExampleForSchema(assetNode, exampleFieldName));
        if (inferred != null) {
            assetNode.put(schemaFieldName, inferred);
        }
    }

    private String normalizeExampleForSchema(ObjectNode assetNode, String exampleFieldName) {
        String example = ImportAgentPlannerSubagentSupport.textValue(assetNode, exampleFieldName);
        return "requestExample".equals(exampleFieldName)
                ? ImportAgentExampleNormalizer.normalizeRequestBodyExample(example)
                : ImportAgentExampleNormalizer.normalizeJsonObjectExample(example);
    }

    private void inferExamplesFromSchemas(ArrayNode assetPlans) {
        for (JsonNode node : assetPlans) {
            if (!node.isObject()) {
                continue;
            }
            ObjectNode assetNode = (ObjectNode) node;
            inferExampleFromSchema(assetNode, "requestExample", "requestJsonSchema");
            inferExampleFromSchema(assetNode, "responseExample", "responseJsonSchema");
        }
    }

    private void inferExampleFromSchema(ObjectNode assetNode, String exampleFieldName, String schemaFieldName) {
        if (ImportAgentPlannerSubagentSupport.textValue(assetNode, exampleFieldName) != null) {
            return;
        }
        String inferred = ImportAgentSchemaNormalizer.inferExampleFromSchema(
                ImportAgentPlannerSubagentSupport.textValue(assetNode, schemaFieldName));
        if (inferred != null) {
            assetNode.put(exampleFieldName, inferred);
        }
    }

    private String displayApiCode(ObjectNode assetNode) {
        String apiCode = ImportAgentPlannerSubagentSupport.textValue(assetNode, "apiCode");
        return apiCode == null || apiCode.isBlank() ? "<unknown>" : apiCode;
    }
}
