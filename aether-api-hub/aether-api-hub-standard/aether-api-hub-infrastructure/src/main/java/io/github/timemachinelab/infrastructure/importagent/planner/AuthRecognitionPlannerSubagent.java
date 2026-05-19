package io.github.timemachinelab.infrastructure.importagent.planner;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

@ImportAgentPlannerSubagentSpec(name = "auth_recognition", role = ImportAgentPlannerSubagentRole.AUTH_RECOGNITION, order = 20)
public class AuthRecognitionPlannerSubagent implements ImportAgentPlannerSubagent {

    @Override
    public void contribute(ImportAgentPlannerSubagentContext context, ObjectNode candidatePlan) {
        ArrayNode assetPlans = ImportAgentPlannerSubagentSupport.ensureArray(candidatePlan, "assetPlans");
        mergeAuthHints(context, assetPlans);
        mergeSlotPatches(context, assetPlans);
    }

    private void mergeAuthHints(ImportAgentPlannerSubagentContext context, ArrayNode assetPlans) {
        JsonNode extractedFacts = context.getExtractedFacts();
        if (extractedFacts == null || !extractedFacts.path("authHints").isArray()) {
            return;
        }
        for (JsonNode authHint : extractedFacts.path("authHints")) {
            applyAuthFields(context, assetPlans, authHint);
        }
    }

    private void mergeSlotPatches(ImportAgentPlannerSubagentContext context, ArrayNode assetPlans) {
        JsonNode slotPatches = context.getSlotPatches();
        if (slotPatches == null || !slotPatches.path("assetPlans").isArray()) {
            return;
        }
        for (JsonNode assetPatch : slotPatches.path("assetPlans")) {
            applyAuthFields(context, assetPlans, assetPatch);
        }
    }

    private void applyAuthFields(ImportAgentPlannerSubagentContext context, ArrayNode assetPlans, JsonNode authNode) {
        String apiCode = ImportAgentPlannerSubagentSupport.textValue(authNode, "apiCode");
        ObjectNode assetNode = ImportAgentPlannerSubagentSupport.findAssetByApiCode(assetPlans, apiCode);
        if (assetNode == null) {
            return;
        }
        ImportAgentPlannerSubagentSupport.applyStringField(assetNode, "authScheme", authNode, "authScheme", context,
                conflictMessage(apiCode, "authScheme"));
        ImportAgentPlannerSubagentSupport.applyStringField(assetNode, "authConfig", authNode, "authConfig", context,
                conflictMessage(apiCode, "authConfig"));
    }

    private String conflictMessage(String apiCode, String fieldName) {
        String displayApiCode = apiCode == null || apiCode.isBlank() ? "<unknown>" : apiCode;
        return "资产计划 " + displayApiCode + " 的 " + fieldName + " 存在冲突，请确认。";
    }
}