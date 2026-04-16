package io.github.timemachinelab.service.adapter;

import io.github.timemachinelab.domain.consumerauth.model.ApiCredentialAggregate;
import io.github.timemachinelab.domain.consumerauth.model.ApiCredentialId;
import io.github.timemachinelab.domain.consumerauth.model.ApiCredentialStatus;
import io.github.timemachinelab.domain.consumerauth.model.ConsumerId;
import io.github.timemachinelab.domain.consumerauth.repository.ApiCredentialRepository;
import io.github.timemachinelab.service.port.out.ApiCredentialRepositoryPort;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * API 凭证仓储适配器。
 */
public class ApiCredentialRepositoryAdapter implements ApiCredentialRepositoryPort {

    private final ApiCredentialRepository delegate;

    public ApiCredentialRepositoryAdapter(ApiCredentialRepository delegate) {
        this.delegate = delegate;
    }

    @Override
    public Optional<ApiCredentialAggregate> findByIdAndConsumerId(ApiCredentialId credentialId, ConsumerId consumerId) {
        return delegate.findByIdAndConsumerId(credentialId, consumerId);
    }

    @Override
    public List<ApiCredentialAggregate> findPageByConsumerId(
            ConsumerId consumerId, ApiCredentialStatus status, boolean expiredOnly, int page, int size, Instant now) {
        return delegate.findPageByConsumerId(consumerId, status, expiredOnly, page, size, now);
    }

    @Override
    public long countByConsumerId(ConsumerId consumerId, ApiCredentialStatus status, boolean expiredOnly, Instant now) {
        return delegate.countByConsumerId(consumerId, status, expiredOnly, now);
    }

    @Override
    public void save(ApiCredentialAggregate aggregate) {
        delegate.save(aggregate);
    }
}
