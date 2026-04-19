package io.github.timemachinelab.service.port.out;

import io.github.timemachinelab.domain.observability.model.ApiCallLogAggregate;

/**
 * Outbound repository port for platform call logs.
 */
public interface ApiCallLogRepositoryPort {

    void save(ApiCallLogAggregate aggregate);
}
