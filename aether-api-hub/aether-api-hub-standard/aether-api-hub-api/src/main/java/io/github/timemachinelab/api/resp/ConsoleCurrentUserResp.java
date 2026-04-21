package io.github.timemachinelab.api.resp;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Console current user response.
 */
public class ConsoleCurrentUserResp {

    @JsonProperty("userId")
    private String userId;

    @JsonProperty("loginName")
    private String loginName;

    @JsonProperty("displayName")
    private String displayName;

    @JsonProperty("email")
    private String email;

    @JsonProperty("role")
    private String role;

    public ConsoleCurrentUserResp() {
    }

    public ConsoleCurrentUserResp(String userId, String loginName, String displayName, String email, String role) {
        this.userId = userId;
        this.loginName = loginName;
        this.displayName = displayName;
        this.email = email;
        this.role = role;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getLoginName() {
        return loginName;
    }

    public void setLoginName(String loginName) {
        this.loginName = loginName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}
