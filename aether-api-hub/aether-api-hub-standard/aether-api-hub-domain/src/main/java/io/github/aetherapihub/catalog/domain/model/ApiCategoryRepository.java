package io.github.aetherapihub.catalog.domain.model;

import java.util.List;
import java.util.Optional;

/**
 * 分类仓储端口（领域层定义，基础设施层实现）。
 */
public interface ApiCategoryRepository {

    /**
     * 根据分类编码查找分类。
     */
    Optional<ApiCategoryAggregate> findByCode(CategoryCode code);

    /**
     * 根据分类 ID 查找分类。
     */
    Optional<ApiCategoryAggregate> findById(CategoryId id);

    /**
     * 保存分类（新增或更新）。
     */
    ApiCategoryAggregate save(ApiCategoryAggregate category);

    /**
     * 判断指定分类编码是否已存在（未删除）。
     */
    boolean existsByCode(CategoryCode code);

    /**
     * 分页查询分类列表（排除已删除）。
     * @param status 状态过滤（null 表示不限制）
     * @param page 页码（从 1 开始）
     * @param size 每页记录数
     */
    List<ApiCategoryAggregate> findPage(CategoryStatus status, int page, int size);

    /**
     * 统计满足条件的记录数（排除已删除）。
     */
    long count(CategoryStatus status);
}
