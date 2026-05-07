package io.github.timemachinelab.domain.platformproxy.model;

import java.util.Objects;
import java.util.UUID;

/**
 * Platform proxy profile identifier.
 */
public final class PlatformProxyProfileId {

    private final String value;

    private PlatformProxyProfileId(String value) {
        this.value = requireText(value);
    }

    public static PlatformProxyProfileId of(String value) {
        return new PlatformProxyProfileId(value);
    }

    public static PlatformProxyProfileId generate() {
        return new PlatformProxyProfileId(UUID.randomUUID().toString());
    }

    public String getValue() {
        return value;
    }

    private static String requireText(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("Proxy profile id must not be blank");
        }
        return value.trim();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        PlatformProxyProfileId that = (PlatformProxyProfileId) o;
        return Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }
}
