package io.github.timemachinelab.service;

import io.github.timemachinelab.domain.catalog.model.ApiAssetAggregate;
import io.github.timemachinelab.domain.catalog.model.ApiCode;
import io.github.timemachinelab.domain.catalog.model.AssetDomainException;
import io.github.timemachinelab.domain.catalog.model.AssetType;
import io.github.timemachinelab.domain.catalog.model.CategoryValidityChecker;
import io.github.timemachinelab.service.application.ApiAssetApplicationService;
import io.github.timemachinelab.service.model.ApiAssetModel;
import io.github.timemachinelab.service.model.RegisterApiAssetCommand;
import io.github.timemachinelab.service.port.out.ApiAssetRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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
}
