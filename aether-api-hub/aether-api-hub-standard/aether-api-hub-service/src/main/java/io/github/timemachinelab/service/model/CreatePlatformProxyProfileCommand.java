package io.github.timemachinelab.service.model;

/**
 * Command to create a platform proxy profile.
 */
public class CreatePlatformProxyProfileCommand {

    private final String actorRole;
    private final String profileCode;
    private final String profileName;
    private final String proxyType;
    private final String proxyHost;
    private final int proxyPort;
    private final String username;
    private final String password;
    private final Boolean enabled;

    public CreatePlatformProxyProfileCommand(
            String actorRole,
            String profileCode,
            String profileName,
            String proxyType,
            String proxyHost,
            int proxyPort,
            String username,
            String password,
            Boolean enabled) {
        this.actorRole = actorRole;
        this.profileCode = profileCode;
        this.profileName = profileName;
        this.proxyType = proxyType;
        this.proxyHost = proxyHost;
        this.proxyPort = proxyPort;
        this.username = username;
        this.password = password;
        this.enabled = enabled;
    }

    public String getActorRole() { return actorRole; }
    public String getProfileCode() { return profileCode; }
    public String getProfileName() { return profileName; }
    public String getProxyType() { return proxyType; }
    public String getProxyHost() { return proxyHost; }
    public int getProxyPort() { return proxyPort; }
    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public Boolean getEnabled() { return enabled; }
}
