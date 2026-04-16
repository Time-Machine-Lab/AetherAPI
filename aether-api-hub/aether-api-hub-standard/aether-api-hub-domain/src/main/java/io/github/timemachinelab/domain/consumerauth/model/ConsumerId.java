package io.github.timemachinelab.domain.consumerauth.model;

import java.util.Objects;
import java.util.UUID;

/**
 * Consumer 唯一标识。
 */
public final class ConsumerId {

    private final String value;

    private ConsumerId(String value) {
        this.value = value;
    }

    public static ConsumerId of(String value) {
        Objects.requireNonNull(value, "ConsumerId value must not be null");
        if (value.isBlank()) {
            throw new IllegalArgumentException("ConsumerId value must not be blank");
        }
        return new ConsumerId(value);
    }

    public static ConsumerId generate() {
        return new ConsumerId(UUID.randomUUID().toString());
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
        ConsumerId that = (ConsumerId) o;
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
