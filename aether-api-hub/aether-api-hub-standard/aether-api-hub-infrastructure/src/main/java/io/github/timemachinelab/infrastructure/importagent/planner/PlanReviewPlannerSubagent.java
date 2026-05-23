package io.github.timemachinelab.infrastructure.importagent.planner;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import io.github.timemachinelab.service.model.ImportAgentCategoryCandidateModel;
import io.github.timemachinelab.service.model.ImportAgentPlanModel;
import io.github.timemachinelab.service.model.ImportAssetPlanModel;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

@ImportAgentPlannerSubagentSpec(name = "plan_review", role = ImportAgentPlannerSubagentRole.PLAN_REVIEW, order = 40)
public class PlanReviewPlannerSubagent implements ImportAgentPlannerSubagent {

    @Override
    public void contribute(ImportAgentPlannerSubagentContext context, ObjectNode candidatePlan) {
        JsonNode assetPlans = candidatePlan.path("assetPlans");
        if (!assetPlans.isArray()) {
            return;
        }
        ArrayNode assetArray = (ArrayNode) assetPlans;
        Map<String, String> enabledCategories = enabledCategoryCodes(context);
        Set<String> categoryPlanCodes = categoryPlanCodes(candidatePlan.path("categoryPlans"));
        for (JsonNode assetNode : assetArray) {
            reviewCategoryConsistency(context, assetNode, enabledCategories, categoryPlanCodes);
            reviewSchemaConsistency(context, assetNode);
            reviewAuthConsistency(context, assetNode);
            reviewAsyncConsistency(context, assetNode);
            reviewAiProfileConsistency(context, assetNode);
        }
        reviewAsyncPairs(context, assetArray);
    }

    private void reviewCategoryConsistency(
            ImportAgentPlannerSubagentContext context,
            JsonNode assetNode,
            Map<String, String> enabledCategories,
            Set<String> categoryPlanCodes) {
        if (!assetNode.isObject() || !assetNode.path("publishAfterImport").asBoolean(false) || enabledCategories.isEmpty()) {
            return;
        }
        String apiCode = ImportAgentPlannerSubagentSupport.textValue(assetNode, "apiCode");
        String categoryCode = ImportAgentPlannerSubagentSupport.textValue(assetNode, "categoryCode");
        if (categoryCode == null) {
            context.addClarificationQuestion("资产计划 " + displayApiCode(apiCode)
                    + " 需要从当前启用分类中选择分类编码；候选：" + categorySuggestions(enabledCategories) + "。");
            return;
        }
        String categoryKey = normalizeKey(categoryCode);
        if (!enabledCategories.containsKey(categoryKey)) {
            context.addClarificationQuestion("资产计划 " + displayApiCode(apiCode)
                    + " 的分类编码 " + categoryCode + " 不在当前启用分类中，请从候选分类中选择；候选："
                    + categorySuggestions(enabledCategories) + "。");
            return;
        }
        if (!categoryPlanCodes.contains(categoryKey)) {
            context.addClarificationQuestion("资产计划 " + displayApiCode(apiCode)
                    + " 已选择分类 " + categoryCode + "，但分类计划中缺少对应的 USE_EXISTING 记录，请确认分类计划。");
        }
    }

    private Map<String, String> enabledCategoryCodes(ImportAgentPlannerSubagentContext context) {
        Map<String, String> values = new LinkedHashMap<>();
        for (ImportAgentCategoryCandidateModel candidate : context.getRequest().getAvailableCategories()) {
            if (candidate == null || candidate.getCategoryCode() == null || candidate.getCategoryCode().isBlank()) {
                continue;
            }
            String status = candidate.getStatus();
            if (status != null && !status.isBlank() && !"ENABLED".equalsIgnoreCase(status.trim())) {
                continue;
            }
            values.put(normalizeKey(candidate.getCategoryCode()),
                    candidate.getCategoryName() == null || candidate.getCategoryName().isBlank()
                            ? candidate.getCategoryCode().trim()
                            : candidate.getCategoryName().trim());
        }
        return values;
    }

    private Set<String> categoryPlanCodes(JsonNode categoryPlans) {
        Set<String> values = new LinkedHashSet<>();
        if (!categoryPlans.isArray()) {
            return values;
        }
        for (JsonNode categoryPlan : categoryPlans) {
            String categoryCode = ImportAgentPlannerSubagentSupport.textValue(categoryPlan, "categoryCode");
            if (categoryCode != null) {
                values.add(normalizeKey(categoryCode));
            }
        }
        return values;
    }

