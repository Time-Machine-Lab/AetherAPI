package io.github.timemachinelab.infrastructure.importagent.planner;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

@ImportAgentPlannerSubagentSpec(name = "plan_review", role = ImportAgentPlannerSubagentRole.PLAN_REVIEW, order = 40)
public class PlanReviewPlannerSubagent implements ImportAgentPlannerSubagent {

    @Override
    public void contribute(ImportAgentPlannerSubagentContext context, ObjectNode candidatePlan) {
        JsonNode assetPlans = candidatePlan.path("assetPlans");
        if (!assetPlans.isArray()) {
            return;
        }
        for (JsonNode assetNode : (ArrayNode) assetPlans) {
            reviewAuthConsistency(context, assetNode);
            reviewAsyncConsistency(context, assetNode);
        }
    }

    private void reviewAuthConsistency(ImportAgentPlannerSubagentContext context, JsonNode assetNode) {
        String apiCode = ImportAgentPlannerSubagentSupport.textValue(assetNode, "apiCode");
        String authScheme = ImportAgentPlannerSubagentSupport.textValue(assetNode, "authScheme");
        String authConfig = ImportAgentPlannerSubagentSupport.textValue(assetNode, "authConfig");
        if (authScheme != null && !"NONE".equalsIgnoreCase(authScheme) && (authConfig == null || authConfig.isBlank())) {
            context.addClarificationQuestion("资产计划 " + displayApiCode(apiCode) + " 缺少 authConfig，请确认上游鉴权配置。");
        }
    }

    private void reviewAsyncConsistency(ImportAgentPlannerSubagentContext context, JsonNode assetNode) {
        JsonNode asyncTaskConfig = assetNode.path("asyncTaskConfig");
        if (!asyncTaskConfig.isObject()) {
            return;
        }
        String queryUrlTemplate = ImportAgentPlannerSubagentSupport.textValue(asyncTaskConfig, "queryUrlTemplate");
        if (queryUrlTemplate != null && !queryUrlTemplate.contains("{taskId}")) {
            context.addClarificationQuestion("资产计划 " + displayApiCode(ImportAgentPlannerSubagentSupport.textValue(assetNode, "apiCode"))
                    + " 的 asyncTaskConfig.queryUrlTemplate 缺少 {taskId} 占位符，请确认。");
        }
    }

    private String displayApiCode(String apiCode) {
        return apiCode == null || apiCode.isBlank() ? "<unknown>" : apiCode;
    }
}