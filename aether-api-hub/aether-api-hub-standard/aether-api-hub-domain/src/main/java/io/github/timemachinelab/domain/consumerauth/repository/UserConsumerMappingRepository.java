package io.github.timemachinelab.domain.consumerauth.repository;

import io.github.timemachinelab.domain.consumerauth.model.UserConsumerMapping;
import io.github.timemachinelab.domain.consumerauth.model.ConsumerId;

import java.util.Optional;

/**
 * 用户与 Consumer 映射仓储接口。
 */
public interface UserConsumerMappingRepository {

    Optional<UserConsumerMapping> findActiveByUserId(String userId);

    Optional<UserConsumerMapping> findActiveByConsumerId(ConsumerId consumerId);

    void save(UserConsumerMapping mapping);
}
