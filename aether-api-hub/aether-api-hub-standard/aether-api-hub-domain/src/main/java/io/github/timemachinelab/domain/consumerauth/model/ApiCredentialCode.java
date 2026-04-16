package io.github.timemachinelab.domain.consumerauth.model;

import java.util.Objects;
import java.util.regex.Pattern;

/**
 * API 凭证业务编码。
 */
public final class ApiCredentialCode {

    private static final int MAX_LENGTH = 64;
    private static final Pattern VALID_PATTERN = Pattern.compile("^[a-z0-9][a-z0-9_-]*[a-z0-9]$|^[a-z0-9]$");

    private final String value;

    private ApiCredentialCode(String value) {
        this.value = value;
    }

    public static ApiCredentialCode of(String value) {
        Objects.requireNonNull(value, "ApiCredentialCode value must not be null");
        String trimmed = value.trim().toLowerCase();
        if (trimmed.isEmpty()) {
            throw new IllegalArgumentException("ApiCredentialCode value must not be blank");
        }
        if (trimmed.length() > MAX_LENGTH) {
            throw new IllegalArgumentException("ApiCredentialCode value must not exceed " + MAX_LENGTH + " characters");
        }
        if (!VALID_PATTERN.matcher(trimmed).matches()) {
            throw new IllegalArgumentException("ApiCredentialCode value contains invalid characters");
        }
        return new ApiCredentialCode(trimmed);
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
        ApiCredentialCode that = (ApiCredentialCode) o;
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
