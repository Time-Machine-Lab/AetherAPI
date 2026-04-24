package io.github.timemachinelab.service;

import io.github.timemachinelab.domain.catalog.model.ApiAssetAggregate;
import io.github.timemachinelab.domain.catalog.model.ApiCode;
import io.github.timemachinelab.domain.catalog.model.AssetDomainException;
import io.github.timemachinelab.domain.catalog.model.AssetType;
import io.github.timemachinelab.domain.catalog.model.AuthScheme;
import io.github.timemachinelab.domain.catalog.model.CategoryValidityChecker;
import io.github.timemachinelab.domain.catalog.model.RequestMethod;
import io.github.timemachinelab.service.application.ApiAssetApplicationService;
import io.github.timemachinelab.service.model.ApiAssetModel;
import io.github.timemachinelab.service.model.ApiAssetPageResult;
import io.github.timemachinelab.service.model.ApiAssetSummaryModel;
import io.github.timemachinelab.service.model.ListApiAssetQuery;
import io.github.timemachinelab.service.model.RegisterApiAssetCommand;
import io.github.timemachinelab.service.model.ReviseApiAssetCommand;
import io.github.timemachinelab.service.port.out.ApiAssetQueryPort;
import io.github.timemachinelab.service.port.out.ApiAssetRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * API asset application service tests.
 */
@ExtendWith(MockitoExtension.class)
class ApiAssetApplicationServiceTest {

    @Mock
    private ApiAssetRepositoryPort apiAssetRepositoryPort;

    @Mock
    private CategoryValidityChecker categoryValidityChecker;

    @Mock
    private ApiAssetQueryPort apiAssetQueryPort;

    private ApiAssetApplicationService service;

    @BeforeEach
    void setUp() {
        service = new ApiAssetApplicationService(apiAssetRepositoryPort, apiAssetQueryPort, categoryValidityChecker);
    }

    @Nested
    @DisplayName("asset list")
    class ListAssetTests {

        @Test
        @DisplayName("should normalize paging and filters for list query")
        void shouldNormalizePagingAndFiltersForListQuery() {
            when(apiAssetQueryPort.findPage("ENABLED", "tools", "chat", 1, 100)).thenReturn(List.of(
                    new ApiAssetSummaryModel(
                            "chat-completion",
                            "Chat Completion",
                            "AI_API",
                            "tools",
                            "Tools",
                            "ENABLED",
                            "2026-04-24T08:00:00Z")
            ));
            when(apiAssetQueryPort.count("ENABLED", "tools", "chat")).thenReturn(1L);

            ApiAssetPageResult result = service.listAssets(new ListApiAssetQuery(
                    " enabled ",
                    " Tools ",
                    "  chat  ",
                    0,
                    500
            ));

            assertEquals(1, result.getPage());
            assertEquals(100, result.getSize());
            assertEquals(1L, result.getTotal());
            assertEquals("chat-completion", result.getItems().get(0).getApiCode());
            verify(apiAssetQueryPort).findPage("ENABLED", "tools", "chat", 1, 100);
            verify(apiAssetQueryPort).count("ENABLED", "tools", "chat");
        }

        @Test
        @DisplayName("should reject invalid category code filter")
        void shouldRejectInvalidCategoryCodeFilter() {
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> service.listAssets(new ListApiAssetQuery(null, "tools--invalid", null, 1, 20))
            );

