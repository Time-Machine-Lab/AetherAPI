package io.github.timemachinelab.infrastructure.importagent.planner;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

@ImportAgentPlannerSubagentSpec(name = "async_pattern", role = ImportAgentPlannerSubagentRole.ASYNC_PATTERN, order = 30)
public class AsyncPatternPlannerSubagent implements ImportAgentPlannerSubagent {

    @Override
    public void contribute(ImportAgentPlannerSubagentContext context, ObjectNode candidatePlan) {
        JsonNode extractedFacts = context.getExtractedFacts();
        if (extractedFacts == null || !extractedFacts.path("asyncHints").isArray()) {
            return;
        }
        ArrayNode assetPlans = ImportAgentPlannerSubagentSupport.ensureArray(candidatePlan, "assetPlans");
        for (JsonNode asyncHint : extractedFacts.path("asyncHints")) {
            String apiCode = ImportAgentPlannerSubagentSupport.textValue(asyncHint, "apiCode");
            ObjectNode assetNode = ImportAgentPlannerSubagentSupport.findAssetByApiCode(assetPlans, apiCode);
            if (assetNode == null) {
                continue;
            }
            ObjectNode asyncTaskConfig = ImportAgentPlannerSubagentSupport.ensureObject(assetNode, "asyncTaskConfig");
            ImportAgentPlannerSubagentSupport.applyBooleanField(asyncTaskConfig, "enabled", asyncHint, "enabled");
            ImportAgentPlannerSubagentSupport.applyStringField(asyncTaskConfig, "queryMethod", asyncHint, "queryMethod", context,
                    conflictMessage(apiCode, "asyncTaskConfig.queryMethod"));
            ImportAgentPlannerSubagentSupport.applyStringField(asyncTaskConfig, "queryUrlTemplate", asyncHint, "queryUrlTemplate", context,
                    conflictMessage(apiCode, "asyncTaskConfig.queryUrlTemplate"));
            ImportAgentPlannerSubagentSupport.applyStringField(asyncTaskConfig, "authMode", asyncHint, "authMode", context,
                    conflictMessage(apiCode, "asyncTaskConfig.authMode"));
            ImportAgentPlannerSubagentSupport.applyStringField(asyncTaskConfig, "authScheme", asyncHint, "authScheme", context,
                    conflictMessage(apiCode, "asyncTaskConfig.authScheme"));
            ImportAgentPlannerSubagentSupport.applyStringField(asyncTaskConfig, "authConfig", asyncHint, "authConfig", context,
                    conflictMessage(apiCode, "asyncTaskConfig.authConfig"));
        }
    }

    private String conflictMessage(String apiCode, String fieldName) {
        String displayApiCode = apiCode == null || apiCode.isBlank() ? "<unknown>" : apiCode;
        return "资产计划 " + displayApiCode + " 的 " + fieldName + " 存在冲突，请确认。";
    }
}