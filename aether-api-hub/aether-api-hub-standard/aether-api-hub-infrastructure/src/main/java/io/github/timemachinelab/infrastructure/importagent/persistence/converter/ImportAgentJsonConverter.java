package io.github.timemachinelab.infrastructure.importagent.persistence.converter;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.github.timemachinelab.domain.catalog.model.AssetType;
import io.github.timemachinelab.domain.catalog.model.AuthScheme;
import io.github.timemachinelab.domain.catalog.model.RequestMethod;
import io.github.timemachinelab.service.model.AsyncTaskConfigModel;
import io.github.timemachinelab.service.model.ImportAgentPlanModel;
import io.github.timemachinelab.service.model.ImportAgentStepType;
import io.github.timemachinelab.service.model.ImportAgentClarificationItemModel;
import io.github.timemachinelab.service.model.ImportAgentClarificationOptionModel;
import io.github.timemachinelab.service.model.ImportAiProfileModel;
import io.github.timemachinelab.service.model.ImportAssetPlanModel;
import io.github.timemachinelab.service.model.ImportCategoryPlanAction;
import io.github.timemachinelab.service.model.ImportCategoryPlanModel;
import io.github.timemachinelab.service.model.ImportStepResultModel;
import io.github.timemachinelab.service.model.ImportStepResultStatus;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Import agent JSON converter.
 */
public final class ImportAgentJsonConverter {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private ImportAgentJsonConverter() {
    }

    public static String serializePlan(ImportAgentPlanModel plan) {
        if (plan == null) {
            return null;
        }
        ObjectNode root = OBJECT_MAPPER.createObjectNode();
        root.put("version", plan.getVersion());
        root.put("executable", plan.isExecutable());
        root.put("summary", plan.getSummary());
        ArrayNode clarificationQuestions = root.putArray("clarificationQuestions");
        for (String question : plan.getClarificationQuestions()) {
            clarificationQuestions.add(question);
        }
        ArrayNode clarificationItems = root.putArray("clarificationItems");
        for (ImportAgentClarificationItemModel item : plan.getClarificationItems()) {
            ObjectNode itemNode = clarificationItems.addObject();
            itemNode.put("id", item.getId());
            itemNode.put("targetPath", item.getTargetPath());
            itemNode.put("fieldKey", item.getFieldKey());
            itemNode.put("label", item.getLabel());
            itemNode.put("description", item.getDescription());
            itemNode.put("inputType", item.getInputType());
            itemNode.put("required", item.isRequired());
            itemNode.put("currentValue", item.getCurrentValue());
            itemNode.put("defaultValue", item.getDefaultValue());
            itemNode.put("defaultLabel", item.getDefaultLabel());
            itemNode.put("defaultSource", item.getDefaultSource());
            itemNode.put("defaultConfidence", item.getDefaultConfidence());
            ArrayNode optionsNode = itemNode.putArray("options");
            for (ImportAgentClarificationOptionModel option : item.getOptions()) {
                ObjectNode optionNode = optionsNode.addObject();
                optionNode.put("value", option.getValue());
                optionNode.put("label", option.getLabel());
            }
        }
        ArrayNode categoryPlans = root.putArray("categoryPlans");
        for (ImportCategoryPlanModel categoryPlan : plan.getCategoryPlans()) {
            ObjectNode categoryNode = categoryPlans.addObject();
            categoryNode.put("categoryCode", categoryPlan.getCategoryCode());
            categoryNode.put("categoryName", categoryPlan.getCategoryName());
            categoryNode.put("action", categoryPlan.getAction().name());
        }
        ArrayNode assetPlans = root.putArray("assetPlans");
        for (ImportAssetPlanModel assetPlan : plan.getAssetPlans()) {
            ObjectNode assetNode = assetPlans.addObject();
            assetNode.put("apiCode", assetPlan.getApiCode());
            assetNode.put("assetName", assetPlan.getAssetName());
            if (assetPlan.getAssetType() != null) {
                assetNode.put("assetType", assetPlan.getAssetType().name());
            }
            assetNode.put("categoryCode", assetPlan.getCategoryCode());
            if (assetPlan.getRequestMethod() != null) {
                assetNode.put("requestMethod", assetPlan.getRequestMethod().name());
            }
            assetNode.put("upstreamUrl", assetPlan.getUpstreamUrl());
            if (assetPlan.getAuthScheme() != null) {
                assetNode.put("authScheme", assetPlan.getAuthScheme().name());
            }
            assetNode.put("authConfig", assetPlan.getAuthConfig());
            assetNode.put("requestTemplate", assetPlan.getRequestTemplate());
            assetNode.put("requestExample", assetPlan.getRequestExample());
            assetNode.put("responseExample", assetPlan.getResponseExample());
            assetNode.put("requestJsonSchema", assetPlan.getRequestJsonSchema());
            assetNode.put("responseJsonSchema", assetPlan.getResponseJsonSchema());
            assetNode.put("publishAfterImport", assetPlan.isPublishAfterImport());
            if (assetPlan.getAsyncTaskConfig() != null) {
                serializeAsyncTaskConfig(assetNode.putObject("asyncTaskConfig"), assetPlan.getAsyncTaskConfig());
            }
            if (assetPlan.getAiProfile() != null) {
                ObjectNode aiNode = assetNode.putObject("aiProfile");
                aiNode.put("provider", assetPlan.getAiProfile().getProvider());
                aiNode.put("model", assetPlan.getAiProfile().getModel());
                aiNode.put("streamingSupported", assetPlan.getAiProfile().isStreamingSupported());
                ArrayNode tagsNode = aiNode.putArray("capabilityTags");
                for (String tag : assetPlan.getAiProfile().getCapabilityTags()) {
                    tagsNode.add(tag);
                }
            }
        }
        return root.toString();
    }

