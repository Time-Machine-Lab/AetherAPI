package io.github.timemachinelab.service.adapter;

import io.github.timemachinelab.domain.catalog.model.CategoryRef;
import io.github.timemachinelab.domain.catalog.model.CategoryValidityChecker;
import io.github.timemachinelab.service.port.out.CategoryRepositoryPort;

/**
 * 分类有效性校验适配器。
 */
public class CategoryValidityAdapter implements CategoryValidityChecker {

    private final CategoryRepositoryPort categoryRepositoryPort;

    public CategoryValidityAdapter(CategoryRepositoryPort categoryRepositoryPort) {
        this.categoryRepositoryPort = categoryRepositoryPort;
    }

    @Override
    public boolean isValid(CategoryRef categoryRef) {
        if (categoryRef == null) {
            return false;
        }
        return categoryRepositoryPort.findByCode(io.github.timemachinelab.domain.catalog.model.CategoryCode.of(categoryRef.getCode()))
                .map(io.github.timemachinelab.domain.catalog.model.ApiCategoryAggregate::isValid)
                .orElse(false);
    }
}

