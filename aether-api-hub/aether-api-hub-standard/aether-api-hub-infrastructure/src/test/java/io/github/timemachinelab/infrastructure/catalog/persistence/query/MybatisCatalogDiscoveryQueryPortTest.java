package io.github.timemachinelab.infrastructure.catalog.persistence.query;

import io.github.timemachinelab.service.model.CatalogDiscoveryAssetDetailModel;
import io.github.timemachinelab.service.model.CatalogDiscoveryAssetSummaryModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

/**
 * MyBatis catalog discovery query port tests.
 */
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
    @DisplayName("list should only return enabled assets")
    void shouldReturnOnlyEnabledAssetsInList() {
        when(mapper.selectAssetSummaries()).thenReturn(List.of(
                assetRecord("weather-forecast", "DRAFT", "STANDARD_API"),
                assetRecord("chat-completion", "ENABLED", "AI_API"),
                assetRecord("invoice-sync", "DISABLED", "STANDARD_API")
        ));

        List<CatalogDiscoveryAssetSummaryModel> result = queryPort.listDiscoverableAssets();

        assertEquals(1, result.size());
        assertEquals("chat-completion", result.get(0).getApiCode());
        assertEquals("AI_API", result.get(0).getAssetType());
        assertEquals("tools", result.get(0).getCategory().getCategoryCode());
    }

    @Test
    @DisplayName("detail should reject non-enabled assets")
    void shouldRejectNonEnabledAssetDetail() {
        when(mapper.selectAssetDetail("invoice-sync"))
                .thenReturn(assetRecord("invoice-sync", "DISABLED", "STANDARD_API"));

        Optional<CatalogDiscoveryAssetDetailModel> result = queryPort.findDiscoverableAssetDetail("invoice-sync");

        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("detail should remain readable when examples are absent")
    void shouldAllowDetailWithoutExampleSnapshot() {
        CatalogDiscoveryAssetRecord record = assetRecord("weather-forecast", "ENABLED", "STANDARD_API");
        record.setRequestExample(null);
        record.setResponseExample(null);
        when(mapper.selectAssetDetail("weather-forecast")).thenReturn(record);

        Optional<CatalogDiscoveryAssetDetailModel> result = queryPort.findDiscoverableAssetDetail("weather-forecast");

        assertTrue(result.isPresent());
        assertNull(result.get().getExampleSnapshot());
        assertNull(result.get().getAiCapabilityProfile());
    }

    @Test
    @DisplayName("detail should expose AI metadata for AI assets")
    void shouldExposeAiMetadataInDetail() {
        CatalogDiscoveryAssetRecord record = assetRecord("chat-completion", "ENABLED", "AI_API");
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

    private CatalogDiscoveryAssetRecord assetRecord(String apiCode, String status, String assetType) {
        CatalogDiscoveryAssetRecord record = new CatalogDiscoveryAssetRecord();
        record.setApiCode(apiCode);
        record.setStatus(status);
        record.setAssetType(assetType);
        record.setAssetName("Asset " + apiCode);
        record.setCategoryCode("tools");
        record.setCategoryName("工具服务");
        record.setRequestMethod("POST");
        record.setAuthScheme("HEADER_TOKEN");
        record.setRequestTemplate("template");
        record.setRequestExample("{\"city\":\"Shanghai\"}");
        record.setResponseExample("{\"temperature\":26}");
        return record;
    }
}
