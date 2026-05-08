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
import io.github.timemachinelab.service.model.ListPlatformProxyAssetCandidateQuery;
import io.github.timemachinelab.service.model.PlatformProxyAssetCandidateModel;
import io.github.timemachinelab.service.model.PlatformProxyAssetCandidatePageResult;
import io.github.timemachinelab.service.model.PlatformProxyProfileModel;
import io.github.timemachinelab.service.port.out.ApiAssetRepositoryPort;
import io.github.timemachinelab.service.port.out.PlatformProxyAssetCandidateQueryPort;
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
                new InMemoryApiAssetRepositoryPort(),
                new InMemoryPlatformProxyAssetCandidateQueryPort()
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
        PlatformProxyProfileApplicationService service = new PlatformProxyProfileApplicationService(
                profiles,
                assets,
                new InMemoryPlatformProxyAssetCandidateQueryPort()
        );

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
        PlatformProxyProfileApplicationService service = new PlatformProxyProfileApplicationService(
                profiles,
                assets,
                new InMemoryPlatformProxyAssetCandidateQueryPort()
        );

        AssetProxyBindingModel binding = service.bindProxyProfile(
                new BindProxyProfileCommand("OWNER", "weather-forecast", profile.getId().getValue()));

        assertEquals(profile.getId().getValue(), binding.getProxyProfileId());
        assertEquals(profile.getId().getValue(), assets.findByCode(ApiCode.of("weather-forecast")).orElseThrow().getProxyProfileId());
    }

    @Test
    @DisplayName("asset candidate search should require admin and normalize filters and pagination")
    void shouldSearchAssetBindingCandidatesWithAdminAndNormalizedFilters() {
        InMemoryPlatformProxyAssetCandidateQueryPort candidates = new InMemoryPlatformProxyAssetCandidateQueryPort();
        candidates.items.add(new PlatformProxyAssetCandidateModel(
                "weather-forecast",
                "Weather Forecast",
                "STANDARD_API",
                "PUBLISHED",
                "Owner",
                "profile-1",
                "default-cn",
                "Default CN",
                "2026-05-01T08:00:00Z",
                "2026-05-02T08:00:00Z"
        ));
        PlatformProxyProfileApplicationService service = new PlatformProxyProfileApplicationService(
                new InMemoryProxyProfileRepositoryPort(),
                new InMemoryApiAssetRepositoryPort(),
                candidates
        );

        assertThrows(PlatformProxyProfileDomainException.class, () -> service.listAssetBindingCandidates(
                new ListPlatformProxyAssetCandidateQuery("USER", "weather", "PUBLISHED", "profile-1", 1, 20)));

        PlatformProxyAssetCandidatePageResult result = service.listAssetBindingCandidates(
                new ListPlatformProxyAssetCandidateQuery("OWNER", " weather ", " PUBLISHED ", " profile-1 ", 0, 200));

        assertEquals(1, result.getItems().size());
        assertEquals(1, result.getPage());
        assertEquals(100, result.getSize());
        assertEquals("weather", candidates.lastKeyword);
        assertEquals("PUBLISHED", candidates.lastStatus);
        assertEquals("profile-1", candidates.lastBoundProfileId);
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

    private static final class InMemoryPlatformProxyAssetCandidateQueryPort
            implements PlatformProxyAssetCandidateQueryPort {

        private final List<PlatformProxyAssetCandidateModel> items = new java.util.ArrayList<>();
        private String lastKeyword;
        private String lastStatus;
        private String lastBoundProfileId;

        @Override
        public List<PlatformProxyAssetCandidateModel> findPage(
                String keyword,
                String status,
                String boundProfileId,
                int page,
                int size) {
            lastKeyword = keyword;
            lastStatus = status;
            lastBoundProfileId = boundProfileId;
            return items;
        }

        @Override
        public long count(String keyword, String status, String boundProfileId) {
            lastKeyword = keyword;
            lastStatus = status;
            lastBoundProfileId = boundProfileId;
            return items.size();
        }
    }
}
