package io.github.timemachinelab.infrastructure.importagent.planner;

import io.github.timemachinelab.domain.catalog.model.AssetType;
import io.github.timemachinelab.domain.catalog.model.AuthScheme;
import io.github.timemachinelab.domain.catalog.model.RequestMethod;
import io.github.timemachinelab.service.model.ImportAgentClarificationItemModel;
import io.github.timemachinelab.service.model.ImportAgentClarificationOptionModel;
import io.github.timemachinelab.service.model.ImportAssetPlanModel;
import io.github.timemachinelab.service.model.ImportCategoryPlanModel;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;

import io.github.timemachinelab.infrastructure.importagent.planner.ImportAgentPlannerJsonSupport.PlanDraft;
import io.github.timemachinelab.infrastructure.importagent.planner.ImportAgentPlannerJsonSupport.PlanValidationResult;

final class ImportAgentPlanDraftValidator {

    private ImportAgentPlanDraftValidator() {
    }

    static PlanValidationResult validateDraft(int nextPlanVersion, PlanDraft draft) {
        ValidationResult validation = validatePlan(nextPlanVersion, draft.categoryPlans(), draft.assetPlans());
        LinkedHashSet<String> mergedQuestions = new LinkedHashSet<>(draft.clarificationQuestions());
        mergedQuestions.addAll(validation.questions());
        List<String> clarificationQuestions = List.copyOf(mergedQuestions);
        boolean executable = clarificationQuestions.isEmpty();
        String summary = draft.summary() == null
                ? buildDefaultSummary(nextPlanVersion, draft.categoryPlans(), draft.assetPlans())
                : draft.summary();
        return new PlanValidationResult(
                executable,
                summary,
                clarificationQuestions,
                validation.items());
    }

    private static String buildDefaultSummary(
            int nextPlanVersion,
            List<ImportCategoryPlanModel> categoryPlans,
            List<ImportAssetPlanModel> assetPlans) {
        return "导入计划草稿 v" + nextPlanVersion
                + " 已准备 " + categoryPlans.size()
                + " 个分类计划和 " + assetPlans.size() + " 个资产计划。";
    }

    private static ValidationResult validatePlan(
            int nextPlanVersion,
            List<ImportCategoryPlanModel> categoryPlans,
            List<ImportAssetPlanModel> assetPlans) {
        LinkedHashSet<String> questions = new LinkedHashSet<>();
        List<ImportAgentClarificationItemModel> items = new ArrayList<>();
        if (assetPlans.isEmpty()) {
            questions.add("请至少提供一个需要导入的 API 资产。");
        }
        for (int categoryIndex = 0; categoryIndex < categoryPlans.size(); categoryIndex += 1) {
            ImportCategoryPlanModel categoryPlan = categoryPlans.get(categoryIndex);
            if (isBlank(categoryPlan.getCategoryCode())) {
                addClarification(
                        nextPlanVersion,
                        questions,
                        items,
                        "/categoryPlans/" + categoryIndex + "/categoryCode",
                        "categoryCode",
                        "分类编码",
                        "请填写这些 API 资产应归属的分类编码。",
                        "TEXT",
                        List.of(),
                        categoryPlan.getCategoryCode());
            }
        }
        for (int assetIndex = 0; assetIndex < assetPlans.size(); assetIndex += 1) {
            validateAssetPlan(nextPlanVersion, questions, items, assetIndex, assetPlans.get(assetIndex));
        }
        return new ValidationResult(List.copyOf(questions), List.copyOf(items));
    }

