package io.github.timemachinelab.infrastructure.catalog.persistence.query;

import io.github.timemachinelab.service.model.CatalogDiscoveryAssetDetailModel;
import io.github.timemachinelab.service.model.CatalogDiscoveryAssetSummaryModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.apache.ibatis.annotations.Select;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MybatisCatalogDiscoveryQueryPortTest {

    @Mock
    private CatalogDiscoveryMapper mapper;

    private MybatisCatalogDiscoveryQueryPort queryPort;

    @BeforeEach
    void setUp() {
        queryPort = new MybatisCatalogDiscoveryQueryPort(mapper);
    }

    @Test
    @DisplayName("list should map published discovery assets with publisher summary")
    void shouldMapPublishedDiscoveryAssetsWithPublisherSummary() {
        when(mapper.selectAssetSummaries()).thenReturn(List.of(
                assetRecord("chat-completion", "PUBLISHED", "AI_API", "Alice"),
                assetRecord("weather-forecast", "PUBLISHED", "STANDARD_API", "Bob")
        ));

        List<CatalogDiscoveryAssetSummaryModel> result = queryPort.listDiscoverableAssets();

        assertEquals(2, result.size());
        assertEquals("chat-completion", result.get(0).getApiCode());
        assertEquals("AI_API", result.get(0).getAssetType());
        assertEquals("tools", result.get(0).getCategory().getCategoryCode());
        assertEquals("Alice", result.get(0).getPublisher().getDisplayName());
    }

    @Test
    @DisplayName("detail should return empty when mapper has no published asset")
    void shouldReturnEmptyWhenMapperHasNoPublishedAsset() {
        when(mapper.selectAssetDetail("invoice-sync")).thenReturn(null);

        Optional<CatalogDiscoveryAssetDetailModel> result = queryPort.findDiscoverableAssetDetail("invoice-sync");

        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("detail should remain readable when examples are absent")
    void shouldAllowDetailWithoutExampleSnapshot() {
        CatalogDiscoveryAssetRecord record = assetRecord("weather-forecast", "PUBLISHED", "STANDARD_API", "Alice");
        record.setRequestExample(null);
        record.setResponseExample(null);
        when(mapper.selectAssetDetail("weather-forecast")).thenReturn(record);

        Optional<CatalogDiscoveryAssetDetailModel> result = queryPort.findDiscoverableAssetDetail("weather-forecast");

        assertTrue(result.isPresent());
        assertNull(result.get().getExampleSnapshot());
        assertNull(result.get().getAiCapabilityProfile());
        assertEquals("Alice", result.get().getPublisher().getDisplayName());
    }

    @Test
    @DisplayName("detail should expose ai metadata for ai assets")
    void shouldExposeAiMetadataInDetail() {
        CatalogDiscoveryAssetRecord record = assetRecord("chat-completion", "PUBLISHED", "AI_API", "Alice");
        record.setAiProvider("OpenAI");
        record.setAiModel("gpt-4.1");
        record.setAiStreamingSupported(true);
        record.setAiCapabilityTagsJson("[\"chat\",\"reasoning\"]");
        when(mapper.selectAssetDetail("chat-completion")).thenReturn(record);

        Optional<CatalogDiscoveryAssetDetailModel> result = queryPort.findDiscoverableAssetDetail("chat-completion");

        assertTrue(result.isPresent());
        assertNotNull(result.get().getAiCapabilityProfile());
        assertEquals("OpenAI", result.get().getAiCapabilityProfile().getProvider());
        assertTrue(result.get().getAiCapabilityProfile().getStreamingSupported());
        assertEquals(List.of("chat", "reasoning"), result.get().getAiCapabilityProfile().getCapabilityTags());
        assertFalse(result.get().getAiCapabilityProfile().getCapabilityTags().isEmpty());
    }

    @Test
    @DisplayName("discovery active read SQL should only expose published non-deleted assets")
    void shouldKeepDeletedAssetsOutOfDiscoveryReadSql() throws NoSuchMethodException {
        Method selectAssetSummaries = CatalogDiscoveryMapper.class.getMethod("selectAssetSummaries");
        Method selectAssetDetail = CatalogDiscoveryMapper.class.getMethod("selectAssetDetail", String.class);

        assertSqlContains(selectAssetSummaries, "WHERE a.is_deleted = FALSE");
        assertSqlContains(selectAssetSummaries, "AND a.status = 'PUBLISHED'");
        assertSqlContains(selectAssetDetail, "WHERE a.is_deleted = FALSE");
        assertSqlContains(selectAssetDetail, "AND a.status = 'PUBLISHED'");
    }

    private CatalogDiscoveryAssetRecord assetRecord(String apiCode, String status, String assetType, String publisher) {
        CatalogDiscoveryAssetRecord record = new CatalogDiscoveryAssetRecord();
        record.setApiCode(apiCode);
        record.setStatus(status);
        record.setAssetType(assetType);
        record.setAssetName("Asset " + apiCode);
        record.setCategoryCode("tools");
        record.setCategoryName("Tools");
        record.setPublisherDisplayName(publisher);
        record.setPublishedAt(LocalDateTime.of(2026, 4, 24, 8, 0));
        record.setRequestMethod("POST");
        record.setAuthScheme("HEADER_TOKEN");
        record.setRequestTemplate("template");
        record.setRequestExample("{\"city\":\"Shanghai\"}");
        record.setResponseExample("{\"temperature\":26}");
        return record;
    }

    private void assertSqlContains(Method method, String expected) {
        String sql = String.join("\n", method.getAnnotation(Select.class).value());
        assertTrue(sql.contains(expected), () -> "Expected SQL to contain: " + expected + "\n" + sql);
    }
}
