package io.github.timemachinelab.infrastructure.importagent.planner;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.timemachinelab.domain.catalog.model.AssetType;
import io.github.timemachinelab.domain.catalog.model.AuthScheme;
import io.github.timemachinelab.service.model.AsyncTaskConfigModel;
import io.github.timemachinelab.service.model.ImportAgentPlanModel;
import io.github.timemachinelab.service.model.ImportAgentPlannerRequest;
import io.github.timemachinelab.service.model.ImportAgentClarificationItemModel;
import io.github.timemachinelab.service.model.ImportAssetPlanModel;
import io.github.timemachinelab.service.model.ImportCategoryPlanModel;

import java.util.List;
import java.util.Locale;

/**
 * Shared JSON planning support facade for import agent planners.
 */
final class ImportAgentPlannerJsonSupport {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private ImportAgentPlannerJsonSupport() {
    }

    static ImportAgentPlanModel buildPlan(ImportAgentPlannerRequest request, JsonNode sourceNode) {
        CurrentPlanState currentPlanState = currentPlanState(request);
        ParsedPlannerPayload parsedPayload = ImportAgentPlanDraftParser.parsePlannerPayload(sourceNode, currentPlanState.summary());
        PlanDraft mergedDraft = ImportAgentPlanDraftMerger.mergeWithCurrentPlan(sourceNode, currentPlanState, parsedPayload);
        PlanDraft normalizedDraft = ImportAgentPlanDraftMerger.normalizeDraft(mergedDraft);
        PlanValidationResult validationResult = ImportAgentPlanDraftValidator.validateDraft(
                request.getNextPlanVersion(),
                normalizedDraft);
        return new ImportAgentPlanModel(
                request.getNextPlanVersion(),
                validationResult.executable(),
                validationResult.summary(),
                validationResult.clarificationQuestions(),
                validationResult.clarificationItems(),
                normalizedDraft.categoryPlans(),
                normalizedDraft.assetPlans());
    }

    private static CurrentPlanState currentPlanState(ImportAgentPlannerRequest request) {
        if (request.getCurrentPlan() == null) {
            return new CurrentPlanState(List.of(), List.of(), null);
        }
        return new CurrentPlanState(
                request.getCurrentPlan().getCategoryPlans(),
                request.getCurrentPlan().getAssetPlans(),
                request.getCurrentPlan().getSummary());
    }

