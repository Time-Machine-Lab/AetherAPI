package io.github.timemachinelab.domain.catalog.model;

import java.util.Objects;
import java.util.regex.Pattern;

/**
 * 分类业务编码，值对象，创建后不可变。
 * 用于在平台内唯一标识一个分类，是分类的业务主键。
 */
public final class CategoryCode {

    private static final int MAX_LENGTH = 64;
    private static final Pattern VALID_PATTERN = Pattern.compile("^[a-z0-9][a-z0-9_-]*[a-z0-9]$|^[a-z0-9]$");

    private final String value;

    private CategoryCode(String value) {
        this.value = value;
    }

    /**
     * 从字符串值创建 CategoryCode。
     *
     * @param value 分类编码值，长度 1-64，只能包含小写字母、数字、下划线和连字符
     * @return CategoryCode 实例
     * @throws IllegalArgumentException 如果值不符合规范
     */
    public static CategoryCode of(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("CategoryCode value must not be blank");
        }
        String trimmed = value.trim().toLowerCase();
        if (trimmed.length() > MAX_LENGTH) {
            throw new IllegalArgumentException(
                    "CategoryCode value must not exceed " + MAX_LENGTH + " characters, got: " + trimmed.length());
        }
        if (!VALID_PATTERN.matcher(trimmed).matches()) {
            throw new IllegalArgumentException(
                    "CategoryCode value must match pattern '^[a-z0-9][a-z0-9_-]*[a-z0-9]$|^[a-z0-9]$', got: " + trimmed);
        }
        return new CategoryCode(trimmed);
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
