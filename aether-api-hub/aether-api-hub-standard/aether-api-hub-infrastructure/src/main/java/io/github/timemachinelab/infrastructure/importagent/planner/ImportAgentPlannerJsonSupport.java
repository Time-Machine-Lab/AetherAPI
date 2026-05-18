package io.github.timemachinelab.infrastructure.importagent.planner;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.timemachinelab.domain.catalog.model.AssetType;
import io.github.timemachinelab.domain.catalog.model.AuthScheme;
import io.github.timemachinelab.domain.catalog.model.RequestMethod;
import io.github.timemachinelab.service.model.ImportAgentPlanModel;
import io.github.timemachinelab.service.model.ImportAgentPlannerRequest;
import io.github.timemachinelab.service.model.ImportAiProfileModel;
import io.github.timemachinelab.service.model.ImportAssetPlanModel;
import io.github.timemachinelab.service.model.ImportCategoryPlanAction;
import io.github.timemachinelab.service.model.ImportCategoryPlanModel;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

/**
 * Shared JSON planning support for import agent planners.
 */
final class ImportAgentPlannerJsonSupport {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private ImportAgentPlannerJsonSupport() {
    }

    static ImportAgentPlanModel buildPlan(ImportAgentPlannerRequest request, JsonNode sourceNode) {
        List<ImportCategoryPlanModel> categoryPlans = request.getCurrentPlan() == null
                ? List.of()
                : request.getCurrentPlan().getCategoryPlans();
        List<ImportAssetPlanModel> assetPlans = request.getCurrentPlan() == null
                ? List.of()
                : request.getCurrentPlan().getAssetPlans();
        List<String> clarificationQuestions = List.of();
        String summary = null;

        if (sourceNode != null && sourceNode.isObject()) {
            categoryPlans = parseCategoryPlans(sourceNode);
            assetPlans = parseAssetPlans(sourceNode);
            clarificationQuestions = parseStringArray(sourceNode, "clarificationQuestions");
            summary = textValue(sourceNode, "summary");
        }

        categoryPlans = ensureCategoryCoverage(categoryPlans, assetPlans);
        LinkedHashSet<String> mergedQuestions = new LinkedHashSet<>(clarificationQuestions);
        mergedQuestions.addAll(validatePlan(categoryPlans, assetPlans));
        boolean executable = mergedQuestions.isEmpty();
        String resolvedSummary = summary == null
                ? buildDefaultSummary(request.getNextPlanVersion(), categoryPlans, assetPlans)
                : summary;
        return new ImportAgentPlanModel(
                request.getNextPlanVersion(),
                executable,
                resolvedSummary,
                List.copyOf(mergedQuestions),
                categoryPlans,
                assetPlans
        );
    }

    static String buildAgentMessage(String providerName, ImportAgentPlanModel plan) {
        String prefix = providerName == null || providerName.isBlank() ? "Planner" : providerName;
        if (plan.isExecutable()) {
            return prefix + " prepared plan version " + plan.getVersion() + ". Ready for confirmation.";
        }
        return prefix + " prepared plan version " + plan.getVersion() + ". Missing information: "
                + String.join("; ", plan.getClarificationQuestions());
    }

    static JsonNode parseJsonCandidate(String candidate) {
        if (candidate == null || candidate.isBlank()) {
            return null;
        }
        String trimmed = candidate.trim();
        if (trimmed.startsWith("```") && trimmed.contains("{")) {
            int start = trimmed.indexOf('{');
            int end = trimmed.lastIndexOf('}');
            if (start >= 0 && end > start) {
                trimmed = trimmed.substring(start, end + 1);
            }
        }
        try {
            JsonNode node = OBJECT_MAPPER.readTree(trimmed);
            return node.isObject() ? node : null;
        } catch (Exception ex) {
            return null;
        }
    }

