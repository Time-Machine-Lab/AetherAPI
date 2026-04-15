package io.github.timemachinelab.domain.catalog.model;

import java.util.Objects;
import java.util.UUID;

/**
 * 分类实体唯一标识，值对象，创建后不可变。
 */
public final class CategoryId {

    private final String value;

    private CategoryId(String value) {
        this.value = value;
    }

    /**
     * 从字符串值创建 CategoryId。
     */
    public static CategoryId of(String value) {
        Objects.requireNonNull(value, "CategoryId value must not be null");
        if (value.isBlank()) {
            throw new IllegalArgumentException("CategoryId value must not be blank");
        }
        return new CategoryId(value);
    }

    /**
     * 生成一个新的随机 CategoryId（UUID）。
     */
    public static CategoryId generate() {
        return new CategoryId(UUID.randomUUID().toString());
    }

    public String getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CategoryId that = (CategoryId) o;
        return Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return value;
    }
}
