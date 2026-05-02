package io.github.timemachinelab.service.adapter;

import io.github.timemachinelab.domain.catalog.model.ApiCode;
import io.github.timemachinelab.domain.consumerauth.model.ConsumerId;
import io.github.timemachinelab.domain.subscription.model.ApiSubscriptionAggregate;
import io.github.timemachinelab.domain.subscription.model.ApiSubscriptionId;
import io.github.timemachinelab.domain.subscription.repository.ApiSubscriptionRepository;
import io.github.timemachinelab.service.port.out.ApiSubscriptionEntitlementPort;
import io.github.timemachinelab.service.port.out.ApiSubscriptionRepositoryPort;

import java.util.List;
import java.util.Optional;

/**
 * API subscription repository adapter.
 */
public class ApiSubscriptionRepositoryAdapter implements ApiSubscriptionRepositoryPort, ApiSubscriptionEntitlementPort {

    private final ApiSubscriptionRepository delegate;

    public ApiSubscriptionRepositoryAdapter(ApiSubscriptionRepository delegate) {
        this.delegate = delegate;
    }

    @Override
    public Optional<ApiSubscriptionAggregate> findById(ApiSubscriptionId id) {
        return delegate.findById(id);
    }

    @Override
    public Optional<ApiSubscriptionAggregate> findActiveByConsumerIdAndApiCode(ConsumerId consumerId, ApiCode apiCode) {
        return delegate.findActiveByConsumerIdAndApiCode(consumerId, apiCode);
    }

    @Override
    public List<ApiSubscriptionAggregate> findPageByConsumerId(ConsumerId consumerId, int page, int size) {
        return delegate.findPageByConsumerId(consumerId, page, size);
    }

    @Override
    public long countByConsumerId(ConsumerId consumerId) {
        return delegate.countByConsumerId(consumerId);
    }

    @Override
    public boolean hasActiveSubscription(ConsumerId consumerId, ApiCode apiCode) {
        return delegate.hasActiveSubscription(consumerId, apiCode);
    }

    @Override
    public void save(ApiSubscriptionAggregate aggregate) {
        delegate.save(aggregate);
    }
}
