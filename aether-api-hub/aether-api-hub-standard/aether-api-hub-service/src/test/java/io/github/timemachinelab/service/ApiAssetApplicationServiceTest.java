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
import io.github.timemachinelab.service.model.RegisterApiAssetCommand;
import io.github.timemachinelab.service.model.ReviseApiAssetCommand;
import io.github.timemachinelab.service.port.out.ApiAssetRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
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
 * API 资产应用服务测试。
 */
@ExtendWith(MockitoExtension.class)
class ApiAssetApplicationServiceTest {

    @Mock
    private ApiAssetRepositoryPort apiAssetRepositoryPort;

    @Mock
    private CategoryValidityChecker categoryValidityChecker;

    private ApiAssetApplicationService service;

    @BeforeEach
    void setUp() {
        service = new ApiAssetApplicationService(apiAssetRepositoryPort, categoryValidityChecker);
    }

    @Nested
    @DisplayName("注册资产")
    class RegisterAssetTests {

        @Test
        @DisplayName("API Code 重复时应拒绝注册")
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
        @DisplayName("应成功注册草稿资产")
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
    @DisplayName("资产生命周期")
    class AssetLifecycleTests {

        @Test
        @DisplayName("应支持草稿注册、修订、启用和停用闭环")
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
        @DisplayName("应支持按契约合并部分修订字段")
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
        @DisplayName("启用配置不完整资产时应返回业务异常")
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
        @DisplayName("修订不存在资产时应返回业务异常")
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
        return new ApiAssetApplicationService(new InMemoryApiAssetRepository(), categoryValidityChecker);
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
}
