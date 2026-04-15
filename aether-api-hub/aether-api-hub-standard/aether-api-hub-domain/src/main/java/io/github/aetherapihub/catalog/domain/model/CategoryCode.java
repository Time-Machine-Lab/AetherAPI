package io.github.aetherapihub.catalog.domain.model;

import java.util.Objects;

/**
 * 分类业务编码值对象。
 * 创建后不可变更，在整个生命周期内保持稳定。
 */
public final class CategoryCode {

    private final String value;

    public CategoryCode(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("分类编码不能为空");
        }
        if (value.length() > 64) {
            throw new IllegalArgumentException("分类编码长度不能超过 64");
        }
        this.value = value.trim();
    }

    public String getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CategoryCode that = (CategoryCode) o;
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
