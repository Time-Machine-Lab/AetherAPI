package io.github.timemachinelab.service.model;

import java.util.List;

/**
 * Append import agent turn command.
 */
public class AppendImportAgentTurnCommand {

    private final String ownerUserId;
    private final String sessionId;
    private final String message;
    private final List<ImportAgentClarificationAnswerModel> clarificationAnswers;

    public AppendImportAgentTurnCommand(String ownerUserId, String sessionId, String message) {
        this(ownerUserId, sessionId, message, List.of());
    }

    public AppendImportAgentTurnCommand(
            String ownerUserId,
            String sessionId,
            String message,
            List<ImportAgentClarificationAnswerModel> clarificationAnswers) {
        this.ownerUserId = ownerUserId;
        this.sessionId = sessionId;
        this.message = message;
        this.clarificationAnswers = clarificationAnswers == null ? List.of() : List.copyOf(clarificationAnswers);
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

    public List<ImportAgentClarificationAnswerModel> getClarificationAnswers() {
        return clarificationAnswers;
    }
}
