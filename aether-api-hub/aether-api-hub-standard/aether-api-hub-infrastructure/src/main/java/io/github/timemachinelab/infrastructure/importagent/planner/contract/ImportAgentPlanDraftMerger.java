package io.github.timemachinelab.infrastructure.importagent.planner.contract;

import com.fasterxml.jackson.databind.JsonNode;
import io.github.timemachinelab.infrastructure.importagent.planner.contract.ImportAgentPlannerJsonSupport.ParsedPlannerPayload;
import io.github.timemachinelab.infrastructure.importagent.planner.contract.ImportAgentPlannerJsonSupport.PlanDraft;
import io.github.timemachinelab.infrastructure.importagent.planner.contract.ImportAgentPlannerJsonSupport.CurrentPlanState;
import io.github.timemachinelab.service.model.ImportAssetPlanModel;
import io.github.timemachinelab.service.model.ImportCategoryPlanModel;

import java.util.List;

final class ImportAgentPlanDraftMerger {

    private ImportAgentPlanDraftMerger() {
    }

    static PlanDraft fromPayload(
            JsonNode sourceNode,
            ParsedPlannerPayload parsedPayload,
            CurrentPlanState currentPlanState) {
        List<ImportCategoryPlanModel> categoryPlans = parsedPayload.hasCategoryPlanPatch()
                ? ImportAgentPlanDraftParser.parseCategoryPlans(sourceNode, currentPlanState.categoryPlans())
                : currentPlanState.categoryPlans();
        List<ImportAssetPlanModel> assetPlans = parsedPayload.hasAssetPlanPatch()
                ? ImportAgentPlanDraftParser.parseAssetPlans(sourceNode, currentPlanState.assetPlans())
                : currentPlanState.assetPlans();
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
