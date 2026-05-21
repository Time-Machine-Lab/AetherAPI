package io.github.timemachinelab.infrastructure.importagent.planner;

import com.fasterxml.jackson.databind.JsonNode;
import io.github.timemachinelab.domain.catalog.model.AssetType;
import io.github.timemachinelab.domain.catalog.model.AuthScheme;
import io.github.timemachinelab.domain.catalog.model.RequestMethod;
import io.github.timemachinelab.service.model.AsyncTaskConfigModel;
import io.github.timemachinelab.service.model.ImportAiProfileModel;
import io.github.timemachinelab.service.model.ImportAssetPlanModel;
import io.github.timemachinelab.service.model.ImportCategoryPlanAction;
import io.github.timemachinelab.service.model.ImportCategoryPlanModel;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import io.github.timemachinelab.infrastructure.importagent.planner.ImportAgentPlannerJsonSupport.ParsedPlannerPayload;

final class ImportAgentPlanDraftParser {

    private ImportAgentPlanDraftParser() {
    }

    static ParsedPlannerPayload parsePlannerPayload(JsonNode sourceNode, String currentSummary) {
        if (sourceNode == null || !sourceNode.isObject()) {
            return new ParsedPlannerPayload(false, false, List.of(), currentSummary);
        }
        String parsedSummary = ImportAgentPlannerJsonSupport.textValue(sourceNode, "summary");
        return new ParsedPlannerPayload(
                hasCategoryPlanField(sourceNode),
                hasAssetPlanField(sourceNode),
                parseStringArray(sourceNode, "clarificationQuestions"),
                parsedSummary == null ? currentSummary : parsedSummary);
    }

    static List<ImportCategoryPlanModel> parseCategoryPlans(
            JsonNode root,
            List<ImportCategoryPlanModel> currentCategoryPlans) {
        JsonNode categoryArray = root.path("categoryPlans");
        if (!categoryArray.isArray() || categoryArray.isEmpty()) {
            categoryArray = root.path("categories");
        }
        Map<String, ImportCategoryPlanModel> currentByCode = new LinkedHashMap<>();
        List<ImportCategoryPlanModel> anonymousCurrent = new ArrayList<>();
        for (ImportCategoryPlanModel currentCategoryPlan : currentCategoryPlans) {
            if (currentCategoryPlan.getCategoryCode() == null || currentCategoryPlan.getCategoryCode().isBlank()) {
                anonymousCurrent.add(currentCategoryPlan);
                continue;
            }
            currentByCode.put(currentCategoryPlan.getCategoryCode(), currentCategoryPlan);
        }
        List<ImportCategoryPlanModel> values = new ArrayList<>();
        if (!categoryArray.isArray()) {
            return values;
        }
        for (JsonNode categoryNode : categoryArray) {
            String categoryCode = ImportAgentPlannerJsonSupport.textValue(categoryNode, "categoryCode");
            ImportCategoryPlanModel currentCategoryPlan = categoryCode == null ? null : currentByCode.remove(categoryCode);
            values.add(mergeCategoryPlan(categoryNode, currentCategoryPlan));
        }
        values.addAll(currentByCode.values());
        values.addAll(anonymousCurrent);
        return values;
    }

    static List<ImportAssetPlanModel> parseAssetPlans(
            JsonNode root,
            List<ImportAssetPlanModel> currentAssetPlans) {
        JsonNode assetArray = root.path("assetPlans");
        if (!assetArray.isArray() || assetArray.isEmpty()) {
            assetArray = root.path("assets");
        }
        Map<String, ImportAssetPlanModel> currentByCode = new LinkedHashMap<>();
        List<ImportAssetPlanModel> anonymousCurrent = new ArrayList<>();
        for (ImportAssetPlanModel currentAssetPlan : currentAssetPlans) {
            if (currentAssetPlan.getApiCode() == null || currentAssetPlan.getApiCode().isBlank()) {
                anonymousCurrent.add(currentAssetPlan);
                continue;
            }
            currentByCode.put(currentAssetPlan.getApiCode(), currentAssetPlan);
        }
        List<ImportAssetPlanModel> values = new ArrayList<>();
        if (!assetArray.isArray()) {
            return values;
        }
        int anonymousIndex = 0;
        for (JsonNode assetNode : assetArray) {
            String apiCode = ImportAgentPlannerJsonSupport.textValue(assetNode, "apiCode");
            ImportAssetPlanModel currentAssetPlan = apiCode == null ? null : currentByCode.remove(apiCode);
            if (currentAssetPlan == null) {
                currentAssetPlan = removeCompatibleCurrentAsset(currentByCode, assetNode);
            }
            if (currentAssetPlan == null && anonymousIndex < anonymousCurrent.size()) {
                currentAssetPlan = anonymousCurrent.get(anonymousIndex);
                anonymousIndex += 1;
            }
            values.add(mergeAssetPlan(assetNode, currentAssetPlan));
        }
        values.addAll(currentByCode.values());
        if (anonymousIndex < anonymousCurrent.size()) {
            values.addAll(anonymousCurrent.subList(anonymousIndex, anonymousCurrent.size()));
        }
        return values;
    }

