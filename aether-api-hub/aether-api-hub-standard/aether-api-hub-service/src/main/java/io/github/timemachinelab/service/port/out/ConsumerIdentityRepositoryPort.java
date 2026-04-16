package io.github.timemachinelab.service.port.out;

import io.github.timemachinelab.domain.consumerauth.model.ConsumerAggregate;
import io.github.timemachinelab.domain.consumerauth.model.ConsumerCode;
import io.github.timemachinelab.domain.consumerauth.model.ConsumerId;

import java.util.Optional;

/**
 * Consumer 仓储端口。
 */
public interface ConsumerIdentityRepositoryPort {

    Optional<ConsumerAggregate> findById(ConsumerId id);

    Optional<ConsumerAggregate> findByCode(ConsumerCode code);

    void save(ConsumerAggregate aggregate);
}
