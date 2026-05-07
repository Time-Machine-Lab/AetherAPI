package io.github.timemachinelab.service.model;

/**
 * Query for platform proxy profile detail.
 */
public class GetPlatformProxyProfileQuery {

    private final String actorRole;
    private final String profileId;

    public GetPlatformProxyProfileQuery(String actorRole, String profileId) {
        this.actorRole = actorRole;
        this.profileId = profileId;
    }

    public String getActorRole() { return actorRole; }
    public String getProfileId() { return profileId; }
}
