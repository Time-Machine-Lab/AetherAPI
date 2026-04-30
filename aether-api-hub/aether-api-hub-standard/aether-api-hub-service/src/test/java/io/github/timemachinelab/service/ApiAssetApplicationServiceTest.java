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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ApiAssetApplicationServiceTest {

    private static final String CURRENT_USER_ID = "user-1";
    private static final String PUBLISHER_DISPLAY_NAME = "alice";

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
        @DisplayName("should normalize paging and filters for current user list query")
        void shouldNormalizePagingAndFiltersForCurrentUserListQuery() {
            when(apiAssetQueryPort.findPage(CURRENT_USER_ID, "PUBLISHED", "tools", "chat", 1, 100)).thenReturn(List.of(
                    new ApiAssetSummaryModel(
                            "chat-completion",
                            "Chat Completion",
                            "AI_API",
                            "tools",
                            "Tools",
                            "PUBLISHED",
                            "alice",
                            "2026-04-24T08:00:00Z",
                            "2026-04-24T08:30:00Z")
            ));
            when(apiAssetQueryPort.count(CURRENT_USER_ID, "PUBLISHED", "tools", "chat")).thenReturn(1L);

            ApiAssetPageResult result = service.listAssets(new ListApiAssetQuery(
                    " user-1 ",
                    " published ",
                    " Tools ",
                    "  chat  ",
                    0,
                    500
            ));

            assertEquals(1, result.getPage());
            assertEquals(100, result.getSize());
            assertEquals(1L, result.getTotal());
            assertEquals("chat-completion", result.getItems().get(0).getApiCode());
            verify(apiAssetQueryPort).findPage(CURRENT_USER_ID, "PUBLISHED", "tools", "chat", 1, 100);
            verify(apiAssetQueryPort).count(CURRENT_USER_ID, "PUBLISHED", "tools", "chat");
        }

        @Test
        @DisplayName("should reject invalid category code filter")
        void shouldRejectInvalidCategoryCodeFilter() {
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> service.listAssets(new ListApiAssetQuery(CURRENT_USER_ID, null, "tools--invalid", null, 1, 20))
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
                    () -> service.registerAsset(new RegisterApiAssetCommand(
                            CURRENT_USER_ID,
                            PUBLISHER_DISPLAY_NAME,
                            "weather-forecast",
                            AssetType.STANDARD_API,
                            null))
            );

            assertTrue(exception.getMessage().contains("already exists"));
            verify(apiAssetRepositoryPort, never()).save(any(ApiAssetAggregate.class));
        }

        @Test
        @DisplayName("should register draft asset")
        void shouldRegisterDraftAsset() {
            when(apiAssetRepositoryPort.existsByCode(any(ApiCode.class))).thenReturn(false);

            ApiAssetModel result = service.registerAsset(new RegisterApiAssetCommand(
                    CURRENT_USER_ID,
                    PUBLISHER_DISPLAY_NAME,
                    "weather-forecast",
                    AssetType.STANDARD_API,
                    "Weather Forecast"
            ));

            assertEquals("weather-forecast", result.getApiCode());
            assertEquals("DRAFT", result.getStatus());
            assertEquals(PUBLISHER_DISPLAY_NAME, result.getPublisherDisplayName());
            verify(apiAssetRepositoryPort).save(any(ApiAssetAggregate.class));
        }
    }

    @Nested
    @DisplayName("asset lifecycle")
    class AssetLifecycleTests {

        @Test
        @DisplayName("should support register revise publish and unpublish flow")
        void shouldSupportRegisterRevisePublishAndUnpublishFlow() {
            ApiAssetApplicationService lifecycleService = lifecycleService(categoryRef -> true);

            ApiAssetModel draft = lifecycleService.registerAsset(new RegisterApiAssetCommand(
                    CURRENT_USER_ID,
                    PUBLISHER_DISPLAY_NAME,
                    "weather-forecast",
                    AssetType.STANDARD_API,
                    null
            ));
            assertEquals("DRAFT", draft.getStatus());

            ApiAssetModel revised = lifecycleService.reviseAsset(completeRevisionCommand("weather-forecast"));
            assertEquals("Weather Forecast", revised.getAssetName());
            assertEquals("tools", revised.getCategoryCode());
            assertEquals("GET", revised.getRequestMethod());
            assertEquals("https://upstream.example.com/weather", revised.getUpstreamUrl());
            assertEquals("NONE", revised.getAuthScheme());
            assertEquals("DRAFT", revised.getStatus());

            ApiAssetModel published = lifecycleService.publishAsset(CURRENT_USER_ID, PUBLISHER_DISPLAY_NAME, "weather-forecast");
            assertEquals("PUBLISHED", published.getStatus());

            ApiAssetModel unpublished = lifecycleService.unpublishAsset(CURRENT_USER_ID, "weather-forecast");
            assertEquals("UNPUBLISHED", unpublished.getStatus());
        }

        @Test
        @DisplayName("should merge partial revision fields and require republish after critical change")
        void shouldMergePartialRevisionFieldsAndRequireRepublishAfterCriticalChange() {
            ApiAssetApplicationService lifecycleService = lifecycleService(categoryRef -> true);
            lifecycleService.registerAsset(new RegisterApiAssetCommand(
                    CURRENT_USER_ID,
                    PUBLISHER_DISPLAY_NAME,
                    "weather-forecast",
                    AssetType.STANDARD_API,
                    "Weather Forecast"));
            lifecycleService.reviseAsset(completeRevisionCommand("weather-forecast"));
            lifecycleService.publishAsset(CURRENT_USER_ID, PUBLISHER_DISPLAY_NAME, "weather-forecast");

            ApiAssetModel revised = lifecycleService.reviseAsset(new ReviseApiAssetCommand(
                    CURRENT_USER_ID,
                    PUBLISHER_DISPLAY_NAME,
                    "weather-forecast",
                    "New Weather Forecast",
                    true,
                    null,
                    false,
                    null,
                    false,
                    null,
                    false,
                    "https://upstream.example.com/weather-v2",
                    true,
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

            assertEquals("New Weather Forecast", revised.getAssetName());
            assertEquals("tools", revised.getCategoryCode());
            assertEquals("GET", revised.getRequestMethod());
            assertEquals("https://upstream.example.com/weather-v2", revised.getUpstreamUrl());
            assertEquals("{\"city\":\"Beijing\"}", revised.getRequestExample());
            assertEquals("{\"temperature\":26}", revised.getResponseExample());
            assertEquals("UNPUBLISHED", revised.getStatus());
        }

        @Test
        @DisplayName("should reject publish when asset is incomplete")
        void shouldRejectPublishWhenAssetIsIncomplete() {
            ApiAssetApplicationService lifecycleService = lifecycleService(categoryRef -> true);
            lifecycleService.registerAsset(new RegisterApiAssetCommand(
                    CURRENT_USER_ID,
                    PUBLISHER_DISPLAY_NAME,
                    "weather-forecast",
                    AssetType.STANDARD_API,
                    "Weather Forecast"));

            AssetDomainException exception = assertThrows(
                    AssetDomainException.class,
                    () -> lifecycleService.publishAsset(CURRENT_USER_ID, PUBLISHER_DISPLAY_NAME, "weather-forecast")
            );

            assertTrue(exception.getMessage().contains("Category code"));
        }

        @Test
        @DisplayName("should enforce owner isolation")
        void shouldEnforceOwnerIsolation() {
            ApiAssetApplicationService lifecycleService = lifecycleService(categoryRef -> true);
            lifecycleService.registerAsset(new RegisterApiAssetCommand(
                    CURRENT_USER_ID,
                    PUBLISHER_DISPLAY_NAME,
                    "weather-forecast",
                    AssetType.STANDARD_API,
                    "Weather Forecast"));

            AssetDomainException exception = assertThrows(
                    AssetDomainException.class,
                    () -> lifecycleService.getAssetByCode("user-2", "weather-forecast")
            );

            assertTrue(exception.getMessage().contains("not found"));
        }

        @Test
        @DisplayName("should hide deleted assets from active owner reads after refresh")
        void shouldHideDeletedAssetsFromActiveOwnerReadsAfterRefresh() {
            ApiAssetApplicationService lifecycleService = lifecycleService(categoryRef -> true);
            lifecycleService.registerAsset(new RegisterApiAssetCommand(
                    CURRENT_USER_ID,
                    PUBLISHER_DISPLAY_NAME,
                    "weather-forecast",
                    AssetType.STANDARD_API,
                    "Weather Forecast"));
            lifecycleService.reviseAsset(completeRevisionCommand("weather-forecast"));
            lifecycleService.publishAsset(CURRENT_USER_ID, PUBLISHER_DISPLAY_NAME, "weather-forecast");

            ApiAssetModel deleted = lifecycleService.deleteAsset(CURRENT_USER_ID, "weather-forecast");

            assertTrue(deleted.isDeleted());
            assertEquals("PUBLISHED", deleted.getStatus());
            ApiAssetPageResult listAfterDelete = lifecycleService.listAssets(new ListApiAssetQuery(
                    CURRENT_USER_ID,
                    null,
                    null,
                    null,
                    1,
                    20
            ));
            assertEquals(0, listAfterDelete.getTotal());
            assertTrue(listAfterDelete.getItems().isEmpty());
            AssetDomainException detailException = assertThrows(
                    AssetDomainException.class,
                    () -> lifecycleService.getAssetByCode(CURRENT_USER_ID, "weather-forecast")
            );
            assertTrue(detailException.getMessage().contains("not found"));
        }
    }

    private ApiAssetApplicationService lifecycleService(CategoryValidityChecker validityChecker) {
        InMemoryApiAssetRepository repository = new InMemoryApiAssetRepository();
        return new ApiAssetApplicationService(repository, new InMemoryApiAssetQueryPort(repository), validityChecker);
    }

    private ReviseApiAssetCommand completeRevisionCommand(String apiCode) {
        return new ReviseApiAssetCommand(
                CURRENT_USER_ID,
                PUBLISHER_DISPLAY_NAME,
                apiCode,
                "Weather Forecast",
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
                true,
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
            ApiAssetAggregate aggregate = assets.get(code.getValue());
            if (aggregate == null || aggregate.isDeleted()) {
                return Optional.empty();
            }
            return Optional.of(aggregate);
        }

        @Override
        public Optional<ApiAssetAggregate> findByCodeIncludingDeleted(ApiCode code) {
            return Optional.ofNullable(assets.get(code.getValue()));
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
        public List<ApiAssetSummaryModel> findPage(
                String ownerUserId,
                String status,
                String categoryCode,
                String keyword,
                int page,
                int size) {
            return filtered(ownerUserId, status, categoryCode, keyword).stream()
                    .skip((long) Math.max(0, page - 1) * size)
                    .limit(size)
                    .map(aggregate -> new ApiAssetSummaryModel(
                            aggregate.getCode().getValue(),
                            aggregate.getName(),
                            aggregate.getType().name(),
                            aggregate.getCategoryRef() == null ? null : aggregate.getCategoryRef().getCode(),
                            null,
                            aggregate.getStatus().name(),
                            aggregate.getPublisherDisplayName(),
                            aggregate.getPublishedAt() == null ? null : aggregate.getPublishedAt().toString(),
                            aggregate.getUpdatedAt().toString()))
                    .toList();
        }

        @Override
        public long count(String ownerUserId, String status, String categoryCode, String keyword) {
            return filtered(ownerUserId, status, categoryCode, keyword).size();
        }

        private List<ApiAssetAggregate> filtered(String ownerUserId, String status, String categoryCode, String keyword) {
            String normalizedKeyword = keyword == null ? null : keyword.toLowerCase();
            return repository.assets.values().stream()
                    .filter(asset -> !asset.isDeleted())
                    .filter(asset -> ownerUserId.equals(asset.getOwnerUserId()))
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
