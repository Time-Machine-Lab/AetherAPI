package io.github.timemachinelab.domain.consumerauth.repository;

import io.github.timemachinelab.domain.consumerauth.model.ConsumerAggregate;
import io.github.timemachinelab.domain.consumerauth.model.ConsumerCode;
import io.github.timemachinelab.domain.consumerauth.model.ConsumerId;

import java.util.Optional;

/**
 * Consumer 仓储接口。
 */
public interface ConsumerIdentityRepository {

    Optional<ConsumerAggregate> findById(ConsumerId id);

    Optional<ConsumerAggregate> findByCode(ConsumerCode code);

    void save(ConsumerAggregate aggregate);
}