    private static void validateAssetPlan(
            int nextPlanVersion,
            LinkedHashSet<String> questions,
            List<ImportAgentClarificationItemModel> items,
            int assetIndex,
            ImportAssetPlanModel assetPlan) {
        if (isBlank(assetPlan.getApiCode())) {
            addAssetClarification(nextPlanVersion, questions, items, assetIndex, "apiCode", "API 编码",
                    "请确认该资产唯一的 API 编码。AI API 默认建议使用模型服务商和模型名生成，例如 dashscope-happyhorse-1-0-t2v。",
                    "TEXT", List.of(), assetPlan.getApiCode(), apiCodeDefaultSuggestion(assetPlan));
        }
        if (isBlank(assetPlan.getAssetName())) {
            addAssetClarification(nextPlanVersion, questions, items, assetIndex, "assetName", "资产名称",
                    "请填写该 API 资产在控制台中展示的名称。", "TEXT", List.of(), assetPlan.getAssetName());
        }
        if (assetPlan.getAssetType() == null) {
            addAssetClarification(nextPlanVersion, questions, items, assetIndex, "assetType", "资产类型",
                    "请选择这是普通 API 还是 AI API。", "SELECT", enumOptions(AssetType.class), null,
                    defaultSuggestion("STANDARD_API", "STANDARD_API", "AGENT_HEURISTIC", "MEDIUM"));
        }
        if (!isBlank(assetPlan.getAuthConfig()) && assetPlan.getAuthScheme() == null) {
            addAssetClarification(nextPlanVersion, questions, items, assetIndex, "authScheme", "鉴权方式",
                    "请选择 " + displayApiCode(assetPlan) + " 调用上游时使用的鉴权方式。",
                    "SELECT", enumOptions(AuthScheme.class), null,
                    defaultSuggestion(inferAuthScheme(assetPlan.getAuthConfig()), inferAuthScheme(assetPlan.getAuthConfig()),
                            "DOCUMENT", "MEDIUM"));
        }
        if (assetPlan.getAuthScheme() != null
                && assetPlan.getAuthScheme() != AuthScheme.NONE
                && isBlank(assetPlan.getAuthConfig())) {
            addAssetClarification(nextPlanVersion, questions, items, assetIndex, "authConfig", "上游鉴权信息",
                    "请提供 " + displayApiCode(assetPlan)
                            + " 的鉴权相关信息，例如文档中的鉴权说明、Header/Query 名称、环境变量名或凭证来源；Agent 会据此生成配置。",
                    "TEXT", List.of(), assetPlan.getAuthConfig());
        }
        if (!assetPlan.isPublishAfterImport()) {
            return;
        }
        validatePublishFields(nextPlanVersion, questions, items, assetIndex, assetPlan);
        validateAsyncTaskConfig(nextPlanVersion, questions, items, assetIndex, assetPlan);
    }

    private static void validatePublishFields(
            int nextPlanVersion,
            LinkedHashSet<String> questions,
            List<ImportAgentClarificationItemModel> items,
            int assetIndex,
            ImportAssetPlanModel assetPlan) {
        if (isBlank(assetPlan.getCategoryCode())) {
            addAssetClarification(nextPlanVersion, questions, items, assetIndex, "categoryCode", "分类编码",
                    "请填写该资产发布时使用的分类编码。", "TEXT", List.of(), assetPlan.getCategoryCode());
        }
        if (assetPlan.getRequestMethod() == null) {
            addAssetClarification(nextPlanVersion, questions, items, assetIndex, "requestMethod", "请求方法",
                    "请选择调用上游接口时使用的 HTTP 方法。", "SELECT", enumOptions(RequestMethod.class), null,
                    defaultSuggestion(inferRequestMethod(assetPlan), inferRequestMethod(assetPlan), "INFERRED_FROM_URL", "MEDIUM"));
        }
        if (isBlank(assetPlan.getUpstreamUrl())) {
            addAssetClarification(nextPlanVersion, questions, items, assetIndex, "upstreamUrl", "上游地址",
                    "请填写该资产的上游请求 URL。", "TEXT", List.of(), assetPlan.getUpstreamUrl());
        }
        if (assetPlan.getAuthScheme() == null) {
            addAssetClarification(nextPlanVersion, questions, items, assetIndex, "authScheme", "鉴权方式",
                    "请选择 " + displayApiCode(assetPlan) + " 调用上游时使用的鉴权方式。",
                    "SELECT", enumOptions(AuthScheme.class), null,
                    defaultSuggestion("NONE", "NONE", "AGENT_HEURISTIC", "LOW"));
        }
        if (assetPlan.getAssetType() == AssetType.AI_API
                && (assetPlan.getAiProfile() == null
                || isBlank(assetPlan.getAiProfile().getProvider())
                || isBlank(assetPlan.getAiProfile().getModel()))) {
            questions.add("请为 AI API 资产 " + displayApiCode(assetPlan) + " 补充 AI 能力信息，包括服务商和模型名称。");
        }
    }

