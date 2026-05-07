package io.github.timemachinelab.service.application;

import io.github.timemachinelab.domain.catalog.model.ApiAssetAggregate;
import io.github.timemachinelab.domain.catalog.model.ApiCode;
import io.github.timemachinelab.domain.platformproxy.model.PlatformProxyProfileAggregate;
import io.github.timemachinelab.domain.platformproxy.model.PlatformProxyProfileDomainException;
import io.github.timemachinelab.domain.platformproxy.model.PlatformProxyProfileId;
import io.github.timemachinelab.domain.platformproxy.model.ProxyType;
import io.github.timemachinelab.service.model.AssetProxyBindingModel;
import io.github.timemachinelab.service.model.BindProxyProfileCommand;
import io.github.timemachinelab.service.model.CreatePlatformProxyProfileCommand;
import io.github.timemachinelab.service.model.GetPlatformProxyProfileQuery;
import io.github.timemachinelab.service.model.ListPlatformProxyProfileQuery;
import io.github.timemachinelab.service.model.PlatformProxyProfileModel;
import io.github.timemachinelab.service.model.PlatformProxyProfilePageResult;
import io.github.timemachinelab.service.model.PlatformProxyProfileStateCommand;
import io.github.timemachinelab.service.model.UnbindProxyProfileCommand;
import io.github.timemachinelab.service.model.UpdatePlatformProxyProfileCommand;
import io.github.timemachinelab.service.port.in.PlatformProxyProfileUseCase;
import io.github.timemachinelab.service.port.out.ApiAssetRepositoryPort;
import io.github.timemachinelab.service.port.out.PlatformProxyProfileRepositoryPort;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

/**
 * Platform proxy profile application service.
 */
public class PlatformProxyProfileApplicationService implements PlatformProxyProfileUseCase {

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ISO_INSTANT;

    private final PlatformProxyProfileRepositoryPort proxyProfileRepositoryPort;
    private final ApiAssetRepositoryPort apiAssetRepositoryPort;

    public PlatformProxyProfileApplicationService(
            PlatformProxyProfileRepositoryPort proxyProfileRepositoryPort,
            ApiAssetRepositoryPort apiAssetRepositoryPort) {
        this.proxyProfileRepositoryPort = proxyProfileRepositoryPort;
        this.apiAssetRepositoryPort = apiAssetRepositoryPort;
    }

    @Override
    public PlatformProxyProfilePageResult listProfiles(ListPlatformProxyProfileQuery query) {
        requireAdmin(query.getActorRole());
        int page = Math.max(1, query.getPage());
        int size = Math.max(1, Math.min(query.getSize(), 100));
        List<PlatformProxyProfileModel> items = proxyProfileRepositoryPort
                .findPage(query.getEnabled(), normalizeOptional(query.getKeyword()), page, size)
                .stream()
                .map(this::toModel)
                .toList();
        return new PlatformProxyProfilePageResult(
                items,
                page,
                size,
                proxyProfileRepositoryPort.count(query.getEnabled(), normalizeOptional(query.getKeyword()))
        );
    }

    @Override
    public PlatformProxyProfileModel getProfile(GetPlatformProxyProfileQuery query) {
        requireAdmin(query.getActorRole());
        return toModel(loadProfile(query.getProfileId()));
    }

    @Override
    public PlatformProxyProfileModel createProfile(CreatePlatformProxyProfileCommand command) {
        requireAdmin(command.getActorRole());
        String profileCode = requireText(command.getProfileCode(), "Proxy profile code must not be blank");
        if (proxyProfileRepositoryPort.existsByCode(profileCode)) {
            throw new PlatformProxyProfileDomainException("Proxy profile code already exists: " + profileCode);
        }
        PlatformProxyProfileAggregate aggregate = PlatformProxyProfileAggregate.create(
                PlatformProxyProfileId.generate(),
                profileCode,
                command.getProfileName(),
                parseProxyType(command.getProxyType()),
                command.getProxyHost(),
                command.getProxyPort(),
                command.getUsername(),
                command.getPassword(),
                command.getEnabled() == null || command.getEnabled()
        );
        proxyProfileRepositoryPort.save(aggregate);
        return toModel(aggregate);
    }

    @Override
    public PlatformProxyProfileModel updateProfile(UpdatePlatformProxyProfileCommand command) {
        requireAdmin(command.getActorRole());
        PlatformProxyProfileAggregate aggregate = loadProfile(command.getProfileId());
        if (!aggregate.getProfileCode().equals(command.getProfileCode())) {
            throw new PlatformProxyProfileDomainException("Proxy profile code cannot be changed");
        }
        aggregate.revise(
                command.getProfileName(),
                parseProxyType(command.getProxyType()),
                command.getProxyHost(),
                command.getProxyPort(),
                command.getUsername(),
                command.getPassword(),
                command.getEnabled() == null || command.getEnabled()
        );
        proxyProfileRepositoryPort.save(aggregate);
        return toModel(aggregate);
    }

