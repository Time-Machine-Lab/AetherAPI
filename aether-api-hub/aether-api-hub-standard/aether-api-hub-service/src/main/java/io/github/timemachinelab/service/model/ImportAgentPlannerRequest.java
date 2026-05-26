package io.github.timemachinelab.service.model;

import java.util.List;

/**
 * Import agent planner request.
 */
public class ImportAgentPlannerRequest {

    private final String documentSource;
    private final String documentSummary;
    private final String importIntent;
    private final String latestUserMessage;
    private final ImportAgentPlanModel currentPlan;
    private final int nextPlanVersion;
    private final List<ImportAgentTurnModel> turns;
    private final List<ImportAgentCategoryCandidateModel> availableCategories;
    private final List<ImportExistingAssetSummaryModel> existingAssetCandidates;
    private final List<ImportExistingAssetSummaryModel> targetExistingAssets;

    public ImportAgentPlannerRequest(
            String documentSource,
            String documentSummary,
            String importIntent,
            String latestUserMessage,
            ImportAgentPlanModel currentPlan,
            int nextPlanVersion,
            List<ImportAgentTurnModel> turns) {
        this(documentSource, documentSummary, importIntent, latestUserMessage, currentPlan, nextPlanVersion, turns, List.of());
    }

    public ImportAgentPlannerRequest(
            String documentSource,
            String documentSummary,
            String importIntent,
            String latestUserMessage,
            ImportAgentPlanModel currentPlan,
            int nextPlanVersion,
            List<ImportAgentTurnModel> turns,
            List<ImportAgentCategoryCandidateModel> availableCategories) {
        this(
                documentSource,
                documentSummary,
                importIntent,
                latestUserMessage,
                currentPlan,
                nextPlanVersion,
                turns,
                availableCategories,
                List.of(),
                List.of()
        );
    }

    public ImportAgentPlannerRequest(
            String documentSource,
            String documentSummary,
            String importIntent,
            String latestUserMessage,
            ImportAgentPlanModel currentPlan,
            int nextPlanVersion,
            List<ImportAgentTurnModel> turns,
            List<ImportAgentCategoryCandidateModel> availableCategories,
            List<ImportExistingAssetSummaryModel> existingAssetCandidates,
            List<ImportExistingAssetSummaryModel> targetExistingAssets) {
        this.documentSource = documentSource;
        this.documentSummary = documentSummary;
        this.importIntent = importIntent;
        this.latestUserMessage = latestUserMessage;
        this.currentPlan = currentPlan;
        this.nextPlanVersion = nextPlanVersion;
        this.turns = turns == null ? List.of() : List.copyOf(turns);
        this.availableCategories = availableCategories == null ? List.of() : List.copyOf(availableCategories);
        this.existingAssetCandidates = existingAssetCandidates == null ? List.of() : List.copyOf(existingAssetCandidates);
        this.targetExistingAssets = targetExistingAssets == null ? List.of() : List.copyOf(targetExistingAssets);
    }

    public String getDocumentSource() {
        return documentSource;
    }

    public String getDocumentSummary() {
        return documentSummary;
    }

    public String getImportIntent() {
        return importIntent;
    }

    public String getLatestUserMessage() {
        return latestUserMessage;
    }

    public ImportAgentPlanModel getCurrentPlan() {
        return currentPlan;
    }

    public int getNextPlanVersion() {
        return nextPlanVersion;
    }

    public List<ImportAgentTurnModel> getTurns() {
        return turns;
    }

    public List<ImportAgentCategoryCandidateModel> getAvailableCategories() {
        return availableCategories;
    }

    public List<ImportExistingAssetSummaryModel> getExistingAssetCandidates() {
        return existingAssetCandidates;
    }

    public List<ImportExistingAssetSummaryModel> getTargetExistingAssets() {
        return targetExistingAssets;
    }
}
