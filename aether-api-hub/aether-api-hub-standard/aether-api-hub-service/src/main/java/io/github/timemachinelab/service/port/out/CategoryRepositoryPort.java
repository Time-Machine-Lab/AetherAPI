package io.github.timemachinelab.service.port.out;

import io.github.timemachinelab.domain.catalog.model.ApiCategoryAggregate;
import io.github.timemachinelab.domain.catalog.model.CategoryCode;
import io.github.timemachinelab.domain.catalog.model.CategoryStatus;

import java.util.List;
import java.util.Optional;

/**
 * 分类仓储出站端口（基础设施层实现）。
 */
public interface CategoryRepositoryPort {

    /**
     * 根据分类编码查询分类。
     *
     * @param code 分类编码
     * @return 分类聚合根（如果存在且未删除）
     */
    Optional<ApiCategoryAggregate> findByCode(CategoryCode code);

    /**
     * 根据分类编码查询分类（包含已删除）。
     *
     * @param code 分类编码
     * @return 分类聚合根（如果存在）
     */
    Optional<ApiCategoryAggregate> findByCodeIncludingDeleted(CategoryCode code);

    /**
     * 分页查询分类列表。
     *
     * @param status 按状态过滤（可选），传 null 表示查询所有非删除分类
     * @param page   页码（从 1 开始）
     * @param size   每页记录数
     * @return 分类聚合根列表
     */
    List<ApiCategoryAggregate> findAll(CategoryStatus status, int page, int size);

    /**
     * 统计指定状态的分类总数。
     *
     * @param status 按状态过滤（可选），传 null 表示统计所有非删除分类
     * @return 分类总数
     */
    long count(CategoryStatus status);

    /**
     * 判断分类编码是否已存在（用于创建时校验唯一性）。
     *
     * @param code 分类编码
     * @return true-已存在，false-不存在
     */
    boolean existsByCode(CategoryCode code);

    /**
     * 保存分类聚合根（新建或更新）。
     *
     * @param aggregate 分类聚合根
     */
    void save(ApiCategoryAggregate aggregate);
}
