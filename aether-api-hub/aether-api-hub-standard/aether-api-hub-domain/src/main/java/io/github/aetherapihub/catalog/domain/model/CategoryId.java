package io.github.aetherapihub.catalog.domain.model;

/**
 * 分类实体 ID 值对象。
 */
public final class CategoryId {

    private final String value;

    public CategoryId(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("分类 ID 不能为空");
        }
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CategoryId categoryId = (CategoryId) o;
        return Objects.equals(value, categoryId.value);
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
