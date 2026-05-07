package io.github.timemachinelab.service.model;

/**
 * Command to unbind a proxy profile from an API asset.
 */
public class UnbindProxyProfileCommand {

    private final String actorRole;
    private final String apiCode;

    public UnbindProxyProfileCommand(String actorRole, String apiCode) {
        this.actorRole = actorRole;
        this.apiCode = apiCode;
    }

    public String getActorRole() { return actorRole; }
    public String getApiCode() { return apiCode; }
}
