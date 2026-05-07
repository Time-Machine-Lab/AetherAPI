package io.github.timemachinelab.service.model;

/**
 * Command to update a platform proxy profile.
 */
public class UpdatePlatformProxyProfileCommand extends CreatePlatformProxyProfileCommand {

    private final String profileId;

    public UpdatePlatformProxyProfileCommand(
            String actorRole,
            String profileId,
            String profileCode,
            String profileName,
            String proxyType,
            String proxyHost,
            int proxyPort,
            String username,
            String password,
            Boolean enabled) {
        super(actorRole, profileCode, profileName, proxyType, proxyHost, proxyPort, username, password, enabled);
        this.profileId = profileId;
    }

    public String getProfileId() { return profileId; }
}
