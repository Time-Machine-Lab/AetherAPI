package io.github.timemachinelab.service.port.out;

import io.github.timemachinelab.service.model.ApiCallLogDetailModel;
import io.github.timemachinelab.service.model.ApiCallLogModel;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * API call log query port.
 */
public interface ApiCallLogQueryPort {

    List<ApiCallLogModel> findPageByConsumerId(
            String consumerId,
            String targetApiCode,
            Instant invocationStartAt,
            Instant invocationEndAt,
            int page,
            int size);

    long countByConsumerId(
            String consumerId,
            String targetApiCode,
            Instant invocationStartAt,
            Instant invocationEndAt);

    Optional<ApiCallLogDetailModel> findDetailByIdAndConsumerId(String logId, String consumerId);
}
