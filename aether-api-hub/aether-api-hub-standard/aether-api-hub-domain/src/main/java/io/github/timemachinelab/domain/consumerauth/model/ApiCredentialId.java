package io.github.timemachinelab.domain.consumerauth.model;

import java.util.Objects;
import java.util.UUID;

/**
 * API 凭证唯一标识。
 */
public final class ApiCredentialId {

    private final String value;

    private ApiCredentialId(String value) {
        this.value = value;
    }

    public static ApiCredentialId of(String value) {
        Objects.requireNonNull(value, "ApiCredentialId value must not be null");
        if (value.isBlank()) {
            throw new IllegalArgumentException("ApiCredentialId value must not be blank");
        }
        return new ApiCredentialId(value);
    }

    public static ApiCredentialId generate() {
        return new ApiCredentialId(UUID.randomUUID().toString());
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
        ApiCredentialId that = (ApiCredentialId) o;
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
