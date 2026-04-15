package io.github.timemachinelab.domain.catalog.model;

import java.util.Objects;
import java.util.UUID;

/**
 * API 资产唯一标识。
 */
public final class AssetId {

    private final String value;

    private AssetId(String value) {
        this.value = value;
    }

    public static AssetId of(String value) {
        Objects.requireNonNull(value, "AssetId value must not be null");
        if (value.isBlank()) {
            throw new IllegalArgumentException("AssetId value must not be blank");
        }
        return new AssetId(value);
    }

    public static AssetId generate() {
        return new AssetId(UUID.randomUUID().toString());
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
        AssetId assetId = (AssetId) o;
        return Objects.equals(value, assetId.value);
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