    @Override
    public PlatformProxyProfileModel enableProfile(PlatformProxyProfileStateCommand command) {
        requireAdmin(command.getActorRole());
        PlatformProxyProfileAggregate aggregate = loadProfile(command.getProfileId());
        aggregate.enable();
        proxyProfileRepositoryPort.save(aggregate);
        return toModel(aggregate);
    }

    @Override
    public PlatformProxyProfileModel disableProfile(PlatformProxyProfileStateCommand command) {
        requireAdmin(command.getActorRole());
        PlatformProxyProfileAggregate aggregate = loadProfile(command.getProfileId());
        aggregate.disable();
        proxyProfileRepositoryPort.save(aggregate);
        return toModel(aggregate);
    }

    @Override
    public PlatformProxyProfileModel deleteProfile(PlatformProxyProfileStateCommand command) {
        requireAdmin(command.getActorRole());
        PlatformProxyProfileAggregate aggregate = loadProfile(command.getProfileId());
        aggregate.softDelete();
        proxyProfileRepositoryPort.save(aggregate);
        return toModel(aggregate);
    }

    @Override
    public AssetProxyBindingModel bindProxyProfile(BindProxyProfileCommand command) {
        requireAdmin(command.getActorRole());
        ApiAssetAggregate asset = loadAsset(command.getApiCode());
        PlatformProxyProfileAggregate profile = loadProfile(command.getProfileId());
        if (!profile.canBeBound()) {
            throw new PlatformProxyProfileDomainException("Proxy profile is not enabled for binding");
        }
        asset.bindProxyProfile(profile.getId().getValue());
        apiAssetRepositoryPort.save(asset);
        return new AssetProxyBindingModel(
                asset.getCode().getValue(),
                profile.getId().getValue(),
                profile.getProfileCode(),
                profile.getProfileName()
        );
    }

    @Override
    public AssetProxyBindingModel unbindProxyProfile(UnbindProxyProfileCommand command) {
        requireAdmin(command.getActorRole());
        ApiAssetAggregate asset = loadAsset(command.getApiCode());
        asset.unbindProxyProfile();
        apiAssetRepositoryPort.save(asset);
        return new AssetProxyBindingModel(asset.getCode().getValue(), null, null, null);
    }

    private PlatformProxyProfileAggregate loadProfile(String profileId) {
        return proxyProfileRepositoryPort.findById(PlatformProxyProfileId.of(profileId))
                .filter(profile -> !profile.isDeleted())
                .orElseThrow(() -> new PlatformProxyProfileDomainException("Proxy profile not found: " + profileId));
    }

    private ApiAssetAggregate loadAsset(String apiCode) {
        return apiAssetRepositoryPort.findByCode(ApiCode.of(apiCode))
                .orElseThrow(() -> new PlatformProxyProfileDomainException("Asset not found: " + apiCode));
    }

    private PlatformProxyProfileModel toModel(PlatformProxyProfileAggregate aggregate) {
        return new PlatformProxyProfileModel(
                aggregate.getId().getValue(),
                aggregate.getProfileCode(),
                aggregate.getProfileName(),
                aggregate.getProxyType().name(),
                aggregate.getProxyHost(),
                aggregate.getProxyPort(),
                aggregate.getUsername(),
                aggregate.hasCredential(),
                aggregate.isEnabled(),
                aggregate.isDeleted(),
                formatInstant(aggregate.getCreatedAt()),
                formatInstant(aggregate.getUpdatedAt())
        );
    }

    private ProxyType parseProxyType(String value) {
        String normalized = requireText(value, "Proxy type must not be blank").toUpperCase(Locale.ROOT);
        try {
            return ProxyType.valueOf(normalized);
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Unsupported proxy type: " + value);
        }
    }

    private void requireAdmin(String actorRole) {
        String normalized = normalizeOptional(actorRole);
        if (normalized == null) {
            throw new PlatformProxyProfileDomainException("Platform administrator role is required");
        }
        String upper = normalized.toUpperCase(Locale.ROOT);
        if (!"OWNER".equals(upper) && !"ADMIN".equals(upper) && !"PLATFORM_ADMIN".equals(upper)) {
            throw new PlatformProxyProfileDomainException("Platform administrator role is required");
        }
    }

    private String requireText(String value, String message) {
        String normalized = normalizeOptional(value);
        if (normalized == null) {
            throw new IllegalArgumentException(message);
        }
        return normalized;
    }

    private String normalizeOptional(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private String formatInstant(Instant instant) {
        return instant == null ? null : TIME_FORMATTER.withZone(ZoneOffset.UTC).format(instant);
    }
}
