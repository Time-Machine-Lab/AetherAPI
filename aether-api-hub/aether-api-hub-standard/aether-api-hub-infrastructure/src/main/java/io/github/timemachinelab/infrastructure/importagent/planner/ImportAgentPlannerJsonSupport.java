package io.github.timemachinelab.infrastructure.importagent.planner;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.timemachinelab.domain.catalog.model.AssetType;
import io.github.timemachinelab.domain.catalog.model.AuthScheme;
import io.github.timemachinelab.domain.catalog.model.RequestMethod;
import io.github.timemachinelab.service.model.AsyncTaskConfigModel;
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
import java.net.URI;

/**
 * Shared JSON planning support for import agent planners.
 */
final class ImportAgentPlannerJsonSupport {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private ImportAgentPlannerJsonSupport() {
    }

    static ImportAgentPlanModel buildPlan(ImportAgentPlannerRequest request, JsonNode sourceNode) {
        CurrentPlanState currentPlanState = currentPlanState(request);
        ParsedPlannerPayload parsedPayload = parsePlannerPayload(sourceNode, currentPlanState.summary());
        PlanDraft mergedDraft = mergeWithCurrentPlan(sourceNode, currentPlanState, parsedPayload);
        PlanDraft normalizedDraft = normalizeDraft(mergedDraft);
        PlanValidationResult validationResult = validateDraft(request.getNextPlanVersion(), normalizedDraft);
        return new ImportAgentPlanModel(
                request.getNextPlanVersion(),
                validationResult.executable(),
                validationResult.summary(),
                validationResult.clarificationQuestions(),
                normalizedDraft.categoryPlans(),
                normalizedDraft.assetPlans()
        );
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

    private static ParsedPlannerPayload parsePlannerPayload(JsonNode sourceNode, String currentSummary) {
        if (sourceNode == null || !sourceNode.isObject()) {
            return new ParsedPlannerPayload(false, false, List.of(), currentSummary);
        }
        String parsedSummary = textValue(sourceNode, "summary");
        return new ParsedPlannerPayload(
                hasCategoryPlanField(sourceNode),
                hasAssetPlanField(sourceNode),
                parseStringArray(sourceNode, "clarificationQuestions"),
                parsedSummary == null ? currentSummary : parsedSummary);
    }

    private static PlanDraft mergeWithCurrentPlan(
            JsonNode sourceNode,
            CurrentPlanState currentPlanState,
            ParsedPlannerPayload parsedPayload) {
        List<ImportCategoryPlanModel> categoryPlans = parsedPayload.hasCategoryPlanPatch()
                ? parseCategoryPlans(sourceNode, currentPlanState.categoryPlans())
                : currentPlanState.categoryPlans();
        List<ImportAssetPlanModel> assetPlans = parsedPayload.hasAssetPlanPatch()
                ? parseAssetPlans(sourceNode, currentPlanState.assetPlans())
                : currentPlanState.assetPlans();
        return new PlanDraft(categoryPlans, assetPlans, parsedPayload.clarificationQuestions(), parsedPayload.summary());
    }

    private static PlanDraft normalizeDraft(PlanDraft draft) {
        List<ImportAssetPlanModel> reconciledAssetPlans = reconcileMissingSlots(draft.assetPlans());
        AsyncAssetPlanNormalizationResult normalizationResult = normalizeAsyncTaskQueryAssets(reconciledAssetPlans);
        List<ImportCategoryPlanModel> prunedCategoryPlans = pruneFoldedAsyncQueryCategories(
                draft.categoryPlans(),
                normalizationResult.assetPlans,
                normalizationResult.foldedCategoryCodes);
        List<ImportCategoryPlanModel> coveredCategoryPlans = ensureCategoryCoverage(
                prunedCategoryPlans,
                normalizationResult.assetPlans);
        return new PlanDraft(
                coveredCategoryPlans,
                normalizationResult.assetPlans,
                draft.clarificationQuestions(),
                draft.summary());
    }

    private static PlanValidationResult validateDraft(int nextPlanVersion, PlanDraft draft) {
        List<String> validationQuestions = validatePlan(draft.categoryPlans(), draft.assetPlans());
        LinkedHashSet<String> mergedQuestions = new LinkedHashSet<>(draft.clarificationQuestions());
        mergedQuestions.addAll(validationQuestions);
        List<String> clarificationQuestions = List.copyOf(mergedQuestions);
        boolean executable = clarificationQuestions.isEmpty();
        String summary = draft.summary() == null
                ? buildDefaultSummary(nextPlanVersion, draft.categoryPlans(), draft.assetPlans())
                : draft.summary();
        return new PlanValidationResult(executable, summary, clarificationQuestions);
    }

    private static boolean hasCategoryPlanField(JsonNode root) {
        return root.has("categoryPlans") || root.has("categories");
    }

    private static boolean hasAssetPlanField(JsonNode root) {
        return root.has("assetPlans") || root.has("assets");
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

    private static String buildDefaultSummary(
            int nextPlanVersion,
            List<ImportCategoryPlanModel> categoryPlans,
            List<ImportAssetPlanModel> assetPlans) {
        return "草稿计划版本 " + nextPlanVersion
                + " 已准备，包含 " + categoryPlans.size() + " 个分类计划和 " + assetPlans.size() + " 个资产计划。";
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
            questions.add("请提供包含 assetPlans 或 assets 的 JSON 以继续。");
        }
        for (ImportCategoryPlanModel categoryPlan : categoryPlans) {
            if (categoryPlan.getCategoryCode() == null || categoryPlan.getCategoryCode().isBlank()) {
                questions.add("每个分类计划都必须提供 categoryCode。");
            }
        }
        for (ImportAssetPlanModel assetPlan : assetPlans) {
            if (assetPlan.getApiCode() == null || assetPlan.getApiCode().isBlank()) {
                questions.add("每个资产计划都必须提供 apiCode。");
            }
            if (assetPlan.getAssetName() == null || assetPlan.getAssetName().isBlank()) {
                questions.add("每个资产计划都必须提供 assetName。");
            }
            if (assetPlan.getAssetType() == null) {
                questions.add("每个资产计划都必须提供 assetType。");
            }
            if (assetPlan.getAuthConfig() != null
                    && !assetPlan.getAuthConfig().isBlank()
                    && assetPlan.getAuthScheme() == null) {
                questions.add("资产计划 " + displayApiCode(assetPlan)
                        + " 已提供 authConfig 时必须同时提供 authScheme。");
            }
            if (assetPlan.getAuthScheme() != null
                    && assetPlan.getAuthScheme() != AuthScheme.NONE
                    && (assetPlan.getAuthConfig() == null || assetPlan.getAuthConfig().isBlank())) {
                questions.add("资产计划 " + displayApiCode(assetPlan) + " 使用 "
                        + assetPlan.getAuthScheme().name()
                        + " 鉴权时必须提供 authConfig；请补充 Header/Query 的参数名和值模板，或将 authScheme 改为 NONE。");
            }
            if (assetPlan.isPublishAfterImport()) {
                if (assetPlan.getCategoryCode() == null || assetPlan.getCategoryCode().isBlank()) {
                    questions.add("需要发布的资产计划必须提供 categoryCode。");
                }
                if (assetPlan.getRequestMethod() == null) {
                    questions.add("需要发布的资产计划必须提供 requestMethod。");
                }
                if (assetPlan.getUpstreamUrl() == null || assetPlan.getUpstreamUrl().isBlank()) {
                    questions.add("需要发布的资产计划必须提供 upstreamUrl。");
                }
                if (assetPlan.getAuthScheme() == null) {
                    questions.add("需要发布的资产计划 " + displayApiCode(assetPlan) + " 必须提供 authScheme。");
                }
                if (assetPlan.getAssetType() == AssetType.AI_API
                        && (assetPlan.getAiProfile() == null
                        || assetPlan.getAiProfile().getProvider() == null
                        || assetPlan.getAiProfile().getModel() == null)) {
                    questions.add("需要发布的 AI_API 资产计划必须提供 aiProfile 的 provider 和 model。");
                }
                if (assetPlan.getAsyncTaskConfig() != null && Boolean.TRUE.equals(assetPlan.getAsyncTaskConfig().getEnabled())) {
                    if (assetPlan.getAsyncTaskConfig().getAuthMode() == null || assetPlan.getAsyncTaskConfig().getAuthMode().isBlank()) {
                        questions.add("启用异步任务查询时必须提供 asyncTaskConfig.authMode。");
                    }
                    if (assetPlan.getAsyncTaskConfig().getQueryMethod() == null || assetPlan.getAsyncTaskConfig().getQueryMethod().isBlank()) {
                        questions.add("启用异步任务查询时必须提供 asyncTaskConfig.queryMethod。");
                    }
                    if (assetPlan.getAsyncTaskConfig().getQueryUrlTemplate() == null
                            || assetPlan.getAsyncTaskConfig().getQueryUrlTemplate().isBlank()
                            || !assetPlan.getAsyncTaskConfig().getQueryUrlTemplate().contains("{taskId}")) {
                        questions.add("启用异步任务查询时必须提供包含 {taskId} 的 asyncTaskConfig.queryUrlTemplate。");
                    }
                    if ("OVERRIDE".equalsIgnoreCase(assetPlan.getAsyncTaskConfig().getAuthMode())) {
                        if (assetPlan.getAsyncTaskConfig().getAuthScheme() == null || assetPlan.getAsyncTaskConfig().getAuthScheme().isBlank()) {
                            questions.add("asyncTaskConfig.authMode 为 OVERRIDE 时必须提供 asyncTaskConfig.authScheme。");
                        }
                        if (assetPlan.getAsyncTaskConfig().getAuthConfig() == null || assetPlan.getAsyncTaskConfig().getAuthConfig().isBlank()) {
                            questions.add("asyncTaskConfig.authMode 为 OVERRIDE 时必须提供 asyncTaskConfig.authConfig。");
                        }
                    }
                }
            }
        }
        return List.copyOf(questions);
    }

    private static String displayApiCode(ImportAssetPlanModel assetPlan) {
        if (assetPlan == null || assetPlan.getApiCode() == null || assetPlan.getApiCode().isBlank()) {
            return "<unknown>";
        }
        return assetPlan.getApiCode();
    }

    private static List<ImportCategoryPlanModel> parseCategoryPlans(
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
            String categoryCode = textValue(categoryNode, "categoryCode");
            ImportCategoryPlanModel currentCategoryPlan = categoryCode == null ? null : currentByCode.remove(categoryCode);
            values.add(mergeCategoryPlan(categoryNode, currentCategoryPlan));
        }
        values.addAll(currentByCode.values());
        values.addAll(anonymousCurrent);
        return values;
    }

