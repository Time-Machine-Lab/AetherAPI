package io.github.timemachinelab.service;

import io.github.timemachinelab.domain.catalog.model.ApiAssetAggregate;
import io.github.timemachinelab.domain.catalog.model.ApiCode;
import io.github.timemachinelab.domain.catalog.model.AssetId;
import io.github.timemachinelab.domain.catalog.model.AssetStatus;
import io.github.timemachinelab.domain.catalog.model.AssetType;
import io.github.timemachinelab.domain.catalog.model.AuthScheme;
import io.github.timemachinelab.domain.catalog.model.RequestMethod;
import io.github.timemachinelab.domain.catalog.model.UpstreamEndpointConfig;
import io.github.timemachinelab.domain.consumerauth.model.ConsumerAggregate;
import io.github.timemachinelab.domain.consumerauth.model.ConsumerCode;
import io.github.timemachinelab.domain.consumerauth.model.ConsumerId;
import io.github.timemachinelab.domain.consumerauth.model.UserConsumerMapping;
import io.github.timemachinelab.domain.subscription.model.ApiSubscriptionAggregate;
import io.github.timemachinelab.domain.subscription.model.ApiSubscriptionDomainException;
import io.github.timemachinelab.domain.subscription.model.ApiSubscriptionId;
import io.github.timemachinelab.service.application.ApiSubscriptionApplicationService;
import io.github.timemachinelab.service.model.ApiSubscriptionModel;
import io.github.timemachinelab.service.model.ApiSubscriptionPageResult;
import io.github.timemachinelab.service.model.ApiSubscriptionStatusModel;
import io.github.timemachinelab.service.model.CancelApiSubscriptionCommand;
import io.github.timemachinelab.service.model.GetApiSubscriptionStatusQuery;
import io.github.timemachinelab.service.model.ListApiSubscriptionQuery;
import io.github.timemachinelab.service.model.SubscribeApiCommand;
import io.github.timemachinelab.service.port.out.ApiAssetRepositoryPort;
import io.github.timemachinelab.service.port.out.ApiSubscriptionRepositoryPort;
import io.github.timemachinelab.service.port.out.ConsumerIdentityRepositoryPort;
import io.github.timemachinelab.service.port.out.UserConsumerMappingRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ApiSubscriptionApplicationServiceTest {

    private InMemoryApiSubscriptionRepositoryPort subscriptionRepositoryPort;
    private InMemoryApiAssetRepositoryPort apiAssetRepositoryPort;
    private InMemoryConsumerIdentityRepositoryPort consumerIdentityRepositoryPort;
    private InMemoryUserConsumerMappingRepositoryPort userConsumerMappingRepositoryPort;
    private ApiSubscriptionApplicationService service;

    @BeforeEach
    void setUp() {
        subscriptionRepositoryPort = new InMemoryApiSubscriptionRepositoryPort();
        apiAssetRepositoryPort = new InMemoryApiAssetRepositoryPort();
        consumerIdentityRepositoryPort = new InMemoryConsumerIdentityRepositoryPort();
        userConsumerMappingRepositoryPort = new InMemoryUserConsumerMappingRepositoryPort();
        service = new ApiSubscriptionApplicationService(
                subscriptionRepositoryPort,
                apiAssetRepositoryPort,
                consumerIdentityRepositoryPort,
                userConsumerMappingRepositoryPort
        );
    }

    @Test
    @DisplayName("subscribe creates active subscription for published non-owner asset")
    void shouldSubscribePublishedAsset() {
        apiAssetRepositoryPort.save(publishedAsset("weather-api", "publisher-user-1"));

        ApiSubscriptionModel result = service.subscribe(new SubscribeApiCommand("user-1", "weather-api"));

        assertNotNull(result.getSubscriptionId());
        assertEquals("weather-api", result.getApiCode());
        assertEquals("ACTIVE", result.getSubscriptionStatus());
        assertTrue(result.isSubscribed());
        assertEquals(1, subscriptionRepositoryPort.size());
        assertEquals(1, consumerIdentityRepositoryPort.size());
        assertEquals(1, userConsumerMappingRepositoryPort.size());
    }

    @Test
    @DisplayName("subscribe rejects unavailable asset")
    void shouldRejectUnavailableAsset() {
        apiAssetRepositoryPort.save(unpublishedAsset("draft-api", "publisher-user-1"));

        ApiSubscriptionDomainException exception = assertThrows(
                ApiSubscriptionDomainException.class,
                () -> service.subscribe(new SubscribeApiCommand("user-1", "draft-api"))
        );

        assertTrue(exception.getMessage().contains("not found"));
        assertEquals(0, subscriptionRepositoryPort.size());
    }

    @Test
    @DisplayName("subscribe owner returns owner access without creating subscription")
    void shouldReturnOwnerAccessWithoutSubscriptionRecord() {
        apiAssetRepositoryPort.save(publishedAsset("my-api", "user-1"));

        ApiSubscriptionModel result = service.subscribe(new SubscribeApiCommand("user-1", "my-api"));

        assertEquals("OWNER", result.getSubscriptionStatus());
        assertTrue(result.isOwnerAccess());
        assertEquals(0, subscriptionRepositoryPort.size());
    }

    @Test
    @DisplayName("subscribe is idempotent for active subscriptions")
    void shouldReturnExistingActiveSubscription() {
        apiAssetRepositoryPort.save(publishedAsset("weather-api", "publisher-user-1"));

        ApiSubscriptionModel first = service.subscribe(new SubscribeApiCommand("user-1", "weather-api"));
        ApiSubscriptionModel second = service.subscribe(new SubscribeApiCommand("user-1", "weather-api"));

        assertEquals(first.getSubscriptionId(), second.getSubscriptionId());
        assertEquals(1, subscriptionRepositoryPort.size());
    }

    @Test
    @DisplayName("list returns only current user's subscriptions")
    void shouldListOnlyCurrentUserSubscriptions() {
        apiAssetRepositoryPort.save(publishedAsset("weather-api", "publisher-user-1"));
        apiAssetRepositoryPort.save(publishedAsset("map-api", "publisher-user-1"));
        ApiSubscriptionModel mySubscription = service.subscribe(new SubscribeApiCommand("user-1", "weather-api"));
        service.subscribe(new SubscribeApiCommand("user-2", "map-api"));

        ApiSubscriptionPageResult page = service.listSubscriptions(new ListApiSubscriptionQuery("user-1", 1, 20));

        assertEquals(1, page.getItems().size());
        assertEquals(mySubscription.getSubscriptionId(), page.getItems().get(0).getSubscriptionId());
    }

    @Test
    @DisplayName("status distinguishes subscribed owner and not subscribed")
    void shouldQuerySubscriptionStatus() {
        apiAssetRepositoryPort.save(publishedAsset("weather-api", "publisher-user-1"));
        apiAssetRepositoryPort.save(publishedAsset("my-api", "user-1"));
        service.subscribe(new SubscribeApiCommand("user-1", "weather-api"));

        ApiSubscriptionStatusModel subscribed = service.getSubscriptionStatus(
                new GetApiSubscriptionStatusQuery("user-1", "weather-api"));
        ApiSubscriptionStatusModel owner = service.getSubscriptionStatus(
                new GetApiSubscriptionStatusQuery("user-1", "my-api"));
        ApiSubscriptionStatusModel notSubscribed = service.getSubscriptionStatus(
                new GetApiSubscriptionStatusQuery("user-1", "unknown-api"));

        assertEquals("SUBSCRIBED", subscribed.getAccessStatus());
        assertEquals("OWNER", owner.getAccessStatus());
        assertEquals("NOT_SUBSCRIBED", notSubscribed.getAccessStatus());
    }

    @Test
    @DisplayName("cancel marks active subscription inactive")
    void shouldCancelActiveSubscription() {
        apiAssetRepositoryPort.save(publishedAsset("weather-api", "publisher-user-1"));
        ApiSubscriptionModel subscription = service.subscribe(new SubscribeApiCommand("user-1", "weather-api"));

        ApiSubscriptionModel cancelled = service.cancelSubscription(
                new CancelApiSubscriptionCommand("user-1", subscription.getSubscriptionId()));

        assertEquals("CANCELLED", cancelled.getSubscriptionStatus());
        assertTrue(!cancelled.isSubscribed());
        assertTrue(!subscriptionRepositoryPort.hasActiveSubscription(
                userConsumerMappingRepositoryPort.byUser("user-1").getConsumerId(),
                ApiCode.of("weather-api")));
    }

    private ApiAssetAggregate publishedAsset(String apiCode, String ownerUserId) {
        Instant now = Instant.now();
        return ApiAssetAggregate.reconstitute(
                AssetId.generate(),
                ApiCode.of(apiCode),
                ownerUserId,
                "Publisher",
                apiCode,
                AssetType.STANDARD_API,
                null,
                AssetStatus.PUBLISHED,
                now.minusSeconds(60),
                UpstreamEndpointConfig.of(
                        RequestMethod.GET,
                        "https://upstream.example.com/" + apiCode,
                        AuthScheme.NONE,
                        null
                ),
                null,
                null,
                null,
                now.minusSeconds(120),
                now.minusSeconds(60),
                false,
                0L
        );
    }

    private ApiAssetAggregate unpublishedAsset(String apiCode, String ownerUserId) {
        Instant now = Instant.now();
        return ApiAssetAggregate.reconstitute(
                AssetId.generate(),
                ApiCode.of(apiCode),
                ownerUserId,
                "Publisher",
                apiCode,
                AssetType.STANDARD_API,
                null,
                AssetStatus.UNPUBLISHED,
                null,
                null,
                null,
                null,
                null,
                now.minusSeconds(120),
                now.minusSeconds(60),
                false,
                0L
        );
    }

    private static final class InMemoryApiSubscriptionRepositoryPort implements ApiSubscriptionRepositoryPort {

        private final Map<String, ApiSubscriptionAggregate> storage = new HashMap<>();

        @Override
        public Optional<ApiSubscriptionAggregate> findById(ApiSubscriptionId id) {
            return Optional.ofNullable(storage.get(id.getValue()));
        }

        @Override
        public Optional<ApiSubscriptionAggregate> findActiveByConsumerIdAndApiCode(ConsumerId consumerId, ApiCode apiCode) {
            return storage.values().stream()
                    .filter(item -> item.getSubscriberConsumerId().equals(consumerId))
                    .filter(item -> item.getApiCode().equals(apiCode))
                    .filter(ApiSubscriptionAggregate::isActive)
                    .findFirst();
        }

        @Override
        public List<ApiSubscriptionAggregate> findPageByConsumerId(ConsumerId consumerId, int page, int size) {
            List<ApiSubscriptionAggregate> filtered = storage.values().stream()
                    .filter(item -> item.getSubscriberConsumerId().equals(consumerId))
                    .sorted(Comparator.comparing(ApiSubscriptionAggregate::getCreatedAt).reversed())
                    .collect(Collectors.toList());
            int fromIndex = Math.max(0, (page - 1) * size);
            int toIndex = Math.min(filtered.size(), fromIndex + size);
            if (fromIndex >= filtered.size()) {
                return List.of();
            }
            return new ArrayList<>(filtered.subList(fromIndex, toIndex));
        }

        @Override
        public long countByConsumerId(ConsumerId consumerId) {
            return storage.values().stream()
                    .filter(item -> item.getSubscriberConsumerId().equals(consumerId))
                    .count();
        }

        @Override
        public boolean hasActiveSubscription(ConsumerId consumerId, ApiCode apiCode) {
            return findActiveByConsumerIdAndApiCode(consumerId, apiCode).isPresent();
        }

        @Override
        public void save(ApiSubscriptionAggregate aggregate) {
            storage.put(aggregate.getId().getValue(), aggregate);
        }

        int size() {
            return storage.size();
        }
    }

    private static final class InMemoryApiAssetRepositoryPort implements ApiAssetRepositoryPort {

        private final Map<String, ApiAssetAggregate> storage = new HashMap<>();

        @Override
        public Optional<ApiAssetAggregate> findByCode(ApiCode code) {
            ApiAssetAggregate aggregate = storage.get(code.getValue());
            if (aggregate == null || aggregate.isDeleted()) {
                return Optional.empty();
            }
            return Optional.of(aggregate);
        }

        @Override
        public Optional<ApiAssetAggregate> findByCodeIncludingDeleted(ApiCode code) {
            return Optional.ofNullable(storage.get(code.getValue()));
        }

        @Override
        public boolean existsByCode(ApiCode code) {
            return storage.containsKey(code.getValue());
        }

        @Override
        public void save(ApiAssetAggregate aggregate) {
            storage.put(aggregate.getCode().getValue(), aggregate);
        }
    }

    private static final class InMemoryConsumerIdentityRepositoryPort implements ConsumerIdentityRepositoryPort {

        private final Map<String, ConsumerAggregate> storage = new HashMap<>();

        @Override
        public Optional<ConsumerAggregate> findById(ConsumerId id) {
            return Optional.ofNullable(storage.get(id.getValue()));
        }

        @Override
        public Optional<ConsumerAggregate> findByCode(ConsumerCode code) {
            return storage.values().stream()
                    .filter(item -> item.getCode().equals(code))
                    .findFirst();
        }

        @Override
        public void save(ConsumerAggregate aggregate) {
            storage.put(aggregate.getId().getValue(), aggregate);
        }

        int size() {
            return storage.size();
        }
    }

    private static final class InMemoryUserConsumerMappingRepositoryPort implements UserConsumerMappingRepositoryPort {

        private final Map<String, UserConsumerMapping> byUser = new HashMap<>();

        @Override
        public Optional<UserConsumerMapping> findActiveByUserId(String userId) {
            return Optional.ofNullable(byUser.get(userId));
        }

        @Override
        public Optional<UserConsumerMapping> findActiveByConsumerId(ConsumerId consumerId) {
            return byUser.values().stream()
                    .filter(item -> item.getConsumerId().equals(consumerId))
                    .findFirst();
        }

        @Override
        public void save(UserConsumerMapping mapping) {
            byUser.put(mapping.getUserId(), mapping);
        }

        UserConsumerMapping byUser(String userId) {
            return byUser.get(userId);
        }

        int size() {
            return byUser.size();
        }
    }
}
