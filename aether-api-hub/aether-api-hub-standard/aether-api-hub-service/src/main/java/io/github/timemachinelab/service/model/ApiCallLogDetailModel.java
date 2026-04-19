package io.github.timemachinelab.service.model;

/**
 * API call log detail model.
 */
public class ApiCallLogDetailModel extends ApiCallLogModel {

    private final String accessChannel;
    private final String credentialCode;
    private final String credentialStatus;
    private final ApiCallLogErrorModel error;
    private final ApiCallLogAiExtensionModel aiExtension;
    private final String createdAt;
    private final String updatedAt;

    public ApiCallLogDetailModel(
            String logId,
            String targetApiCode,
            String targetApiName,
            String requestMethod,
            String invocationTime,
            Long durationMs,
            String resultType,
            boolean success,
            Integer httpStatusCode,
            String accessChannel,
            String credentialCode,
            String credentialStatus,
            ApiCallLogErrorModel error,
            ApiCallLogAiExtensionModel aiExtension,
            String createdAt,
            String updatedAt) {
        super(logId, targetApiCode, targetApiName, requestMethod, invocationTime, durationMs, resultType, success, httpStatusCode);
        this.accessChannel = accessChannel;
        this.credentialCode = credentialCode;
        this.credentialStatus = credentialStatus;
        this.error = error;
        this.aiExtension = aiExtension;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public String getAccessChannel() {
        return accessChannel;
    }

    public String getCredentialCode() {
        return credentialCode;
    }

    public String getCredentialStatus() {
        return credentialStatus;
    }

    public ApiCallLogErrorModel getError() {
        return error;
    }

    public ApiCallLogAiExtensionModel getAiExtension() {
        return aiExtension;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }
}
