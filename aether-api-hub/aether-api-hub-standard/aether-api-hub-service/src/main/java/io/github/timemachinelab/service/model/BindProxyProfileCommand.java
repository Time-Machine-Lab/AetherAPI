package io.github.timemachinelab.service.model;

/**
 * Command to bind a proxy profile to an API asset.
 */
public class BindProxyProfileCommand {

    private final String actorRole;
    private final String apiCode;
    private final String profileId;

    public BindProxyProfileCommand(String actorRole, String apiCode, String profileId) {
        this.actorRole = actorRole;
        this.apiCode = apiCode;
        this.profileId = profileId;
    }

    public String getActorRole() { return actorRole; }
    public String getApiCode() { return apiCode; }
    public String getProfileId() { return profileId; }
}
