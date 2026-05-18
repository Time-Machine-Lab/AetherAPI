package io.github.timemachinelab.service.model;

/**
 * Append import agent turn command.
 */
public class AppendImportAgentTurnCommand {

    private final String ownerUserId;
    private final String sessionId;
    private final String message;

    public AppendImportAgentTurnCommand(String ownerUserId, String sessionId, String message) {
        this.ownerUserId = ownerUserId;
        this.sessionId = sessionId;
        this.message = message;
    }

    public String getOwnerUserId() {
        return ownerUserId;
    }

    public String getSessionId() {
        return sessionId;
    }

    public String getMessage() {
        return message;
    }
}