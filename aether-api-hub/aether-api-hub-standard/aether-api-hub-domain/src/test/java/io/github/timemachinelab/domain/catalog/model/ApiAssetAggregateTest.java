package io.github.timemachinelab.domain.catalog.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ApiAssetAggregateTest {

    private static final String OWNER_USER_ID = "user-1";
    private static final String PUBLISHER_DISPLAY_NAME = "Alice";
    private static final CategoryValidityChecker VALID_CATEGORY_CHECKER = categoryRef -> true;
    private static final CategoryValidityChecker INVALID_CATEGORY_CHECKER = categoryRef -> false;

    @Nested
    @DisplayName("draft revision")
    class DraftRevisionTests {

        @Test
        @DisplayName("should save draft without examples")
        void shouldSaveDraftWithoutExamples() {
            ApiAssetAggregate aggregate = ApiAssetAggregate.registerDraft(
                    AssetId.generate(),
                    ApiCode.of("weather-forecast"),
                    OWNER_USER_ID,
                    PUBLISHER_DISPLAY_NAME,
                    AssetType.STANDARD_API,
                    null
            );

            aggregate.revise(
                    "Weather Forecast",
                    AssetType.STANDARD_API,
                    CategoryRef.of("tools"),
                    UpstreamEndpointConfig.of(RequestMethod.POST, "https://example.com/api", AuthScheme.NONE, null),
                    "template",
                    null,
                    "Alice Cooper"
            );

            assertEquals(AssetStatus.DRAFT, aggregate.getStatus());
            assertNull(aggregate.getExampleSnapshot());
            assertEquals("Weather Forecast", aggregate.getName());
            assertEquals("Alice Cooper", aggregate.getPublisherDisplayName());
        }

        @Test
        @DisplayName("should reject access from another owner")
        void shouldRejectAccessFromAnotherOwner() {
            ApiAssetAggregate aggregate = configuredAggregate(AssetType.STANDARD_API, AssetStatus.DRAFT);

            AssetDomainException exception = assertThrows(
                    AssetDomainException.class,
                    () -> aggregate.assertOwnedBy("user-2")
            );

            assertTrue(exception.getMessage().contains("not found"));
        }
    }

    @Nested
    @DisplayName("publish validation")
    class PublishValidationTests {

        @Test
        @DisplayName("should reject publish when configuration incomplete")
        void shouldRejectPublishWhenConfigurationIncomplete() {
            ApiAssetAggregate aggregate = ApiAssetAggregate.registerDraft(
                    AssetId.generate(),
                    ApiCode.of("stock-price"),
                    OWNER_USER_ID,
                    PUBLISHER_DISPLAY_NAME,
                    AssetType.STANDARD_API,
                    "Stock Price"
            );

            AssetDomainException exception = assertThrows(
                    AssetDomainException.class,
                    () -> aggregate.publish(VALID_CATEGORY_CHECKER, PUBLISHER_DISPLAY_NAME)
            );

            assertTrue(exception.getMessage().contains("Category code"));
        }

        @Test
        @DisplayName("should reject publish when category invalid")
        void shouldRejectPublishWhenCategoryInvalid() {
            ApiAssetAggregate aggregate = configuredAggregate(AssetType.STANDARD_API, AssetStatus.DRAFT);

            AssetDomainException exception = assertThrows(
                    AssetDomainException.class,
                    () -> aggregate.publish(INVALID_CATEGORY_CHECKER, PUBLISHER_DISPLAY_NAME)
            );

            assertTrue(exception.getMessage().contains("category"));
        }

        @Test
        @DisplayName("should reject ai publish without profile")
        void shouldRejectAiPublishWithoutProfile() {
            ApiAssetAggregate aggregate = configuredAggregate(AssetType.AI_API, AssetStatus.DRAFT);

            AssetDomainException exception = assertThrows(
                    AssetDomainException.class,
                    () -> aggregate.publish(VALID_CATEGORY_CHECKER, PUBLISHER_DISPLAY_NAME)
            );

            assertTrue(exception.getMessage().contains("AI capability profile"));
        }
    }

    @Nested
    @DisplayName("ai profile")
    class AiCapabilityTests {

        @Test
        @DisplayName("should reject profile for non ai asset")
        void shouldRejectProfileForNonAiAsset() {
            ApiAssetAggregate aggregate = configuredAggregate(AssetType.STANDARD_API, AssetStatus.DRAFT);

            AssetDomainException exception = assertThrows(
                    AssetDomainException.class,
                    () -> aggregate.attachAiCapabilityProfile(
                            AiCapabilityProfile.of("OpenAI", "gpt-4.1", true, List.of("chat")),
                            PUBLISHER_DISPLAY_NAME
                    )
            );

            assertTrue(exception.getMessage().contains("only allowed"));
        }

        @Test
        @DisplayName("should publish ai asset after attaching profile")
        void shouldPublishAiAssetAfterAttachingProfile() {
            ApiAssetAggregate aggregate = configuredAggregate(AssetType.AI_API, AssetStatus.DRAFT);
            aggregate.attachAiCapabilityProfile(
                    AiCapabilityProfile.of("OpenAI", "gpt-4.1", true, List.of("chat", "vision")),
                    "AI Alice"
            );

            aggregate.publish(VALID_CATEGORY_CHECKER, "AI Alice");

            assertEquals(AssetStatus.PUBLISHED, aggregate.getStatus());
            assertNotNull(aggregate.getAiCapabilityProfile());
            assertEquals("AI Alice", aggregate.getPublisherDisplayName());
            assertNotNull(aggregate.getPublishedAt());
        }
    }

    @Nested
    @DisplayName("republish and deletion")
    class RevalidationTests {

        @Test
        @DisplayName("should move published asset to unpublished after critical config change")
        void shouldMovePublishedAssetToUnpublishedAfterCriticalConfigChanged() {
            ApiAssetAggregate aggregate = configuredAggregate(AssetType.STANDARD_API, AssetStatus.PUBLISHED);

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
                    aggregate.getExampleSnapshot(),
                    aggregate.getPublisherDisplayName()
            );

            assertEquals(AssetStatus.UNPUBLISHED, aggregate.getStatus());
            assertNull(aggregate.getPublishedAt());
        }

        @Test
        @DisplayName("should unpublish published asset explicitly")
        void shouldUnpublishPublishedAssetExplicitly() {
            ApiAssetAggregate aggregate = configuredAggregate(AssetType.STANDARD_API, AssetStatus.PUBLISHED);

            aggregate.unpublish();

            assertEquals(AssetStatus.UNPUBLISHED, aggregate.getStatus());
            assertNull(aggregate.getPublishedAt());
        }

        @Test
        @DisplayName("should soft delete published asset as unpublished")
        void shouldSoftDeletePublishedAssetAsUnpublished() {
            ApiAssetAggregate aggregate = configuredAggregate(AssetType.STANDARD_API, AssetStatus.PUBLISHED);

            aggregate.softDelete();

            assertTrue(aggregate.isDeleted());
            assertEquals(AssetStatus.UNPUBLISHED, aggregate.getStatus());
            assertNull(aggregate.getPublishedAt());
        }
    }

    private ApiAssetAggregate configuredAggregate(AssetType assetType, AssetStatus status) {
        Instant now = Instant.now();
        return ApiAssetAggregate.reconstitute(
                AssetId.of("550e8400-e29b-41d4-a716-446655440000"),
                ApiCode.of("demo-api"),
                OWNER_USER_ID,
                PUBLISHER_DISPLAY_NAME,
                "Demo API",
                assetType,
                CategoryRef.of("tools"),
                status,
                status == AssetStatus.PUBLISHED ? now.minusSeconds(60) : null,
                UpstreamEndpointConfig.of(RequestMethod.POST, "https://example.com/api", AuthScheme.NONE, null),
                "template",
                ExampleSnapshot.of("{\"city\":\"shanghai\"}", "{\"temp\":26}"),
                null,
                now.minusSeconds(120),
                now.minusSeconds(60),
                false,
                0L
        );
    }
}
