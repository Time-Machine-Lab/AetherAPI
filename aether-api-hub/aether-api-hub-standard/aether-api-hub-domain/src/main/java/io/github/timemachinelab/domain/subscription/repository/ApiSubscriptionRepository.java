package io.github.timemachinelab.domain.subscription.repository;

import io.github.timemachinelab.domain.catalog.model.ApiCode;
import io.github.timemachinelab.domain.consumerauth.model.ConsumerId;
import io.github.timemachinelab.domain.subscription.model.ApiSubscriptionAggregate;
import io.github.timemachinelab.domain.subscription.model.ApiSubscriptionId;

import java.util.List;
import java.util.Optional;

/**
 * API subscription repository.
 */
public interface ApiSubscriptionRepository {

    Optional<ApiSubscriptionAggregate> findById(ApiSubscriptionId id);

    Optional<ApiSubscriptionAggregate> findActiveByConsumerIdAndApiCode(ConsumerId consumerId, ApiCode apiCode);

    List<ApiSubscriptionAggregate> findPageByConsumerId(ConsumerId consumerId, int page, int size);

    long countByConsumerId(ConsumerId consumerId);

    boolean hasActiveSubscription(ConsumerId consumerId, ApiCode apiCode);

    void save(ApiSubscriptionAggregate aggregate);
}
