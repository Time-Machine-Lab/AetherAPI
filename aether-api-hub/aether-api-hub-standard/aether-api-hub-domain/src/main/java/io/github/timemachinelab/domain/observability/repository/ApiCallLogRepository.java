package io.github.timemachinelab.domain.observability.repository;

import io.github.timemachinelab.domain.observability.model.ApiCallLogAggregate;

/**
 * Domain repository for platform call logs.
 */
public interface ApiCallLogRepository {

    void save(ApiCallLogAggregate aggregate);
}
