package io.github.aetherapihub.catalog.domain.model;

import java.time.Instant;
import java.util.Objects;

/**
 * API Catalog 分类聚合根。
 * <p>
 * 承担分类命名与启停语义。CategoryCode 创建后不可变更。
 * 只有处于 ENABLED 状态的分类才可被新的 API 资产引用。
 */
public class ApiCategoryAggregate {

    private final CategoryId id;
    private final CategoryCode code;
    private String name;
    private CategoryStatus status;
    private final Instant createdAt;
    private Instant updatedAt;
    private boolean deleted;
    private long version;

    public ApiCategoryAggregate(CategoryId id, CategoryCode code, String name, Instant createdAt) {
        this(id, code, name, CategoryStatus.ENABLED, createdAt, Instant.now(), false, 0L);
    }

    public ApiCategoryAggregate(CategoryId id, CategoryCode code, String name,
                                  CategoryStatus status, Instant createdAt, Instant updatedAt,
                                  boolean deleted, long version) {
        this.id = Objects.requireNonNull(id, "id");
        this.code = Objects.requireNonNull(code, "code");
        this.name = Objects.requireNonNull(name, "name");
        this.status = Objects.requireNonNull(status, "status");
        this.createdAt = Objects.requireNonNull(createdAt, "createdAt");
        this.updatedAt = Objects.requireNonNull(updatedAt, "updatedAt");
        this.deleted = deleted;
        this.version = version;
    }

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

    /**
     * 重命名分类。展示名称可以随时修改，编码不可变更。
     */
    public void rename(String newName) {
        if (newName == null || newName.isBlank()) {
            throw new IllegalArgumentException("分类名称不能为空");
        }
        if (newName.length() > 128) {
            throw new IllegalArgumentException("分类名称长度不能超过 128");
        }
        this.name = newName;
        this.updatedAt = Instant.now();
        this.version++;
    }

    /**
     * 启用分类。
     */
    public void enable() {
        if (this.status == CategoryStatus.ENABLED) {
            return;
        }
        this.status = CategoryStatus.ENABLED;
        this.updatedAt = Instant.now();
        this.version++;
    }

    /**
     * 停用分类。停用后不得被新资产引用。
     */
    public void disable() {
        if (this.status == CategoryStatus.DISABLED) {
            return;
        }
        this.status = CategoryStatus.DISABLED;
        this.updatedAt = Instant.now();
        this.version++;
    }

    /**
     * 判断分类是否有效（存在且处于 ENABLED 状态）。
     */
    public boolean isValidForAssignment() {
        return !deleted && status == CategoryStatus.ENABLED;
    }
}
