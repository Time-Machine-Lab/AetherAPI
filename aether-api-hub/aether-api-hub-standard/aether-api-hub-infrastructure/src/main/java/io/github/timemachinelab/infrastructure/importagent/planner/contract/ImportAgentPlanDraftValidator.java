package io.github.timemachinelab.infrastructure.importagent.planner.contract;

import io.github.timemachinelab.domain.catalog.model.AssetType;
import io.github.timemachinelab.domain.catalog.model.AuthScheme;
import io.github.timemachinelab.domain.catalog.model.RequestMethod;
import io.github.timemachinelab.domain.catalog.model.UpstreamRequestHeader;
import io.github.timemachinelab.infrastructure.importagent.planner.contract.ImportAgentPlannerJsonSupport.PlanDraft;
import io.github.timemachinelab.infrastructure.importagent.planner.contract.ImportAgentPlannerJsonSupport.PlanValidationResult;
import io.github.timemachinelab.service.model.AsyncTaskConfigModel;
import io.github.timemachinelab.service.model.ImportAgentClarificationItemModel;
import io.github.timemachinelab.service.model.ImportAgentClarificationOptionModel;
import io.github.timemachinelab.service.model.ImportAssetPlanModel;
import io.github.timemachinelab.service.model.ImportCategoryPlanAction;
import io.github.timemachinelab.service.model.ImportCategoryPlanModel;
import io.github.timemachinelab.service.model.UpstreamRequestHeaderModel;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

final class ImportAgentPlanDraftValidator {

    private ImportAgentPlanDraftValidator() {
    }

    static PlanValidationResult validateDraft(int nextPlanVersion, PlanDraft draft) {
        LinkedHashSet<String> questions = new LinkedHashSet<>(draft.clarificationQuestions());
        List<ImportAgentClarificationItemModel> items = new ArrayList<>();
        validateCategories(nextPlanVersion, questions, items, draft.categoryPlans());
        validateAssets(nextPlanVersion, questions, items, draft.assetPlans());
        String summary = hasText(draft.summary())
                ? draft.summary()
                : "导入计划草稿 v" + nextPlanVersion + " 包含 "
                + draft.categoryPlans().size() + " 个分类计划和 "
                + draft.assetPlans().size() + " 个资产计划。";
        return new PlanValidationResult(
                questions.isEmpty(),
                summary,
                List.copyOf(questions),
                List.copyOf(items));
    }

    private static void validateCategories(
            int nextPlanVersion,
            LinkedHashSet<String> questions,
            List<ImportAgentClarificationItemModel> items,
            List<ImportCategoryPlanModel> categoryPlans) {
        for (int index = 0; index < categoryPlans.size(); index += 1) {
            ImportCategoryPlanModel categoryPlan = categoryPlans.get(index);
            if (!hasText(categoryPlan.getCategoryCode())) {
                addClarification(nextPlanVersion, questions, items, "/categoryPlans/" + index + "/categoryCode",
                        "categoryCode", "分类编码", "请提供此分类计划的分类编码。",
                        "TEXT", List.of(), categoryPlan.getCategoryCode());
            }
            if (categoryPlan.getAction() == null) {
                addClarification(nextPlanVersion, questions, items, "/categoryPlans/" + index + "/action",
                        "action", "分类处理方式", "请选择此分类的处理方式。",
                        "SELECT", enumOptions(ImportCategoryPlanAction.class), null);
            }
        }
    }

    private static void validateAssets(
            int nextPlanVersion,
            LinkedHashSet<String> questions,
            List<ImportAgentClarificationItemModel> items,
            List<ImportAssetPlanModel> assetPlans) {
        if (assetPlans.isEmpty()) {
            questions.add("请至少提供一个要导入的 API 资产。");
        }
        for (int index = 0; index < assetPlans.size(); index += 1) {
            ImportAssetPlanModel assetPlan = assetPlans.get(index);
            validateAsset(nextPlanVersion, questions, items, index, assetPlan);
        }
    }