    private String categorySuggestions(Map<String, String> enabledCategories) {
        return enabledCategories.entrySet().stream()
                .limit(3)
                .map(entry -> entry.getKey() + "（" + entry.getValue() + "）")
                .reduce((left, right) -> left + "、" + right)
                .orElse("请先创建或启用分类");
    }

    private void reviewSchemaConsistency(ImportAgentPlannerSubagentContext context, JsonNode assetNode) {
        if (!assetNode.isObject()) {
            return;
        }
        ObjectNode assetObject = (ObjectNode) assetNode;
        reviewSchemaField(context, assetObject, "requestJsonSchema", "请求体 Schema");
        reviewSchemaField(context, assetObject, "responseJsonSchema", "响应体 Schema");
    }

    private void reviewSchemaField(
            ImportAgentPlannerSubagentContext context,
            ObjectNode assetNode,
            String fieldName,
            String displayName) {
        if (!assetNode.has(fieldName)) {
            return;
        }
        String normalized = ImportAgentSchemaNormalizer.normalize(assetNode.path(fieldName));
        if (normalized != null) {
            assetNode.put(fieldName, normalized);
            return;
        }
        String current = currentSchema(context, assetNode, fieldName);
        if (current != null) {
            assetNode.put(fieldName, current);
            return;
        }
        assetNode.remove(fieldName);
        context.addClarificationQuestion("资产计划 " + displayApiCode(ImportAgentPlannerSubagentSupport.textValue(assetNode, "apiCode"))
                + " 的" + displayName + "需要是合法 JSON 对象，请提供请求/响应示例或字段说明。");
    }

    private void reviewAuthConsistency(ImportAgentPlannerSubagentContext context, JsonNode assetNode) {
        String apiCode = ImportAgentPlannerSubagentSupport.textValue(assetNode, "apiCode");
        String authScheme = ImportAgentPlannerSubagentSupport.textValue(assetNode, "authScheme");
        String authConfig = ImportAgentPlannerSubagentSupport.textValue(assetNode, "authConfig");
        boolean publishAfterImport = assetNode.path("publishAfterImport").asBoolean(false);
        if (publishAfterImport && (authScheme == null || authScheme.isBlank())) {
            context.addClarificationQuestion("资产计划 " + displayApiCode(apiCode) + " 需要确认上游鉴权方式。");
            return;
        }
        if (authScheme != null && !"NONE".equalsIgnoreCase(authScheme) && (authConfig == null || authConfig.isBlank())) {
            context.addClarificationQuestion("资产计划 " + displayApiCode(apiCode) + " 需要补充上游鉴权相关信息，例如 Header/Query 名称、环境变量名或凭证来源；Agent 会据此生成配置。");
        }
    }

    private void reviewAsyncConsistency(ImportAgentPlannerSubagentContext context, JsonNode assetNode) {
        JsonNode asyncTaskConfig = assetNode.path("asyncTaskConfig");
        if (!asyncTaskConfig.isObject()) {
            return;
        }
        if (!asyncTaskConfig.path("enabled").asBoolean(false)) {
            return;
        }
        String apiCode = ImportAgentPlannerSubagentSupport.textValue(assetNode, "apiCode");
        String authMode = normalizedToken(ImportAgentPlannerSubagentSupport.textValue(asyncTaskConfig, "authMode"));
        String authScheme = normalizedToken(ImportAgentPlannerSubagentSupport.textValue(asyncTaskConfig, "authScheme"));
        String authConfig = ImportAgentPlannerSubagentSupport.textValue(asyncTaskConfig, "authConfig");
        String queryMethod = ImportAgentPlannerSubagentSupport.textValue(asyncTaskConfig, "queryMethod");
        String queryUrlTemplate = ImportAgentPlannerSubagentSupport.textValue(asyncTaskConfig, "queryUrlTemplate");
        String normalizedQueryUrlTemplate = ImportAgentPlannerJsonSupport.normalizeAsyncTaskQueryUrlTemplate(queryUrlTemplate);
        if (normalizedQueryUrlTemplate != null && !normalizedQueryUrlTemplate.equals(queryUrlTemplate)) {
            ((ObjectNode) asyncTaskConfig).put("queryUrlTemplate", normalizedQueryUrlTemplate);
            queryUrlTemplate = normalizedQueryUrlTemplate;
        }
        if (authMode == null) {
            context.addClarificationQuestion("资产计划 " + displayApiCode(apiCode) + " 启用了异步查询，请确认查询鉴权是复用提交接口还是单独覆盖。");
        } else if (!"SAME_AS_SUBMIT".equals(authMode) && !"OVERRIDE".equals(authMode)) {
            context.addClarificationQuestion("资产计划 " + displayApiCode(apiCode)
                    + " 的任务查询鉴权模式只能是 SAME_AS_SUBMIT 或 OVERRIDE，请确认。");
        }
        if (queryMethod == null) {
            context.addClarificationQuestion("资产计划 " + displayApiCode(apiCode) + " 需要确认任务查询接口的请求方法。");
        }
        if (queryUrlTemplate == null || queryUrlTemplate.isBlank()) {
            context.addClarificationQuestion("资产计划 " + displayApiCode(apiCode) + " 需要确认任务查询接口 URL 模板。");
            return;
        }
        if (!queryUrlTemplate.contains("{taskId}")) {
            context.addClarificationQuestion("资产计划 " + displayApiCode(apiCode)
                    + " 的任务查询 URL 模板需要包含 {taskId} 占位符，请确认。");
        }
        if ("OVERRIDE".equals(authMode)) {
            if (authScheme == null) {
                context.addClarificationQuestion("资产计划 " + displayApiCode(apiCode) + " 的任务查询使用独立鉴权，请确认任务查询鉴权方式。");
            }
            if (authConfig == null || authConfig.isBlank()) {
                context.addClarificationQuestion("资产计划 " + displayApiCode(apiCode) + " 的任务查询使用独立鉴权，请补充任务查询鉴权相关信息；Agent 会据此生成配置。");
            }
        }
    }

