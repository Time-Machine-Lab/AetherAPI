package io.github.timemachinelab.infrastructure.importagent.planner;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
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
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Shared JSON planning support for import agent planners.
 */
final class ImportAgentPlannerJsonSupport {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final Pattern HTTP_URL_PATTERN = Pattern.compile("https?://[^\\s)>\"]+", Pattern.CASE_INSENSITIVE);
    private static final Pattern HEADING_PATTERN = Pattern.compile("(?m)^#{1,6}\\s+(.+?)\\s*$");
    private static final Pattern LIST_FIELD_PATTERN = Pattern.compile(
        "(?im)^(?:[-*]\\s*)?(?:请求地址|接口地址|接口URL|请求URL|URL|url)\\s*[:：]\\s*(https?://\\S+)\\s*$"
    );
    private static final Pattern METHOD_FIELD_PATTERN = Pattern.compile(
        "(?im)^(?:[-*]\\s*)?(?:请求方式|调用方式|Method|METHOD)\\s*[:：]\\s*(GET|POST|PUT|DELETE|PATCH)\\s*$"
    );
    private static final Pattern AUTH_FIELD_PATTERN = Pattern.compile(
        "(?im)^(?:[-*]\\s*)?(?:鉴权方式|认证方式|Auth|AUTH)\\s*[:：]\\s*(.+?)\\s*$"
    );

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
        String summary = request.getCurrentPlan() == null ? null : request.getCurrentPlan().getSummary();

