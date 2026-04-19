package io.github.timemachinelab.service.adapter;

import io.github.timemachinelab.domain.observability.model.ApiCallLogAggregate;
import io.github.timemachinelab.domain.observability.repository.ApiCallLogRepository;
import io.github.timemachinelab.service.port.out.ApiCallLogRepositoryPort;

/**
 * Platform call log repository adapter.
 */
public class ApiCallLogRepositoryAdapter implements ApiCallLogRepositoryPort {

    private final ApiCallLogRepository delegate;

    public ApiCallLogRepositoryAdapter(ApiCallLogRepository delegate) {
        this.delegate = delegate;
    }

    @Override
    public void save(ApiCallLogAggregate aggregate) {
        delegate.save(aggregate);
    }
}
