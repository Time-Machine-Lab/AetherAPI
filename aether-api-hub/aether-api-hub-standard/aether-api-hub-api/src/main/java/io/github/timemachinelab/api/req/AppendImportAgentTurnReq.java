package io.github.timemachinelab.api.req;

import jakarta.validation.constraints.Size;
import jakarta.validation.Valid;
import java.util.List;

/**
 * Append import agent turn request.
 */
public class AppendImportAgentTurnReq {

    @Size(max = 64000)
    private String message;

    @Valid
    private List<ImportAgentClarificationAnswerReq> clarificationAnswers;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<ImportAgentClarificationAnswerReq> getClarificationAnswers() {
        return clarificationAnswers;
    }

    public void setClarificationAnswers(List<ImportAgentClarificationAnswerReq> clarificationAnswers) {
        this.clarificationAnswers = clarificationAnswers;
    }
}
