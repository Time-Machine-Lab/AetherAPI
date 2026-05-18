package io.github.timemachinelab.service.model;

/**
 * Start import run command.
 */
public class StartImportAgentRunCommand {

    private final String ownerUserId;
    private final String publisherDisplayName;
    private final String sessionId;
    private final int planVersion;

    public StartImportAgentRunCommand(String ownerUserId, String publisherDisplayName, String sessionId, int planVersion) {
        this.ownerUserId = ownerUserId;
        this.publisherDisplayName = publisherDisplayName;
        this.sessionId = sessionId;
        this.planVersion = planVersion;
    }

    public String getOwnerUserId() {
        return ownerUserId;
    }

    public String getPublisherDisplayName() {
        return publisherDisplayName;
    }

    public String getSessionId() {
        return sessionId;
    }

    public int getPlanVersion() {
        return planVersion;
    }
}