    private static ImportAssetPlanModel removeCompatibleCurrentAsset(
            Map<String, ImportAssetPlanModel> currentByCode,
            JsonNode assetNode) {
        if (currentByCode.isEmpty()) {
            return null;
        }
        String assetName = ImportAgentPlannerJsonSupport.textValue(assetNode, "assetName");
        String upstreamUrl = ImportAgentPlannerJsonSupport.textValue(assetNode, "upstreamUrl");
        Iterator<Map.Entry<String, ImportAssetPlanModel>> iterator = currentByCode.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, ImportAssetPlanModel> entry = iterator.next();
            ImportAssetPlanModel current = entry.getValue();
            if (sameText(assetName, current.getAssetName()) || sameText(upstreamUrl, current.getUpstreamUrl())) {
                iterator.remove();
                return current;
            }
        }
        return null;
    }

    private static boolean sameText(String left, String right) {
        return left != null && right != null && left.trim().equalsIgnoreCase(right.trim());
    }

    private static boolean hasCategoryPlanField(JsonNode root) {
        return root.has("categoryPlans") || root.has("categories");
    }

    private static boolean hasAssetPlanField(JsonNode root) {
        return root.has("assetPlans") || root.has("assets");
    }

    private static List<String> parseStringArray(JsonNode root, String fieldName) {
        JsonNode arrayNode = root.path(fieldName);
        if (!arrayNode.isArray()) {
            return List.of();
        }
        List<String> values = new ArrayList<>();
        for (JsonNode item : arrayNode) {
            String value = item.asText(null);
            if (value != null && !value.isBlank()) {
                values.add(value);
            }
        }
        return List.copyOf(values);
    }

    private static ImportCategoryPlanModel mergeCategoryPlan(JsonNode categoryNode, ImportCategoryPlanModel currentCategoryPlan) {
        return new ImportCategoryPlanModel(
                ImportAgentPlannerJsonSupport.hasField(categoryNode, "categoryCode")
                        ? ImportAgentPlannerJsonSupport.textValue(categoryNode, "categoryCode")
                        : ImportAgentPlannerJsonSupport.currentValue(currentCategoryPlan, ImportCategoryPlanModel::getCategoryCode),
                ImportAgentPlannerJsonSupport.hasField(categoryNode, "categoryName")
                        ? ImportAgentPlannerJsonSupport.textValue(categoryNode, "categoryName")
                        : ImportAgentPlannerJsonSupport.currentValue(currentCategoryPlan, ImportCategoryPlanModel::getCategoryName),
                ImportAgentPlannerJsonSupport.hasField(categoryNode, "action")
                        ? ImportAgentPlannerJsonSupport.enumValue(ImportCategoryPlanAction.class, ImportAgentPlannerJsonSupport.textValue(categoryNode, "action"), ImportCategoryPlanAction.CREATE_IF_MISSING)
                        : ImportAgentPlannerJsonSupport.currentValue(currentCategoryPlan, ImportCategoryPlanModel::getAction, ImportCategoryPlanAction.CREATE_IF_MISSING));
    }

    private static ImportAssetPlanModel mergeAssetPlan(JsonNode assetNode, ImportAssetPlanModel currentAssetPlan) {
        return new ImportAssetPlanModel(
                ImportAgentPlannerJsonSupport.hasField(assetNode, "apiCode")
                        ? ImportAgentPlannerJsonSupport.textValue(assetNode, "apiCode")
                        : ImportAgentPlannerJsonSupport.currentValue(currentAssetPlan, ImportAssetPlanModel::getApiCode),
                ImportAgentPlannerJsonSupport.hasField(assetNode, "assetName")
                        ? ImportAgentPlannerJsonSupport.textValue(assetNode, "assetName")
                        : ImportAgentPlannerJsonSupport.currentValue(currentAssetPlan, ImportAssetPlanModel::getAssetName),
                ImportAgentPlannerJsonSupport.hasField(assetNode, "assetType")
                        ? ImportAgentPlannerJsonSupport.enumValue(AssetType.class, ImportAgentPlannerJsonSupport.textValue(assetNode, "assetType"), null)
                        : ImportAgentPlannerJsonSupport.currentValue(currentAssetPlan, ImportAssetPlanModel::getAssetType),
                ImportAgentPlannerJsonSupport.hasField(assetNode, "categoryCode")
                        ? ImportAgentPlannerJsonSupport.textValue(assetNode, "categoryCode")
                        : ImportAgentPlannerJsonSupport.currentValue(currentAssetPlan, ImportAssetPlanModel::getCategoryCode),
                ImportAgentPlannerJsonSupport.hasField(assetNode, "requestMethod")
                        ? ImportAgentPlannerJsonSupport.enumValue(RequestMethod.class, ImportAgentPlannerJsonSupport.textValue(assetNode, "requestMethod"), null)
                        : ImportAgentPlannerJsonSupport.currentValue(currentAssetPlan, ImportAssetPlanModel::getRequestMethod),
                ImportAgentPlannerJsonSupport.hasField(assetNode, "upstreamUrl")
                        ? ImportAgentPlannerJsonSupport.textValue(assetNode, "upstreamUrl")
                        : ImportAgentPlannerJsonSupport.currentValue(currentAssetPlan, ImportAssetPlanModel::getUpstreamUrl),
                ImportAgentPlannerJsonSupport.hasField(assetNode, "authScheme")
                        ? ImportAgentPlannerJsonSupport.enumValue(AuthScheme.class, ImportAgentPlannerJsonSupport.textValue(assetNode, "authScheme"), null)
                        : ImportAgentPlannerJsonSupport.currentValue(currentAssetPlan, ImportAssetPlanModel::getAuthScheme),
                ImportAgentPlannerJsonSupport.hasField(assetNode, "authConfig")
                        ? ImportAgentPlannerJsonSupport.authConfigValue(assetNode.path("authConfig"))
                        : ImportAgentPlannerJsonSupport.currentValue(currentAssetPlan, ImportAssetPlanModel::getAuthConfig),
                ImportAgentPlannerJsonSupport.hasField(assetNode, "requestTemplate")
                        ? ImportAgentPlannerJsonSupport.textValue(assetNode, "requestTemplate")
                        : ImportAgentPlannerJsonSupport.currentValue(currentAssetPlan, ImportAssetPlanModel::getRequestTemplate),
                ImportAgentPlannerJsonSupport.hasField(assetNode, "requestExample")
                        ? requestExampleValue(assetNode, currentAssetPlan)
                        : ImportAgentPlannerJsonSupport.currentValue(currentAssetPlan, ImportAssetPlanModel::getRequestExample),
                ImportAgentPlannerJsonSupport.hasField(assetNode, "responseExample")
                        ? responseExampleValue(assetNode, currentAssetPlan)
                        : ImportAgentPlannerJsonSupport.currentValue(currentAssetPlan, ImportAssetPlanModel::getResponseExample),
                ImportAgentPlannerJsonSupport.schemaValue(assetNode, ImportAgentPlannerJsonSupport.currentValue(currentAssetPlan, ImportAssetPlanModel::getRequestJsonSchema), "requestJsonSchema", "requestSchema", "inputSchema"),
                ImportAgentPlannerJsonSupport.schemaValue(assetNode, ImportAgentPlannerJsonSupport.currentValue(currentAssetPlan, ImportAssetPlanModel::getResponseJsonSchema), "responseJsonSchema", "responseSchema", "outputSchema"),
                ImportAgentPlannerJsonSupport.hasField(assetNode, "publishAfterImport")
                        ? assetNode.path("publishAfterImport").asBoolean(false)
                        : ImportAgentPlannerJsonSupport.currentValue(currentAssetPlan, ImportAssetPlanModel::isPublishAfterImport, false),
                ImportAgentPlannerJsonSupport.hasField(assetNode, "asyncTaskConfig")
                        ? parseAsyncTaskConfig(assetNode.path("asyncTaskConfig"), ImportAgentPlannerJsonSupport.currentValue(currentAssetPlan, ImportAssetPlanModel::getAsyncTaskConfig))
                        : ImportAgentPlannerJsonSupport.currentValue(currentAssetPlan, ImportAssetPlanModel::getAsyncTaskConfig),
                ImportAgentPlannerJsonSupport.hasField(assetNode, "aiProfile")
                        ? parseAiProfile(assetNode.path("aiProfile"), ImportAgentPlannerJsonSupport.currentValue(currentAssetPlan, ImportAssetPlanModel::getAiProfile))
                        : ImportAgentPlannerJsonSupport.currentValue(currentAssetPlan, ImportAssetPlanModel::getAiProfile));
    }

    private static String requestExampleValue(JsonNode assetNode, ImportAssetPlanModel currentAssetPlan) {
        String normalized = ImportAgentExampleNormalizer.normalizeRequestBodyExample(
                ImportAgentPlannerJsonSupport.textValue(assetNode, "requestExample"));
        return normalized == null
                ? ImportAgentPlannerJsonSupport.currentValue(currentAssetPlan, ImportAssetPlanModel::getRequestExample)
                : normalized;
    }

    private static String responseExampleValue(JsonNode assetNode, ImportAssetPlanModel currentAssetPlan) {
        String normalized = ImportAgentExampleNormalizer.normalizeJsonObjectExample(
                ImportAgentPlannerJsonSupport.textValue(assetNode, "responseExample"));
        return normalized == null
                ? ImportAgentPlannerJsonSupport.currentValue(currentAssetPlan, ImportAssetPlanModel::getResponseExample)
                : normalized;
    }

    private static AsyncTaskConfigModel parseAsyncTaskConfig(JsonNode node, AsyncTaskConfigModel current) {
        if (node == null || node.isMissingNode() || node.isNull()) {
            return null;
        }
        if (!node.isObject()) {
            return current;
        }
        return ImportAgentPlannerJsonSupport.normalizeAsyncTaskConfig(new AsyncTaskConfigModel(
                ImportAgentPlannerJsonSupport.hasField(node, "enabled") ? node.path("enabled").asBoolean(false) : ImportAgentPlannerJsonSupport.currentValue(current, AsyncTaskConfigModel::getEnabled),
                ImportAgentPlannerJsonSupport.hasField(node, "queryMethod") ? ImportAgentPlannerJsonSupport.textValue(node, "queryMethod") : ImportAgentPlannerJsonSupport.currentValue(current, AsyncTaskConfigModel::getQueryMethod),
                ImportAgentPlannerJsonSupport.hasField(node, "queryUrlTemplate") ? ImportAgentPlannerJsonSupport.textValue(node, "queryUrlTemplate") : ImportAgentPlannerJsonSupport.currentValue(current, AsyncTaskConfigModel::getQueryUrlTemplate),
                ImportAgentPlannerJsonSupport.hasField(node, "authMode") ? ImportAgentPlannerJsonSupport.textValue(node, "authMode") : ImportAgentPlannerJsonSupport.currentValue(current, AsyncTaskConfigModel::getAuthMode),
                ImportAgentPlannerJsonSupport.hasField(node, "authScheme") ? ImportAgentPlannerJsonSupport.textValue(node, "authScheme") : ImportAgentPlannerJsonSupport.currentValue(current, AsyncTaskConfigModel::getAuthScheme),
                ImportAgentPlannerJsonSupport.hasField(node, "authConfig") ? ImportAgentPlannerJsonSupport.authConfigValue(node.path("authConfig")) : ImportAgentPlannerJsonSupport.currentValue(current, AsyncTaskConfigModel::getAuthConfig),
                ImportAgentPlannerJsonSupport.hasField(node, "statusPath") ? ImportAgentPlannerJsonSupport.textValue(node, "statusPath") : ImportAgentPlannerJsonSupport.currentValue(current, AsyncTaskConfigModel::getStatusPath),
                ImportAgentPlannerJsonSupport.hasField(node, "resultPath") ? ImportAgentPlannerJsonSupport.textValue(node, "resultPath") : ImportAgentPlannerJsonSupport.currentValue(current, AsyncTaskConfigModel::getResultPath),
                ImportAgentPlannerJsonSupport.hasField(node, "errorPath") ? ImportAgentPlannerJsonSupport.textValue(node, "errorPath") : ImportAgentPlannerJsonSupport.currentValue(current, AsyncTaskConfigModel::getErrorPath)));
    }

    private static ImportAiProfileModel parseAiProfile(JsonNode node, ImportAiProfileModel currentAiProfile) {
        if (node == null || node.isMissingNode() || node.isNull()) {
            return null;
        }
        if (!node.isObject()) {
            return currentAiProfile;
        }
        List<String> tags = new ArrayList<>();
        for (JsonNode tagNode : node.path("capabilityTags")) {
            tags.add(tagNode.asText());
        }
        return new ImportAiProfileModel(
                ImportAgentPlannerJsonSupport.hasField(node, "provider") ? ImportAgentPlannerJsonSupport.textValue(node, "provider") : ImportAgentPlannerJsonSupport.currentValue(currentAiProfile, ImportAiProfileModel::getProvider),
                ImportAgentPlannerJsonSupport.hasField(node, "model") ? ImportAgentPlannerJsonSupport.textValue(node, "model") : ImportAgentPlannerJsonSupport.currentValue(currentAiProfile, ImportAiProfileModel::getModel),
                ImportAgentPlannerJsonSupport.hasField(node, "streamingSupported") ? node.path("streamingSupported").asBoolean(false) : ImportAgentPlannerJsonSupport.currentValue(currentAiProfile, ImportAiProfileModel::isStreamingSupported, false),
                ImportAgentPlannerJsonSupport.hasField(node, "capabilityTags") ? tags : ImportAgentPlannerJsonSupport.currentValue(currentAiProfile, ImportAiProfileModel::getCapabilityTags, List.of()));
    }
}
