package io.github.timemachinelab.service.application;

import io.github.timemachinelab.domain.catalog.model.ApiCategoryAggregate;
import io.github.timemachinelab.domain.catalog.model.CategoryCode;
import io.github.timemachinelab.domain.catalog.model.CategoryDomainException;
import io.github.timemachinelab.domain.catalog.model.CategoryId;
import io.github.timemachinelab.domain.catalog.model.CategoryStatus;
import io.github.timemachinelab.service.model.CategoryModel;
import io.github.timemachinelab.service.model.CategoryPageResult;
import io.github.timemachinelab.service.model.CategoryValidityResult;
import io.github.timemachinelab.service.model.CreateCategoryCommand;
import io.github.timemachinelab.service.model.RenameCategoryCommand;
import io.github.timemachinelab.service.port.in.CategoryUseCase;
import io.github.timemachinelab.service.port.out.CategoryRepositoryPort;

import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 分类管理应用服务实现。
 */
public class CategoryApplicationService implements CategoryUseCase {

    private final CategoryRepositoryPort categoryRepositoryPort;

    public CategoryApplicationService(CategoryRepositoryPort categoryRepositoryPort) {
        this.categoryRepositoryPort = categoryRepositoryPort;
    }

    @Override
    public CategoryModel createCategory(CreateCategoryCommand command) {
        // 1. 校验分类编码唯一性
        CategoryCode code = CategoryCode.of(command.getCategoryCode());
        if (categoryRepositoryPort.existsByCode(code)) {
            throw new CategoryDomainException("Category code already exists: " + code.getValue());
        }

        // 2. 创建聚合根
        CategoryId id = CategoryId.generate();
        ApiCategoryAggregate aggregate = ApiCategoryAggregate.create(id, code, command.getCategoryName());

        // 3. 保存
        categoryRepositoryPort.save(aggregate);

        return toModel(aggregate);
    }

    @Override
    public CategoryModel renameCategory(RenameCategoryCommand command) {
        CategoryCode code = CategoryCode.of(command.getCategoryCode());
        ApiCategoryAggregate aggregate = loadAggregate(code);

        aggregate.rename(command.getNewCategoryName());
        categoryRepositoryPort.save(aggregate);

        return toModel(aggregate);
    }

    @Override
    public CategoryModel enableCategory(String categoryCode) {
        CategoryCode code = CategoryCode.of(categoryCode);
        ApiCategoryAggregate aggregate = loadAggregate(code);

        aggregate.enable();
        categoryRepositoryPort.save(aggregate);

        return toModel(aggregate);
    }

    @Override
    public CategoryModel disableCategory(String categoryCode) {
        CategoryCode code = CategoryCode.of(categoryCode);
        ApiCategoryAggregate aggregate = loadAggregate(code);

        aggregate.disable();
        categoryRepositoryPort.save(aggregate);

        return toModel(aggregate);
    }

    @Override
    public CategoryModel getCategoryByCode(String categoryCode) {
        CategoryCode code = CategoryCode.of(categoryCode);
        ApiCategoryAggregate aggregate = loadAggregate(code);
        return toModel(aggregate);
    }

    @Override
    public CategoryPageResult listCategories(CategoryStatus status, int page, int size) {
        // 确保分页参数合法
        page = Math.max(1, page);
        size = Math.max(1, Math.min(size, 100));

        int offset = (page - 1) * size;
        List<ApiCategoryAggregate> aggregates = categoryRepositoryPort.findAll(status, offset, size);
        long total = categoryRepositoryPort.count(status);

        List<CategoryModel> items = aggregates.stream()
                .map(this::toModel)
                .collect(Collectors.toList());

        return new CategoryPageResult(items, page, size, total);
    }

    @Override
    public CategoryValidityResult validateCategory(String categoryCode) {
        CategoryCode code = CategoryCode.of(categoryCode);
        return categoryRepositoryPort.findByCode(code)
                .map(aggregate -> {
                    if (aggregate.isDeleted()) {
                        return CategoryValidityResult.invalid(categoryCode, "Category has been deleted");
                    }
                    if (aggregate.getStatus() == CategoryStatus.DISABLED) {
                        return CategoryValidityResult.invalid(categoryCode, "Category is disabled");
                    }
                    return CategoryValidityResult.valid(categoryCode);
                })
                .orElse(CategoryValidityResult.invalid(categoryCode, "Category not found"));
    }

    // -------------------- 私有辅助方法 --------------------

    private ApiCategoryAggregate loadAggregate(CategoryCode code) {
        return categoryRepositoryPort.findByCode(code)
                .orElseThrow(() -> new CategoryDomainException("Category not found: " + code.getValue()));
    }

    private CategoryModel toModel(ApiCategoryAggregate aggregate) {
        DateTimeFormatter formatter = DateTimeFormatter.ISO_INSTANT;
        return new CategoryModel(
                aggregate.getId().getValue(),
                aggregate.getCode().getValue(),
                aggregate.getName(),
                aggregate.getStatus().name(),
                formatter.format(aggregate.getCreatedAt()),
                formatter.format(aggregate.getUpdatedAt())
        );
    }
}
