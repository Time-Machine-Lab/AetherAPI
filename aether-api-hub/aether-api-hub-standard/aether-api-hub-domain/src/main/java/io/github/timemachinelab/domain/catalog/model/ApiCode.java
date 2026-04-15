package io.github.timemachinelab.domain.catalog.model;

import java.util.Objects;
import java.util.regex.Pattern;

/**
 * API 资产业务编码。
 */
public final class ApiCode {

    private static final int MAX_LENGTH = 64;
    private static final Pattern VALID_PATTERN = Pattern.compile("^[a-z0-9][a-z0-9_-]*[a-z0-9]$|^[a-z0-9]$");

    private final String value;

    private ApiCode(String value) {
        this.value = value;
    }

    public static ApiCode of(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("ApiCode value must not be blank");
        }
        String trimmed = value.trim().toLowerCase();
        if (trimmed.length() > MAX_LENGTH) {
            throw new IllegalArgumentException("ApiCode value must not exceed " + MAX_LENGTH + " characters");
        }
        if (!VALID_PATTERN.matcher(trimmed).matches()) {
            throw new IllegalArgumentException(
                    "ApiCode value must match pattern '^[a-z0-9][a-z0-9_-]*[a-z0-9]$|^[a-z0-9]$'");
        }
        return new ApiCode(trimmed);
    }

    public String getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ApiCode apiCode = (ApiCode) o;
        return Objects.equals(value, apiCode.value);
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

