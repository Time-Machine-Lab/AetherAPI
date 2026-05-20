package io.github.timemachinelab.infrastructure.importagent.planner;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

@ImportAgentPlannerSubagentSpec(name = "document_facts", role = ImportAgentPlannerSubagentRole.DOCUMENT_FACTS, order = 10)
public class DocumentFactsPlannerSubagent implements ImportAgentPlannerSubagent {

    @Override
    public void contribute(ImportAgentPlannerSubagentContext context, ObjectNode candidatePlan) {
        JsonNode extractedFacts = context.getExtractedFacts();
        if (extractedFacts == null || !extractedFacts.isObject()) {
            return;
        }
        ArrayNode assetPlans = ImportAgentPlannerSubagentSupport.ensureArray(candidatePlan, "assetPlans");
        JsonNode assetFacts = extractedFacts.path("assetFacts");
        if (!assetFacts.isArray()) {
            return;
        }
        for (JsonNode factNode : assetFacts) {
            String apiCode = ImportAgentPlannerSubagentSupport.textValue(factNode, "apiCode");
            ObjectNode assetNode = ImportAgentPlannerSubagentSupport.findOrCreateAsset(assetPlans, apiCode);
            ImportAgentPlannerSubagentSupport.applyStringField(assetNode, "apiCode", factNode, "apiCode", context,
                    "资产计划的 API 编码存在冲突，请确认最终导入目标。");
            ImportAgentPlannerSubagentSupport.applyStringField(assetNode, "assetName", factNode, "assetName", context,
                    conflictMessage(apiCode, "assetName"));
            ImportAgentPlannerSubagentSupport.applyStringField(assetNode, "assetType", factNode, "assetType", context,
                    conflictMessage(apiCode, "assetType"));
            ImportAgentPlannerSubagentSupport.applyStringField(assetNode, "requestMethod", factNode, "requestMethod", context,
                    conflictMessage(apiCode, "requestMethod"));
            ImportAgentPlannerSubagentSupport.applyStringField(assetNode, "upstreamUrl", factNode, "upstreamUrl", context,
                    conflictMessage(apiCode, "upstreamUrl"));
            ImportAgentPlannerSubagentSupport.applyStringField(assetNode, "categoryCode", factNode, "categoryCode", context,
                    conflictMessage(apiCode, "categoryCode"));
            ImportAgentPlannerSubagentSupport.applyStringField(assetNode, "requestExample", factNode, "requestExample", context,
                    conflictMessage(apiCode, "requestExample"));
            ImportAgentPlannerSubagentSupport.applyStringField(assetNode, "responseExample", factNode, "responseExample", context,
                    conflictMessage(apiCode, "responseExample"));
        }
    }

    private String conflictMessage(String apiCode, String fieldName) {
        String displayApiCode = apiCode == null || apiCode.isBlank() ? "<unknown>" : apiCode;
        return "资产计划 " + displayApiCode + " 的" + displayFieldName(fieldName) + "存在冲突，请确认。";
    }

    private String displayFieldName(String fieldName) {
        return switch (fieldName) {
            case "assetName" -> "资产名称";
            case "assetType" -> "资产类型";
            case "requestMethod" -> "请求方法";
            case "upstreamUrl" -> "上游地址";
            case "categoryCode" -> "分类编码";
            case "requestExample" -> "请求示例";
            case "responseExample" -> "响应示例";
            default -> fieldName;
        };
    }
}
