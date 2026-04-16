package io.github.timemachinelab.infrastructure.consumerauth.persistence.repository;

import io.github.timemachinelab.domain.consumerauth.model.ConsumerAggregate;
import io.github.timemachinelab.domain.consumerauth.model.ConsumerCode;
import io.github.timemachinelab.domain.consumerauth.model.ConsumerId;
import io.github.timemachinelab.domain.consumerauth.repository.ConsumerIdentityRepository;
import io.github.timemachinelab.infrastructure.consumerauth.persistence.converter.ConsumerIdentityConverter;
import io.github.timemachinelab.infrastructure.consumerauth.persistence.entity.ConsumerIdentityDo;
import io.github.timemachinelab.infrastructure.consumerauth.persistence.mapper.ConsumerIdentityMapper;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Consumer MyBatis 仓储实现。
 */
@Repository
public class MybatisConsumerIdentityRepository implements ConsumerIdentityRepository {

    private final ConsumerIdentityMapper mapper;

    public MybatisConsumerIdentityRepository(ConsumerIdentityMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public Optional<ConsumerAggregate> findById(ConsumerId id) {
        return Optional.ofNullable(ConsumerIdentityConverter.toAggregate(mapper.selectById(id.getValue())));
    }

    @Override
    public Optional<ConsumerAggregate> findByCode(ConsumerCode code) {
        return Optional.ofNullable(ConsumerIdentityConverter.toAggregate(mapper.selectByCode(code.getValue())));
    }

    @Override
    public void save(ConsumerAggregate aggregate) {
        ConsumerIdentityDo existing = mapper.selectById(aggregate.getId().getValue());
        if (existing == null) {
            mapper.insert(ConsumerIdentityConverter.toDo(aggregate));
            return;
        }
        ConsumerIdentityConverter.updateDo(existing, aggregate);
        mapper.updateById(existing);
    }
}
