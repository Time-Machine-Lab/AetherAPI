package io.github.timemachinelab.api.resp;

import java.util.List;

/**
 * Import agent session response.
 */
public class ApiImportAgentSessionResp {

    private final String sessionId;
    private final String status;
    private final String documentSource;
    private final String documentSummary;
    private final String importIntent;
    private final String publisherDisplayName;
    private final Integer currentPlanVersion;
    private final Integer confirmedPlanVersion;
    private final String latestRunId;
    private final ImportAgentPlanResp currentPlan;
    private final List<ImportAgentTurnResp> turns;
    private final String createdAt;
    private final String updatedAt;

    public ApiImportAgentSessionResp(
            String sessionId,
            String status,
            String documentSource,
            String documentSummary,
            String importIntent,
            String publisherDisplayName,
            Integer currentPlanVersion,
            Integer confirmedPlanVersion,
            String latestRunId,
            ImportAgentPlanResp currentPlan,
            List<ImportAgentTurnResp> turns,
            String createdAt,
            String updatedAt) {
        this.sessionId = sessionId;
        this.status = status;
        this.documentSource = documentSource;
        this.documentSummary = documentSummary;
        this.importIntent = importIntent;
        this.publisherDisplayName = publisherDisplayName;
        this.currentPlanVersion = currentPlanVersion;
        this.confirmedPlanVersion = confirmedPlanVersion;
        this.latestRunId = latestRunId;
        this.currentPlan = currentPlan;
        this.turns = turns == null ? List.of() : List.copyOf(turns);
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public String getSessionId() {
        return sessionId;
    }

    public String getStatus() {
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

    public ImportAgentPlanResp getCurrentPlan() {
        return currentPlan;
    }

    public List<ImportAgentTurnResp> getTurns() {
        return turns;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }
}