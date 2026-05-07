package io.github.timemachinelab.service.model;

/**
 * Command for platform proxy profile state changes.
 */
public class PlatformProxyProfileStateCommand extends GetPlatformProxyProfileQuery {

    public PlatformProxyProfileStateCommand(String actorRole, String profileId) {
        super(actorRole, profileId);
    }
}