    private void reviewAiProfileConsistency(ImportAgentPlannerSubagentContext context, JsonNode assetNode) {
        if (!assetNode.path("publishAfterImport").asBoolean(false)) {
            return;
        }
        String assetType = normalizedToken(ImportAgentPlannerSubagentSupport.textValue(assetNode, "assetType"));
        if (!"AI_API".equals(assetType)) {
            return;
        }
        JsonNode aiProfile = assetNode.path("aiProfile");
        String provider = ImportAgentPlannerSubagentSupport.textValue(aiProfile, "provider");
        String model = ImportAgentPlannerSubagentSupport.textValue(aiProfile, "model");
        if (provider == null || model == null) {
            context.addClarificationQuestion("资产计划 " + displayApiCode(ImportAgentPlannerSubagentSupport.textValue(assetNode, "apiCode"))
                    + " 是待发布的 AI API，请补充 AI 能力信息，包括服务商和模型名称。");
        }
    }

    private void reviewAsyncPairs(ImportAgentPlannerSubagentContext context, ArrayNode assetPlans) {
        for (int index = 0; index < assetPlans.size(); index += 1) {
            JsonNode queryAsset = assetPlans.get(index);
            if (!isAsyncQueryAsset(queryAsset)) {
                continue;
            }
            JsonNode submitAsset = findSubmitAsset(assetPlans, index);
            if (submitAsset == null) {
                continue;
            }
            JsonNode asyncTaskConfig = submitAsset.path("asyncTaskConfig");
            if (asyncTaskConfig.isObject() && asyncTaskConfig.path("enabled").asBoolean(false)) {
                continue;
            }
            context.addClarificationQuestion("检测到资产计划 " + displayApiCode(ImportAgentPlannerSubagentSupport.textValue(queryAsset, "apiCode"))
                    + " 更像任务查询接口；请为资产计划 "
                    + displayApiCode(ImportAgentPlannerSubagentSupport.textValue(submitAsset, "apiCode"))
                    + " 补充任务查询方法、任务查询 URL 模板和任务查询鉴权模式。");
        }
    }

    private JsonNode findSubmitAsset(ArrayNode assetPlans, int queryIndex) {
        JsonNode queryAsset = assetPlans.get(queryIndex);
        int bestIndex = -1;
        int bestScore = -1;
        for (int index = 0; index < assetPlans.size(); index += 1) {
            if (index == queryIndex) {
                continue;
            }
            JsonNode candidate = assetPlans.get(index);
            if (!candidate.isObject() || isAsyncQueryAsset(candidate)) {
                continue;
            }
            int score = scoreSubmitCandidate(candidate, queryAsset);
            if (score > bestScore) {
                bestScore = score;
                bestIndex = index;
            }
        }
        return bestScore >= 4 ? assetPlans.get(bestIndex) : null;
    }

