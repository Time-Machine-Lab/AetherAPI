package io.github.timemachinelab.service.port.out;

/**
 * Console session auth settings port.
 */
public interface ConsoleSessionSettingsPort {

    String getUserId();

    String getLoginName();

    String getPassword();

    String getDisplayName();

    String getEmail();

    String getRole();

    String getTokenSecret();

    long getTokenTtlSeconds();
}