    private static List<ImportAssetPlanModel> parseAssetPlans(
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
        for (JsonNode assetNode : assetArray) {
            String apiCode = textValue(assetNode, "apiCode");
            ImportAssetPlanModel currentAssetPlan = apiCode == null ? null : currentByCode.remove(apiCode);
            values.add(mergeAssetPlan(assetNode, currentAssetPlan));
        }
        values.addAll(currentByCode.values());
        values.addAll(anonymousCurrent);
        return values;
    }

    private static AsyncAssetPlanNormalizationResult normalizeAsyncTaskQueryAssets(List<ImportAssetPlanModel> assetPlans) {
        if (assetPlans == null || assetPlans.size() < 2) {
            return new AsyncAssetPlanNormalizationResult(assetPlans == null ? List.of() : assetPlans, List.of());
        }
        List<ImportAssetPlanModel> normalized = new ArrayList<>(assetPlans);
        LinkedHashSet<String> foldedCategoryCodes = new LinkedHashSet<>();
        boolean changed = false;
        for (int index = normalized.size() - 1; index >= 0; index -= 1) {
            ImportAssetPlanModel queryAsset = normalized.get(index);
            if (!isAsyncTaskQueryAsset(queryAsset)) {
                continue;
            }
            int submitIndex = findAsyncSubmitAssetIndex(normalized, index);
            if (submitIndex < 0) {
                continue;
            }
            ImportAssetPlanModel submitAsset = normalized.get(submitIndex);
            normalized.set(submitIndex, mergeAsyncTaskConfig(submitAsset, queryAsset));
            normalized.remove(index);
            if (queryAsset.getCategoryCode() != null && !queryAsset.getCategoryCode().isBlank()) {
                foldedCategoryCodes.add(queryAsset.getCategoryCode());
            }
            changed = true;
        }
        return new AsyncAssetPlanNormalizationResult(changed ? List.copyOf(normalized) : assetPlans, List.copyOf(foldedCategoryCodes));
    }

