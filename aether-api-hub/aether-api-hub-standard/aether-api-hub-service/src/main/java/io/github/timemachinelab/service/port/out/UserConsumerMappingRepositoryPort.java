package io.github.timemachinelab.service.port.out;

import io.github.timemachinelab.domain.consumerauth.model.ConsumerId;
import io.github.timemachinelab.domain.consumerauth.model.UserConsumerMapping;

import java.util.Optional;

/**
 * 用户-Consumer 映射仓储端口。
 */
public interface UserConsumerMappingRepositoryPort {

    Optional<UserConsumerMapping> findActiveByUserId(String userId);

    Optional<UserConsumerMapping> findActiveByConsumerId(ConsumerId consumerId);

    void save(UserConsumerMapping mapping);
}