    private static void validateAsyncTaskConfig(
            int nextPlanVersion,
            LinkedHashSet<String> questions,
            List<ImportAgentClarificationItemModel> items,
            int assetIndex,
            ImportAssetPlanModel assetPlan) {
        if (assetPlan.getAsyncTaskConfig() == null
                || !Boolean.TRUE.equals(assetPlan.getAsyncTaskConfig().getEnabled())) {
            return;
        }
        if (isBlank(assetPlan.getAsyncTaskConfig().getAuthMode())) {
            addAsyncClarification(nextPlanVersion, questions, items, assetIndex, "authMode", "任务查询鉴权模式",
                    "请选择任务查询接口如何处理上游鉴权。", "SELECT",
                    options("SAME_AS_SUBMIT", "OVERRIDE"), assetPlan.getAsyncTaskConfig().getAuthMode(),
                    defaultSuggestion("SAME_AS_SUBMIT", "SAME_AS_SUBMIT", "AGENT_HEURISTIC", "MEDIUM"));
        } else if (!isSupportedAsyncAuthMode(assetPlan.getAsyncTaskConfig().getAuthMode())) {
            addAsyncClarification(nextPlanVersion, questions, items, assetIndex, "authMode", "任务查询鉴权模式",
                    "任务查询鉴权模式必须是 SAME_AS_SUBMIT 或 OVERRIDE。", "SELECT",
                    options("SAME_AS_SUBMIT", "OVERRIDE"), assetPlan.getAsyncTaskConfig().getAuthMode(),
                    defaultSuggestion("SAME_AS_SUBMIT", "SAME_AS_SUBMIT", "AGENT_HEURISTIC", "LOW"));
        }
        if (isBlank(assetPlan.getAsyncTaskConfig().getQueryMethod())) {
            addAsyncClarification(nextPlanVersion, questions, items, assetIndex, "queryMethod", "任务查询方法",
                    "请选择查询任务状态时使用的 HTTP 方法。", "SELECT",
                    options("GET", "POST"), assetPlan.getAsyncTaskConfig().getQueryMethod(),
                    defaultSuggestion("GET", "GET", "AGENT_HEURISTIC", "MEDIUM"));
        }
        if (isBlank(assetPlan.getAsyncTaskConfig().getQueryUrlTemplate())
                || !assetPlan.getAsyncTaskConfig().getQueryUrlTemplate().contains("{taskId}")) {
            addAsyncClarification(nextPlanVersion, questions, items, assetIndex, "queryUrlTemplate", "任务查询 URL 模板",
                    "请填写包含 {taskId} 占位符的任务查询 URL 模板。", "TEXT",
                    List.of(), assetPlan.getAsyncTaskConfig().getQueryUrlTemplate());
        }
        if (!"OVERRIDE".equalsIgnoreCase(assetPlan.getAsyncTaskConfig().getAuthMode())) {
            return;
        }
        if (isBlank(assetPlan.getAsyncTaskConfig().getAuthScheme())) {
            addAsyncClarification(nextPlanVersion, questions, items, assetIndex, "authScheme", "任务查询鉴权方式",
                    "请选择任务查询请求使用的鉴权方式。", "SELECT",
                    enumOptions(AuthScheme.class), assetPlan.getAsyncTaskConfig().getAuthScheme());
        } else if (ImportAgentPlannerJsonSupport.resolveAuthScheme(assetPlan.getAsyncTaskConfig().getAuthScheme()) == null) {
            addAsyncClarification(nextPlanVersion, questions, items, assetIndex, "authScheme", "任务查询鉴权方式",
                    "任务查询鉴权方式必须是系统支持的取值。", "SELECT",
                    enumOptions(AuthScheme.class), assetPlan.getAsyncTaskConfig().getAuthScheme());
        }
        if (isBlank(assetPlan.getAsyncTaskConfig().getAuthConfig())) {
            addAsyncClarification(nextPlanVersion, questions, items, assetIndex, "authConfig", "任务查询鉴权信息",
                    "请提供任务查询请求的鉴权相关信息，例如鉴权说明、Header/Query 名称、环境变量名或凭证来源；Agent 会据此生成配置。",
                    "TEXT", List.of(), assetPlan.getAsyncTaskConfig().getAuthConfig());
        }
    }

