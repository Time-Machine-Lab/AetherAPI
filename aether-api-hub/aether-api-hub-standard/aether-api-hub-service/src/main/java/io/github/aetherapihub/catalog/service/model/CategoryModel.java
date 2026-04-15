package io.github.aetherapihub.catalog.service.model;

import io.github.aetherapihub.catalog.domain.model.CategoryCode;
import io.github.aetherapihub.catalog.domain.model.CategoryId;
import io.github.aetherapihub.catalog.domain.model.CategoryStatus;

import java.time.Instant;

/**
 * 分类领域模型（应用层内部使用，不泄漏给 adapter）。
 */
public class CategoryModel {

    private final CategoryId id;
    private final CategoryCode code;
    private final String name;
    private final CategoryStatus status;
    private final Instant createdAt;
    private final Instant updatedAt;
    private final long version;

    public CategoryModel(CategoryId id, CategoryCode code, String name,
                          CategoryStatus status, Instant createdAt,
                          Instant updatedAt, long version) {
        this.id = id;
        this.code = code;
        this.name = name;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.version = version;
    }

    public CategoryId getId() { return id; }
    public CategoryCode getCode() { return code; }
    public String getName() { return name; }
    public CategoryStatus getStatus() { return status; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public long getVersion() { return version; }
}
