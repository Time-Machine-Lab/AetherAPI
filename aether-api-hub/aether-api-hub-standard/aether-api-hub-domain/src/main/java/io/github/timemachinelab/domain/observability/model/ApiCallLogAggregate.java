package io.github.timemachinelab.domain.observability.model;

import java.time.Instant;
import java.util.Locale;
import java.util.Objects;

/**
 * Phase-one platform call log aggregate.
 */
public class ApiCallLogAggregate {

    private final ApiCallLogId id;
    private final ConsumerSnapshot consumerSnapshot;
    private final TargetApiSnapshot targetApiSnapshot;
    private final String accessChannel;
    private final String requestMethod;
    private final Instant invocationTime;
    private final long durationMs;
    private InvocationResult invocationResult;
    private ErrorSnapshot errorSnapshot;
    private AiInvocationExtension aiInvocationExtension;
    private final Instant createdAt;
    private Instant updatedAt;
    private final boolean deleted;
    private long version;

    private ApiCallLogAggregate(
            ApiCallLogId id,
            ConsumerSnapshot consumerSnapshot,
            TargetApiSnapshot targetApiSnapshot,
            String accessChannel,
            String requestMethod,
            Instant invocationTime,
            long durationMs,
            InvocationResult invocationResult,
            ErrorSnapshot errorSnapshot,
            AiInvocationExtension aiInvocationExtension,
            Instant createdAt,
            Instant updatedAt,
            boolean deleted,
            long version) {
        this.id = Objects.requireNonNull(id, "ApiCallLogId must not be null");
        this.consumerSnapshot = Objects.requireNonNull(consumerSnapshot, "ConsumerSnapshot must not be null");
        this.targetApiSnapshot = Objects.requireNonNull(targetApiSnapshot, "TargetApiSnapshot must not be null");
        this.accessChannel = normalizeRequired(accessChannel, 64, "access channel");
        this.requestMethod = normalizeRequired(requestMethod, 16, "request method").toUpperCase(Locale.ROOT);
        this.invocationTime = Objects.requireNonNull(invocationTime, "Invocation time must not be null");
        if (durationMs < 0) {
            throw new IllegalArgumentException("Duration must not be negative");
        }
        this.durationMs = durationMs;
        this.invocationResult = invocationResult;
        this.errorSnapshot = errorSnapshot;
        this.aiInvocationExtension = aiInvocationExtension;
        this.createdAt = createdAt == null ? Instant.now() : createdAt;
        this.updatedAt = updatedAt == null ? this.createdAt : updatedAt;
        this.deleted = deleted;
        this.version = version;
    }

    public static ApiCallLogAggregate record(
            ApiCallLogId id,
            ConsumerSnapshot consumerSnapshot,
            TargetApiSnapshot targetApiSnapshot,
            String accessChannel,
            String requestMethod,
            Instant invocationTime,
            long durationMs) {
        Instant now = Instant.now();
        return new ApiCallLogAggregate(
                id,
                consumerSnapshot,
                targetApiSnapshot,
                accessChannel,
                requestMethod,
                invocationTime,
                durationMs,
                null,
                null,
                null,
                now,
                now,
                false,
                0L
        );
    }

    public static ApiCallLogAggregate reconstitute(
            ApiCallLogId id,
            ConsumerSnapshot consumerSnapshot,
            TargetApiSnapshot targetApiSnapshot,
            String accessChannel,
            String requestMethod,
            Instant invocationTime,
            long durationMs,
            InvocationResult invocationResult,
            ErrorSnapshot errorSnapshot,
            AiInvocationExtension aiInvocationExtension,
            Instant createdAt,
            Instant updatedAt,
            boolean deleted,
            long version) {
        return new ApiCallLogAggregate(
                id,
                consumerSnapshot,
                targetApiSnapshot,
                accessChannel,
                requestMethod,
                invocationTime,
                durationMs,
                invocationResult,
                errorSnapshot,
                aiInvocationExtension,
                createdAt,
                updatedAt,
                deleted,
                version
        );
    }

    public void markSucceeded(String resultType, Integer httpStatusCode) {
        this.invocationResult = InvocationResult.success(resultType, httpStatusCode);
        this.errorSnapshot = null;
        touch();
    }

    public void markFailed(String resultType, Integer httpStatusCode, ErrorSnapshot errorSnapshot) {
        this.invocationResult = InvocationResult.failure(resultType, httpStatusCode);
        this.errorSnapshot = Objects.requireNonNull(errorSnapshot, "ErrorSnapshot must not be null when invocation fails");
        touch();
    }

    public void attachAiExtension(AiInvocationExtension aiInvocationExtension) {
        this.aiInvocationExtension = aiInvocationExtension;
        touch();
    }

    public ApiCallLogId getId() {
        return id;
    }

    public ConsumerSnapshot getConsumerSnapshot() {
        return consumerSnapshot;
    }

    public TargetApiSnapshot getTargetApiSnapshot() {
        return targetApiSnapshot;
    }

    public String getAccessChannel() {
        return accessChannel;
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

    public InvocationResult getInvocationResult() {
        return invocationResult;
    }

    public ErrorSnapshot getErrorSnapshot() {
        return errorSnapshot;
    }

    public AiInvocationExtension getAiInvocationExtension() {
        return aiInvocationExtension;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public long getVersion() {
        return version;
    }

    private void touch() {
        this.updatedAt = Instant.now();
        this.version++;
    }

    private static String normalizeRequired(String value, int maxLength, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
        String trimmed = value.trim();
        if (trimmed.length() > maxLength) {
            throw new IllegalArgumentException(fieldName + " must not exceed " + maxLength + " characters");
        }
        return trimmed;
    }
}
