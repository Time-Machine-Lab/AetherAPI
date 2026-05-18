package io.github.timemachinelab.service.model;

/**
 * Import agent turn model.
 */
public class ImportAgentTurnModel {

    private final String turnId;
    private final String sessionId;
    private final int turnIndex;
    private final ImportAgentActorType actorType;
    private final String message;
    private final Integer planVersion;
    private final String createdAt;

    public ImportAgentTurnModel(
            String turnId,
            String sessionId,
            int turnIndex,
            ImportAgentActorType actorType,
            String message,
            Integer planVersion,
            String createdAt) {
        this.turnId = turnId;
        this.sessionId = sessionId;
        this.turnIndex = turnIndex;
        this.actorType = actorType;
        this.message = message;
        this.planVersion = planVersion;
        this.createdAt = createdAt;
    }

    public String getTurnId() {
        return turnId;
    }

    public String getSessionId() {
        return sessionId;
    }

    public int getTurnIndex() {
        return turnIndex;
    }

    public ImportAgentActorType getActorType() {
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