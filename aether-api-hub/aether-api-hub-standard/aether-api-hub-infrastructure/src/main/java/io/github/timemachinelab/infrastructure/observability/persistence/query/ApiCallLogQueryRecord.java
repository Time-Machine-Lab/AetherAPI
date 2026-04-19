package io.github.timemachinelab.infrastructure.observability.persistence.query;

import java.time.LocalDateTime;

/**
 * API call log query record.
 */
public class ApiCallLogQueryRecord {

    private String logId;
    private String targetApiCode;
    private String targetApiName;
    private String requestMethod;
    private String accessChannel;
    private LocalDateTime invocationTime;
    private Long durationMs;
    private String resultType;
    private Boolean success;
    private Integer httpStatusCode;
    private String credentialCode;
    private String credentialStatus;
    private String errorCode;
    private String errorType;
    private String errorSummary;
    private String aiProvider;
    private String aiModel;
    private Boolean aiStreaming;
    private String aiUsageSnapshot;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public String getLogId() {
        return logId;
    }

    public void setLogId(String logId) {
        this.logId = logId;
    }

    public String getTargetApiCode() {
        return targetApiCode;
    }

    public void setTargetApiCode(String targetApiCode) {
        this.targetApiCode = targetApiCode;
    }

    public String getTargetApiName() {
        return targetApiName;
    }

    public void setTargetApiName(String targetApiName) {
        this.targetApiName = targetApiName;
    }

    public String getRequestMethod() {
        return requestMethod;
    }

    public void setRequestMethod(String requestMethod) {
        this.requestMethod = requestMethod;
    }

    public String getAccessChannel() {
        return accessChannel;
    }

    public void setAccessChannel(String accessChannel) {
        this.accessChannel = accessChannel;
    }

    public LocalDateTime getInvocationTime() {
        return invocationTime;
    }

    public void setInvocationTime(LocalDateTime invocationTime) {
        this.invocationTime = invocationTime;
    }

    public Long getDurationMs() {
        return durationMs;
    }

    public void setDurationMs(Long durationMs) {
        this.durationMs = durationMs;
    }

    public String getResultType() {
        return resultType;
    }

    public void setResultType(String resultType) {
        this.resultType = resultType;
    }

    public Boolean getSuccess() {
        return success;
    }

    public void setSuccess(Boolean success) {
        this.success = success;
    }

    public Integer getHttpStatusCode() {
        return httpStatusCode;
    }

    public void setHttpStatusCode(Integer httpStatusCode) {
        this.httpStatusCode = httpStatusCode;
    }

    public String getCredentialCode() {
        return credentialCode;
    }

    public void setCredentialCode(String credentialCode) {
        this.credentialCode = credentialCode;
    }

    public String getCredentialStatus() {
        return credentialStatus;
    }

    public void setCredentialStatus(String credentialStatus) {
        this.credentialStatus = credentialStatus;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public String getErrorType() {
        return errorType;
    }

    public void setErrorType(String errorType) {
        this.errorType = errorType;
    }

    public String getErrorSummary() {
        return errorSummary;
    }

    public void setErrorSummary(String errorSummary) {
        this.errorSummary = errorSummary;
    }

    public String getAiProvider() {
        return aiProvider;
    }

    public void setAiProvider(String aiProvider) {
        this.aiProvider = aiProvider;
    }

    public String getAiModel() {
        return aiModel;
    }

    public void setAiModel(String aiModel) {
        this.aiModel = aiModel;
    }

    public Boolean getAiStreaming() {
        return aiStreaming;
    }

    public void setAiStreaming(Boolean aiStreaming) {
        this.aiStreaming = aiStreaming;
    }

    public String getAiUsageSnapshot() {
        return aiUsageSnapshot;
    }

    public void setAiUsageSnapshot(String aiUsageSnapshot) {
        this.aiUsageSnapshot = aiUsageSnapshot;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
