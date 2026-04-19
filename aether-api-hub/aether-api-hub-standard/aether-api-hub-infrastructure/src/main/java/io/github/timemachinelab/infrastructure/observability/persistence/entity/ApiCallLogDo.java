package io.github.timemachinelab.infrastructure.observability.persistence.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;

import java.time.LocalDateTime;

/**
 * Platform call log persistence object.
 */
@TableName("api_call_log")
public class ApiCallLogDo {

    @TableId(type = IdType.ASSIGN_UUID)
    private String id;
    private String consumerId;
    private String consumerCode;
    private String consumerName;
    private String consumerType;
    private String credentialId;
    private String credentialCode;
    private String credentialStatus;
    private String accessChannel;
    private String targetApiId;
    private String targetApiCode;
    private String targetApiName;
    private String targetApiType;
    private String requestMethod;
    private LocalDateTime invocationTime;
    private Long durationMs;
    private String resultType;
    private Boolean success;
    private Integer httpStatusCode;
    private String errorCode;
    private String errorType;
    private String errorSummary;
    private String aiProvider;
    private String aiModel;
    private Boolean aiStreaming;
    private String aiUsageSnapshot;
    private String aiBillingReserved;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @TableLogic
    private Boolean isDeleted;

    @Version
    private Long version;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getConsumerId() {
        return consumerId;
    }

    public void setConsumerId(String consumerId) {
        this.consumerId = consumerId;
    }

    public String getConsumerCode() {
        return consumerCode;
    }

    public void setConsumerCode(String consumerCode) {
        this.consumerCode = consumerCode;
    }

    public String getConsumerName() {
        return consumerName;
    }

    public void setConsumerName(String consumerName) {
        this.consumerName = consumerName;
    }

    public String getConsumerType() {
        return consumerType;
    }

    public void setConsumerType(String consumerType) {
        this.consumerType = consumerType;
    }

    public String getCredentialId() {
        return credentialId;
    }

    public void setCredentialId(String credentialId) {
        this.credentialId = credentialId;
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

    public String getAccessChannel() {
        return accessChannel;
    }

    public void setAccessChannel(String accessChannel) {
        this.accessChannel = accessChannel;
    }

    public String getTargetApiId() {
        return targetApiId;
    }

    public void setTargetApiId(String targetApiId) {
        this.targetApiId = targetApiId;
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

    public String getTargetApiType() {
        return targetApiType;
    }

    public void setTargetApiType(String targetApiType) {
        this.targetApiType = targetApiType;
    }

    public String getRequestMethod() {
        return requestMethod;
    }

    public void setRequestMethod(String requestMethod) {
        this.requestMethod = requestMethod;
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

    public String getAiBillingReserved() {
        return aiBillingReserved;
    }

    public void setAiBillingReserved(String aiBillingReserved) {
        this.aiBillingReserved = aiBillingReserved;
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

    public Boolean getIsDeleted() {
        return isDeleted;
    }

    public void setIsDeleted(Boolean isDeleted) {
        this.isDeleted = isDeleted;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }
}
