package io.github.timemachinelab.service.adapter;

import io.github.timemachinelab.domain.catalog.model.ApiCategoryAggregate;
import io.github.timemachinelab.domain.catalog.model.CategoryCode;
import io.github.timemachinelab.domain.catalog.model.CategoryStatus;
import io.github.timemachinelab.domain.catalog.repository.ApiCategoryRepository;
import io.github.timemachinelab.service.port.out.CategoryRepositoryPort;

import java.util.List;
import java.util.Optional;

/**
 * 分类仓储端口适配器，将领域仓储适配到服务层端口。
 */
public class CategoryRepositoryAdapter implements CategoryRepositoryPort {

    private final ApiCategoryRepository delegate;

    public CategoryRepositoryAdapter(ApiCategoryRepository delegate) {
        this.delegate = delegate;
    }

    @Override
    public Optional<ApiCategoryAggregate> findByCode(CategoryCode code) {
        return delegate.findByCode(code);
    }

    @Override
    public Optional<ApiCategoryAggregate> findByCodeIncludingDeleted(CategoryCode code) {
        return delegate.findByCodeIncludingDeleted(code);
    }

    @Override
    public List<ApiCategoryAggregate> findAll(CategoryStatus status, int page, int size) {
        return delegate.findAll(status, page, size);
    }

    @Override
    public long count(CategoryStatus status) {
        return delegate.count(status);
    }

    @Override
    public boolean existsByCode(CategoryCode code) {
        return delegate.existsByCode(code);
    }

    @Override
    public void save(ApiCategoryAggregate aggregate) {
        delegate.save(aggregate);
    }
}
