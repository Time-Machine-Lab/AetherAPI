package io.github.timemachinelab.infrastructure.consumerauth.persistence.repository;

import io.github.timemachinelab.domain.consumerauth.model.ConsumerId;
import io.github.timemachinelab.domain.consumerauth.model.UserConsumerMapping;
import io.github.timemachinelab.domain.consumerauth.repository.UserConsumerMappingRepository;
import io.github.timemachinelab.infrastructure.consumerauth.persistence.converter.UserConsumerMappingConverter;
import io.github.timemachinelab.infrastructure.consumerauth.persistence.entity.UserConsumerMappingDo;
import io.github.timemachinelab.infrastructure.consumerauth.persistence.mapper.UserConsumerMappingMapper;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 用户-Consumer 映射 MyBatis 仓储实现。
 */
@Repository
public class MybatisUserConsumerMappingRepository implements UserConsumerMappingRepository {

    private final UserConsumerMappingMapper mapper;

    public MybatisUserConsumerMappingRepository(UserConsumerMappingMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public Optional<UserConsumerMapping> findActiveByUserId(String userId) {
        return Optional.ofNullable(UserConsumerMappingConverter.toAggregate(mapper.selectActiveByUserId(userId)));
    }

    @Override
    public Optional<UserConsumerMapping> findActiveByConsumerId(ConsumerId consumerId) {
        return Optional.ofNullable(UserConsumerMappingConverter.toAggregate(
                mapper.selectActiveByConsumerId(consumerId.getValue())));
    }

    @Override
    public void save(UserConsumerMapping mapping) {
        UserConsumerMappingDo existing = mapper.selectById(mapping.getId());
        if (existing == null) {
            mapper.insert(UserConsumerMappingConverter.toDo(mapping));
            return;
        }
        UserConsumerMappingConverter.updateDo(existing, mapping);
        mapper.updateById(existing);
    }
}
