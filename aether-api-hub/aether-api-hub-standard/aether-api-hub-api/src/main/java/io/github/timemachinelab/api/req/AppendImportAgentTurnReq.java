package io.github.timemachinelab.api.req;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Append import agent turn request.
 */
public class AppendImportAgentTurnReq {

    @NotBlank
    @Size(max = 20000)
    private String message;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}