    private static void addAssetClarification(
            int nextPlanVersion,
            LinkedHashSet<String> questions,
            List<ImportAgentClarificationItemModel> items,
            int assetIndex,
            String fieldKey,
            String label,
            String description,
            String inputType,
            List<ImportAgentClarificationOptionModel> options,
            String currentValue) {
        addAssetClarification(nextPlanVersion, questions, items, assetIndex, fieldKey, label, description,
                inputType, options, currentValue, null);
    }

    private static void addAssetClarification(
            int nextPlanVersion,
            LinkedHashSet<String> questions,
            List<ImportAgentClarificationItemModel> items,
            int assetIndex,
            String fieldKey,
            String label,
            String description,
            String inputType,
            List<ImportAgentClarificationOptionModel> options,
            String currentValue,
            ClarificationDefaultSuggestion defaultSuggestion) {
        addClarification(nextPlanVersion, questions, items, "/assetPlans/" + assetIndex + "/" + fieldKey,
                fieldKey, label, description, inputType, options, currentValue, defaultSuggestion);
    }

    private static void addAsyncClarification(
            int nextPlanVersion,
            LinkedHashSet<String> questions,
            List<ImportAgentClarificationItemModel> items,
            int assetIndex,
            String fieldKey,
            String label,
            String description,
            String inputType,
            List<ImportAgentClarificationOptionModel> options,
            String currentValue) {
        addAsyncClarification(nextPlanVersion, questions, items, assetIndex, fieldKey, label, description,
                inputType, options, currentValue, null);
    }

    private static void addAsyncClarification(
            int nextPlanVersion,
            LinkedHashSet<String> questions,
            List<ImportAgentClarificationItemModel> items,
            int assetIndex,
            String fieldKey,
            String label,
            String description,
            String inputType,
            List<ImportAgentClarificationOptionModel> options,
            String currentValue,
            ClarificationDefaultSuggestion defaultSuggestion) {
        addClarification(nextPlanVersion, questions, items, "/assetPlans/" + assetIndex + "/asyncTaskConfig/" + fieldKey,
                fieldKey, label, description, inputType, options, currentValue, defaultSuggestion);
    }

    private static void addClarification(
            int nextPlanVersion,
            LinkedHashSet<String> questions,
            List<ImportAgentClarificationItemModel> items,
            String targetPath,
            String fieldKey,
            String label,
            String description,
            String inputType,
            List<ImportAgentClarificationOptionModel> options,
            String currentValue) {
        addClarification(nextPlanVersion, questions, items, targetPath, fieldKey, label, description,
                inputType, options, currentValue, null);
    }

    private static void addClarification(
            int nextPlanVersion,
            LinkedHashSet<String> questions,
            List<ImportAgentClarificationItemModel> items,
            String targetPath,
            String fieldKey,
            String label,
            String description,
            String inputType,
            List<ImportAgentClarificationOptionModel> options,
            String currentValue,
            ClarificationDefaultSuggestion defaultSuggestion) {
        questions.add(label + ": " + description);
        items.add(new ImportAgentClarificationItemModel(
                "plan-" + nextPlanVersion + ":" + targetPath + ":" + fieldKey,
                targetPath,
                fieldKey,
                label,
                description,
                inputType,
                true,
                options,
                currentValue,
                defaultSuggestion == null ? null : defaultSuggestion.value(),
                defaultSuggestion == null ? null : defaultSuggestion.label(),
                defaultSuggestion == null ? null : defaultSuggestion.source(),
                defaultSuggestion == null ? null : defaultSuggestion.confidence()));
    }

