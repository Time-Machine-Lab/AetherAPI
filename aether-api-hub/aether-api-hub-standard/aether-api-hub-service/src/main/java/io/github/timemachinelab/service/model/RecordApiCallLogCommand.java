package io.github.timemachinelab.service.model;

import java.time.Instant;
import java.util.Locale;
import java.util.Objects;

/**
 * Command for recording one platform call log fact.
 */
public class RecordApiCallLogCommand {

    private final String consumerId;
    private final String consumerCode;
    private final String consumerName;
    private final String consumerType;
    private final String credentialId;
    private final String credentialCode;
    private final String credentialStatus;
    private final String accessChannel;
    private final String targetApiId;
    private final String targetApiCode;
    private final String targetApiName;
    private final String targetApiType;
    private final String requestMethod;
    private final Instant invocationTime;
    private final long durationMs;
    private final String resultType;
    private final boolean success;
    private final Integer httpStatusCode;
    private final String errorCode;
    private final String errorType;
    private final String errorSummary;
    private final String aiProvider;
    private final String aiModel;
    private final Boolean aiStreaming;
    private final String aiUsageSnapshot;
    private final String aiBillingReserved;

    public RecordApiCallLogCommand(
            String consumerId,
            String consumerCode,
            String consumerName,
            String consumerType,
            String credentialId,
            String credentialCode,
            String credentialStatus,
            String accessChannel,
            String targetApiId,
            String targetApiCode,
            String targetApiName,
            String targetApiType,
            String requestMethod,
            Instant invocationTime,
            long durationMs,
            String resultType,
            boolean success,
            Integer httpStatusCode,
            String errorCode,
            String errorType,
            String errorSummary,
            String aiProvider,
            String aiModel,
            Boolean aiStreaming,
            String aiUsageSnapshot,
            String aiBillingReserved) {
        this.consumerId = normalize(consumerId);
        this.consumerCode = normalize(consumerCode);
        this.consumerName = normalize(consumerName);
        this.consumerType = normalize(consumerType);
        this.credentialId = normalize(credentialId);
        this.credentialCode = normalize(credentialCode);
        this.credentialStatus = normalize(credentialStatus);
        this.accessChannel = normalizeRequired(accessChannel, "access channel").toUpperCase(Locale.ROOT);
        this.targetApiId = normalize(targetApiId);
        this.targetApiCode = normalize(targetApiCode);
        this.targetApiName = normalize(targetApiName);
        this.targetApiType = normalize(targetApiType);
        this.requestMethod = normalizeRequired(requestMethod, "request method").toUpperCase(Locale.ROOT);
        this.invocationTime = Objects.requireNonNull(invocationTime, "Invocation time must not be null");
        if (durationMs < 0) {
            throw new IllegalArgumentException("Duration must not be negative");
        }
        this.durationMs = durationMs;
        this.resultType = normalizeRequired(resultType, "result type").toUpperCase(Locale.ROOT);
        this.success = success;
        this.httpStatusCode = httpStatusCode;
        this.errorCode = normalize(errorCode);
        this.errorType = normalize(errorType);
        this.errorSummary = normalize(errorSummary);
        this.aiProvider = normalize(aiProvider);
        this.aiModel = normalize(aiModel);
        this.aiStreaming = aiStreaming;
        this.aiUsageSnapshot = normalize(aiUsageSnapshot);
        this.aiBillingReserved = normalize(aiBillingReserved);
    }

    public String getConsumerId() {
        return consumerId;
    }

    public String getConsumerCode() {
        return consumerCode;
    }

    public String getConsumerName() {
        return consumerName;
    }

    public String getConsumerType() {
        return consumerType;
    }

    public String getCredentialId() {
        return credentialId;
    }

    public String getCredentialCode() {
        return credentialCode;
    }

    public String getCredentialStatus() {
        return credentialStatus;
    }

    public String getAccessChannel() {
        return accessChannel;
    }

    public String getTargetApiId() {
        return targetApiId;
    }

    public String getTargetApiCode() {
        return targetApiCode;
    }

    public String getTargetApiName() {
        return targetApiName;
    }

    public String getTargetApiType() {
        return targetApiType;
    }

    public String getRequestMethod() {
        return requestMethod;
    }

    public Instant getInvocationTime() {
        return invocationTime;
    }

    public long getDurationMs() {
        return durationMs;
    }

    public String getResultType() {
        return resultType;
    }

    public boolean isSuccess() {
        return success;
    }

    public Integer getHttpStatusCode() {
        return httpStatusCode;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public String getErrorType() {
        return errorType;
    }

    public String getErrorSummary() {
        return errorSummary;
    }

    public String getAiProvider() {
        return aiProvider;
    }

    public String getAiModel() {
        return aiModel;
    }

    public Boolean getAiStreaming() {
        return aiStreaming;
    }

    public String getAiUsageSnapshot() {
        return aiUsageSnapshot;
    }

    public String getAiBillingReserved() {
        return aiBillingReserved;
    }

    private static String normalize(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private static String normalizeRequired(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
        return value.trim();
    }
}
