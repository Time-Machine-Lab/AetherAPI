package io.github.timemachinelab.api.resp;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Console sign-in response.
 */
public class ConsoleSignInResp {

    @JsonProperty("accessToken")
    private String accessToken;

    @JsonProperty("tokenType")
    private String tokenType;

    @JsonProperty("expiresAt")
    private String expiresAt;

    @JsonProperty("expiresInSeconds")
    private long expiresInSeconds;

    @JsonProperty("currentUser")
    private ConsoleCurrentUserResp currentUser;

    public ConsoleSignInResp() {
    }

    public ConsoleSignInResp(
            String accessToken,
            String tokenType,
            String expiresAt,
            long expiresInSeconds,
            ConsoleCurrentUserResp currentUser) {
        this.accessToken = accessToken;
        this.tokenType = tokenType;
        this.expiresAt = expiresAt;
        this.expiresInSeconds = expiresInSeconds;
        this.currentUser = currentUser;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getTokenType() {
        return tokenType;
    }

    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }

    public String getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(String expiresAt) {
        this.expiresAt = expiresAt;
    }

    public long getExpiresInSeconds() {
        return expiresInSeconds;
    }

    public void setExpiresInSeconds(long expiresInSeconds) {
        this.expiresInSeconds = expiresInSeconds;
    }

    public ConsoleCurrentUserResp getCurrentUser() {
        return currentUser;
    }

    public void setCurrentUser(ConsoleCurrentUserResp currentUser) {
        this.currentUser = currentUser;
    }
}
