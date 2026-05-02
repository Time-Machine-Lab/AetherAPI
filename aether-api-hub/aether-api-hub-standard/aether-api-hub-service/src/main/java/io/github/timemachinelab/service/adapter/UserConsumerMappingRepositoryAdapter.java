package io.github.timemachinelab.service.adapter;

import io.github.timemachinelab.domain.consumerauth.model.ConsumerId;
import io.github.timemachinelab.domain.consumerauth.model.UserConsumerMapping;
import io.github.timemachinelab.domain.consumerauth.repository.UserConsumerMappingRepository;
import io.github.timemachinelab.service.port.out.UserConsumerMappingRepositoryPort;

import java.util.Optional;

/**
 * 用户-Consumer 映射仓储适配器。
 */
public class UserConsumerMappingRepositoryAdapter implements UserConsumerMappingRepositoryPort {

    private final UserConsumerMappingRepository delegate;

    public UserConsumerMappingRepositoryAdapter(UserConsumerMappingRepository delegate) {
        this.delegate = delegate;
    }

    @Override
    public Optional<UserConsumerMapping> findActiveByUserId(String userId) {
        return delegate.findActiveByUserId(userId);
    }

    @Override
    public Optional<UserConsumerMapping> findActiveByConsumerId(ConsumerId consumerId) {
        return delegate.findActiveByConsumerId(consumerId);
    }

    @Override
    public void save(UserConsumerMapping mapping) {
        delegate.save(mapping);
    }
}
