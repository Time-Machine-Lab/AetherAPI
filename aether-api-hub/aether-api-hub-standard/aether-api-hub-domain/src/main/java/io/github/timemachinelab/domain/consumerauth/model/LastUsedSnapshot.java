package io.github.timemachinelab.domain.consumerauth.model;

import java.time.Instant;

/**
 * 最近使用快照。
 */
public final class LastUsedSnapshot {

    private final Instant lastUsedAt;
    private final String lastUsedChannel;
    private final String lastUsedResult;

    private LastUsedSnapshot(Instant lastUsedAt, String lastUsedChannel, String lastUsedResult) {
        this.lastUsedAt = lastUsedAt;
        this.lastUsedChannel = lastUsedChannel;
        this.lastUsedResult = lastUsedResult;
    }

    public static LastUsedSnapshot empty() {
        return new LastUsedSnapshot(null, null, null);
    }

    public static LastUsedSnapshot of(Instant lastUsedAt, String lastUsedChannel, String lastUsedResult) {
        if (lastUsedAt == null && lastUsedChannel == null && lastUsedResult == null) {
            return empty();
        }
        return new LastUsedSnapshot(lastUsedAt, normalize(lastUsedChannel), normalize(lastUsedResult));
    }

    private static String normalize(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    public Instant getLastUsedAt() {
        return lastUsedAt;
    }

    public String getLastUsedChannel() {
        return lastUsedChannel;
    }

    public String getLastUsedResult() {
        return lastUsedResult;
    }
}
