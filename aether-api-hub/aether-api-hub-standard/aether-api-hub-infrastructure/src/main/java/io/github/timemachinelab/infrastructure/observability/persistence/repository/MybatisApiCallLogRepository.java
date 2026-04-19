package io.github.timemachinelab.infrastructure.observability.persistence.repository;

import io.github.timemachinelab.domain.observability.model.ApiCallLogAggregate;
import io.github.timemachinelab.domain.observability.repository.ApiCallLogRepository;
import io.github.timemachinelab.infrastructure.observability.persistence.converter.ApiCallLogConverter;
import io.github.timemachinelab.infrastructure.observability.persistence.entity.ApiCallLogDo;
import io.github.timemachinelab.infrastructure.observability.persistence.mapper.ApiCallLogMapper;
import org.springframework.stereotype.Repository;

/**
 * Platform call log MyBatis repository.
 */
@Repository
public class MybatisApiCallLogRepository implements ApiCallLogRepository {

    private final ApiCallLogMapper mapper;

    public MybatisApiCallLogRepository(ApiCallLogMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public void save(ApiCallLogAggregate aggregate) {
        ApiCallLogDo existing = mapper.selectById(aggregate.getId().getValue());
        if (existing == null) {
            mapper.insert(ApiCallLogConverter.toDo(aggregate));
            return;
        }
        ApiCallLogConverter.updateDo(existing, aggregate);
        mapper.updateById(existing);
    }
}
