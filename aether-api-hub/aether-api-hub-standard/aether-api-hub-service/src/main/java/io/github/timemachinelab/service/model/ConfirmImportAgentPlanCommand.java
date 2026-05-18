package io.github.timemachinelab.service.model;

/**
 * Confirm import plan command.
 */
public class ConfirmImportAgentPlanCommand {

    private final String ownerUserId;
    private final String sessionId;
    private final int planVersion;

    public ConfirmImportAgentPlanCommand(String ownerUserId, String sessionId, int planVersion) {
        this.ownerUserId = ownerUserId;
        this.sessionId = sessionId;
        this.planVersion = planVersion;
    }

    public String getOwnerUserId() {
        return ownerUserId;
    }

    public String getSessionId() {
        return sessionId;
    }

    public int getPlanVersion() {
        return planVersion;
    }
}