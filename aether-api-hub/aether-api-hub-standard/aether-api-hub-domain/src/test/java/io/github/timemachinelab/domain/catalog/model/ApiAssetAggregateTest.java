package io.github.timemachinelab.domain.catalog.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * API 资产聚合测试。
 */
class ApiAssetAggregateTest {

    private static final CategoryValidityChecker VALID_CATEGORY_CHECKER = categoryRef -> true;
    private static final CategoryValidityChecker INVALID_CATEGORY_CHECKER = categoryRef -> false;

    @Nested
    @DisplayName("草稿修订")
    class DraftRevisionTests {

        @Test
        @DisplayName("应允许在无示例快照的情况下保存草稿")
        void shouldSaveDraftWithoutExamples() {
            ApiAssetAggregate aggregate = ApiAssetAggregate.registerDraft(
                    AssetId.generate(),
                    ApiCode.of("weather-forecast"),
                    AssetType.STANDARD_API,
                    null
            );

            aggregate.revise(
                    "天气预报",
                    AssetType.STANDARD_API,
                    CategoryRef.of("llm"),
                    UpstreamEndpointConfig.of(RequestMethod.POST, "https://example.com/api", AuthScheme.NONE, null),
                    "template",
                    null
            );

            assertEquals(AssetStatus.DRAFT, aggregate.getStatus());
            assertNull(aggregate.getExampleSnapshot());
            assertEquals("天气预报", aggregate.getName());
        }
    }

    @Nested
    @DisplayName("启用校验")
    class ActivationValidationTests {

        @Test
        @DisplayName("缺少必填配置时应拒绝启用")
        void shouldRejectEnableWhenConfigurationIncomplete() {
            ApiAssetAggregate aggregate = ApiAssetAggregate.registerDraft(
                    AssetId.generate(),
                    ApiCode.of("stock-price"),
                    AssetType.STANDARD_API,
                    "股票价格"
            );

            AssetDomainException exception = assertThrows(
                    AssetDomainException.class,
                    () -> aggregate.enable(VALID_CATEGORY_CHECKER)
            );

            assertTrue(exception.getMessage().contains("Category code"));
        }

        @Test
        @DisplayName("分类无效时应拒绝启用")
        void shouldRejectEnableWhenCategoryInvalid() {
            ApiAssetAggregate aggregate = configuredAggregate(AssetType.STANDARD_API);

            AssetDomainException exception = assertThrows(
                    AssetDomainException.class,
                    () -> aggregate.enable(INVALID_CATEGORY_CHECKER)
            );

            assertTrue(exception.getMessage().contains("category"));
        }

        @Test
        @DisplayName("AI 资产缺少能力档案时应拒绝启用")
        void shouldRejectAiAssetEnableWithoutProfile() {
            ApiAssetAggregate aggregate = configuredAggregate(AssetType.AI_API);

            AssetDomainException exception = assertThrows(
                    AssetDomainException.class,
                    () -> aggregate.enable(VALID_CATEGORY_CHECKER)
            );

            assertTrue(exception.getMessage().contains("AI capability profile"));
        }
    }

    @Nested
    @DisplayName("AI 能力档案")
    class AiCapabilityTests {

        @Test
        @DisplayName("非 AI 资产不允许绑定 AI 能力档案")
        void shouldRejectProfileForNonAiAsset() {
            ApiAssetAggregate aggregate = configuredAggregate(AssetType.STANDARD_API);

            AssetDomainException exception = assertThrows(
                    AssetDomainException.class,
                    () -> aggregate.attachAiCapabilityProfile(AiCapabilityProfile.of(
                            "OpenAI", "gpt-4.1", true, List.of("chat")))
            );

            assertTrue(exception.getMessage().contains("only allowed"));
        }

        @Test
        @DisplayName("AI 资产绑定能力档案后可通过启用校验")
        void shouldEnableAiAssetAfterAttachingProfile() {
            ApiAssetAggregate aggregate = configuredAggregate(AssetType.AI_API);
            aggregate.attachAiCapabilityProfile(AiCapabilityProfile.of(
                    "OpenAI", "gpt-4.1", true, List.of("chat", "vision")));

            aggregate.enable(VALID_CATEGORY_CHECKER);

            assertEquals(AssetStatus.ENABLED, aggregate.getStatus());
            assertNotNull(aggregate.getAiCapabilityProfile());
        }
    }

    @Nested
    @DisplayName("关键配置再校验")
    class RevalidationTests {

        @Test
        @DisplayName("启用后修改关键配置应回退为草稿")
        void shouldReturnToDraftAfterCriticalConfigChanged() {
            ApiAssetAggregate aggregate = configuredAggregate(AssetType.STANDARD_API);
            aggregate.enable(VALID_CATEGORY_CHECKER);

            aggregate.revise(
                    aggregate.getName(),
                    aggregate.getType(),
                    aggregate.getCategoryRef(),
                    UpstreamEndpointConfig.of(
                            RequestMethod.POST,
                            "https://example.com/api/v2",
                            AuthScheme.NONE,
                            null
                    ),
                    aggregate.getRequestTemplate(),
                    aggregate.getExampleSnapshot()
            );

            assertEquals(AssetStatus.DRAFT, aggregate.getStatus());
        }
    }

    private ApiAssetAggregate configuredAggregate(AssetType assetType) {
        return ApiAssetAggregate.reconstitute(
                AssetId.of("550e8400-e29b-41d4-a716-446655440000"),
                ApiCode.of("demo-api"),
                "Demo API",
                assetType,
                CategoryRef.of("llm"),
                AssetStatus.DRAFT,
                UpstreamEndpointConfig.of(RequestMethod.POST, "https://example.com/api", AuthScheme.NONE, null),
                "template",
                ExampleSnapshot.of("{\"city\":\"shanghai\"}", "{\"temp\":26}"),
                null,
                Instant.now(),
                Instant.now(),
                false,
                0L
        );
    }
}

