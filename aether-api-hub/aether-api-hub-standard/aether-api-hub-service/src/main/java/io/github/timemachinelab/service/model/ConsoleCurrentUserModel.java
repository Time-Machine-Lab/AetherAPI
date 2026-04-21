package io.github.timemachinelab.service.model;

/**
 * Console current user model.
 */
public class ConsoleCurrentUserModel {

    private final String userId;
    private final String loginName;
    private final String displayName;
    private final String email;
    private final String role;

    public ConsoleCurrentUserModel(String userId, String loginName, String displayName, String email, String role) {
        this.userId = userId;
        this.loginName = loginName;
        this.displayName = displayName;
        this.email = email;
        this.role = role;
    }

    public String getUserId() {
        return userId;
    }

    public String getLoginName() {
        return loginName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getEmail() {
        return email;
    }

    public String getRole() {
        return role;
    }
}
