package io.github.timemachinelab.service.adapter;

import io.github.timemachinelab.domain.catalog.model.ApiAssetAggregate;
import io.github.timemachinelab.domain.catalog.model.ApiCode;
import io.github.timemachinelab.domain.catalog.repository.ApiAssetRepository;
import io.github.timemachinelab.service.port.out.ApiAssetRepositoryPort;

import java.util.Optional;

/**
 * API 资产仓储适配器。
 */
public class ApiAssetRepositoryAdapter implements ApiAssetRepositoryPort {

    private final ApiAssetRepository delegate;

    public ApiAssetRepositoryAdapter(ApiAssetRepository delegate) {
        this.delegate = delegate;
    }

    @Override
    public Optional<ApiAssetAggregate> findByCode(ApiCode code) {
        return delegate.findByCode(code);
    }

    @Override
    public Optional<ApiAssetAggregate> findByCodeIncludingDeleted(ApiCode code) {
        return delegate.findByCodeIncludingDeleted(code);
    }

    @Override
    public boolean existsByCode(ApiCode code) {
        return delegate.existsByCode(code);
    }

    @Override
    public void save(ApiAssetAggregate aggregate) {
        delegate.save(aggregate);
    }
}

