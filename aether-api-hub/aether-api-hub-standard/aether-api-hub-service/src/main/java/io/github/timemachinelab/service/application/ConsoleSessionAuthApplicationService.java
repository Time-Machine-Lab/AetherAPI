package io.github.timemachinelab.service.application;

import io.github.timemachinelab.domain.consolesessionauth.model.ConsoleSessionAuthDomainException;
import io.github.timemachinelab.service.model.ConsoleCurrentUserModel;
import io.github.timemachinelab.service.model.ConsoleSessionModel;
import io.github.timemachinelab.service.model.ConsoleSignInCommand;
import io.github.timemachinelab.service.port.in.ConsoleSessionAuthUseCase;
import io.github.timemachinelab.service.port.out.ConsoleSessionSettingsPort;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Base64;

/**
 * Console session auth application service.
 */
public class ConsoleSessionAuthApplicationService implements ConsoleSessionAuthUseCase {

    private static final String TOKEN_VERSION = "cs1";
    private static final String TOKEN_TYPE = "Bearer";
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ISO_INSTANT;

    private final ConsoleSessionSettingsPort settingsPort;

    public ConsoleSessionAuthApplicationService(ConsoleSessionSettingsPort settingsPort) {
        this.settingsPort = settingsPort;
    }

    @Override
    public ConsoleSessionModel signIn(ConsoleSignInCommand command) {
        String loginName = normalize(command.getLoginName(), "Login name");
        String password = normalize(command.getPassword(), "Password");
        if (!matchesSecret(loginName, normalize(settingsPort.getLoginName(), "Configured login name"))
                || !matchesSecret(password, normalize(settingsPort.getPassword(), "Configured password"))) {
            throw new ConsoleSessionAuthDomainException("Invalid console sign-in credentials");
        }

        long ttlSeconds = resolveTokenTtlSeconds();
        Instant expiresAt = Instant.now().plusSeconds(ttlSeconds);
        String payload = TOKEN_VERSION + "|" + normalize(settingsPort.getUserId(), "Configured user id") + "|" + expiresAt.getEpochSecond();
        String token = encodePayload(payload) + "." + sign(payload);
        return new ConsoleSessionModel(
                token,
                TOKEN_TYPE,
                formatInstant(expiresAt),
                ttlSeconds,
                currentUser()
        );
    }

    @Override
    public ConsoleCurrentUserModel authenticate(String bearerToken) {
        String token = normalizeBearerToken(bearerToken);
        int separatorIndex = token.indexOf('.');
        if (separatorIndex <= 0 || separatorIndex == token.length() - 1) {
            throw unauthorized();
        }

        String encodedPayload = token.substring(0, separatorIndex);
        String signature = token.substring(separatorIndex + 1);
        String payload = decodePayload(encodedPayload);
        if (!matchesSecret(signature, sign(payload))) {
            throw unauthorized();
        }

        String[] parts = payload.split("\\|", -1);
        if (parts.length != 3 || !TOKEN_VERSION.equals(parts[0])) {
            throw unauthorized();
        }

        String configuredUserId = normalize(settingsPort.getUserId(), "Configured user id");
        if (!configuredUserId.equals(parts[1])) {
            throw unauthorized();
        }

        long expiresAtEpochSecond;
        try {
            expiresAtEpochSecond = Long.parseLong(parts[2]);
        } catch (NumberFormatException ex) {
            throw unauthorized();
        }
        if (Instant.now().isAfter(Instant.ofEpochSecond(expiresAtEpochSecond))) {
            throw unauthorized();
        }
        return currentUser();
    }

    private ConsoleCurrentUserModel currentUser() {
        return new ConsoleCurrentUserModel(
                normalize(settingsPort.getUserId(), "Configured user id"),
                normalize(settingsPort.getLoginName(), "Configured login name"),
                normalize(settingsPort.getDisplayName(), "Configured display name"),
                normalize(settingsPort.getEmail(), "Configured email"),
                normalize(settingsPort.getRole(), "Configured role")
        );
    }

    private long resolveTokenTtlSeconds() {
        long ttlSeconds = settingsPort.getTokenTtlSeconds();
        if (ttlSeconds <= 0) {
            throw new IllegalStateException("Console session token TTL must be greater than 0");
        }
        return ttlSeconds;
    }

    private boolean matchesSecret(String left, String right) {
        return MessageDigest.isEqual(left.getBytes(StandardCharsets.UTF_8), right.getBytes(StandardCharsets.UTF_8));
    }

    private String encodePayload(String payload) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(payload.getBytes(StandardCharsets.UTF_8));
    }

    private String decodePayload(String encodedPayload) {
        try {
            return new String(Base64.getUrlDecoder().decode(encodedPayload), StandardCharsets.UTF_8);
        } catch (IllegalArgumentException ex) {
            throw unauthorized();
        }
    }

    private String sign(String payload) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(
                    normalize(settingsPort.getTokenSecret(), "Configured token secret").getBytes(StandardCharsets.UTF_8),
                    "HmacSHA256"
            ));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(mac.doFinal(payload.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception ex) {
            throw new ConsoleSessionAuthDomainException("Console session token signing failed", ex);
        }
    }

    private String formatInstant(Instant instant) {
        return TIME_FORMATTER.withZone(ZoneOffset.UTC).format(instant);
    }

    private String normalize(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalStateException(fieldName + " must not be blank");
        }
        return value.trim();
    }

    private String normalizeBearerToken(String value) {
        if (value == null || value.isBlank()) {
            throw unauthorized();
        }
        return value.trim();
    }

    private ConsoleSessionAuthDomainException unauthorized() {
        return new ConsoleSessionAuthDomainException("Console session authentication required");
    }
}