    private static boolean isAsyncTaskQueryAsset(ImportAssetPlanModel assetPlan) {
        if (assetPlan == null || assetPlan.getRequestMethod() != RequestMethod.GET) {
            return false;
        }
        String searchable = String.join(" ",
                safeLower(assetPlan.getApiCode()),
                safeLower(assetPlan.getAssetName()),
                safeLower(assetPlan.getUpstreamUrl())
        );
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

    private static int findAsyncSubmitAssetIndex(List<ImportAssetPlanModel> assetPlans, int queryIndex) {
        ImportAssetPlanModel queryAsset = assetPlans.get(queryIndex);
        int bestIndex = -1;
        int bestScore = -1;
        for (int index = 0; index < assetPlans.size(); index += 1) {
            if (index == queryIndex) {
                continue;
            }
            ImportAssetPlanModel candidate = assetPlans.get(index);
            if (candidate == null || isAsyncTaskQueryAsset(candidate)) {
                continue;
            }
            int score = scoreAsyncSubmitCandidate(candidate, queryAsset);
            if (score > bestScore) {
                bestIndex = index;
                bestScore = score;
            }
        }
        return bestScore >= 4 ? bestIndex : -1;
    }

    private static int scoreAsyncSubmitCandidate(ImportAssetPlanModel candidate, ImportAssetPlanModel queryAsset) {
        int score = candidate.getRequestMethod() == RequestMethod.POST ? 2 : 0;
        if (candidate.getAsyncTaskConfig() != null && Boolean.TRUE.equals(candidate.getAsyncTaskConfig().getEnabled())) {
            score += 4;
        }
        if (sameText(candidate.getCategoryCode(), queryAsset.getCategoryCode())) {
            score += 3;
        }
        if (sameUrlHost(candidate.getUpstreamUrl(), queryAsset.getUpstreamUrl())) {
            score += 2;
        }
        if (shareMeaningfulToken(candidate.getApiCode(), queryAsset.getApiCode())
                || shareMeaningfulToken(candidate.getAssetName(), queryAsset.getAssetName())) {
            score += 2;
        }
        return score;
    }

    private static ImportAssetPlanModel mergeAsyncTaskConfig(ImportAssetPlanModel submitAsset, ImportAssetPlanModel queryAsset) {
        AsyncTaskConfigModel current = submitAsset.getAsyncTaskConfig();
        AsyncTaskConfigModel merged = normalizeAsyncTaskConfig(new AsyncTaskConfigModel(
                true,
                queryAsset.getRequestMethod() == null
                        ? currentValue(current, AsyncTaskConfigModel::getQueryMethod, "GET")
                        : queryAsset.getRequestMethod().name(),
                resolveAsyncTaskQueryUrlTemplate(queryAsset, current),
                resolveAsyncTaskAuthMode(submitAsset, queryAsset, current),
                resolveAsyncTaskAuthScheme(submitAsset, queryAsset, current),
                resolveAsyncTaskAuthConfig(submitAsset, queryAsset, current),
                currentValue(current, AsyncTaskConfigModel::getStatusPath),
                currentValue(current, AsyncTaskConfigModel::getResultPath),
                currentValue(current, AsyncTaskConfigModel::getErrorPath)
            ));
        return new ImportAssetPlanModel(
                submitAsset.getApiCode(),
                submitAsset.getAssetName(),
                submitAsset.getAssetType(),
                submitAsset.getCategoryCode(),
                submitAsset.getRequestMethod(),
                submitAsset.getUpstreamUrl(),
                submitAsset.getAuthScheme(),
                submitAsset.getAuthConfig(),
                submitAsset.getRequestTemplate(),
                submitAsset.getRequestExample(),
                submitAsset.getResponseExample(),
                submitAsset.getRequestJsonSchema(),
                submitAsset.getResponseJsonSchema(),
                submitAsset.isPublishAfterImport(),
                merged,
                submitAsset.getAiProfile()
        );
    }

    private static String resolveAsyncTaskQueryUrlTemplate(ImportAssetPlanModel queryAsset, AsyncTaskConfigModel current) {
        String currentTemplate = normalizeTaskIdPlaceholder(currentValue(current, AsyncTaskConfigModel::getQueryUrlTemplate));
        if (currentTemplate != null) {
            return currentTemplate;
        }
        return normalizeTaskIdPlaceholder(queryAsset.getUpstreamUrl());
    }

    private static String resolveAsyncTaskAuthMode(
            ImportAssetPlanModel submitAsset,
            ImportAssetPlanModel queryAsset,
            AsyncTaskConfigModel current) {
        String currentAuthMode = currentValue(current, AsyncTaskConfigModel::getAuthMode);
        if (currentAuthMode != null) {
            return currentAuthMode;
        }
        if (queryAsset.getAuthScheme() == null && queryAsset.getAuthConfig() == null) {
            return "SAME_AS_SUBMIT";
        }
        if (queryAsset.getAuthScheme() == submitAsset.getAuthScheme() && sameText(queryAsset.getAuthConfig(), submitAsset.getAuthConfig())) {
            return "SAME_AS_SUBMIT";
        }
        return "OVERRIDE";
    }

    private static String resolveAsyncTaskAuthScheme(
            ImportAssetPlanModel submitAsset,
            ImportAssetPlanModel queryAsset,
            AsyncTaskConfigModel current) {
        String currentAuthScheme = currentValue(current, AsyncTaskConfigModel::getAuthScheme);
        if (currentAuthScheme != null) {
            return currentAuthScheme;
        }
        String authMode = resolveAsyncTaskAuthMode(submitAsset, queryAsset, current);
        if ("SAME_AS_SUBMIT".equals(authMode)) {
            return null;
        }
        return queryAsset.getAuthScheme() == null ? null : queryAsset.getAuthScheme().name();
    }

    private static String resolveAsyncTaskAuthConfig(
            ImportAssetPlanModel submitAsset,
            ImportAssetPlanModel queryAsset,
            AsyncTaskConfigModel current) {
        String currentAuthConfig = currentValue(current, AsyncTaskConfigModel::getAuthConfig);
        if (currentAuthConfig != null) {
            return currentAuthConfig;
        }
        String authMode = resolveAsyncTaskAuthMode(submitAsset, queryAsset, current);
        return "SAME_AS_SUBMIT".equals(authMode) ? null : queryAsset.getAuthConfig();
    }

    private static List<ImportCategoryPlanModel> pruneFoldedAsyncQueryCategories(
            List<ImportCategoryPlanModel> categoryPlans,
            List<ImportAssetPlanModel> assetPlans,
            List<String> foldedCategoryCodes) {
        if (foldedCategoryCodes.isEmpty() || categoryPlans == null || categoryPlans.isEmpty()) {
            return categoryPlans;
        }
        LinkedHashSet<String> usedCategoryCodes = new LinkedHashSet<>();
        for (ImportAssetPlanModel assetPlan : assetPlans) {
            if (assetPlan.getCategoryCode() != null && !assetPlan.getCategoryCode().isBlank()) {
                usedCategoryCodes.add(assetPlan.getCategoryCode());
            }
        }
        List<ImportCategoryPlanModel> values = new ArrayList<>();
        for (ImportCategoryPlanModel categoryPlan : categoryPlans) {
            if (categoryPlan.getCategoryCode() != null
                    && foldedCategoryCodes.contains(categoryPlan.getCategoryCode())
                    && !usedCategoryCodes.contains(categoryPlan.getCategoryCode())) {
                continue;
            }
            values.add(categoryPlan);
        }
        return List.copyOf(values);
    }

    private static ImportCategoryPlanModel mergeCategoryPlan(JsonNode categoryNode, ImportCategoryPlanModel currentCategoryPlan) {
        return new ImportCategoryPlanModel(
                hasField(categoryNode, "categoryCode") ? textValue(categoryNode, "categoryCode") : currentValue(currentCategoryPlan, ImportCategoryPlanModel::getCategoryCode),
                hasField(categoryNode, "categoryName") ? textValue(categoryNode, "categoryName") : currentValue(currentCategoryPlan, ImportCategoryPlanModel::getCategoryName),
                hasField(categoryNode, "action")
                        ? enumValue(ImportCategoryPlanAction.class, textValue(categoryNode, "action"), ImportCategoryPlanAction.CREATE_IF_MISSING)
                        : currentValue(currentCategoryPlan, ImportCategoryPlanModel::getAction, ImportCategoryPlanAction.CREATE_IF_MISSING)
        );
    }

    private static ImportAssetPlanModel mergeAssetPlan(JsonNode assetNode, ImportAssetPlanModel currentAssetPlan) {
        return new ImportAssetPlanModel(
                hasField(assetNode, "apiCode") ? textValue(assetNode, "apiCode") : currentValue(currentAssetPlan, ImportAssetPlanModel::getApiCode),
                hasField(assetNode, "assetName") ? textValue(assetNode, "assetName") : currentValue(currentAssetPlan, ImportAssetPlanModel::getAssetName),
                hasField(assetNode, "assetType")
                        ? enumValue(AssetType.class, textValue(assetNode, "assetType"), null)
                        : currentValue(currentAssetPlan, ImportAssetPlanModel::getAssetType),
                hasField(assetNode, "categoryCode") ? textValue(assetNode, "categoryCode") : currentValue(currentAssetPlan, ImportAssetPlanModel::getCategoryCode),
                hasField(assetNode, "requestMethod")
                        ? enumValue(RequestMethod.class, textValue(assetNode, "requestMethod"), null)
                        : currentValue(currentAssetPlan, ImportAssetPlanModel::getRequestMethod),
                hasField(assetNode, "upstreamUrl") ? textValue(assetNode, "upstreamUrl") : currentValue(currentAssetPlan, ImportAssetPlanModel::getUpstreamUrl),
                hasField(assetNode, "authScheme")
                        ? enumValue(AuthScheme.class, textValue(assetNode, "authScheme"), null)
                        : currentValue(currentAssetPlan, ImportAssetPlanModel::getAuthScheme),
                hasField(assetNode, "authConfig") ? authConfigValue(assetNode.path("authConfig")) : currentValue(currentAssetPlan, ImportAssetPlanModel::getAuthConfig),
                hasField(assetNode, "requestTemplate") ? textValue(assetNode, "requestTemplate") : currentValue(currentAssetPlan, ImportAssetPlanModel::getRequestTemplate),
                hasField(assetNode, "requestExample") ? textValue(assetNode, "requestExample") : currentValue(currentAssetPlan, ImportAssetPlanModel::getRequestExample),
                hasField(assetNode, "responseExample") ? textValue(assetNode, "responseExample") : currentValue(currentAssetPlan, ImportAssetPlanModel::getResponseExample),
                schemaValue(assetNode, currentValue(currentAssetPlan, ImportAssetPlanModel::getRequestJsonSchema), "requestJsonSchema", "requestSchema", "inputSchema"),
                schemaValue(assetNode, currentValue(currentAssetPlan, ImportAssetPlanModel::getResponseJsonSchema), "responseJsonSchema", "responseSchema", "outputSchema"),
                hasField(assetNode, "publishAfterImport")
                        ? assetNode.path("publishAfterImport").asBoolean(false)
                        : currentValue(currentAssetPlan, ImportAssetPlanModel::isPublishAfterImport, false),
                hasField(assetNode, "asyncTaskConfig")
                        ? parseAsyncTaskConfig(assetNode.path("asyncTaskConfig"), currentValue(currentAssetPlan, ImportAssetPlanModel::getAsyncTaskConfig))
                        : currentValue(currentAssetPlan, ImportAssetPlanModel::getAsyncTaskConfig),
                hasField(assetNode, "aiProfile")
                        ? parseAiProfile(assetNode.path("aiProfile"), currentValue(currentAssetPlan, ImportAssetPlanModel::getAiProfile))
                        : currentValue(currentAssetPlan, ImportAssetPlanModel::getAiProfile)
        );
    }

    private static AsyncTaskConfigModel parseAsyncTaskConfig(JsonNode node, AsyncTaskConfigModel current) {
        if (node == null || node.isMissingNode() || node.isNull()) {
            return null;
        }
        if (!node.isObject()) {
            return current;
        }
        return normalizeAsyncTaskConfig(new AsyncTaskConfigModel(
                hasField(node, "enabled") ? node.path("enabled").asBoolean(false) : currentValue(current, AsyncTaskConfigModel::getEnabled),
                hasField(node, "queryMethod") ? textValue(node, "queryMethod") : currentValue(current, AsyncTaskConfigModel::getQueryMethod),
                hasField(node, "queryUrlTemplate") ? textValue(node, "queryUrlTemplate") : currentValue(current, AsyncTaskConfigModel::getQueryUrlTemplate),
                hasField(node, "authMode") ? textValue(node, "authMode") : currentValue(current, AsyncTaskConfigModel::getAuthMode),
                hasField(node, "authScheme") ? textValue(node, "authScheme") : currentValue(current, AsyncTaskConfigModel::getAuthScheme),
                hasField(node, "authConfig") ? authConfigValue(node.path("authConfig")) : currentValue(current, AsyncTaskConfigModel::getAuthConfig),
                hasField(node, "statusPath") ? textValue(node, "statusPath") : currentValue(current, AsyncTaskConfigModel::getStatusPath),
                hasField(node, "resultPath") ? textValue(node, "resultPath") : currentValue(current, AsyncTaskConfigModel::getResultPath),
                hasField(node, "errorPath") ? textValue(node, "errorPath") : currentValue(current, AsyncTaskConfigModel::getErrorPath)
        ));
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
                hasField(node, "provider") ? textValue(node, "provider") : currentValue(currentAiProfile, ImportAiProfileModel::getProvider),
                hasField(node, "model") ? textValue(node, "model") : currentValue(currentAiProfile, ImportAiProfileModel::getModel),
                hasField(node, "streamingSupported") ? node.path("streamingSupported").asBoolean(false) : currentValue(currentAiProfile, ImportAiProfileModel::isStreamingSupported, false),
                hasField(node, "capabilityTags") ? tags : currentValue(currentAiProfile, ImportAiProfileModel::getCapabilityTags, List.of())
        );
    }

