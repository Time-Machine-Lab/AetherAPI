package io.github.timemachinelab.domain.consumerauth.model;

import java.time.Instant;

/**
 * 过期策略。
 */
public final class ExpirationPolicy {

    private final Instant expireAt;

    private ExpirationPolicy(Instant expireAt) {
        this.expireAt = expireAt;
    }

    public static ExpirationPolicy of(Instant expireAt) {
        return new ExpirationPolicy(expireAt);
    }

    public Instant getExpireAt() {
        return expireAt;
    }

    public boolean neverExpire() {
        return expireAt == null;
    }

    public boolean isExpired(Instant now) {
        return expireAt != null && !expireAt.isAfter(now);
    }
}