    public static ImportAgentPlanModel deserializePlan(String json) {
        if (json == null || json.isBlank()) {
            return null;
        }
        try {
            JsonNode root = OBJECT_MAPPER.readTree(json);
            List<String> clarificationQuestions = new ArrayList<>();
            for (JsonNode questionNode : root.path("clarificationQuestions")) {
                clarificationQuestions.add(questionNode.asText());
            }
            List<ImportAgentClarificationItemModel> clarificationItems = new ArrayList<>();
            for (JsonNode itemNode : root.path("clarificationItems")) {
                List<ImportAgentClarificationOptionModel> options = new ArrayList<>();
                for (JsonNode optionNode : itemNode.path("options")) {
                    options.add(new ImportAgentClarificationOptionModel(
                            textValue(optionNode, "value"),
                            textValue(optionNode, "label")
                    ));
                }
                clarificationItems.add(new ImportAgentClarificationItemModel(
                        textValue(itemNode, "id"),
                        textValue(itemNode, "targetPath"),
                        textValue(itemNode, "fieldKey"),
                        textValue(itemNode, "label"),
                        textValue(itemNode, "description"),
                        textValue(itemNode, "inputType"),
                        itemNode.path("required").asBoolean(false),
                        options,
                        textValue(itemNode, "currentValue"),
                        textValue(itemNode, "defaultValue"),
                        textValue(itemNode, "defaultLabel"),
                        textValue(itemNode, "defaultSource"),
                        textValue(itemNode, "defaultConfidence")
                ));
            }
            List<ImportCategoryPlanModel> categoryPlans = new ArrayList<>();
            for (JsonNode categoryNode : root.path("categoryPlans")) {
                categoryPlans.add(new ImportCategoryPlanModel(
                        textValue(categoryNode, "categoryCode"),
                        textValue(categoryNode, "categoryName"),
                        enumValue(ImportCategoryPlanAction.class, textValue(categoryNode, "action"), ImportCategoryPlanAction.CREATE_IF_MISSING)
                ));
            }
            List<ImportAssetPlanModel> assetPlans = new ArrayList<>();
            for (JsonNode assetNode : root.path("assetPlans")) {
                assetPlans.add(new ImportAssetPlanModel(
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
                        deserializeAsyncTaskConfig(assetNode.path("asyncTaskConfig")),
                        deserializeAiProfile(assetNode.path("aiProfile"))
                ));
            }
            return new ImportAgentPlanModel(
                    root.path("version").isMissingNode() ? null : root.path("version").asInt(),
                    root.path("executable").asBoolean(false),
                    textValue(root, "summary"),
                    clarificationQuestions,
                    clarificationItems,
                    categoryPlans,
                    assetPlans
            );
        } catch (IOException ex) {
            throw new IllegalArgumentException("Invalid import plan json", ex);
        }
    }