    private static boolean hasField(JsonNode node, String fieldName) {
        return node != null && node.has(fieldName);
    }

    private static List<ImportAssetPlanModel> reconcileMissingSlots(List<ImportAssetPlanModel> assetPlans) {
        if (assetPlans == null || assetPlans.isEmpty()) {
            return assetPlans;
        }
        List<ImportAssetPlanModel> resolved = new ArrayList<>(assetPlans.size());
        for (ImportAssetPlanModel assetPlan : assetPlans) {
            resolved.add(reconcileAssetPlan(assetPlan));
        }
        return List.copyOf(resolved);
    }

    private static ImportAssetPlanModel reconcileAssetPlan(ImportAssetPlanModel assetPlan) {
        if (assetPlan == null) {
            return null;
        }
        AsyncTaskConfigModel asyncTaskConfig = assetPlan.getAsyncTaskConfig();
        if (asyncTaskConfig != null && Boolean.TRUE.equals(asyncTaskConfig.getEnabled())) {
            String queryUrlTemplate = asyncTaskConfig.getQueryUrlTemplate();
            String queryMethod = asyncTaskConfig.getQueryMethod();
            String authMode = asyncTaskConfig.getAuthMode();
            if (authMode == null || authMode.isBlank()) {
                authMode = (asyncTaskConfig.getAuthScheme() == null && (asyncTaskConfig.getAuthConfig() == null || asyncTaskConfig.getAuthConfig().isBlank()))
                        ? "SAME_AS_SUBMIT"
                        : "OVERRIDE";
            }
            String asyncAuthConfig = asyncTaskConfig.getAuthConfig();
            String asyncAuthScheme = asyncTaskConfig.getAuthScheme();
            asyncTaskConfig = normalizeAsyncTaskConfig(new AsyncTaskConfigModel(
                    asyncTaskConfig.getEnabled(),
                    queryMethod,
                    queryUrlTemplate,
                    authMode,
                    asyncAuthScheme,
                    asyncAuthConfig,
                    asyncTaskConfig.getStatusPath(),
                    asyncTaskConfig.getResultPath(),
                    asyncTaskConfig.getErrorPath()));
        }

        return new ImportAssetPlanModel(
                assetPlan.getApiCode(),
                assetPlan.getAssetName(),
                assetPlan.getAssetType(),
                assetPlan.getCategoryCode(),
                assetPlan.getRequestMethod(),
                assetPlan.getUpstreamUrl(),
                assetPlan.getAuthScheme(),
                assetPlan.getAuthConfig(),
                assetPlan.getRequestTemplate(),
                assetPlan.getRequestExample(),
                assetPlan.getResponseExample(),
                assetPlan.getRequestJsonSchema(),
                assetPlan.getResponseJsonSchema(),
                assetPlan.isPublishAfterImport(),
                asyncTaskConfig,
                assetPlan.getAiProfile());
    }