    private String currentSchema(ImportAgentPlannerSubagentContext context, ObjectNode assetNode, String fieldName) {
        ImportAgentPlanModel currentPlan = context.getRequest().getCurrentPlan();
        if (currentPlan == null) {
            return null;
        }
        String apiCode = ImportAgentPlannerSubagentSupport.textValue(assetNode, "apiCode");
        if (apiCode == null) {
            return null;
        }
        for (ImportAssetPlanModel assetPlan : currentPlan.getAssetPlans()) {
            if (!apiCode.equals(assetPlan.getApiCode())) {
                continue;
            }
            String value = "requestJsonSchema".equals(fieldName)
                    ? assetPlan.getRequestJsonSchema()
                    : assetPlan.getResponseJsonSchema();
            return ImportAgentSchemaNormalizer.normalize(value);
        }
        return null;
    }

    private int scoreSubmitCandidate(JsonNode candidate, JsonNode queryAsset) {
        int score = "POST".equals(normalizedToken(ImportAgentPlannerSubagentSupport.textValue(candidate, "requestMethod"))) ? 2 : 0;
        if (sameText(ImportAgentPlannerSubagentSupport.textValue(candidate, "categoryCode"), ImportAgentPlannerSubagentSupport.textValue(queryAsset, "categoryCode"))) {
            score += 3;
        }
        if (shareMeaningfulToken(ImportAgentPlannerSubagentSupport.textValue(candidate, "apiCode"), ImportAgentPlannerSubagentSupport.textValue(queryAsset, "apiCode"))
                || shareMeaningfulToken(ImportAgentPlannerSubagentSupport.textValue(candidate, "assetName"), ImportAgentPlannerSubagentSupport.textValue(queryAsset, "assetName"))) {
            score += 2;
        }
        if (sameHost(ImportAgentPlannerSubagentSupport.textValue(candidate, "upstreamUrl"), ImportAgentPlannerSubagentSupport.textValue(queryAsset, "upstreamUrl"))) {
            score += 2;
        }
        return score;
    }

    private boolean isAsyncQueryAsset(JsonNode assetNode) {
        if (!"GET".equals(normalizedToken(ImportAgentPlannerSubagentSupport.textValue(assetNode, "requestMethod")))) {
            return false;
        }
        String searchable = String.join(" ",
                lowerText(ImportAgentPlannerSubagentSupport.textValue(assetNode, "apiCode")),
                lowerText(ImportAgentPlannerSubagentSupport.textValue(assetNode, "assetName")),
                lowerText(ImportAgentPlannerSubagentSupport.textValue(assetNode, "upstreamUrl")));
        return searchable.contains("query")
                || searchable.contains("detail")
                || searchable.contains("result")
                || searchable.contains("status")
                || searchable.contains("task")
                || searchable.contains("查询")
                || searchable.contains("详情")
                || searchable.contains("结果")
                || searchable.contains("状态");
    }

    private boolean sameText(String left, String right) {
        return left != null && right != null && left.equalsIgnoreCase(right);
    }

    private boolean sameHost(String left, String right) {
        if (left == null || right == null) {
            return false;
        }
        int leftScheme = left.indexOf("://");
        int rightScheme = right.indexOf("://");
        if (leftScheme < 0 || rightScheme < 0) {
            return false;
        }
        String leftHost = left.substring(leftScheme + 3).split("/", 2)[0];
        String rightHost = right.substring(rightScheme + 3).split("/", 2)[0];
        return leftHost.equalsIgnoreCase(rightHost);
    }

    private boolean shareMeaningfulToken(String left, String right) {
        if (left == null || right == null) {
            return false;
        }
        String[] leftParts = normalizedWords(left).split(" ");
        String[] rightParts = normalizedWords(right).split(" ");
        for (String leftPart : leftParts) {
            if (leftPart.length() < 4) {
                continue;
            }
            for (String rightPart : rightParts) {
                if (leftPart.equals(rightPart)) {
                    return true;
                }
            }
        }
        return false;
    }

    private String normalizedWords(String value) {
        return lowerText(value).replace('-', ' ').replace('_', ' ');
    }

    private String lowerText(String value) {
        return value == null ? "" : value.toLowerCase(Locale.ROOT);
    }

    private String normalizeKey(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    }

    private String normalizedToken(String value) {
        return value == null || value.isBlank() ? null : value.trim().toUpperCase(Locale.ROOT);
    }

    private String displayApiCode(String apiCode) {
        return apiCode == null || apiCode.isBlank() ? "<unknown>" : apiCode;
    }
}