            assertTrue(exception.getMessage().contains("CategoryCode"));
        }
    }

    @Nested
    @DisplayName("register asset")
    class RegisterAssetTests {

        @Test
        @DisplayName("should reject duplicate api code")
        void shouldRejectDuplicateApiCode() {
            when(apiAssetRepositoryPort.existsByCode(any(ApiCode.class))).thenReturn(true);

            AssetDomainException exception = assertThrows(
                    AssetDomainException.class,
                    () -> service.registerAsset(new RegisterApiAssetCommand("weather-forecast", AssetType.STANDARD_API, null))
            );

            assertTrue(exception.getMessage().contains("already exists"));
            verify(apiAssetRepositoryPort, never()).save(any(ApiAssetAggregate.class));
        }

        @Test
        @DisplayName("should register draft asset")
        void shouldRegisterDraftAsset() {
            when(apiAssetRepositoryPort.existsByCode(any(ApiCode.class))).thenReturn(false);
            doNothing().when(apiAssetRepositoryPort).save(any(ApiAssetAggregate.class));

            ApiAssetModel result = service.registerAsset(
                    new RegisterApiAssetCommand("weather-forecast", AssetType.STANDARD_API, "天气预报"));

            assertEquals("weather-forecast", result.getApiCode());
            assertEquals("DRAFT", result.getStatus());
            verify(apiAssetRepositoryPort).save(any(ApiAssetAggregate.class));
        }
    }

    @Nested
    @DisplayName("asset lifecycle")
    class AssetLifecycleTests {

        @Test
        @DisplayName("should support register revise enable and disable flow")
        void shouldRegisterReviseEnableAndDisableAsset() {
            ApiAssetApplicationService lifecycleService = lifecycleService(categoryRef -> true);

            ApiAssetModel draft = lifecycleService.registerAsset(
                    new RegisterApiAssetCommand("weather-forecast", AssetType.STANDARD_API, null));
            assertEquals("DRAFT", draft.getStatus());

            ApiAssetModel revised = lifecycleService.reviseAsset(completeRevisionCommand("weather-forecast"));

            assertEquals("天气预报", revised.getAssetName());
            assertEquals("tools", revised.getCategoryCode());
            assertEquals("GET", revised.getRequestMethod());
            assertEquals("https://upstream.example.com/weather", revised.getUpstreamUrl());
            assertEquals("NONE", revised.getAuthScheme());
            assertEquals("DRAFT", revised.getStatus());

            ApiAssetModel enabled = lifecycleService.enableAsset("weather-forecast");
            assertEquals("ENABLED", enabled.getStatus());

            ApiAssetModel disabled = lifecycleService.disableAsset("weather-forecast");
            assertEquals("DISABLED", disabled.getStatus());
        }

        @Test
        @DisplayName("should merge partial revision fields")
        void shouldMergePartialRevisionFields() {
            ApiAssetApplicationService lifecycleService = lifecycleService(categoryRef -> true);
            lifecycleService.registerAsset(
                    new RegisterApiAssetCommand("weather-forecast", AssetType.STANDARD_API, "天气预报"));
            lifecycleService.reviseAsset(completeRevisionCommand("weather-forecast"));

            ApiAssetModel revised = lifecycleService.reviseAsset(new ReviseApiAssetCommand(
                    "weather-forecast",
                    "新版天气预报",
                    true,
                    null,
                    false,
                    null,
                    false,
                    null,
                    false,
                    null,
                    false,
                    null,
                    false,
                    null,
                    false,
                    null,
                    false,
                    "{\"city\":\"Beijing\"}",
                    true,
                    null,
                    false
            ));

            assertEquals("新版天气预报", revised.getAssetName());
            assertEquals("tools", revised.getCategoryCode());
            assertEquals("GET", revised.getRequestMethod());
            assertEquals("https://upstream.example.com/weather", revised.getUpstreamUrl());
            assertEquals("{\"city\":\"Beijing\"}", revised.getRequestExample());
            assertEquals("{\"temperature\":26}", revised.getResponseExample());
        }

        @Test
        @DisplayName("should reject enable when asset is incomplete")
        void shouldRejectEnableWhenAssetIsIncomplete() {
            ApiAssetApplicationService lifecycleService = lifecycleService(categoryRef -> true);
            lifecycleService.registerAsset(
                    new RegisterApiAssetCommand("weather-forecast", AssetType.STANDARD_API, "天气预报"));

            AssetDomainException exception = assertThrows(
                    AssetDomainException.class,
                    () -> lifecycleService.enableAsset("weather-forecast")
            );

            assertTrue(exception.getMessage().contains("Category code"));
        }

        @Test
        @DisplayName("should return business error when revision target is missing")
        void shouldReturnBusinessErrorWhenRevisionTargetMissing() {
            ApiAssetApplicationService lifecycleService = lifecycleService(categoryRef -> true);

            AssetDomainException exception = assertThrows(
                    AssetDomainException.class,
                    () -> lifecycleService.reviseAsset(completeRevisionCommand("missing-asset"))
            );

            assertTrue(exception.getMessage().contains("not found"));
        }
    }

    private ApiAssetApplicationService lifecycleService(CategoryValidityChecker categoryValidityChecker) {
        InMemoryApiAssetRepository repository = new InMemoryApiAssetRepository();
        return new ApiAssetApplicationService(repository, new InMemoryApiAssetQueryPort(repository), categoryValidityChecker);
    }

    private ReviseApiAssetCommand completeRevisionCommand(String apiCode) {
        return new ReviseApiAssetCommand(
                apiCode,
                "天气预报",
                true,
                AssetType.STANDARD_API,
                true,
                "tools",
                true,
                RequestMethod.GET,
                true,
                "https://upstream.example.com/weather",
                true,
                AuthScheme.NONE,
                true,
                null,
                false,
                "template",
                true,
                "{\"city\":\"Shanghai\"}",
                true,
                "{\"temperature\":26}",
                true
        );
    }

    private static final class InMemoryApiAssetRepository implements ApiAssetRepositoryPort {

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

    private static final class InMemoryApiAssetQueryPort implements ApiAssetQueryPort {

        private final InMemoryApiAssetRepository repository;

        private InMemoryApiAssetQueryPort(InMemoryApiAssetRepository repository) {
            this.repository = repository;
        }

        @Override
        public List<ApiAssetSummaryModel> findPage(String status, String categoryCode, String keyword, int page, int size) {
            return filtered(status, categoryCode, keyword).stream()
                    .skip((long) Math.max(0, page - 1) * size)
                    .limit(size)
                    .map(aggregate -> new ApiAssetSummaryModel(
                            aggregate.getCode().getValue(),
                            aggregate.getName(),
                            aggregate.getType().name(),
                            aggregate.getCategoryRef() == null ? null : aggregate.getCategoryRef().getCode(),
                            null,
                            aggregate.getStatus().name(),
                            aggregate.getUpdatedAt().toString()))
                    .toList();
        }

        @Override
        public long count(String status, String categoryCode, String keyword) {
            return filtered(status, categoryCode, keyword).size();
        }

        private List<ApiAssetAggregate> filtered(String status, String categoryCode, String keyword) {
            String normalizedKeyword = keyword == null ? null : keyword.toLowerCase();
            return repository.assets.values().stream()
                    .filter(asset -> status == null || asset.getStatus().name().equals(status))
                    .filter(asset -> categoryCode == null
                            || (asset.getCategoryRef() != null && categoryCode.equals(asset.getCategoryRef().getCode())))
                    .filter(asset -> normalizedKeyword == null
                            || asset.getCode().getValue().contains(normalizedKeyword)
                            || (asset.getName() != null && asset.getName().toLowerCase().contains(normalizedKeyword)))
                    .sorted((left, right) -> right.getUpdatedAt().compareTo(left.getUpdatedAt()))
                    .toList();
        }
    }
}
