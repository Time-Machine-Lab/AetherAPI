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
            ImportAgentPlannerSubagentSupport.applyStringField(asyncTaskConfig, "statusPath", asyncHint, "statusPath", context,
                    conflictMessage(apiCode, "asyncTaskConfig.statusPath"));
            ImportAgentPlannerSubagentSupport.applyStringField(asyncTaskConfig, "resultPath", asyncHint, "resultPath", context,
                    conflictMessage(apiCode, "asyncTaskConfig.resultPath"));
            ImportAgentPlannerSubagentSupport.applyStringField(asyncTaskConfig, "errorPath", asyncHint, "errorPath", context,
                    conflictMessage(apiCode, "asyncTaskConfig.errorPath"));
            if (!asyncTaskConfig.has("enabled")) {
                asyncTaskConfig.put("enabled", true);
            }
            if (!asyncTaskConfig.has("authMode") && hasText(ImportAgentPlannerSubagentSupport.textValue(asyncTaskConfig, "queryUrlTemplate"))) {
                asyncTaskConfig.put("authMode", "SAME_AS_SUBMIT");
            }
        }
    }

    private String conflictMessage(String apiCode, String fieldName) {
        String displayApiCode = apiCode == null || apiCode.isBlank() ? "<unknown>" : apiCode;
        return "资产计划 " + displayApiCode + " 的" + displayFieldName(fieldName) + "存在冲突，请确认。";
    }

    private String displayFieldName(String fieldName) {
        return switch (fieldName) {
            case "asyncTaskConfig.queryMethod" -> "任务查询方法";
            case "asyncTaskConfig.queryUrlTemplate" -> "任务查询 URL 模板";
            case "asyncTaskConfig.authMode" -> "任务查询鉴权模式";
            case "asyncTaskConfig.authScheme" -> "任务查询鉴权方式";
            case "asyncTaskConfig.authConfig" -> "任务查询鉴权信息";
            case "asyncTaskConfig.statusPath" -> "任务状态路径";
            case "asyncTaskConfig.resultPath" -> "任务结果路径";
            case "asyncTaskConfig.errorPath" -> "任务错误路径";
            default -> fieldName;
        };
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