    private static AsyncTaskConfigModel normalizeAsyncTaskConfig(AsyncTaskConfigModel config) {
        if (config == null) {
            return null;
        }
        String authMode = normalizeAsyncTaskAuthMode(config.getAuthMode(), config.getAuthScheme(), config.getAuthConfig());
        String authScheme = normalizeAsyncTaskAuthScheme(config.getAuthMode(), config.getAuthScheme(), authMode);
        return new AsyncTaskConfigModel(
                config.getEnabled(),
                config.getQueryMethod(),
                config.getQueryUrlTemplate(),
                authMode,
                authScheme,
                config.getAuthConfig(),
                config.getStatusPath(),
                config.getResultPath(),
                config.getErrorPath());
    }

    private static String normalizeAsyncTaskAuthMode(String authMode, String authScheme, String authConfig) {
        String normalized = normalizeEnumText(authMode);
        if ("SAME_AS_SUBMIT".equals(normalized) || "OVERRIDE".equals(normalized)) {
            return normalized;
        }
        if (resolveAuthScheme(normalized) != null) {
            return "OVERRIDE";
        }
        return null;
    }

    private static String normalizeAsyncTaskAuthScheme(String authMode, String authScheme, String normalizedAuthMode) {
        AuthScheme normalizedScheme = resolveAuthScheme(authScheme);
        if (normalizedScheme != null) {
            return normalizedScheme.name();
        }
        if ("SAME_AS_SUBMIT".equals(normalizedAuthMode)) {
            return null;
        }
        normalizedScheme = resolveAuthScheme(authMode);
        if (normalizedScheme != null) {
            return normalizedScheme.name();
        }
        return null;
    }

