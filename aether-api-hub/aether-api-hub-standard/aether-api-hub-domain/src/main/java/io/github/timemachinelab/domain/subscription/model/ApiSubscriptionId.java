package io.github.timemachinelab.domain.subscription.model;

import java.util.Objects;
import java.util.UUID;

/**
 * API subscription unique id.
 */
public final class ApiSubscriptionId {

    private final String value;

    private ApiSubscriptionId(String value) {
        this.value = value;
    }

    public static ApiSubscriptionId of(String value) {
        Objects.requireNonNull(value, "Subscription id must not be null");
        if (value.isBlank()) {
            throw new IllegalArgumentException("Subscription id must not be blank");
        }
        return new ApiSubscriptionId(value.trim());
    }

    public static ApiSubscriptionId generate() {
        return new ApiSubscriptionId(UUID.randomUUID().toString());
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
        ApiSubscriptionId that = (ApiSubscriptionId) o;
        return Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }
}
