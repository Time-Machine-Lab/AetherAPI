package io.github.timemachinelab.infrastructure.catalog.persistence.repository;

import io.github.timemachinelab.domain.catalog.model.ApiAssetAggregate;
import io.github.timemachinelab.domain.catalog.model.ApiCode;
import io.github.timemachinelab.domain.catalog.model.AssetDomainException;
import io.github.timemachinelab.domain.catalog.model.AssetId;
import io.github.timemachinelab.domain.catalog.model.AssetStatus;
import io.github.timemachinelab.domain.catalog.model.AssetType;
import io.github.timemachinelab.domain.catalog.model.AuthScheme;
import io.github.timemachinelab.domain.catalog.model.CategoryRef;
import io.github.timemachinelab.domain.catalog.model.ExampleSnapshot;
import io.github.timemachinelab.domain.catalog.model.RequestMethod;
import io.github.timemachinelab.domain.catalog.model.UpstreamEndpointConfig;
import io.github.timemachinelab.infrastructure.catalog.persistence.entity.ApiAssetDo;
import io.github.timemachinelab.infrastructure.catalog.persistence.mapper.ApiAssetMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MybatisApiAssetRepositoryTest {

    @Mock
    private ApiAssetMapper mapper;

    private MybatisApiAssetRepository repository;

    @BeforeEach
    void setUp() {
        repository = new MybatisApiAssetRepository(mapper);
    }

    @Test
    @DisplayName("update should use persisted optimistic lock version")
    void shouldUsePersistedVersionWhenUpdatingExistingAsset() {
        ApiAssetDo existing = existingDo();
        when(mapper.selectByCodeIncludingDeleted("weather-forecast")).thenReturn(existing);
        when(mapper.updateById(existing)).thenReturn(1);

        repository.save(revisedAggregate(1L));

        ArgumentCaptor<ApiAssetDo> captor = ArgumentCaptor.forClass(ApiAssetDo.class);
        verify(mapper).updateById(captor.capture());
        assertEquals(0L, captor.getValue().getVersion());
        assertEquals("user-1", captor.getValue().getOwnerUserId());
        assertEquals("Alice", captor.getValue().getPublisherDisplayName());
        assertEquals("Weather Forecast", captor.getValue().getAssetName());
        assertEquals("tools", captor.getValue().getCategoryCode());
        assertEquals("https://upstream.example.com/weather", captor.getValue().getUpstreamUrl());
    }

    @Test
    @DisplayName("update conflict should be exposed as asset business error")
    void shouldThrowBusinessErrorWhenUpdateMissesRow() {
        ApiAssetDo existing = existingDo();
        when(mapper.selectByCodeIncludingDeleted("weather-forecast")).thenReturn(existing);
        when(mapper.updateById(existing)).thenReturn(0);

        AssetDomainException exception = assertThrows(
                AssetDomainException.class,
                () -> repository.save(revisedAggregate(1L))
        );

        assertEquals("Asset update conflict: weather-forecast", exception.getMessage());
    }

    private ApiAssetDo existingDo() {
        ApiAssetDo existing = new ApiAssetDo();
        existing.setId("550e8400-e29b-41d4-a716-446655440000");
        existing.setApiCode("weather-forecast");
        existing.setOwnerUserId("user-1");
        existing.setPublisherDisplayName("Alice");
        existing.setAssetName(null);
        existing.setAssetType("STANDARD_API");
        existing.setStatus("DRAFT");
        existing.setCreatedAt(LocalDateTime.now());
        existing.setUpdatedAt(LocalDateTime.now());
        existing.setIsDeleted(false);
        existing.setVersion(0L);
        return existing;
    }

    private ApiAssetAggregate revisedAggregate(long version) {
        Instant now = Instant.now();
        return ApiAssetAggregate.reconstitute(
                AssetId.of("550e8400-e29b-41d4-a716-446655440000"),
                ApiCode.of("weather-forecast"),
                "user-1",
                "Alice",
                "Weather Forecast",
                AssetType.STANDARD_API,
                CategoryRef.of("tools"),
                AssetStatus.DRAFT,
                null,
                UpstreamEndpointConfig.of(
                        RequestMethod.GET,
                        "https://upstream.example.com/weather",
                        AuthScheme.NONE,
                        null
                ),
                "template",
                ExampleSnapshot.of("{\"city\":\"Shanghai\"}", "{\"temperature\":26}"),
                null,
                now,
                now,
                false,
                version
        );
    }
}