    private static AuthScheme resolveAuthScheme(String authScheme) {
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

    private static String normalizeEnumText(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim().toUpperCase(Locale.ROOT);
    }

    private static String schemaValue(JsonNode node, String currentValue, String... fieldNames) {
        for (String fieldName : fieldNames) {
            if (hasField(node, fieldName)) {
                return textValue(node, fieldName);
            }
        }
        return currentValue;
    }

    private static String authConfigValue(JsonNode node) {
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

    private static String normalizeTaskIdPlaceholder(String candidate) {
        if (candidate == null || candidate.isBlank()) {
            return null;
        }
        String normalized = candidate
                .replace("{id}", "{taskId}")
                .replace("{task_id}", "{taskId}")
                .replace("{video_id}", "{taskId}")
                .replace("<task_id>", "{taskId}")
                .replace("<video_id>", "{taskId}")
                .replace(":task_id", "{taskId}")
                .replace(":video_id", "{taskId}");
        normalized = normalized.replaceAll("(?i)([?&](?:id|task_id|taskId|video_id)=)[^&#\\s]+", "$1{taskId}");
        if (normalized.contains("{taskId}")) {
            return normalized;
        }
        return null;
    }

    private static <T, R> R currentValue(T current, java.util.function.Function<T, R> extractor) {
        return current == null ? null : extractor.apply(current);
    }

    private static <T, R> R currentValue(T current, java.util.function.Function<T, R> extractor, R defaultValue) {
        return current == null ? defaultValue : extractor.apply(current);
    }

    private static boolean sameText(String left, String right) {
        if (left == null || right == null) {
            return left == null && right == null;
        }
        return left.trim().equalsIgnoreCase(right.trim());
    }

    private static boolean sameUrlHost(String left, String right) {
        try {
            if (left == null || right == null) {
                return false;
            }
            URI leftUri = URI.create(left);
            URI rightUri = URI.create(right);
            return leftUri.getHost() != null && leftUri.getHost().equalsIgnoreCase(rightUri.getHost());
        } catch (IllegalArgumentException ex) {
            return false;
        }
    }

    private static boolean shareMeaningfulToken(String left, String right) {
        if (left == null || right == null) {
            return false;
        }
        String normalizedRight = safeLower(right);
        for (String token : safeLower(left).split("[^a-z0-9\\u4e00-\\u9fa5]+")) {
            if (token.length() < 4 || isAsyncQueryToken(token)) {
                continue;
            }
            if (normalizedRight.contains(token)) {
                return true;
            }
        }
        return false;
    }

    private static boolean isAsyncQueryToken(String token) {
        return "query".equals(token)
                || "detail".equals(token)
                || "result".equals(token)
                || "status".equals(token)
                || "task".equals(token)
                || "submit".equals(token);
    }

    private static String firstText(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value.trim();
            }
        }
        return null;
    }

