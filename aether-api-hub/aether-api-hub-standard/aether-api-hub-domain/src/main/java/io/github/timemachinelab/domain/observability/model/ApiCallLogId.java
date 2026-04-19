package io.github.timemachinelab.domain.observability.model;

import java.util.Objects;
import java.util.UUID;

/**
 * API call log aggregate identifier.
 */
public final class ApiCallLogId {

    private final String value;

    private ApiCallLogId(String value) {
        this.value = value;
    }

    public static ApiCallLogId of(String value) {
        Objects.requireNonNull(value, "ApiCallLogId value must not be null");
        String trimmed = value.trim();
        if (trimmed.isEmpty()) {
            throw new IllegalArgumentException("ApiCallLogId value must not be blank");
        }
        return new ApiCallLogId(trimmed);
    }

    public static ApiCallLogId generate() {
        return new ApiCallLogId(UUID.randomUUID().toString());
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
        ApiCallLogId that = (ApiCallLogId) o;
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
