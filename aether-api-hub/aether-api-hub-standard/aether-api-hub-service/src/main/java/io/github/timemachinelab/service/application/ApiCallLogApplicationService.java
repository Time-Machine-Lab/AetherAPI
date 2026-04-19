package io.github.timemachinelab.service.application;

import io.github.timemachinelab.domain.catalog.model.ApiCode;
import io.github.timemachinelab.domain.consumerauth.model.UserConsumerMapping;
import io.github.timemachinelab.domain.observability.model.ApiCallLogId;
import io.github.timemachinelab.domain.observability.model.ObservabilityDomainException;
import io.github.timemachinelab.service.model.ApiCallLogDetailModel;
import io.github.timemachinelab.service.model.ApiCallLogPageResult;
import io.github.timemachinelab.service.model.GetApiCallLogDetailQuery;
import io.github.timemachinelab.service.model.ListApiCallLogQuery;
import io.github.timemachinelab.service.port.in.ApiCallLogUseCase;
import io.github.timemachinelab.service.port.out.ApiCallLogQueryPort;
import io.github.timemachinelab.service.port.out.UserConsumerMappingRepositoryPort;

import java.time.Instant;
import java.util.List;

/**
 * API call log query application service.
 */
public class ApiCallLogApplicationService implements ApiCallLogUseCase {

    private final ApiCallLogQueryPort apiCallLogQueryPort;
    private final UserConsumerMappingRepositoryPort userConsumerMappingRepositoryPort;

    public ApiCallLogApplicationService(
            ApiCallLogQueryPort apiCallLogQueryPort,
            UserConsumerMappingRepositoryPort userConsumerMappingRepositoryPort) {
        this.apiCallLogQueryPort = apiCallLogQueryPort;
        this.userConsumerMappingRepositoryPort = userConsumerMappingRepositoryPort;
    }

    @Override
    public ApiCallLogPageResult listApiCallLogs(ListApiCallLogQuery query) {
        String currentUserId = normalizeCurrentUserId(query.getCurrentUserId());
        int page = Math.max(1, query.getPage());
        int size = Math.max(1, Math.min(query.getSize(), 100));
        Instant invocationStartAt = query.getInvocationStartAt();
        Instant invocationEndAt = query.getInvocationEndAt();
        validateTimeRange(invocationStartAt, invocationEndAt);

        UserConsumerMapping mapping = userConsumerMappingRepositoryPort.findActiveByUserId(currentUserId).orElse(null);
        if (mapping == null) {
            return new ApiCallLogPageResult(List.of(), page, size, 0L);
        }

        String targetApiCode = normalizeTargetApiCode(query.getTargetApiCode());
        return new ApiCallLogPageResult(
                apiCallLogQueryPort.findPageByConsumerId(
                        mapping.getConsumerId().getValue(),
                        targetApiCode,
                        invocationStartAt,
                        invocationEndAt,
                        page,
                        size
                ),
                page,
                size,
                apiCallLogQueryPort.countByConsumerId(
                        mapping.getConsumerId().getValue(),
                        targetApiCode,
                        invocationStartAt,
                        invocationEndAt
                )
        );
    }

    @Override
    public ApiCallLogDetailModel getApiCallLogDetail(GetApiCallLogDetailQuery query) {
        String currentUserId = normalizeCurrentUserId(query.getCurrentUserId());
        String logId = normalizeLogId(query.getLogId());
        UserConsumerMapping mapping = userConsumerMappingRepositoryPort.findActiveByUserId(currentUserId)
                .orElseThrow(() -> new ObservabilityDomainException("API call log not found for current user"));
        return apiCallLogQueryPort.findDetailByIdAndConsumerId(logId, mapping.getConsumerId().getValue())
                .orElseThrow(() -> new ObservabilityDomainException("API call log not found for current user"));
    }

    private String normalizeCurrentUserId(String currentUserId) {
        if (currentUserId == null || currentUserId.isBlank()) {
            throw new IllegalArgumentException("Current user id must not be blank");
        }
        return currentUserId.trim();
    }

    private String normalizeTargetApiCode(String targetApiCode) {
        if (targetApiCode == null || targetApiCode.isBlank()) {
            return null;
        }
        return ApiCode.of(targetApiCode).getValue();
    }

    private String normalizeLogId(String logId) {
        return ApiCallLogId.of(logId).getValue();
    }

    private void validateTimeRange(Instant invocationStartAt, Instant invocationEndAt) {
        if (invocationStartAt != null && invocationEndAt != null && invocationStartAt.isAfter(invocationEndAt)) {
            throw new IllegalArgumentException("Invocation time range startAt must not be after endAt");
        }
    }
}
