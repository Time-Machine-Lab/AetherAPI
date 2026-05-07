package io.github.timemachinelab.service.model;

/**
 * Resolved platform proxy profile snapshot for Unified Access forwarding.
 */
public class ProxyProfileSnapshotModel {

    private final String profileId;
    private final String profileCode;
    private final String proxyType;
    private final String proxyHost;
    private final int proxyPort;
    private final String username;
    private final String passwordSecret;
    private final long version;

    public ProxyProfileSnapshotModel(
            String profileId,
            String profileCode,
            String proxyType,
            String proxyHost,
            int proxyPort,
            String username,
            String passwordSecret,
            long version) {
        this.profileId = profileId;
        this.profileCode = profileCode;
        this.proxyType = proxyType;
        this.proxyHost = proxyHost;
        this.proxyPort = proxyPort;
        this.username = username;
        this.passwordSecret = passwordSecret;
        this.version = version;
    }

    public String getProfileId() { return profileId; }
    public String getProfileCode() { return profileCode; }
    public String getProxyType() { return proxyType; }
    public String getProxyHost() { return proxyHost; }
    public int getProxyPort() { return proxyPort; }
    public String getUsername() { return username; }
    public String getPasswordSecret() { return passwordSecret; }
    public long getVersion() { return version; }
}
