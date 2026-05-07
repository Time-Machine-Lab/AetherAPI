package io.github.timemachinelab.service.model;

/**
 * Platform proxy profile application model with redacted credential state.
 */
public class PlatformProxyProfileModel {

    private final String id;
    private final String profileCode;
    private final String profileName;
    private final String proxyType;
    private final String proxyHost;
    private final int proxyPort;
    private final String username;
    private final boolean credentialConfigured;
    private final boolean enabled;
    private final boolean deleted;
    private final String createdAt;
    private final String updatedAt;

    public PlatformProxyProfileModel(
            String id,
            String profileCode,
            String profileName,
            String proxyType,
            String proxyHost,
            int proxyPort,
            String username,
            boolean credentialConfigured,
            boolean enabled,
            boolean deleted,
            String createdAt,
            String updatedAt) {
        this.id = id;
        this.profileCode = profileCode;
        this.profileName = profileName;
        this.proxyType = proxyType;
        this.proxyHost = proxyHost;
        this.proxyPort = proxyPort;
        this.username = username;
        this.credentialConfigured = credentialConfigured;
        this.enabled = enabled;
        this.deleted = deleted;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public String getId() { return id; }
    public String getProfileCode() { return profileCode; }
    public String getProfileName() { return profileName; }
    public String getProxyType() { return proxyType; }
    public String getProxyHost() { return proxyHost; }
    public int getProxyPort() { return proxyPort; }
    public String getUsername() { return username; }
    public boolean isCredentialConfigured() { return credentialConfigured; }
    public boolean isEnabled() { return enabled; }
    public boolean isDeleted() { return deleted; }
    public String getCreatedAt() { return createdAt; }
    public String getUpdatedAt() { return updatedAt; }
}
