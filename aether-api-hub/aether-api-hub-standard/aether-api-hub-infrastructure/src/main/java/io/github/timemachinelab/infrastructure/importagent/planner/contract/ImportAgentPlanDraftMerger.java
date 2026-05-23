package io.github.timemachinelab.infrastructure.importagent.planner.contract;

import com.fasterxml.jackson.databind.JsonNode;
import io.github.timemachinelab.infrastructure.importagent.planner.contract.ImportAgentPlannerJsonSupport.ParsedPlannerPayload;
import io.github.timemachinelab.infrastructure.importagent.planner.contract.ImportAgentPlannerJsonSupport.PlanDraft;
import io.github.timemachinelab.service.model.ImportAssetPlanModel;
import io.github.timemachinelab.service.model.ImportCategoryPlanModel;

import java.util.List;

final class ImportAgentPlanDraftMerger {

    private ImportAgentPlanDraftMerger() {
    }

    static PlanDraft fromPayload(
            JsonNode sourceNode,
            ParsedPlannerPayload parsedPayload) {
        List<ImportCategoryPlanModel> categoryPlans = parsedPayload.hasCategoryPlanPatch()
                ? ImportAgentPlanDraftParser.parseCategoryPlans(sourceNode, List.of())
                : List.of();
        List<ImportAssetPlanModel> assetPlans = parsedPayload.hasAssetPlanPatch()
                ? ImportAgentPlanDraftParser.parseAssetPlans(sourceNode, List.of())
                : List.of();
        return new PlanDraft(categoryPlans, assetPlans, parsedPayload.clarificationQuestions(), parsedPayload.summary());
    }

    static PlanDraft normalizeDraft(PlanDraft draft) {
        return new PlanDraft(
                draft.categoryPlans() == null ? List.of() : List.copyOf(draft.categoryPlans()),
                draft.assetPlans() == null ? List.of() : List.copyOf(draft.assetPlans()),
                draft.clarificationQuestions() == null ? List.of() : List.copyOf(draft.clarificationQuestions()),
                draft.summary());
    }
}
