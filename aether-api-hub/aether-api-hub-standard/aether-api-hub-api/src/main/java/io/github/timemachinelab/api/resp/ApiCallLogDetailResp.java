package io.github.timemachinelab.api.resp;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * API call log detail response.
 */
public class ApiCallLogDetailResp extends ApiCallLogResp {

    @JsonProperty("accessChannel")
    private String accessChannel;

    @JsonProperty("credentialCode")
    private String credentialCode;

    @JsonProperty("credentialStatus")
    private String credentialStatus;

    @JsonProperty("error")
    private ApiCallLogErrorResp error;

    @JsonProperty("aiExtension")
    private ApiCallLogAiExtensionResp aiExtension;

    @JsonProperty("createdAt")
    private String createdAt;

    @JsonProperty("updatedAt")
    private String updatedAt;

    public ApiCallLogDetailResp() {
    }

    public ApiCallLogDetailResp(
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
            ApiCallLogErrorResp error,
            ApiCallLogAiExtensionResp aiExtension,
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

    public void setAccessChannel(String accessChannel) {
        this.accessChannel = accessChannel;
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

    public ApiCallLogErrorResp getError() {
        return error;
    }

    public void setError(ApiCallLogErrorResp error) {
        this.error = error;
    }

    public ApiCallLogAiExtensionResp getAiExtension() {
        return aiExtension;
    }

    public void setAiExtension(ApiCallLogAiExtensionResp aiExtension) {
        this.aiExtension = aiExtension;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }
}
