package io.github.timemachinelab.service.model;

/**
 * Console sign-in command.
 */
public class ConsoleSignInCommand {

    private final String loginName;
    private final String password;

    public ConsoleSignInCommand(String loginName, String password) {
        this.loginName = loginName;
        this.password = password;
    }

    public String getLoginName() {
        return loginName;
    }

    public String getPassword() {
        return password;
    }
}