        if (sourceNode != null && sourceNode.isObject()) {
            if (hasCategoryPlanField(sourceNode)) {
                categoryPlans = parseCategoryPlans(sourceNode);
            }
            if (hasAssetPlanField(sourceNode)) {
                assetPlans = parseAssetPlans(sourceNode);
            }
            clarificationQuestions = parseStringArray(sourceNode, "clarificationQuestions");
            String resolvedSummary = textValue(sourceNode, "summary");
            if (resolvedSummary != null) {
                summary = resolvedSummary;
            }
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

    private static boolean hasCategoryPlanField(JsonNode root) {
        return root.has("categoryPlans") || root.has("categories");
    }

    private static boolean hasAssetPlanField(JsonNode root) {
        return root.has("assetPlans") || root.has("assets");
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
            JsonNode extracted = parseEmbeddedJsonObject(trimmed);
            if (extracted != null) {
                return extracted;
            }
            return parseDocumentCandidate(trimmed);
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

    private static JsonNode parseDocumentCandidate(String candidate) {
        if (candidate == null || candidate.isBlank()) {
            return null;
        }

        String assetName = detectAssetName(candidate);
        String upstreamUrl = detectUpstreamUrl(candidate);
        RequestMethod requestMethod = detectRequestMethod(candidate);
        AuthScheme authScheme = detectAuthScheme(candidate);
        String apiCode = buildApiCode(assetName, upstreamUrl);
        if (apiCode == null || assetName == null) {
            return null;
        }

        ObjectNode root = OBJECT_MAPPER.createObjectNode();
        root.put("summary", "Draft plan inferred from API documentation.");
        ArrayNode assetPlans = root.putArray("assetPlans");
        ObjectNode assetNode = assetPlans.addObject();
        assetNode.put("apiCode", apiCode);
        assetNode.put("assetName", assetName);
        assetNode.put("assetType", detectAssetType(candidate).name());
        if (requestMethod != null) {
            assetNode.put("requestMethod", requestMethod.name());
        }
        if (upstreamUrl != null) {
            assetNode.put("upstreamUrl", upstreamUrl);
        }
        if (authScheme != null) {
            assetNode.put("authScheme", authScheme.name());
        }
        assetNode.put("publishAfterImport", false);
        return root;
    }

    private static AssetType detectAssetType(String candidate) {
        String normalized = candidate.toLowerCase(Locale.ROOT);
        if (normalized.contains("openai")
                || normalized.contains("claude")
                || normalized.contains("gemini")
                || normalized.contains("llm")
                || normalized.contains("模型")
                || normalized.contains("chat completions")
                || normalized.contains("chat/completions")) {
            return AssetType.AI_API;
        }
        return AssetType.STANDARD_API;
    }

    private static String detectAssetName(String candidate) {
        Matcher headingMatcher = HEADING_PATTERN.matcher(candidate);
        while (headingMatcher.find()) {
            String heading = cleanHeading(headingMatcher.group(1));
            if (heading != null) {
                return heading;
            }
        }
        return null;
    }

    private static String cleanHeading(String heading) {
        if (heading == null) {
            return null;
        }
        String normalized = heading
                .replace("`", "")
                .replace("|", " ")
                .trim();
        if (normalized.isBlank()) {
            return null;
        }
        String lower = normalized.toLowerCase(Locale.ROOT);
        if (lower.startsWith("接口信息")
                || lower.startsWith("请求参数")
                || lower.startsWith("返回参数")
                || lower.startsWith("response")
                || lower.startsWith("request")) {
            return null;
        }
        return normalized;
    }

    private static String detectUpstreamUrl(String candidate) {
        Matcher labelled = LIST_FIELD_PATTERN.matcher(candidate);
        if (labelled.find()) {
            return labelled.group(1);
        }
        Matcher generic = HTTP_URL_PATTERN.matcher(candidate);
        return generic.find() ? generic.group() : null;
    }

    private static RequestMethod detectRequestMethod(String candidate) {
        Matcher labelled = METHOD_FIELD_PATTERN.matcher(candidate);
        if (labelled.find()) {
            return enumValue(RequestMethod.class, labelled.group(1).toUpperCase(Locale.ROOT), null);
        }
        String upper = candidate.toUpperCase(Locale.ROOT);
        for (RequestMethod requestMethod : RequestMethod.values()) {
            if (upper.contains(" " + requestMethod.name() + " ")
                    || upper.contains("(" + requestMethod.name() + ")")
                    || upper.contains("请求方式: " + requestMethod.name())
                    || upper.contains("请求方式：" + requestMethod.name())) {
                return requestMethod;
            }
        }
        return null;
    }

    private static AuthScheme detectAuthScheme(String candidate) {
        Matcher labelled = AUTH_FIELD_PATTERN.matcher(candidate);
        if (!labelled.find()) {
            return null;
        }
        String normalized = labelled.group(1).toLowerCase(Locale.ROOT);
        if (normalized.contains("header") && normalized.contains("token")) {
            return AuthScheme.HEADER_TOKEN;
        }
        if (normalized.contains("bearer")) {
            return AuthScheme.HEADER_TOKEN;
        }
        if (normalized.contains("query")) {
            return AuthScheme.QUERY_TOKEN;
        }
        if (normalized.contains("none") || normalized.contains("无需") || normalized.contains("无")) {
            return AuthScheme.NONE;
        }
        return null;
    }

    private static String buildApiCode(String assetName, String upstreamUrl) {
        if (upstreamUrl != null && !upstreamUrl.isBlank()) {
            String path = upstreamUrl.replaceFirst("https?://[^/]+", "");
            String[] segments = path.split("/");
            List<String> normalizedSegments = new ArrayList<>();
            for (int index = segments.length - 1; index >= 0; index -= 1) {
                String segment = segments[index] == null ? "" : segments[index].trim();
                if (segment.isBlank() || segment.startsWith("{")) {
                    continue;
                }
                String normalized = normalizeApiCode(segment);
                if (normalized.isBlank() || isBoilerplatePathSegment(normalized)) {
                    continue;
                }
                normalizedSegments.add(0, normalized);
            }
            if (!normalizedSegments.isEmpty()) {
                if (normalizedSegments.size() >= 2) {
                    return normalizedSegments.get(normalizedSegments.size() - 2)
                            + "-"
                            + normalizedSegments.get(normalizedSegments.size() - 1);
                }
                return normalizedSegments.get(0);
            }
        }
        return assetName == null ? null : normalizeApiCode(assetName);
    }

    private static boolean isBoilerplatePathSegment(String segment) {
        return "api".equals(segment)
                || "apis".equals(segment)
                || segment.matches("v\\d+")
                || segment.matches("version-?\\d+");
    }

    private static String normalizeApiCode(String raw) {
        if (raw == null) {
            return "";
        }
        String normalized = raw
                .replaceAll("([a-z0-9])([A-Z])", "$1-$2")
                .replaceAll("[^\\p{IsAlphabetic}\\p{IsDigit}]+", "-")
                .replaceAll("-+", "-")
                .replaceAll("^-|-$", "")
                .toLowerCase(Locale.ROOT);
        if (!normalized.isBlank()) {
            return normalized;
        }

        StringBuilder ascii = new StringBuilder();
        for (char character : raw.toCharArray()) {
            if (Character.isLetterOrDigit(character)) {
                ascii.append(Character.toLowerCase(character));
            } else if (ascii.length() > 0 && ascii.charAt(ascii.length() - 1) != '-') {
                ascii.append('-');
            }
        }
        return ascii.toString().replaceAll("-+", "-").replaceAll("^-|-$", "");
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