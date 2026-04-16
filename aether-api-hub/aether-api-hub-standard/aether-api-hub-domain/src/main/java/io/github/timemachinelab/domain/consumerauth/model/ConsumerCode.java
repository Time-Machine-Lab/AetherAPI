package io.github.timemachinelab.domain.consumerauth.model;

import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Consumer 业务编码。
 */
public final class ConsumerCode {

    private static final int MAX_LENGTH = 64;
    private static final Pattern VALID_PATTERN = Pattern.compile("^[a-z0-9][a-z0-9_-]*[a-z0-9]$|^[a-z0-9]$");

    private final String value;

    private ConsumerCode(String value) {
        this.value = value;
    }

    public static ConsumerCode of(String value) {
        Objects.requireNonNull(value, "ConsumerCode value must not be null");
        String trimmed = value.trim().toLowerCase();
        if (trimmed.isEmpty()) {
            throw new IllegalArgumentException("ConsumerCode value must not be blank");
        }
        if (trimmed.length() > MAX_LENGTH) {
            throw new IllegalArgumentException("ConsumerCode value must not exceed " + MAX_LENGTH + " characters");
        }
        if (!VALID_PATTERN.matcher(trimmed).matches()) {
            throw new IllegalArgumentException("ConsumerCode value contains invalid characters");
        }
        return new ConsumerCode(trimmed);
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
        ConsumerCode that = (ConsumerCode) o;
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
