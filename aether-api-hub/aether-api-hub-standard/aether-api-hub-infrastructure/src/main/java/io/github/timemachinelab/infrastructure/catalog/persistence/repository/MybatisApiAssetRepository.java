package io.github.timemachinelab.infrastructure.catalog.persistence.repository;

import io.github.timemachinelab.domain.catalog.model.ApiAssetAggregate;
import io.github.timemachinelab.domain.catalog.model.ApiCode;
import io.github.timemachinelab.domain.catalog.repository.ApiAssetRepository;
import io.github.timemachinelab.infrastructure.catalog.persistence.converter.ApiAssetConverter;
import io.github.timemachinelab.infrastructure.catalog.persistence.entity.ApiAssetDo;
import io.github.timemachinelab.infrastructure.catalog.persistence.mapper.ApiAssetMapper;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * MyBatis-Plus API 资产仓储实现。
 */
@Repository
public class MybatisApiAssetRepository implements ApiAssetRepository {

    private final ApiAssetMapper mapper;

    public MybatisApiAssetRepository(ApiAssetMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public Optional<ApiAssetAggregate> findByCode(ApiCode code) {
        return Optional.ofNullable(ApiAssetConverter.toAggregate(mapper.selectByCode(code.getValue())));
    }

    @Override
    public Optional<ApiAssetAggregate> findByCodeIncludingDeleted(ApiCode code) {
        return Optional.ofNullable(ApiAssetConverter.toAggregate(mapper.selectByCodeIncludingDeleted(code.getValue())));
    }

    @Override
    public boolean existsByCode(ApiCode code) {
        return mapper.existsByCode(code.getValue()) > 0;
    }

    @Override
    public void save(ApiAssetAggregate aggregate) {
        ApiAssetDo existing = mapper.selectByCodeIncludingDeleted(aggregate.getCode().getValue());
        if (existing == null) {
            mapper.insert(ApiAssetConverter.toDo(aggregate));
            return;
        }
        ApiAssetConverter.updateDo(existing, aggregate);
        mapper.updateById(existing);
    }
}

