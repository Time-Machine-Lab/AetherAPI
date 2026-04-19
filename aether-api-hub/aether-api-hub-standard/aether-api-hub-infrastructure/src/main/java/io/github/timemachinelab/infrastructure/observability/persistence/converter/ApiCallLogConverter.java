package io.github.timemachinelab.infrastructure.observability.persistence.converter;

import io.github.timemachinelab.domain.observability.model.AiInvocationExtension;
import io.github.timemachinelab.domain.observability.model.ApiCallLogAggregate;
import io.github.timemachinelab.domain.observability.model.ApiCallLogId;
import io.github.timemachinelab.domain.observability.model.ConsumerSnapshot;
import io.github.timemachinelab.domain.observability.model.ErrorSnapshot;
import io.github.timemachinelab.domain.observability.model.InvocationResult;
import io.github.timemachinelab.domain.observability.model.TargetApiSnapshot;
import io.github.timemachinelab.infrastructure.observability.persistence.entity.ApiCallLogDo;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

/**
 * Platform call log converter.
 */
public final class ApiCallLogConverter {

    private ApiCallLogConverter() {
    }

    public static ApiCallLogAggregate toAggregate(ApiCallLogDo source) {
        if (source == null) {
            return null;
        }
        return ApiCallLogAggregate.reconstitute(
                ApiCallLogId.of(source.getId()),
                ConsumerSnapshot.of(
                        source.getConsumerId(),
                        source.getConsumerCode(),
                        source.getConsumerName(),
                        source.getConsumerType(),
                        source.getCredentialId(),
                        source.getCredentialCode(),
                        source.getCredentialStatus()
                ),
                TargetApiSnapshot.of(
                        source.getTargetApiId(),
                        source.getTargetApiCode(),
                        source.getTargetApiName(),
                        source.getTargetApiType()
                ),
                source.getAccessChannel(),
                source.getRequestMethod(),
                toInstant(source.getInvocationTime()),
                source.getDurationMs() == null ? 0L : source.getDurationMs(),
                Boolean.TRUE.equals(source.getSuccess())
                        ? InvocationResult.success(source.getResultType(), source.getHttpStatusCode())
                        : InvocationResult.failure(source.getResultType(), source.getHttpStatusCode()),
                ErrorSnapshot.of(source.getErrorCode(), source.getErrorType(), source.getErrorSummary()),
                AiInvocationExtension.of(
                        source.getAiProvider(),
                        source.getAiModel(),
                        source.getAiStreaming(),
                        source.getAiUsageSnapshot(),
                        source.getAiBillingReserved()
                ),
                toInstant(source.getCreatedAt()),
                toInstant(source.getUpdatedAt()),
                Boolean.TRUE.equals(source.getIsDeleted()),
                source.getVersion() == null ? 0L : source.getVersion()
        );
    }

    public static ApiCallLogDo toDo(ApiCallLogAggregate source) {
        ApiCallLogDo target = new ApiCallLogDo();
        updateDo(target, source);
        return target;
    }

    public static void updateDo(ApiCallLogDo target, ApiCallLogAggregate source) {
        target.setId(source.getId().getValue());
        target.setConsumerId(source.getConsumerSnapshot().getConsumerId());
        target.setConsumerCode(source.getConsumerSnapshot().getConsumerCode());
        target.setConsumerName(source.getConsumerSnapshot().getConsumerName());
        target.setConsumerType(source.getConsumerSnapshot().getConsumerType());
        target.setCredentialId(source.getConsumerSnapshot().getCredentialId());
        target.setCredentialCode(source.getConsumerSnapshot().getCredentialCode());
        target.setCredentialStatus(source.getConsumerSnapshot().getCredentialStatus());
        target.setAccessChannel(source.getAccessChannel());
        target.setTargetApiId(source.getTargetApiSnapshot().getTargetApiId());
        target.setTargetApiCode(source.getTargetApiSnapshot().getTargetApiCode());
        target.setTargetApiName(source.getTargetApiSnapshot().getTargetApiName());
        target.setTargetApiType(source.getTargetApiSnapshot().getTargetApiType());
        target.setRequestMethod(source.getRequestMethod());
        target.setInvocationTime(toLocalDateTime(source.getInvocationTime()));
        target.setDurationMs(source.getDurationMs());
        target.setResultType(source.getInvocationResult().getResultType());
        target.setSuccess(source.getInvocationResult().isSuccess());
        target.setHttpStatusCode(source.getInvocationResult().getHttpStatusCode());
        target.setErrorCode(source.getErrorSnapshot() == null ? null : source.getErrorSnapshot().getErrorCode());
        target.setErrorType(source.getErrorSnapshot() == null ? null : source.getErrorSnapshot().getErrorType());
        target.setErrorSummary(source.getErrorSnapshot() == null ? null : source.getErrorSnapshot().getErrorSummary());
        target.setAiProvider(source.getAiInvocationExtension() == null ? null : source.getAiInvocationExtension().getProvider());
        target.setAiModel(source.getAiInvocationExtension() == null ? null : source.getAiInvocationExtension().getModel());
        target.setAiStreaming(source.getAiInvocationExtension() == null ? null : source.getAiInvocationExtension().getStreaming());
        target.setAiUsageSnapshot(
                source.getAiInvocationExtension() == null ? null : source.getAiInvocationExtension().getUsageSnapshot());
        target.setAiBillingReserved(
                source.getAiInvocationExtension() == null ? null : source.getAiInvocationExtension().getBillingReserved());
        target.setCreatedAt(toLocalDateTime(source.getCreatedAt()));
        target.setUpdatedAt(toLocalDateTime(source.getUpdatedAt()));
        target.setIsDeleted(source.isDeleted());
        target.setVersion(source.getVersion());
    }

    private static Instant toInstant(LocalDateTime value) {
        return value == null ? null : value.toInstant(ZoneOffset.UTC);
    }

    private static LocalDateTime toLocalDateTime(Instant value) {
        return value == null ? null : LocalDateTime.ofInstant(value, ZoneOffset.UTC);
    }
}