    private static void validateAsset(
            int nextPlanVersion,
            LinkedHashSet<String> questions,
            List<ImportAgentClarificationItemModel> items,
            int assetIndex,
            ImportAssetPlanModel assetPlan) {
        addAssetRequired(nextPlanVersion, questions, items, assetIndex, "apiCode", "API 编码",
                "请提供唯一 API 编码。", "TEXT", List.of(), assetPlan.getApiCode(), !hasText(assetPlan.getApiCode()));
        addAssetRequired(nextPlanVersion, questions, items, assetIndex, "assetName", "资产名称",
                "请提供此资产的展示名称。", "TEXT", List.of(), assetPlan.getAssetName(), !hasText(assetPlan.getAssetName()));
        addAssetRequired(nextPlanVersion, questions, items, assetIndex, "assetType", "资产类型",
                "请选择 STANDARD_API 或 AI_API。", "SELECT", enumOptions(AssetType.class), null, assetPlan.getAssetType() == null);

        if (assetPlan.getAssetType() == AssetType.AI_API
                && (assetPlan.getAiProfile() == null
                || !hasText(assetPlan.getAiProfile().getProvider())
                || !hasText(assetPlan.getAiProfile().getModel()))) {
            addAssetRequired(nextPlanVersion, questions, items, assetIndex, "aiProfile", "AI 配置",
                    "请为此 AI API 资产提供 AI 服务商和模型。", "TEXT", List.of(), null, true);
        }

        validateUpstreamRequestHeaders(nextPlanVersion, questions, items, assetIndex, assetPlan.getUpstreamRequestHeaders());

        if (!assetPlan.isPublishAfterImport()) {
            return;
        }
        addAssetRequired(nextPlanVersion, questions, items, assetIndex, "categoryCode", "分类编码",
                "请提供发布此资产时使用的分类编码。", "TEXT", List.of(),
                assetPlan.getCategoryCode(), !hasText(assetPlan.getCategoryCode()));
        addAssetRequired(nextPlanVersion, questions, items, assetIndex, "requestMethod", "请求方法",
                "请选择上游 HTTP 请求方法。", "SELECT", enumOptions(RequestMethod.class), null,
                assetPlan.getRequestMethod() == null);
        addAssetRequired(nextPlanVersion, questions, items, assetIndex, "upstreamUrl", "上游 URL",
                "请提供上游 API URL。", "TEXT", List.of(), assetPlan.getUpstreamUrl(),
                !hasText(assetPlan.getUpstreamUrl()));
        addAssetRequired(nextPlanVersion, questions, items, assetIndex, "authScheme", "认证方案",
                "请选择上游认证方案。", "SELECT", enumOptions(AuthScheme.class), null,
                assetPlan.getAuthScheme() == null);
        if (assetPlan.getAuthScheme() != null
                && assetPlan.getAuthScheme() != AuthScheme.NONE
                && !hasText(assetPlan.getAuthConfig())) {
            addAssetRequired(nextPlanVersion, questions, items, assetIndex, "authConfig", "认证配置",
                    "请提供上游认证配置。", "TEXT", List.of(), assetPlan.getAuthConfig(), true);
        }
        validateAsyncTaskConfig(nextPlanVersion, questions, items, assetIndex, assetPlan.getAsyncTaskConfig());
    }

    private static void validateUpstreamRequestHeaders(
            int nextPlanVersion,
            LinkedHashSet<String> questions,
            List<ImportAgentClarificationItemModel> items,
            int assetIndex,
            List<UpstreamRequestHeaderModel> headers) {
        if (headers == null || headers.isEmpty()) {
            return;
        }
        for (int headerIndex = 0; headerIndex < headers.size(); headerIndex += 1) {
            UpstreamRequestHeaderModel header = headers.get(headerIndex);
            if (header == null) {
                continue;
            }
            if (!hasText(header.getName()) || UpstreamRequestHeader.isProtectedName(header.getName())) {
                addHeaderClarification(nextPlanVersion, questions, items, assetIndex, headerIndex, "name", "上游请求头名称",
                        "请提供非保留的固定上游请求头名称。", "TEXT", header.getName());
            }
            if (!hasText(header.getValue())) {
                addHeaderClarification(nextPlanVersion, questions, items, assetIndex, headerIndex, "value", "上游请求头值",
                        "请提供该固定上游请求头的值。", "TEXT", header.getValue());
            }
        }
    }

