package io.github.aetherapihub.catalog.service;

import io.github.aetherapihub.catalog.domain.model.ApiCategoryAggregate;
import io.github.aetherapihub.catalog.domain.model.ApiCategoryRepository;
import io.github.aetherapihub.catalog.domain.model.CategoryCode;
import io.github.aetherapihub.catalog.domain.model.CategoryDomainException;
import io.github.aetherapihub.catalog.domain.model.CategoryId;
import io.github.aetherapihub.catalog.service.model.CategoryModel;
import io.github.aetherapihub.catalog.service.model.CategoryValidityResult;
import io.github.aetherapihub.catalog.service.model.CreateCategoryCommand;
import io.github.aetherapihub.catalog.service.model.RenameCategoryCommand;
import io.github.aetherapihub.catalog.service.port.in_.CategoryValidityQuery;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

/**
 * 分类生命周期应用服务。
 * <p>
 * 编排分类创建、重命名、启用、停用和有效性校验等用例。
 * 管理事务边界，负责业务规则校验与聚合操作。
 */
public class CategoryApplicationService implements CategoryValidityQuery {

    private final ApiCategoryRepository repository;

    public CategoryApplicationService(ApiCategoryRepository repository) {
        this.repository = repository;
    }

    /**
     * 创建新分类。
     */
    public CategoryModel createCategory(CreateCategoryCommand command) {
        CategoryCode code = new CategoryCode(command.getCategoryCode());

        if (repository.existsByCode(code)) {
            throw new CategoryDomainException(
                    "CATEGORY_CODE_DUPLICATE",
                    "分类编码已存在: " + code.getValue());
        }

        ApiCategoryAggregate category = new ApiCategoryAggregate(
                new CategoryId(UUID.randomUUID().toString()),
                code,
                command.getCategoryName(),
                Instant.now()
        );

        ApiCategoryAggregate saved = repository.save(category);
        return toModel(saved);
    }

    /**
     * 根据分类编码查询详情。
     */
    public Optional<CategoryModel> getByCode(String categoryCode) {
        CategoryCode code = new CategoryCode(categoryCode);
        return repository.findByCode(code).filter(c -> !c.isDeleted()).map(this::toModel);
    }

    /**
     * 重命名分类。
     */
    public CategoryModel renameCategory(RenameCategoryCommand command) {
        CategoryCode code = new CategoryCode(command.getCategoryCode());
        ApiCategoryAggregate category = loadActiveOrThrow(code);

        category.rename(command.getNewName());
        ApiCategoryAggregate saved = repository.save(category);
        return toModel(saved);
    }

    /**
     * 启用分类。
     */
    public CategoryModel enableCategory(String categoryCode) {
        CategoryCode code = new CategoryCode(categoryCode);
        ApiCategoryAggregate category = loadActiveOrThrow(code);

        category.enable();
        ApiCategoryAggregate saved = repository.save(category);
        return toModel(saved);
    }

    /**
     * 停用分类。
     */
    public CategoryModel disableCategory(String categoryCode) {
        CategoryCode code = new CategoryCode(categoryCode);
        ApiCategoryAggregate category = loadActiveOrThrow(code);

        category.disable();
        ApiCategoryAggregate saved = repository.save(category);
        return toModel(saved);
    }

    @Override
    public CategoryValidityResult checkValidity(String categoryCode) {
        CategoryCode code = new CategoryCode(categoryCode);
        Optional<ApiCategoryAggregate> opt = repository.findByCode(code);

        if (opt.isEmpty() || opt.get().isDeleted()) {
            return CategoryValidityResult.invalid(categoryCode, "分类不存在或已删除");
        }

        ApiCategoryAggregate category = opt.get();
        if (!category.isValidForAssignment()) {
            return CategoryValidityResult.invalid(categoryCode, "分类已停用，不得被新资产引用");
        }

        return CategoryValidityResult.valid(categoryCode);
    }

    private ApiCategoryAggregate loadActiveOrThrow(CategoryCode code) {
        return repository.findByCode(code)
                .filter(c -> !c.isDeleted())
                .orElseThrow(() -> new CategoryDomainException(
                        "CATEGORY_NOT_FOUND",
                        "分类不存在或已删除: " + code.getValue()));
    }

package io.github.aetherapihub.catalog.service;

import io.github.aetherapihub.catalog.domain.model.ApiCategoryAggregate;
import io.github.aetherapihub.catalog.domain.model.ApiCategoryRepository;
import io.github.aetherapihub.catalog.domain.model.CategoryCode;
import io.github.aetherapihub.catalog.domain.model.CategoryDomainException;
import io.github.aetherapihub.catalog.domain.model.CategoryId;
import io.github.aetherapihub.catalog.domain.model.CategoryStatus;
import io.github.aetherapihub.catalog.service.model.CategoryPageResult;
import io.github.aetherapihub.catalog.service.model.CategoryModel;
import io.github.aetherapihub.catalog.service.model.CategoryValidityResult;
import io.github.aetherapihub.catalog.service.model.CreateCategoryCommand;
import io.github.aetherapihub.catalog.service.model.RenameCategoryCommand;
import io.github.aetherapihub.catalog.service.port.in_.CategoryValidityQuery;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * 分类生命周期应用服务。
 * <p>
 * 编排分类创建、重命名、启用、停用和有效性校验等用例。
 * 管理事务边界，负责业务规则校验与聚合操作。
 */
public class CategoryApplicationService implements CategoryValidityQuery {

