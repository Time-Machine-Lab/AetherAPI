package io.github.timemachinelab.infrastructure.platformproxy.persistence.converter;

import io.github.timemachinelab.domain.platformproxy.model.PlatformProxyProfileAggregate;
import io.github.timemachinelab.domain.platformproxy.model.PlatformProxyProfileId;
import io.github.timemachinelab.domain.platformproxy.model.ProxyType;
import io.github.timemachinelab.infrastructure.platformproxy.persistence.entity.PlatformProxyProfileDo;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

/**
 * Platform proxy profile converter.
 */
public final class PlatformProxyProfileConverter {

    private PlatformProxyProfileConverter() {
    }

    public static PlatformProxyProfileAggregate toAggregate(PlatformProxyProfileDo source) {
        if (source == null) {
            return null;
        }
        return PlatformProxyProfileAggregate.reconstitute(
                PlatformProxyProfileId.of(source.getId()),
                source.getProfileCode(),
                source.getProfileName(),
                ProxyType.valueOf(source.getProxyType()),
                source.getProxyHost(),
                source.getProxyPort() == null ? 0 : source.getProxyPort(),
                source.getUsername(),
                source.getPasswordSecret(),
                Boolean.TRUE.equals(source.getEnabled()),
                toInstant(source.getCreatedAt()),
                toInstant(source.getUpdatedAt()),
                Boolean.TRUE.equals(source.getIsDeleted()),
                source.getVersion() == null ? 0L : source.getVersion()
        );
    }

    public static PlatformProxyProfileDo toDo(PlatformProxyProfileAggregate source) {
        if (source == null) {
            return null;
        }
        PlatformProxyProfileDo target = new PlatformProxyProfileDo();
        updateDo(target, source);
        return target;
    }

    public static void updateDo(PlatformProxyProfileDo target, PlatformProxyProfileAggregate source) {
        target.setId(source.getId().getValue());
        target.setProfileCode(source.getProfileCode());
        target.setProfileName(source.getProfileName());
        target.setProxyType(source.getProxyType().name());
        target.setProxyHost(source.getProxyHost());
        target.setProxyPort(source.getProxyPort());
        target.setUsername(source.getUsername());
        target.setPasswordSecret(source.getPasswordSecret());
        target.setEnabled(source.isEnabled());
        target.setCreatedAt(toLocalDateTime(source.getCreatedAt()));
        target.setUpdatedAt(toLocalDateTime(source.getUpdatedAt()));
        target.setIsDeleted(source.isDeleted());
        target.setVersion(source.getVersion());
    }

    private static Instant toInstant(LocalDateTime value) {
        return value == null ? null : value.toInstant(ZoneOffset.UTC);
    }

    private static LocalDateTime toLocalDateTime(Instant value) {
        return value == null ? null : LocalDateTime.ofInstant(value, ZoneOffset.UTC);
    }
}