    private static List<ImportAgentClarificationOptionModel> enumOptions(Class<? extends Enum<?>> type) {
        List<ImportAgentClarificationOptionModel> values = new ArrayList<>();
        for (Enum<?> constant : type.getEnumConstants()) {
            values.add(new ImportAgentClarificationOptionModel(constant.name(), constant.name()));
        }
        return List.copyOf(values);
    }

    private static List<ImportAgentClarificationOptionModel> options(String... values) {
        List<ImportAgentClarificationOptionModel> options = new ArrayList<>();
        for (String value : values) {
            options.add(new ImportAgentClarificationOptionModel(value, value));
        }
        return List.copyOf(options);
    }

    private static String displayApiCode(ImportAssetPlanModel assetPlan) {
        if (assetPlan == null || isBlank(assetPlan.getApiCode())) {
            return "<asset>";
        }
        return assetPlan.getApiCode();
    }

    private static boolean isSupportedAsyncAuthMode(String authMode) {
        String normalized = ImportAgentPlannerJsonSupport.normalizeEnumText(authMode);
        return "SAME_AS_SUBMIT".equals(normalized) || "OVERRIDE".equals(normalized);
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private static ClarificationDefaultSuggestion defaultSuggestion(
            String value,
            String label,
            String source,
            String confidence) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return new ClarificationDefaultSuggestion(value, label, source, confidence);
    }

    private static String inferRequestMethod(ImportAssetPlanModel assetPlan) {
        String searchable = String.join(" ",
                lower(assetPlan.getApiCode()),
                lower(assetPlan.getAssetName()),
                lower(assetPlan.getUpstreamUrl()));
        if (searchable.contains("query")
                || searchable.contains("status")
                || searchable.contains("detail")
                || searchable.contains("result")
                || searchable.contains("list")
                || searchable.contains("get")) {
            return "GET";
        }
        return "POST";
    }

    private static String lower(String value) {
        return value == null ? "" : value.toLowerCase(java.util.Locale.ROOT);
    }

    private static String inferAuthScheme(String authConfig) {
        if (authConfig == null || authConfig.isBlank()) {
            return null;
        }
        return authConfig.contains(":") ? "HEADER_TOKEN" : "QUERY_TOKEN";
    }

    private static ClarificationDefaultSuggestion apiCodeDefaultSuggestion(ImportAssetPlanModel assetPlan) {
        if (assetPlan == null
                || assetPlan.getAiProfile() == null
                || isBlank(assetPlan.getAiProfile().getProvider())
                || isBlank(assetPlan.getAiProfile().getModel())) {
            return null;
        }
        String apiCode = toApiCode(assetPlan.getAiProfile().getProvider() + "-" + assetPlan.getAiProfile().getModel());
        if (isBlank(apiCode)) {
            return null;
        }
        return defaultSuggestion(apiCode, apiCode, "CURRENT_PLAN", "HIGH");
    }

    private static String toApiCode(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        String normalized = value.trim().toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9_-]+", "-")
                .replaceAll("[-_]{2,}", "-")
                .replaceAll("^[^a-z0-9]+|[^a-z0-9]+$", "");
        if (normalized.isBlank()) {
            return null;
        }
        if (normalized.length() <= 64) {
            return normalized;
        }
        String truncated = normalized.substring(0, 64)
                .replaceAll("[^a-z0-9]+$", "");
        return truncated.isBlank() ? null : truncated;
    }

    private record ValidationResult(
            List<String> questions,
            List<ImportAgentClarificationItemModel> items) {
    }

    private record ClarificationDefaultSuggestion(
            String value,
            String label,
            String source,
            String confidence) {
    }
}
