package io.github.timemachinelab.api.resp;

import java.util.List;

/**
 * Import agent run response.
 */
public class ApiImportAgentRunResp {

    private final String runId;
    private final String sessionId;
    private final int planVersion;
    private final String status;
    private final String summary;
    private final String failureReason;
    private final List<String> affectedApiCodes;
    private final List<ImportStepResultResp> stepResults;
    private final String createdAt;
    private final String updatedAt;

    public ApiImportAgentRunResp(
            String runId,
            String sessionId,
            int planVersion,
            String status,
            String summary,
            String failureReason,
            List<String> affectedApiCodes,
            List<ImportStepResultResp> stepResults,
            String createdAt,
            String updatedAt) {
        this.runId = runId;
        this.sessionId = sessionId;
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

    public int getPlanVersion() {
        return planVersion;
    }

    public String getStatus() {
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

    public List<ImportStepResultResp> getStepResults() {
        return stepResults;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }
}