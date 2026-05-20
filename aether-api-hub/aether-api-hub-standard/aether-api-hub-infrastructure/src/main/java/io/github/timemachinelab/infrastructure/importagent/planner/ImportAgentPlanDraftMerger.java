package io.github.timemachinelab.infrastructure.importagent.planner;

import com.fasterxml.jackson.databind.JsonNode;
import io.github.timemachinelab.domain.catalog.model.AuthScheme;
import io.github.timemachinelab.domain.catalog.model.RequestMethod;
import io.github.timemachinelab.service.model.AsyncTaskConfigModel;
import io.github.timemachinelab.service.model.ImportAssetPlanModel;
import io.github.timemachinelab.service.model.ImportCategoryPlanAction;
import io.github.timemachinelab.service.model.ImportCategoryPlanModel;

import java.net.URI;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

import io.github.timemachinelab.infrastructure.importagent.planner.ImportAgentPlannerJsonSupport.CurrentPlanState;
import io.github.timemachinelab.infrastructure.importagent.planner.ImportAgentPlannerJsonSupport.ParsedPlannerPayload;
import io.github.timemachinelab.infrastructure.importagent.planner.ImportAgentPlannerJsonSupport.PlanDraft;

final class ImportAgentPlanDraftMerger {

    private ImportAgentPlanDraftMerger() {
    }

    static PlanDraft mergeWithCurrentPlan(
            JsonNode sourceNode,
            CurrentPlanState currentPlanState,
            ParsedPlannerPayload parsedPayload) {
        List<ImportCategoryPlanModel> categoryPlans = parsedPayload.hasCategoryPlanPatch()
                ? ImportAgentPlanDraftParser.parseCategoryPlans(sourceNode, currentPlanState.categoryPlans())
                : currentPlanState.categoryPlans();
        List<ImportAssetPlanModel> assetPlans = parsedPayload.hasAssetPlanPatch()
                ? ImportAgentPlanDraftParser.parseAssetPlans(sourceNode, currentPlanState.assetPlans())
                : currentPlanState.assetPlans();
        return new PlanDraft(categoryPlans, assetPlans, parsedPayload.clarificationQuestions(), parsedPayload.summary());
    }

    static PlanDraft normalizeDraft(PlanDraft draft) {
        AsyncAssetPlanNormalizationResult asyncNormalization = normalizeAsyncTaskQueryAssets(draft.assetPlans());
        List<ImportCategoryPlanModel> coveredCategoryPlans = ensureCategoryCoverage(
                draft.categoryPlans(),
                asyncNormalization.assetPlans());
        return new PlanDraft(
                coveredCategoryPlans,
                asyncNormalization.assetPlans(),
                draft.clarificationQuestions(),
                draft.summary());
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
        String upstreamUrl = assetPlan.getUpstreamUrl();
        if (upstreamUrl == null || upstreamUrl.isBlank() || !containsTaskPlaceholder(upstreamUrl)) {
            return false;
        }
        String searchable = String.join(" ",
                safeLower(assetPlan.getApiCode()),
                safeLower(assetPlan.getAssetName()),
                safeLower(upstreamUrl));
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

    private static boolean containsTaskPlaceholder(String value) {
        return value.contains("{taskId}")
                || value.contains("{task_id}")
                || value.contains("{taskID}")
                || value.contains("{task-id}");
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
            if (candidate == null || candidate.getRequestMethod() == RequestMethod.GET) {
                continue;
            }
            int score = scoreSubmitCandidate(candidate, queryAsset);
            if (score > bestScore) {
                bestScore = score;
                bestIndex = index;
            }
        }
        return bestScore >= 4 ? bestIndex : -1;
    }

    private static int scoreSubmitCandidate(ImportAssetPlanModel candidate, ImportAssetPlanModel queryAsset) {
        int score = candidate.getRequestMethod() == RequestMethod.POST ? 2 : 0;
        if (sameText(candidate.getCategoryCode(), queryAsset.getCategoryCode())) {
            score += 3;
        }
        if (shareMeaningfulToken(candidate.getApiCode(), queryAsset.getApiCode())
                || shareMeaningfulToken(candidate.getAssetName(), queryAsset.getAssetName())) {
            score += 2;
        }
        if (sameHost(candidate.getUpstreamUrl(), queryAsset.getUpstreamUrl())) {
            score += 2;
        }
        return score;
    }

    private static ImportAssetPlanModel mergeAsyncTaskConfig(
            ImportAssetPlanModel submitAsset,
            ImportAssetPlanModel queryAsset) {
        AsyncTaskConfigModel current = submitAsset.getAsyncTaskConfig();
        String authMode = currentValue(current == null ? null : current.getAuthMode(), inferAsyncAuthMode(submitAsset, queryAsset));
        String authScheme = currentValue(current == null ? null : current.getAuthScheme(), inferAsyncAuthScheme(queryAsset, authMode));
        String authConfig = currentValue(current == null ? null : current.getAuthConfig(), queryAsset.getAuthConfig());
        AsyncTaskConfigModel asyncTaskConfig = new AsyncTaskConfigModel(
                true,
                currentValue(current == null ? null : current.getQueryMethod(), queryAsset.getRequestMethod().name()),
                currentValue(
                        current == null ? null : current.getQueryUrlTemplate(),
                        ImportAgentPlannerJsonSupport.normalizeAsyncTaskQueryUrlTemplate(queryAsset.getUpstreamUrl())),
                authMode,
                authScheme,
                authConfig,
                current == null ? null : current.getStatusPath(),
                current == null ? null : current.getResultPath(),
                current == null ? null : current.getErrorPath());
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
                ImportAgentPlannerJsonSupport.normalizeAsyncTaskConfig(asyncTaskConfig),
                submitAsset.getAiProfile());
    }

    private static String inferAsyncAuthMode(ImportAssetPlanModel submitAsset, ImportAssetPlanModel queryAsset) {
        if (queryAsset.getAuthScheme() == null
                || queryAsset.getAuthScheme() == AuthScheme.NONE
                || queryAsset.getAuthScheme() == submitAsset.getAuthScheme()) {
            return "SAME_AS_SUBMIT";
        }
        return "OVERRIDE";
    }

    private static String inferAsyncAuthScheme(ImportAssetPlanModel queryAsset, String authMode) {
        if (!"OVERRIDE".equalsIgnoreCase(authMode) || queryAsset.getAuthScheme() == null) {
            return null;
        }
        return queryAsset.getAuthScheme().name();
    }

    private static String currentValue(String currentValue, String inferredValue) {
        return currentValue == null || currentValue.isBlank() ? inferredValue : currentValue;
    }

    private static boolean sameText(String left, String right) {
        return left != null && right != null && left.trim().equalsIgnoreCase(right.trim());
    }

    private static boolean sameHost(String left, String right) {
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

    private static String normalizedWords(String value) {
        return safeLower(value).replace('-', ' ').replace('_', ' ');
    }

    private static String safeLower(String value) {
        return value == null ? "" : value.toLowerCase(java.util.Locale.ROOT);
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
                        ImportCategoryPlanAction.CREATE_IF_MISSING));
            }
        }
        return List.copyOf(merged.values());
    }

    private record AsyncAssetPlanNormalizationResult(
            List<ImportAssetPlanModel> assetPlans,
            List<String> foldedCategoryCodes) {
    }
}
