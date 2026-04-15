package io.github.timemachinelab.domain.catalog.model;

import java.time.Instant;
import java.util.Objects;

/**
 * API 分类聚合根。
 * 负责维护分类的生命周期状态流转与业务不变量。
 */
public class ApiCategoryAggregate {

    private CategoryId id;
    private CategoryCode code;
    private String name;
    private CategoryStatus status;
    private Instant createdAt;
    private Instant updatedAt;
    private boolean deleted;
    private long version;

    /**
     * 受保护的默认构造函数，供 MyBatis 反射使用。
     */
    protected ApiCategoryAggregate() {
    }

    private ApiCategoryAggregate(CategoryId id, CategoryCode code, String name, CategoryStatus status,
                                  Instant createdAt, Instant updatedAt, boolean deleted, long version) {
        this.id = id;
        this.code = code;
        this.name = name;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.deleted = deleted;
        this.version = version;
    }

    /**
     * 创建新的分类聚合根。
     *
     * @param id   分类 ID
     * @param code 分类编码（不可变）
     * @param name 分类展示名称
     * @return 新的分类聚合根
     */
    public static ApiCategoryAggregate create(CategoryId id, CategoryCode code, String name) {
        Instant now = Instant.now();
        ApiCategoryAggregate aggregate = new ApiCategoryAggregate(
                id, code, name, CategoryStatus.ENABLED, now, now, false, 0L);
        return aggregate;
    }

    /**
     * 从持久化数据重建聚合根（供仓储使用）。
     */
    public static ApiCategoryAggregate reconstitute(
            CategoryId id, CategoryCode code, String name, CategoryStatus status,
            Instant createdAt, Instant updatedAt, boolean deleted, long version) {
        return new ApiCategoryAggregate(id, code, name, status, createdAt, updatedAt, deleted, version);
    }

    // -------------------- 业务行为 --------------------

    /**
     * 重命名分类。
     *
     * @param newName 新的展示名称
     * @throws IllegalArgumentException 如果新名称为空或空白
     */
    public void rename(String newName) {
        if (newName == null || newName.isBlank()) {
            throw new IllegalArgumentException("Category name must not be blank");
        }
        this.name = newName.trim();
        this.updatedAt = Instant.now();
        this.version++;
    }

    /**
     * 启用分类。
     *
     * @throws CategoryDomainException 如果分类已处于 ENABLED 状态或已删除
     */
    public void enable() {
        ensureNotDeleted();
        if (this.status == CategoryStatus.ENABLED) {
            throw new CategoryDomainException("Category is already enabled");
        }
        this.status = CategoryStatus.ENABLED;
        this.updatedAt = Instant.now();
        this.version++;
    }

    /**
     * 停用分类。
     *
     * @throws CategoryDomainException 如果分类已处于 DISABLED 状态或已删除
     */
    public void disable() {
        ensureNotDeleted();
        if (this.status == CategoryStatus.DISABLED) {
            throw new CategoryDomainException("Category is already disabled");
        }
        this.status = CategoryStatus.DISABLED;
        this.updatedAt = Instant.now();
        this.version++;
    }

    /**
     * 标记分类为已删除（软删除）。
     */
    public void markDeleted() {
        this.deleted = true;
        this.updatedAt = Instant.now();
        this.version++;
    }

    /**
     * 判断分类是否有效：必须存在、且处于 ENABLED 状态、且未删除。
     */
    public boolean isValid() {
        return !deleted && status == CategoryStatus.ENABLED;
    }

    // -------------------- 私有辅助方法 --------------------

    private void ensureNotDeleted() {
        if (this.deleted) {
            throw new CategoryDomainException("Category has been deleted");
        }
    }

    // -------------------- Getter --------------------

    public CategoryId getId() {
        return id;
    }

    public CategoryCode getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public CategoryStatus getStatus() {
        return status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public long getVersion() {
        return version;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ApiCategoryAggregate that = (ApiCategoryAggregate) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
