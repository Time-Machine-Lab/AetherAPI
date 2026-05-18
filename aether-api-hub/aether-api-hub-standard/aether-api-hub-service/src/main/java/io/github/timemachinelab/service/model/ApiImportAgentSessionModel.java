package io.github.timemachinelab.service.model;

import java.util.List;

/**
 * Import agent session model.
 */
public class ApiImportAgentSessionModel {

    private final String sessionId;
    private final String ownerUserId;
    private final ImportAgentSessionStatus status;
    private final String documentSource;
    private final String documentSummary;
    private final String importIntent;
    private final String publisherDisplayName;
    private final Integer currentPlanVersion;
    private final Integer confirmedPlanVersion;
    private final String latestRunId;
    private final String latestConfirmedAt;
    private final ImportAgentPlanModel currentPlan;
    private final List<ImportAgentTurnModel> turns;
    private final String createdAt;
    private final String updatedAt;

    public ApiImportAgentSessionModel(
            String sessionId,
            String ownerUserId,
            ImportAgentSessionStatus status,
            String documentSource,
            String documentSummary,
            String importIntent,
            String publisherDisplayName,
            Integer currentPlanVersion,
            Integer confirmedPlanVersion,
            String latestRunId,
            String latestConfirmedAt,
            ImportAgentPlanModel currentPlan,
            List<ImportAgentTurnModel> turns,
            String createdAt,
            String updatedAt) {
        this.sessionId = sessionId;
        this.ownerUserId = ownerUserId;
        this.status = status;
        this.documentSource = documentSource;
        this.documentSummary = documentSummary;
        this.importIntent = importIntent;
        this.publisherDisplayName = publisherDisplayName;
        this.currentPlanVersion = currentPlanVersion;
        this.confirmedPlanVersion = confirmedPlanVersion;
        this.latestRunId = latestRunId;
        this.latestConfirmedAt = latestConfirmedAt;
        this.currentPlan = currentPlan;
        this.turns = turns == null ? List.of() : List.copyOf(turns);
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public String getSessionId() {
        return sessionId;
    }

    public String getOwnerUserId() {
        return ownerUserId;
    }

    public ImportAgentSessionStatus getStatus() {
        return status;
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

    public String getPublisherDisplayName() {
        return publisherDisplayName;
    }

    public Integer getCurrentPlanVersion() {
        return currentPlanVersion;
    }

    public Integer getConfirmedPlanVersion() {
        return confirmedPlanVersion;
    }

    public String getLatestRunId() {
        return latestRunId;
    }

    public String getLatestConfirmedAt() {
        return latestConfirmedAt;
    }

    public ImportAgentPlanModel getCurrentPlan() {
        return currentPlan;
    }

    public List<ImportAgentTurnModel> getTurns() {
        return turns;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }
}