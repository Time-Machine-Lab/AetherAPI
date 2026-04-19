package io.github.timemachinelab.infrastructure.observability.persistence.query;

import io.github.timemachinelab.service.model.ApiCallLogAiExtensionModel;
import io.github.timemachinelab.service.model.ApiCallLogDetailModel;
import io.github.timemachinelab.service.model.ApiCallLogErrorModel;
import io.github.timemachinelab.service.model.ApiCallLogModel;
import io.github.timemachinelab.service.port.out.ApiCallLogQueryPort;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

/**
 * MyBatis-backed API call log query port.
 */
@Repository
public class MybatisApiCallLogQueryPort implements ApiCallLogQueryPort {

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ISO_INSTANT;

    private final ApiCallLogQueryMapper mapper;

    public MybatisApiCallLogQueryPort(ApiCallLogQueryMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public List<ApiCallLogModel> findPageByConsumerId(
            String consumerId,
            String targetApiCode,
            Instant invocationStartAt,
            Instant invocationEndAt,
            int page,
            int size) {
        return mapper.selectPageByConsumerId(
                        consumerId,
                        targetApiCode,
                        toLocalDateTime(invocationStartAt),
                        toLocalDateTime(invocationEndAt),
                        size,
                        Math.max(0, (page - 1) * size)
                ).stream()
                .map(this::toSummaryModel)
                .toList();
    }

    @Override
    public long countByConsumerId(
            String consumerId,
            String targetApiCode,
            Instant invocationStartAt,
            Instant invocationEndAt) {
        return mapper.countByConsumerId(
                consumerId,
                targetApiCode,
                toLocalDateTime(invocationStartAt),
                toLocalDateTime(invocationEndAt)
        );
    }

    @Override
    public Optional<ApiCallLogDetailModel> findDetailByIdAndConsumerId(String logId, String consumerId) {
        return Optional.ofNullable(mapper.selectDetailByIdAndConsumerId(logId, consumerId))
                .map(this::toDetailModel);
    }

    private ApiCallLogModel toSummaryModel(ApiCallLogQueryRecord record) {
        return new ApiCallLogModel(
                record.getLogId(),
                record.getTargetApiCode(),
                record.getTargetApiName(),
                record.getRequestMethod(),
                formatInstant(record.getInvocationTime()),
                record.getDurationMs(),
                record.getResultType(),
                Boolean.TRUE.equals(record.getSuccess()),
                record.getHttpStatusCode()
        );
    }

    private ApiCallLogDetailModel toDetailModel(ApiCallLogQueryRecord record) {
        return new ApiCallLogDetailModel(
                record.getLogId(),
                record.getTargetApiCode(),
                record.getTargetApiName(),
                record.getRequestMethod(),
                formatInstant(record.getInvocationTime()),
                record.getDurationMs(),
                record.getResultType(),
                Boolean.TRUE.equals(record.getSuccess()),
                record.getHttpStatusCode(),
                record.getAccessChannel(),
                record.getCredentialCode(),
                record.getCredentialStatus(),
                toErrorModel(record),
                toAiExtensionModel(record),
                formatInstant(record.getCreatedAt()),
                formatInstant(record.getUpdatedAt())
        );
    }

    private ApiCallLogErrorModel toErrorModel(ApiCallLogQueryRecord record) {
        if (isBlank(record.getErrorCode()) && isBlank(record.getErrorType()) && isBlank(record.getErrorSummary())) {
            return null;
        }
        return new ApiCallLogErrorModel(record.getErrorCode(), record.getErrorType(), record.getErrorSummary());
    }

    private ApiCallLogAiExtensionModel toAiExtensionModel(ApiCallLogQueryRecord record) {
        if (isBlank(record.getAiProvider())
                && isBlank(record.getAiModel())
                && record.getAiStreaming() == null
                && isBlank(record.getAiUsageSnapshot())) {
            return null;
        }
        return new ApiCallLogAiExtensionModel(
                record.getAiProvider(),
                record.getAiModel(),
                record.getAiStreaming(),
                record.getAiUsageSnapshot()
        );
    }

    private String formatInstant(LocalDateTime value) {
        return value == null ? null : TIME_FORMATTER.withZone(ZoneOffset.UTC).format(value.toInstant(ZoneOffset.UTC));
    }

    private LocalDateTime toLocalDateTime(Instant value) {
        return value == null ? null : LocalDateTime.ofInstant(value, ZoneOffset.UTC);
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
