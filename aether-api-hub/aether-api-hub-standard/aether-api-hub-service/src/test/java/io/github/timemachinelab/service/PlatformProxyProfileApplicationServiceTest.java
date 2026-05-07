package io.github.timemachinelab.service;

import io.github.timemachinelab.domain.catalog.model.ApiAssetAggregate;
import io.github.timemachinelab.domain.catalog.model.ApiCode;
import io.github.timemachinelab.domain.catalog.model.AssetId;
import io.github.timemachinelab.domain.catalog.model.AssetType;
import io.github.timemachinelab.domain.platformproxy.model.PlatformProxyProfileAggregate;
import io.github.timemachinelab.domain.platformproxy.model.PlatformProxyProfileDomainException;
import io.github.timemachinelab.domain.platformproxy.model.PlatformProxyProfileId;
import io.github.timemachinelab.domain.platformproxy.model.ProxyType;
import io.github.timemachinelab.service.application.PlatformProxyProfileApplicationService;
import io.github.timemachinelab.service.model.AssetProxyBindingModel;
import io.github.timemachinelab.service.model.BindProxyProfileCommand;
import io.github.timemachinelab.service.model.CreatePlatformProxyProfileCommand;
import io.github.timemachinelab.service.model.PlatformProxyProfileModel;
import io.github.timemachinelab.service.port.out.ApiAssetRepositoryPort;
import io.github.timemachinelab.service.port.out.PlatformProxyProfileRepositoryPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PlatformProxyProfileApplicationServiceTest {

    @Test
    @DisplayName("create should require administrator capable role and redact credential")
    void shouldRequireAdminAndRedactCredential() {
        InMemoryProxyProfileRepositoryPort profiles = new InMemoryProxyProfileRepositoryPort();
        PlatformProxyProfileApplicationService service = new PlatformProxyProfileApplicationService(
                profiles,
                new InMemoryApiAssetRepositoryPort()
        );

        CreatePlatformProxyProfileCommand command = createCommand("USER");
        assertThrows(PlatformProxyProfileDomainException.class, () -> service.createProfile(command));

        PlatformProxyProfileModel model = service.createProfile(createCommand("OWNER"));

        assertEquals("openai-egress", model.getProfileCode());
        assertTrue(model.isCredentialConfigured());
    }

    @Test
    @DisplayName("bind should reject disabled profiles and preserve asset binding")
    void shouldRejectDisabledProfileBinding() {
        InMemoryProxyProfileRepositoryPort profiles = new InMemoryProxyProfileRepositoryPort();
        PlatformProxyProfileAggregate disabled = PlatformProxyProfileAggregate.create(
                PlatformProxyProfileId.generate(),
                "disabled-egress",
                "Disabled Egress",
                ProxyType.HTTP,
                "127.0.0.1",
                7890,
                null,
                null,
                false
        );
        profiles.save(disabled);
        InMemoryApiAssetRepositoryPort assets = new InMemoryApiAssetRepositoryPort();
        assets.save(ApiAssetAggregate.registerDraft(
                AssetId.generate(),
                ApiCode.of("weather-forecast"),
                "owner-1",
                "Owner",
                AssetType.STANDARD_API,
                "Weather Forecast"
        ));
        PlatformProxyProfileApplicationService service = new PlatformProxyProfileApplicationService(profiles, assets);

        assertThrows(PlatformProxyProfileDomainException.class, () -> service.bindProxyProfile(
                new BindProxyProfileCommand("OWNER", "weather-forecast", disabled.getId().getValue())));
        assertEquals(null, assets.findByCode(ApiCode.of("weather-forecast")).orElseThrow().getProxyProfileId());
    }

    @Test
    @DisplayName("bind should assign enabled profile to asset")
    void shouldBindEnabledProfile() {
        InMemoryProxyProfileRepositoryPort profiles = new InMemoryProxyProfileRepositoryPort();
        PlatformProxyProfileAggregate profile = PlatformProxyProfileAggregate.create(
                PlatformProxyProfileId.generate(),
                "default-cn",
                "Default CN",
                ProxyType.HTTP,
                "127.0.0.1",
                7890,
                null,
                null,
                true
        );
        profiles.save(profile);
        InMemoryApiAssetRepositoryPort assets = new InMemoryApiAssetRepositoryPort();
        assets.save(ApiAssetAggregate.registerDraft(
                AssetId.generate(),
                ApiCode.of("weather-forecast"),
                "owner-1",
                "Owner",
                AssetType.STANDARD_API,
                "Weather Forecast"
        ));
        PlatformProxyProfileApplicationService service = new PlatformProxyProfileApplicationService(profiles, assets);

        AssetProxyBindingModel binding = service.bindProxyProfile(
                new BindProxyProfileCommand("OWNER", "weather-forecast", profile.getId().getValue()));

        assertEquals(profile.getId().getValue(), binding.getProxyProfileId());
        assertEquals(profile.getId().getValue(), assets.findByCode(ApiCode.of("weather-forecast")).orElseThrow().getProxyProfileId());
    }

    private CreatePlatformProxyProfileCommand createCommand(String role) {
        return new CreatePlatformProxyProfileCommand(
                role,
                "openai-egress",
                "OpenAI Egress",
                "HTTP",
                "127.0.0.1",
                7890,
                "proxy-user",
                "proxy-secret",
                true
        );
    }

    private static final class InMemoryProxyProfileRepositoryPort implements PlatformProxyProfileRepositoryPort {

        private final Map<String, PlatformProxyProfileAggregate> profiles = new HashMap<>();

        @Override
        public Optional<PlatformProxyProfileAggregate> findById(PlatformProxyProfileId id) {
            return Optional.ofNullable(profiles.get(id.getValue()));
        }

        @Override
        public Optional<PlatformProxyProfileAggregate> findByCode(String profileCode) {
            return profiles.values().stream().filter(profile -> profile.getProfileCode().equals(profileCode)).findFirst();
        }

        @Override
        public List<PlatformProxyProfileAggregate> findPage(Boolean enabled, String keyword, int page, int size) {
            return profiles.values().stream().toList();
        }

        @Override
        public long count(Boolean enabled, String keyword) {
            return profiles.size();
        }

        @Override
        public boolean existsByCode(String profileCode) {
            return findByCode(profileCode).isPresent();
        }

        @Override
        public void save(PlatformProxyProfileAggregate aggregate) {
            profiles.put(aggregate.getId().getValue(), aggregate);
        }
    }

    private static final class InMemoryApiAssetRepositoryPort implements ApiAssetRepositoryPort {

        private final Map<String, ApiAssetAggregate> assets = new HashMap<>();

        @Override
        public Optional<ApiAssetAggregate> findByCode(ApiCode code) {
            return Optional.ofNullable(assets.get(code.getValue()));
        }

        @Override
        public Optional<ApiAssetAggregate> findByCodeIncludingDeleted(ApiCode code) {
            return findByCode(code);
        }

        @Override
        public boolean existsByCode(ApiCode code) {
            return assets.containsKey(code.getValue());
        }

        @Override
        public void save(ApiAssetAggregate aggregate) {
            assets.put(aggregate.getCode().getValue(), aggregate);
        }
    }
}
