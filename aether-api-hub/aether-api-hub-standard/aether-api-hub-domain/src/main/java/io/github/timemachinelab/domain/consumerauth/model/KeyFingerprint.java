package io.github.timemachinelab.domain.consumerauth.model;

import java.util.Objects;

/**
 * API Key 指纹与掩码快照。
 */
public final class KeyFingerprint {

    private final String prefix;
    private final String maskedKey;
    private final String hashValue;

    private KeyFingerprint(String prefix, String maskedKey, String hashValue) {
        this.prefix = prefix;
        this.maskedKey = maskedKey;
        this.hashValue = hashValue;
    }

    public static KeyFingerprint of(String prefix, String maskedKey, String hashValue) {
        Objects.requireNonNull(prefix, "Key prefix must not be null");
        Objects.requireNonNull(maskedKey, "Masked key must not be null");
        Objects.requireNonNull(hashValue, "Fingerprint hash must not be null");
        if (prefix.isBlank() || maskedKey.isBlank() || hashValue.isBlank()) {
            throw new IllegalArgumentException("Key fingerprint fields must not be blank");
        }
        return new KeyFingerprint(prefix, maskedKey, hashValue);
    }

    public String getPrefix() {
        return prefix;
    }

    public String getMaskedKey() {
        return maskedKey;
    }

    public String getHashValue() {
        return hashValue;
    }
}