    private static void serializeAsyncTaskConfig(ObjectNode target, AsyncTaskConfigModel config) {
        target.put("enabled", config.getEnabled());
        target.put("queryMethod", config.getQueryMethod());
        target.put("queryUrlTemplate", config.getQueryUrlTemplate());
        target.put("authMode", config.getAuthMode());
        target.put("authScheme", config.getAuthScheme());
        target.put("authConfig", config.getAuthConfig());
        target.put("statusPath", config.getStatusPath());
        target.put("resultPath", config.getResultPath());
        target.put("errorPath", config.getErrorPath());
    }

    private static AsyncTaskConfigModel deserializeAsyncTaskConfig(JsonNode node) {
        if (node == null || node.isMissingNode() || node.isNull() || !node.isObject()) {
            return null;
        }
        return new AsyncTaskConfigModel(
                node.has("enabled") ? node.path("enabled").asBoolean(false) : null,
                textValue(node, "queryMethod"),
                textValue(node, "queryUrlTemplate"),
                textValue(node, "authMode"),
                textValue(node, "authScheme"),
                textValue(node, "authConfig"),
                textValue(node, "statusPath"),
                textValue(node, "resultPath"),
                textValue(node, "errorPath")
        );
    }

    public static String serializeStringList(List<String> values) {
        ArrayNode arrayNode = OBJECT_MAPPER.createArrayNode();
        if (values != null) {
            for (String value : values) {
                arrayNode.add(value);
            }
        }
        return arrayNode.toString();
    }

    public static List<String> deserializeStringList(String json) {
        if (json == null || json.isBlank()) {
            return List.of();
        }
        try {
            JsonNode root = OBJECT_MAPPER.readTree(json);
            List<String> values = new ArrayList<>();
            for (JsonNode valueNode : root) {
                values.add(valueNode.asText());
            }
            return values;
        } catch (IOException ex) {
            throw new IllegalArgumentException("Invalid string list json", ex);
        }
    }

    public static String serializeStepResults(List<ImportStepResultModel> stepResults) {
        ArrayNode arrayNode = OBJECT_MAPPER.createArrayNode();
        if (stepResults != null) {
            for (ImportStepResultModel stepResult : stepResults) {
                ObjectNode stepNode = arrayNode.addObject();
                stepNode.put("stepType", stepResult.getStepType().name());
                stepNode.put("targetRef", stepResult.getTargetRef());
                stepNode.put("status", stepResult.getStatus().name());
                stepNode.put("message", stepResult.getMessage());
            }
        }
        return arrayNode.toString();
    }

    public static List<ImportStepResultModel> deserializeStepResults(String json) {
        if (json == null || json.isBlank()) {
            return List.of();
        }
        try {
            JsonNode root = OBJECT_MAPPER.readTree(json);
            List<ImportStepResultModel> values = new ArrayList<>();
            for (JsonNode node : root) {
                values.add(new ImportStepResultModel(
                        enumValue(ImportAgentStepType.class, textValue(node, "stepType"), ImportAgentStepType.REVISE_ASSET),
                        textValue(node, "targetRef"),
                        enumValue(ImportStepResultStatus.class, textValue(node, "status"), ImportStepResultStatus.FAILED),
                        textValue(node, "message")
                ));
            }
            return values;
        } catch (IOException ex) {
            throw new IllegalArgumentException("Invalid step results json", ex);
        }
    }

    private static ImportAiProfileModel deserializeAiProfile(JsonNode node) {
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
        if (type == AuthScheme.class) {
            AuthScheme resolved = AuthScheme.fromToken(value);
            return resolved == null ? defaultValue : type.cast(resolved);
        }
        return Enum.valueOf(type, value);
    }
}