    private static String buildDefaultSummary(
            int nextPlanVersion,
            List<ImportCategoryPlanModel> categoryPlans,
            List<ImportAssetPlanModel> assetPlans) {
        return "Draft plan version " + nextPlanVersion
                + " prepared with " + categoryPlans.size() + " category plan(s) and " + assetPlans.size() + " asset plan(s).";
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

    private static List<ImportCategoryPlanModel> ensureCategoryCoverage(
            List<ImportCategoryPlanModel> categoryPlans,
            List<ImportAssetPlanModel> assetPlans) {
        Map<String, ImportCategoryPlanModel> merged = new LinkedHashMap<>();
        for (ImportCategoryPlanModel categoryPlan : categoryPlans) {
            if (categoryPlan.getCategoryCode() != null) {
                merged.put(categoryPlan.getCategoryCode(), categoryPlan);
            }
        }
        for (ImportAssetPlanModel assetPlan : assetPlans) {
            if (assetPlan.getCategoryCode() != null && !merged.containsKey(assetPlan.getCategoryCode())) {
                merged.put(assetPlan.getCategoryCode(), new ImportCategoryPlanModel(
                        assetPlan.getCategoryCode(),
                        assetPlan.getCategoryCode(),
                        ImportCategoryPlanAction.CREATE_IF_MISSING
                ));
            }
        }
        return List.copyOf(merged.values());
    }

    private static List<String> validatePlan(
            List<ImportCategoryPlanModel> categoryPlans,
            List<ImportAssetPlanModel> assetPlans) {
        LinkedHashSet<String> questions = new LinkedHashSet<>();
        if (assetPlans.isEmpty()) {
            questions.add("Provide JSON with assetPlans or assets to continue.");
        }
        for (ImportCategoryPlanModel categoryPlan : categoryPlans) {
            if (categoryPlan.getCategoryCode() == null || categoryPlan.getCategoryCode().isBlank()) {
                questions.add("Each category plan must provide categoryCode.");
            }
        }
        for (ImportAssetPlanModel assetPlan : assetPlans) {
            if (assetPlan.getApiCode() == null || assetPlan.getApiCode().isBlank()) {
                questions.add("Each asset plan must provide apiCode.");
            }
            if (assetPlan.getAssetName() == null || assetPlan.getAssetName().isBlank()) {
                questions.add("Each asset plan must provide assetName.");
            }
            if (assetPlan.getAssetType() == null) {
                questions.add("Each asset plan must provide assetType.");
            }
            if (assetPlan.isPublishAfterImport()) {
                if (assetPlan.getCategoryCode() == null || assetPlan.getCategoryCode().isBlank()) {
                    questions.add("Published asset plans must provide categoryCode.");
                }
                if (assetPlan.getRequestMethod() == null) {
                    questions.add("Published asset plans must provide requestMethod.");
                }
                if (assetPlan.getUpstreamUrl() == null || assetPlan.getUpstreamUrl().isBlank()) {
                    questions.add("Published asset plans must provide upstreamUrl.");
                }
                if (assetPlan.getAssetType() == AssetType.AI_API
                        && (assetPlan.getAiProfile() == null
                        || assetPlan.getAiProfile().getProvider() == null
                        || assetPlan.getAiProfile().getModel() == null)) {
                    questions.add("Published AI_API asset plans must provide aiProfile provider and model.");
                }
            }
        }
        return List.copyOf(questions);
    }

    private static List<ImportCategoryPlanModel> parseCategoryPlans(JsonNode root) {
        JsonNode categoryArray = root.path("categoryPlans");
        if (!categoryArray.isArray() || categoryArray.isEmpty()) {
            categoryArray = root.path("categories");
        }
        List<ImportCategoryPlanModel> values = new ArrayList<>();
        if (!categoryArray.isArray()) {
            return values;
        }
        for (JsonNode categoryNode : categoryArray) {
            values.add(new ImportCategoryPlanModel(
                    textValue(categoryNode, "categoryCode"),
                    textValue(categoryNode, "categoryName"),
                    enumValue(ImportCategoryPlanAction.class, textValue(categoryNode, "action"), ImportCategoryPlanAction.CREATE_IF_MISSING)
            ));
        }
        return values;
    }

    private static List<ImportAssetPlanModel> parseAssetPlans(JsonNode root) {
        JsonNode assetArray = root.path("assetPlans");
        if (!assetArray.isArray() || assetArray.isEmpty()) {
            assetArray = root.path("assets");
        }
        List<ImportAssetPlanModel> values = new ArrayList<>();
        if (!assetArray.isArray()) {
            return values;
        }
        for (JsonNode assetNode : assetArray) {
            values.add(new ImportAssetPlanModel(
                    textValue(assetNode, "apiCode"),
                    textValue(assetNode, "assetName"),
                    enumValue(AssetType.class, textValue(assetNode, "assetType"), null),
                    textValue(assetNode, "categoryCode"),
                    enumValue(RequestMethod.class, textValue(assetNode, "requestMethod"), null),
                    textValue(assetNode, "upstreamUrl"),
                    enumValue(AuthScheme.class, textValue(assetNode, "authScheme"), null),
                    textValue(assetNode, "authConfig"),
                    textValue(assetNode, "requestTemplate"),
                    textValue(assetNode, "requestExample"),
                    textValue(assetNode, "responseExample"),
                    textValue(assetNode, "requestJsonSchema"),
                    textValue(assetNode, "responseJsonSchema"),
                    assetNode.path("publishAfterImport").asBoolean(false),
                    parseAiProfile(assetNode.path("aiProfile"))
            ));
        }
        return values;
    }

    private static ImportAiProfileModel parseAiProfile(JsonNode node) {
        if (node == null || node.isMissingNode() || node.isNull()) {
            return null;
        }
        List<String> tags = new ArrayList<>();
        for (JsonNode tagNode : node.path("capabilityTags")) {
            tags.add(tagNode.asText());
        }
        return new ImportAiProfileModel(
                textValue(node, "provider"),
                textValue(node, "model"),
                node.path("streamingSupported").asBoolean(false),
                tags
        );
    }

    private static String textValue(JsonNode node, String fieldName) {
        JsonNode valueNode = node.path(fieldName);
        if (valueNode.isMissingNode() || valueNode.isNull()) {
            return null;
        }
        String value = valueNode.asText();
        return value == null || value.isBlank() ? null : value;
    }

    private static <T extends Enum<T>> T enumValue(Class<T> type, String value, T defaultValue) {
        if (value == null || value.isBlank()) {
            return defaultValue;
        }
        return Enum.valueOf(type, value);
    }
}