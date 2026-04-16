package io.github.timemachinelab.service.adapter;

import io.github.timemachinelab.domain.consumerauth.model.ConsumerAggregate;
import io.github.timemachinelab.domain.consumerauth.model.ConsumerCode;
import io.github.timemachinelab.domain.consumerauth.model.ConsumerId;
import io.github.timemachinelab.domain.consumerauth.repository.ConsumerIdentityRepository;
import io.github.timemachinelab.service.port.out.ConsumerIdentityRepositoryPort;

import java.util.Optional;

/**
 * Consumer 仓储适配器。
 */
public class ConsumerIdentityRepositoryAdapter implements ConsumerIdentityRepositoryPort {

    private final ConsumerIdentityRepository delegate;

    public ConsumerIdentityRepositoryAdapter(ConsumerIdentityRepository delegate) {
        this.delegate = delegate;
    }

    @Override
    public Optional<ConsumerAggregate> findById(ConsumerId id) {
        return delegate.findById(id);
    }

    @Override
    public Optional<ConsumerAggregate> findByCode(ConsumerCode code) {
        return delegate.findByCode(code);
    }

    @Override
    public void save(ConsumerAggregate aggregate) {
        delegate.save(aggregate);
    }
}