    private final ApiCategoryRepository repository;

    public CategoryApplicationService(ApiCategoryRepository repository) {
        this.repository = repository;
    }

    /**
     * 分页查询分类列表。
     */
    public CategoryPageResult listCategories(String status, int page, int size) {
        CategoryStatus statusEnum = (status != null && !status.isBlank())
                ? CategoryStatus.valueOf(status.toUpperCase())
                : null;
        List<CategoryModel> items = repository.findPage(statusEnum, page, size)
                .stream().map(this::toModel).toList();
        long total = repository.count(statusEnum);
        return new CategoryPageResult(items, page, size, total);
    }

    /**
     * 创建新分类。
     */
    public CategoryModel createCategory(CreateCategoryCommand command) {
        CategoryCode code = new CategoryCode(command.getCategoryCode());

        if (repository.existsByCode(code)) {
            throw new CategoryDomainException(
                    "CATEGORY_CODE_DUPLICATE",
                    "分类编码已存在: " + code.getValue());
        }

        ApiCategoryAggregate category = new ApiCategoryAggregate(
                new CategoryId(UUID.randomUUID().toString()),
                code,
                command.getCategoryName(),
                Instant.now()
        );

        ApiCategoryAggregate saved = repository.save(category);
        return toModel(saved);
    }

    /**
     * 根据分类编码查询详情。
     */
    public Optional<CategoryModel> getByCode(String categoryCode) {
        CategoryCode code = new CategoryCode(categoryCode);
        return repository.findByCode(code).filter(c -> !c.isDeleted()).map(this::toModel);
    }

    /**
     * 重命名分类。
     */
    public CategoryModel renameCategory(RenameCategoryCommand command) {
        CategoryCode code = new CategoryCode(command.getCategoryCode());
        ApiCategoryAggregate category = loadActiveOrThrow(code);

        category.rename(command.getNewName());
        ApiCategoryAggregate saved = repository.save(category);
        return toModel(saved);
    }

    /**
     * 启用分类。
     */
    public CategoryModel enableCategory(String categoryCode) {
        CategoryCode code = new CategoryCode(categoryCode);
        ApiCategoryAggregate category = loadActiveOrThrow(code);

        category.enable();
        ApiCategoryAggregate saved = repository.save(category);
        return toModel(saved);
    }

    /**
     * 停用分类。
     */
    public CategoryModel disableCategory(String categoryCode) {
        CategoryCode code = new CategoryCode(categoryCode);
        ApiCategoryAggregate category = loadActiveOrThrow(code);

        category.disable();
        ApiCategoryAggregate saved = repository.save(category);
        return toModel(saved);
    }

    @Override
    public CategoryValidityResult checkValidity(String categoryCode) {
        CategoryCode code = new CategoryCode(categoryCode);
        Optional<ApiCategoryAggregate> opt = repository.findByCode(code);

        if (opt.isEmpty() || opt.get().isDeleted()) {
            return CategoryValidityResult.invalid(categoryCode, "分类不存在或已删除");
        }

        ApiCategoryAggregate category = opt.get();
        if (!category.isValidForAssignment()) {
            return CategoryValidityResult.invalid(categoryCode, "分类已停用，不得被新资产引用");
        }

        return CategoryValidityResult.valid(categoryCode);
    }

    private ApiCategoryAggregate loadActiveOrThrow(CategoryCode code) {
        return repository.findByCode(code)
                .filter(c -> !c.isDeleted())
                .orElseThrow(() -> new CategoryDomainException(
                        "CATEGORY_NOT_FOUND",
                        "分类不存在或已删除: " + code.getValue()));
    }

    private CategoryModel toModel(ApiCategoryAggregate c) {
        return new CategoryModel(
                c.getId(),
                c.getCode(),
                c.getName(),
                c.getStatus(),
                c.getCreatedAt(),
                c.getUpdatedAt(),
                c.getVersion()
        );
    }

}
