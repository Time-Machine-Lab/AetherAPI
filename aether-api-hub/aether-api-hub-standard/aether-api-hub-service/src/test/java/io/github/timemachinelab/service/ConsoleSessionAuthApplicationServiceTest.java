package io.github.timemachinelab.service;

import io.github.timemachinelab.domain.consolesessionauth.model.ConsoleSessionAuthDomainException;
import io.github.timemachinelab.service.application.ConsoleSessionAuthApplicationService;
import io.github.timemachinelab.service.model.ConsoleCurrentUserModel;
import io.github.timemachinelab.service.model.ConsoleSessionModel;
import io.github.timemachinelab.service.model.ConsoleSignInCommand;
import io.github.timemachinelab.service.port.out.ConsoleSessionSettingsPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ConsoleSessionAuthApplicationServiceTest {

    private final ConsoleSessionSettingsPort settingsPort = new FixedConsoleSessionSettingsPort();

    @Test
    @DisplayName("signIn should issue backend managed bearer token for valid credentials")
    void shouldIssueConsoleTokenForValidCredentials() {
        ConsoleSessionAuthApplicationService service = new ConsoleSessionAuthApplicationService(settingsPort);

        ConsoleSessionModel session = service.signIn(new ConsoleSignInCommand("console@aetherapi.local", "change-me-console-password"));

        assertNotNull(session.getAccessToken());
        assertTrue(session.getAccessToken().contains("."));
        assertEquals("Bearer", session.getTokenType());
        assertEquals(43200L, session.getExpiresInSeconds());
        assertEquals("console-operator", session.getCurrentUser().getUserId());
        assertEquals("OWNER", session.getCurrentUser().getRole());
    }

    @Test
    @DisplayName("signIn should reject invalid credentials")
    void shouldRejectInvalidCredentials() {
        ConsoleSessionAuthApplicationService service = new ConsoleSessionAuthApplicationService(settingsPort);

        ConsoleSessionAuthDomainException ex = assertThrows(ConsoleSessionAuthDomainException.class, () ->
                service.signIn(new ConsoleSignInCommand("console@aetherapi.local", "wrong-password")));

        assertEquals("Invalid console sign-in credentials", ex.getMessage());
    }

    @Test
    @DisplayName("authenticate should restore current user from valid token")
    void shouldAuthenticateConsoleToken() {
        ConsoleSessionAuthApplicationService service = new ConsoleSessionAuthApplicationService(settingsPort);
        ConsoleSessionModel session = service.signIn(new ConsoleSignInCommand("console@aetherapi.local", "change-me-console-password"));

        ConsoleCurrentUserModel currentUser = service.authenticate(session.getAccessToken());

        assertEquals("console-operator", currentUser.getUserId());
        assertEquals("console@aetherapi.local", currentUser.getLoginName());
        assertEquals("Aether Console Operator", currentUser.getDisplayName());
    }

    @Test
    @DisplayName("authenticate should reject tampered token")
    void shouldRejectTamperedToken() {
        ConsoleSessionAuthApplicationService service = new ConsoleSessionAuthApplicationService(settingsPort);
        ConsoleSessionModel session = service.signIn(new ConsoleSignInCommand("console@aetherapi.local", "change-me-console-password"));
        String tamperedToken = session.getAccessToken() + "broken";

        ConsoleSessionAuthDomainException ex = assertThrows(ConsoleSessionAuthDomainException.class, () ->
                service.authenticate(tamperedToken));

        assertEquals("Console session authentication required", ex.getMessage());
    }

    private static final class FixedConsoleSessionSettingsPort implements ConsoleSessionSettingsPort {

        @Override
        public String getUserId() {
            return "console-operator";
        }

        @Override
        public String getLoginName() {
            return "console@aetherapi.local";
        }

        @Override
        public String getPassword() {
            return "change-me-console-password";
        }

        @Override
        public String getDisplayName() {
            return "Aether Console Operator";
        }

        @Override
        public String getEmail() {
            return "console@aetherapi.local";
        }

        @Override
        public String getRole() {
            return "OWNER";
        }

        @Override
        public String getTokenSecret() {
            return "change-me-console-session-secret";
        }

        @Override
        public long getTokenTtlSeconds() {
            return 43200;
        }
    }
}