    private static void validateAsyncTaskConfig(
            int nextPlanVersion,
            LinkedHashSet<String> questions,
            List<ImportAgentClarificationItemModel> items,
            int assetIndex,
            AsyncTaskConfigModel config) {
        if (config == null || !Boolean.TRUE.equals(config.getEnabled())) {
            return;
        }
        if (!hasText(config.getAuthMode()) || !isSupportedAsyncAuthMode(config.getAuthMode())) {
            addAsyncClarification(nextPlanVersion, questions, items, assetIndex, "authMode", "异步认证模式",
                    "请选择 SAME_AS_SUBMIT 或 OVERRIDE。", "SELECT", options("SAME_AS_SUBMIT", "OVERRIDE"), config.getAuthMode());
        }
        if (!hasText(config.getQueryMethod())) {
            addAsyncClarification(nextPlanVersion, questions, items, assetIndex, "queryMethod", "异步查询方法",
                    "请选择异步查询 HTTP 方法。", "SELECT", options("GET", "POST"), config.getQueryMethod());
        }
        if (!hasText(config.getQueryUrlTemplate()) || !config.getQueryUrlTemplate().contains("{taskId}")) {
            addAsyncClarification(nextPlanVersion, questions, items, assetIndex, "queryUrlTemplate", "异步查询 URL 模板",
                    "请提供包含 {taskId} 的查询 URL 模板。", "TEXT", List.of(), config.getQueryUrlTemplate());
        }
        if (!"OVERRIDE".equalsIgnoreCase(config.getAuthMode())) {
            return;
        }
        if (!hasText(config.getAuthScheme()) || ImportAgentPlannerJsonSupport.resolveAuthScheme(config.getAuthScheme()) == null) {
            addAsyncClarification(nextPlanVersion, questions, items, assetIndex, "authScheme", "异步认证方案",
                    "请选择异步查询认证方案。", "SELECT", enumOptions(AuthScheme.class), config.getAuthScheme());
        }
        AuthScheme scheme = ImportAgentPlannerJsonSupport.resolveAuthScheme(config.getAuthScheme());
        if (scheme != null && scheme != AuthScheme.NONE && !hasText(config.getAuthConfig())) {
            addAsyncClarification(nextPlanVersion, questions, items, assetIndex, "authConfig", "异步查询认证配置",
                    "请提供异步查询认证配置。", "TEXT", List.of(), config.getAuthConfig());
        }
    }

    private static void addAssetRequired(
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
            boolean required) {
        if (!required) {
            return;
        }
        addClarification(nextPlanVersion, questions, items, "/assetPlans/" + assetIndex + "/" + fieldKey,
                fieldKey, label, description, inputType, options, currentValue);
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
        addClarification(nextPlanVersion, questions, items, "/assetPlans/" + assetIndex + "/asyncTaskConfig/" + fieldKey,
                fieldKey, label, description, inputType, options, currentValue);
    }

    private static void addHeaderClarification(
            int nextPlanVersion,
            LinkedHashSet<String> questions,
            List<ImportAgentClarificationItemModel> items,
            int assetIndex,
            int headerIndex,
            String fieldKey,
            String label,
            String description,
            String inputType,
            String currentValue) {
        addClarification(nextPlanVersion, questions, items,
                "/assetPlans/" + assetIndex + "/upstreamRequestHeaders/" + headerIndex + "/" + fieldKey,
                fieldKey, label, description, inputType, List.of(), currentValue);
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
                null,
                null,
                null,
                null));
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

    private static boolean isSupportedAsyncAuthMode(String authMode) {
        String normalized = ImportAgentPlannerJsonSupport.normalizeEnumText(authMode);
        return "SAME_AS_SUBMIT".equals(normalized) || "OVERRIDE".equals(normalized);
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
