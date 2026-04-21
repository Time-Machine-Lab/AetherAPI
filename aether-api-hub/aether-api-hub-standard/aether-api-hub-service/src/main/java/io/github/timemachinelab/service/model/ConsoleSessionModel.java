package io.github.timemachinelab.service.model;

/**
 * Console session model.
 */
public class ConsoleSessionModel {

    private final String accessToken;
    private final String tokenType;
    private final String expiresAt;
    private final long expiresInSeconds;
    private final ConsoleCurrentUserModel currentUser;

    public ConsoleSessionModel(
            String accessToken,
            String tokenType,
            String expiresAt,
            long expiresInSeconds,
            ConsoleCurrentUserModel currentUser) {
        this.accessToken = accessToken;
        this.tokenType = tokenType;
        this.expiresAt = expiresAt;
        this.expiresInSeconds = expiresInSeconds;
        this.currentUser = currentUser;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public String getTokenType() {
        return tokenType;
    }

    public String getExpiresAt() {
        return expiresAt;
    }

    public long getExpiresInSeconds() {
        return expiresInSeconds;
    }

    public ConsoleCurrentUserModel getCurrentUser() {
        return currentUser;
    }
}