    static String buildAgentMessage(String providerName, ImportAgentPlanModel plan) {
        String prefix = providerName == null || providerName.isBlank() ? "规划器" : providerName;
        if (plan.isExecutable()) {
            return prefix + " 已准备计划版本 " + plan.getVersion() + "，可以确认。";
        }
        if (plan.getClarificationQuestions().isEmpty()) {
            return prefix + " 已准备计划版本 " + plan.getVersion() + "，计划不完整。";
        }
        return prefix + " 已准备计划版本 " + plan.getVersion() + "，仍缺少信息："
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
            JsonNode extracted = parseEmbeddedJsonObject(trimmed);
            if (extracted != null) {
                return extracted;
            }
            return null;
        }
    }

    private static JsonNode parseEmbeddedJsonObject(String candidate) {
        int start = candidate.indexOf('{');
        while (start >= 0) {
            int end = findMatchingObjectEnd(candidate, start);
            if (end > start) {
                try {
                    JsonNode node = OBJECT_MAPPER.readTree(candidate.substring(start, end + 1));
                    if (node.isObject()) {
                        return node;
                    }
                } catch (Exception ignored) {
                    // Continue scanning for the next plausible JSON object boundary.
                }
            }
            start = candidate.indexOf('{', start + 1);
        }
        return null;
    }

    private static int findMatchingObjectEnd(String candidate, int startIndex) {
        boolean inString = false;
        boolean escaped = false;
        int depth = 0;
        for (int index = startIndex; index < candidate.length(); index += 1) {
            char current = candidate.charAt(index);
            if (escaped) {
                escaped = false;
                continue;
            }
            if (current == '\\') {
                escaped = true;
                continue;
            }
            if (current == '"') {
                inString = !inString;
                continue;
            }
            if (inString) {
                continue;
            }
            if (current == '{') {
                depth += 1;
                continue;
            }
            if (current == '}') {
                depth -= 1;
                if (depth == 0) {
                    return index;
                }
            }
        }
        return -1;
    }

    static String textValue(JsonNode node, String fieldName) {
        JsonNode valueNode = node.path(fieldName);
        if (valueNode.isMissingNode() || valueNode.isNull()) {
            return null;
        }
        String value = valueNode.asText();
        return value == null || value.isBlank() ? null : value;
    }

    static boolean hasField(JsonNode node, String fieldName) {
        return node != null && node.has(fieldName);
    }

    static String schemaValue(JsonNode node, String currentValue, String... fieldNames) {
        for (String fieldName : fieldNames) {
            if (hasField(node, fieldName)) {
                return ImportAgentSchemaNormalizer.normalizeOrCurrent(node.path(fieldName), currentValue);
            }
        }
        return ImportAgentSchemaNormalizer.normalize(currentValue);
    }

    static String authConfigValue(JsonNode node) {
        if (node == null || node.isMissingNode() || node.isNull()) {
            return null;
        }
        if (node.isTextual()) {
            String value = node.asText();
            return value == null || value.isBlank() ? null : value;
        }
        if (!node.isObject()) {
            return null;
        }
        String headerName = firstText(textValue(node, "headerName"), textValue(node, "name"));
        String queryParamName = firstText(textValue(node, "queryParamName"), textValue(node, "paramName"));
        String tokenValue = firstText(textValue(node, "value"), textValue(node, "token"), textValue(node, "secret"));
        if (headerName != null && tokenValue != null) {
            return headerName + ": " + tokenValue;
        }
        if (queryParamName != null && tokenValue != null) {
            return queryParamName + "=" + tokenValue;
        }
        return null;
    }

    static AsyncTaskConfigModel normalizeAsyncTaskConfig(AsyncTaskConfigModel config) {
        if (config == null) {
            return null;
        }
        String authMode = normalizeAsyncTaskAuthMode(config.getAuthMode());
        String authScheme = normalizeAsyncTaskAuthScheme(config.getAuthScheme());
        return new AsyncTaskConfigModel(
                config.getEnabled(),
                config.getQueryMethod(),
                normalizeAsyncTaskQueryUrlTemplate(config.getQueryUrlTemplate()),
                authMode,
                authScheme,
                config.getAuthConfig(),
                config.getStatusPath(),
                config.getResultPath(),
                config.getErrorPath());
    }

    static String normalizeAsyncTaskQueryUrlTemplate(String queryUrlTemplate) {
        if (queryUrlTemplate == null) {
            return null;
        }
        String normalized = queryUrlTemplate.trim();
        if (normalized.isBlank()) {
            return null;
        }
        return normalized
                .replace("{task_id}", "{taskId}")
                .replace("{taskID}", "{taskId}")
                .replace("{task-id}", "{taskId}");
    }

    static String normalizeAsyncTaskAuthMode(String authMode) {
        return normalizeEnumText(authMode);
    }

    static String normalizeAsyncTaskAuthScheme(String authScheme) {
        AuthScheme normalizedScheme = resolveAuthScheme(authScheme);
        if (normalizedScheme != null) {
            return normalizedScheme.name();
        }
        return normalizeEnumText(authScheme);
    }

    static AuthScheme resolveAuthScheme(String authScheme) {
        String normalized = normalizeEnumText(authScheme);
        if (normalized == null) {
            return null;
        }
        try {
            return AuthScheme.fromToken(normalized);
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    static String normalizeEnumText(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim().toUpperCase(Locale.ROOT);
    }

    static <T, R> R currentValue(T current, java.util.function.Function<T, R> extractor) {
        return current == null ? null : extractor.apply(current);
    }

    static <T, R> R currentValue(T current, java.util.function.Function<T, R> extractor, R defaultValue) {
        return current == null ? defaultValue : extractor.apply(current);
    }

    static <T extends Enum<T>> T enumValue(Class<T> type, String value, T defaultValue) {
        if (value == null || value.isBlank()) {
            return defaultValue;
        }
        if (type == AuthScheme.class) {
            AuthScheme resolved = resolveAuthScheme(value);
            return resolved == null ? defaultValue : type.cast(resolved);
        }
        return Enum.valueOf(type, normalizeEnumToken(type, value));
    }

    static <T extends Enum<T>> String normalizeEnumToken(Class<T> type, String value) {
        String normalized = value.trim().replace('-', '_').replace(' ', '_').toUpperCase(Locale.ROOT);
        if (type == AssetType.class) {
            return normalizeAssetTypeToken(normalized);
        }
        return normalized;
    }

    static String normalizeAssetTypeToken(String normalized) {
        return switch (normalized) {
            case "API", "STANDARD", "STANDARDAPI", "REST_API", "HTTP_API" -> "STANDARD_API";
            case "AI", "AI_MODEL", "MODEL", "LLM", "LLM_API" -> "AI_API";
            default -> normalized;
        };
    }

    static String firstText(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value.trim();
            }
        }
        return null;
    }

    record CurrentPlanState(
            List<ImportCategoryPlanModel> categoryPlans,
            List<ImportAssetPlanModel> assetPlans,
            String summary) {
    }

    record ParsedPlannerPayload(
            boolean hasCategoryPlanPatch,
            boolean hasAssetPlanPatch,
            List<String> clarificationQuestions,
            String summary) {
    }

    record PlanDraft(
            List<ImportCategoryPlanModel> categoryPlans,
            List<ImportAssetPlanModel> assetPlans,
            List<String> clarificationQuestions,
            String summary) {
    }

    record PlanValidationResult(
            boolean executable,
            String summary,
            List<String> clarificationQuestions,
            List<ImportAgentClarificationItemModel> clarificationItems) {
    }
}
