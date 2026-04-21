package io.github.timemachinelab.adapter.web.auth;

import io.github.timemachinelab.service.model.ConsoleCurrentUserModel;

import java.security.Principal;

/**
 * Console session principal.
 */
public class ConsoleSessionPrincipal implements Principal {

    public static final String REQUEST_ATTRIBUTE =
            "io.github.timemachinelab.adapter.web.auth.ConsoleSessionPrincipal.REQUEST_ATTRIBUTE";

    private final String userId;
    private final String loginName;
    private final String displayName;
    private final String email;
    private final String role;

    public ConsoleSessionPrincipal(String userId, String loginName, String displayName, String email, String role) {
        this.userId = userId;
        this.loginName = loginName;
        this.displayName = displayName;
        this.email = email;
        this.role = role;
    }

    public static ConsoleSessionPrincipal from(ConsoleCurrentUserModel model) {
        return new ConsoleSessionPrincipal(
                model.getUserId(),
                model.getLoginName(),
                model.getDisplayName(),
                model.getEmail(),
                model.getRole()
        );
    }

    @Override
    public String getName() {
        return userId;
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
