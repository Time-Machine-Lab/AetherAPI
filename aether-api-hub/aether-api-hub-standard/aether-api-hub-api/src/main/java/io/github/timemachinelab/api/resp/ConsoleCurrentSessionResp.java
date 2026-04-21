package io.github.timemachinelab.api.resp;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Console current session response.
 */
public class ConsoleCurrentSessionResp {

    @JsonProperty("currentUser")
    private ConsoleCurrentUserResp currentUser;

    public ConsoleCurrentSessionResp() {
    }

    public ConsoleCurrentSessionResp(ConsoleCurrentUserResp currentUser) {
        this.currentUser = currentUser;
    }

    public ConsoleCurrentUserResp getCurrentUser() {
        return currentUser;
    }

    public void setCurrentUser(ConsoleCurrentUserResp currentUser) {
        this.currentUser = currentUser;
    }
}
