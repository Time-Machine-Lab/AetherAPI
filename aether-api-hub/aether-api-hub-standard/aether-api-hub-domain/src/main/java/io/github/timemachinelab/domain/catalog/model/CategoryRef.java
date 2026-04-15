package io.github.timemachinelab.domain.catalog.model;

import java.util.Objects;

/**
 * 分类引用值对象。
 */
public final class CategoryRef {

    private final CategoryCode code;

    private CategoryRef(CategoryCode code) {
        this.code = code;
    }

    public static CategoryRef of(String categoryCode) {
        if (categoryCode == null || categoryCode.isBlank()) {
            return null;
        }
        return new CategoryRef(CategoryCode.of(categoryCode));
    }

    public String getCode() {
        return code.getValue();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        CategoryRef that = (CategoryRef) o;
        return Objects.equals(code, that.code);
    }

    @Override
    public int hashCode() {
        return Objects.hash(code);
    }
}