    private static String safeLower(String value) {
        return value == null ? "" : value.toLowerCase(Locale.ROOT);
    }

        private record CurrentPlanState(
            List<ImportCategoryPlanModel> categoryPlans,
            List<ImportAssetPlanModel> assetPlans,
            String summary) {
        }

        private record ParsedPlannerPayload(
            boolean hasCategoryPlanPatch,
            boolean hasAssetPlanPatch,
            List<String> clarificationQuestions,
            String summary) {
        }

        private record PlanDraft(
            List<ImportCategoryPlanModel> categoryPlans,
            List<ImportAssetPlanModel> assetPlans,
            List<String> clarificationQuestions,
            String summary) {
        }

        private record PlanValidationResult(
            boolean executable,
            String summary,
            List<String> clarificationQuestions) {
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
            AuthScheme resolved = resolveAuthScheme(value);
            return resolved == null ? defaultValue : type.cast(resolved);
        }
        return Enum.valueOf(type, normalizeEnumToken(type, value));
    }

    private static <T extends Enum<T>> String normalizeEnumToken(Class<T> type, String value) {
        String normalized = value.trim().replace('-', '_').replace(' ', '_').toUpperCase(Locale.ROOT);
        if (type == AssetType.class) {
            return normalizeAssetTypeToken(normalized);
        }
        return normalized;
    }

    private static String normalizeAssetTypeToken(String normalized) {
        return switch (normalized) {
            case "API", "STANDARD", "STANDARDAPI", "REST_API", "HTTP_API" -> "STANDARD_API";
            case "AI", "AI_MODEL", "MODEL", "LLM", "LLM_API" -> "AI_API";
            default -> normalized;
        };
    }

    private static final class AsyncAssetPlanNormalizationResult {
        private final List<ImportAssetPlanModel> assetPlans;
        private final List<String> foldedCategoryCodes;

        private AsyncAssetPlanNormalizationResult(
                List<ImportAssetPlanModel> assetPlans,
                List<String> foldedCategoryCodes) {
            this.assetPlans = assetPlans;
            this.foldedCategoryCodes = foldedCategoryCodes;
        }
    }
}
