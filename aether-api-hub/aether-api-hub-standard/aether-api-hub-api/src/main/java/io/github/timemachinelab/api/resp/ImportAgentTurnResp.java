package io.github.timemachinelab.api.resp;

/**
 * Import agent turn response.
 */
public class ImportAgentTurnResp {

    private final String turnId;
    private final String actorType;
    private final String message;
    private final Integer planVersion;
    private final String createdAt;

    public ImportAgentTurnResp(String turnId, String actorType, String message, Integer planVersion, String createdAt) {
        this.turnId = turnId;
        this.actorType = actorType;
        this.message = message;
        this.planVersion = planVersion;
        this.createdAt = createdAt;
    }

    public String getTurnId() {
        return turnId;
    }

    public String getActorType() {
        return actorType;
    }

    public String getMessage() {
        return message;
    }

    public Integer getPlanVersion() {
        return planVersion;
    }

    public String getCreatedAt() {
        return createdAt;
    }
}