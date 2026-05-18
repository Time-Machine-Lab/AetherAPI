package io.github.timemachinelab.service.model;

import java.util.List;

/**
 * Import agent run model.
 */
public class ApiImportAgentRunModel {

    private final String runId;
    private final String sessionId;
    private final String ownerUserId;
    private final int planVersion;
    private final ImportAgentRunStatus status;
    private final String summary;
    private final String failureReason;
    private final List<String> affectedApiCodes;
    private final List<ImportStepResultModel> stepResults;
    private final String createdAt;
    private final String updatedAt;

    public ApiImportAgentRunModel(
            String runId,
            String sessionId,
            String ownerUserId,
            int planVersion,
            ImportAgentRunStatus status,
            String summary,
            String failureReason,
            List<String> affectedApiCodes,
            List<ImportStepResultModel> stepResults,
            String createdAt,
            String updatedAt) {
        this.runId = runId;
        this.sessionId = sessionId;
        this.ownerUserId = ownerUserId;
        this.planVersion = planVersion;
        this.status = status;
        this.summary = summary;
        this.failureReason = failureReason;
        this.affectedApiCodes = affectedApiCodes == null ? List.of() : List.copyOf(affectedApiCodes);
        this.stepResults = stepResults == null ? List.of() : List.copyOf(stepResults);
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public String getRunId() {
        return runId;
    }

    public String getSessionId() {
        return sessionId;
    }

    public String getOwnerUserId() {
        return ownerUserId;
    }

    public int getPlanVersion() {
        return planVersion;
    }

    public ImportAgentRunStatus getStatus() {
        return status;
    }

    public String getSummary() {
        return summary;
    }

    public String getFailureReason() {
        return failureReason;
    }

    public List<String> getAffectedApiCodes() {
        return affectedApiCodes;
    }

    public List<ImportStepResultModel> getStepResults() {
        return stepResults;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }
}