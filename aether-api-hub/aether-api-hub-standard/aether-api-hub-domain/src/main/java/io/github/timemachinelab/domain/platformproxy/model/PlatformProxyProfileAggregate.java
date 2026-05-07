package io.github.timemachinelab.domain.platformproxy.model;

import java.time.Instant;
import java.util.Objects;

/**
 * Platform-managed outbound proxy profile aggregate root.
 */
public class PlatformProxyProfileAggregate {

    private PlatformProxyProfileId id;
    private String profileCode;
    private String profileName;
    private ProxyType proxyType;
    private String proxyHost;
    private int proxyPort;
    private String username;
    private String passwordSecret;
    private boolean enabled;
    private Instant createdAt;
    private Instant updatedAt;
    private boolean deleted;
    private long version;

    protected PlatformProxyProfileAggregate() {
    }

    private PlatformProxyProfileAggregate(
            PlatformProxyProfileId id,
            String profileCode,
            String profileName,
            ProxyType proxyType,
            String proxyHost,
            int proxyPort,
            String username,
            String passwordSecret,
            boolean enabled,
            Instant createdAt,
            Instant updatedAt,
            boolean deleted,
            long version) {
        this.id = Objects.requireNonNull(id, "Proxy profile id must not be null");
        this.profileCode = requireText(profileCode, "Proxy profile code must not be blank");
        this.profileName = requireText(profileName, "Proxy profile name must not be blank");
        this.proxyType = Objects.requireNonNull(proxyType, "Proxy type must not be null");
        this.proxyHost = requireText(proxyHost, "Proxy host must not be blank");
        this.proxyPort = requirePort(proxyPort);
        this.username = normalizeOptional(username);
        this.passwordSecret = normalizeOptional(passwordSecret);
        this.enabled = enabled;
        this.createdAt = Objects.requireNonNull(createdAt, "Created time must not be null");
        this.updatedAt = Objects.requireNonNull(updatedAt, "Updated time must not be null");
        this.deleted = deleted;
        this.version = version;
    }

    public static PlatformProxyProfileAggregate create(
            PlatformProxyProfileId id,
            String profileCode,
            String profileName,
            ProxyType proxyType,
            String proxyHost,
            int proxyPort,
            String username,
            String passwordSecret,
            boolean enabled) {
        Instant now = Instant.now();
        return new PlatformProxyProfileAggregate(
                id,
                profileCode,
                profileName,
                proxyType,
                proxyHost,
                proxyPort,
                username,
                passwordSecret,
                enabled,
                now,
                now,
                false,
                0L
        );
    }

    public static PlatformProxyProfileAggregate reconstitute(
            PlatformProxyProfileId id,
            String profileCode,
            String profileName,
            ProxyType proxyType,
            String proxyHost,
            int proxyPort,
            String username,
            String passwordSecret,
            boolean enabled,
            Instant createdAt,
            Instant updatedAt,
            boolean deleted,
            long version) {
        return new PlatformProxyProfileAggregate(
                id,
                profileCode,
                profileName,
                proxyType,
                proxyHost,
                proxyPort,
                username,
                passwordSecret,
                enabled,
                createdAt,
                updatedAt,
                deleted,
                version
        );
    }

    public void revise(
            String newProfileName,
            ProxyType newProxyType,
            String newProxyHost,
            int newProxyPort,
            String newUsername,
            String newPasswordSecret,
            boolean newEnabled) {
        ensureNotDeleted();
        this.profileName = requireText(newProfileName, "Proxy profile name must not be blank");
        this.proxyType = Objects.requireNonNull(newProxyType, "Proxy type must not be null");
        this.proxyHost = requireText(newProxyHost, "Proxy host must not be blank");
        this.proxyPort = requirePort(newProxyPort);
        this.username = normalizeOptional(newUsername);
        this.passwordSecret = normalizeOptional(newPasswordSecret);
        this.enabled = newEnabled;
        touch();
    }

    public void enable() {
        ensureNotDeleted();
        if (!enabled) {
            enabled = true;
            touch();
        }
    }

    public void disable() {
        ensureNotDeleted();
        if (enabled) {
            enabled = false;
            touch();
        }
    }

    public void softDelete() {
        ensureNotDeleted();
        deleted = true;
        enabled = false;
        touch();
    }

    public boolean canBeBound() {
        return !deleted && enabled;
    }

    public boolean hasCredential() {
        return passwordSecret != null;
    }

    private void ensureNotDeleted() {
        if (deleted) {
            throw new PlatformProxyProfileDomainException("Proxy profile has been deleted");
        }
    }

    private void touch() {
        updatedAt = Instant.now();
        version++;
    }

    private static String requireText(String value, String message) {
        String normalized = normalizeOptional(value);
        if (normalized == null) {
            throw new IllegalArgumentException(message);
        }
        return normalized;
    }

    private static int requirePort(int value) {
        if (value < 1 || value > 65535) {
            throw new IllegalArgumentException("Proxy port must be between 1 and 65535");
        }
        return value;
    }

    private static String normalizeOptional(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    public PlatformProxyProfileId getId() {
        return id;
    }

    public String getProfileCode() {
        return profileCode;
    }

    public String getProfileName() {
        return profileName;
    }

    public ProxyType getProxyType() {
        return proxyType;
    }

    public String getProxyHost() {
        return proxyHost;
    }

    public int getProxyPort() {
        return proxyPort;
    }

    public String getUsername() {
        return username;
    }

    public String getPasswordSecret() {
        return passwordSecret;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public long getVersion() {
        return version;
    }
}
