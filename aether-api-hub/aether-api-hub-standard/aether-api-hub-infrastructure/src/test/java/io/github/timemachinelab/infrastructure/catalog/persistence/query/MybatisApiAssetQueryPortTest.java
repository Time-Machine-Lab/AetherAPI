package io.github.timemachinelab.infrastructure.catalog.persistence.query;

import io.github.timemachinelab.service.model.ApiAssetSummaryModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * MyBatis asset management query port tests.
 */
@ExtendWith(MockitoExtension.class)
class MybatisApiAssetQueryPortTest {

    @Mock
    private ApiAssetManagementQueryMapper mapper;

    private MybatisApiAssetQueryPort queryPort;

    @BeforeEach
    void setUp() {
        queryPort = new MybatisApiAssetQueryPort(mapper);
    }

    @Test
    @DisplayName("management list should keep mixed lifecycle states visible")
    void shouldKeepMixedLifecycleStatesVisible() {
        when(mapper.selectPage(null, null, null, 20, 0)).thenReturn(List.of(
                record("weather-draft", "DRAFT", "STANDARD_API", "2026-04-20T08:00:00"),
                record("chat-enabled", "ENABLED", "AI_API", "2026-04-21T08:00:00"),
                record("invoice-disabled", "DISABLED", "STANDARD_API", "2026-04-22T08:00:00")
        ));

        List<ApiAssetSummaryModel> result = queryPort.findPage(null, null, null, 1, 20);

        assertEquals(3, result.size());
        assertEquals("DRAFT", result.get(0).getStatus());
        assertEquals("ENABLED", result.get(1).getStatus());
        assertEquals("DISABLED", result.get(2).getStatus());
        assertEquals("2026-04-20T08:00:00Z", result.get(0).getUpdatedAt());
    }

    @Test
    @DisplayName("management list should translate filters and pagination to mapper arguments")
    void shouldTranslateFiltersAndPaginationToMapperArguments() {
        ApiAssetManagementQueryRecord filtered = record("chat-enabled", "ENABLED", "AI_API", "2026-04-21T08:00:00");
        filtered.setCategoryCode("tools");
        filtered.setCategoryName("Tools");
        when(mapper.selectPage("ENABLED", "tools", "chat", 10, 10)).thenReturn(List.of(filtered));
        when(mapper.count("ENABLED", "tools", "chat")).thenReturn(1L);

        List<ApiAssetSummaryModel> result = queryPort.findPage("ENABLED", "tools", "chat", 2, 10);
        long total = queryPort.count("ENABLED", "tools", "chat");

        verify(mapper).selectPage("ENABLED", "tools", "chat", 10, 10);
        verify(mapper).count("ENABLED", "tools", "chat");
        assertEquals(1, total);
        assertEquals("tools", result.get(0).getCategoryCode());
        assertEquals("Tools", result.get(0).getCategoryName());
    }

    @Test
    @DisplayName("management list should allow missing category joins")
    void shouldAllowMissingCategoryJoins() {
        ApiAssetManagementQueryRecord record = record("uncategorized", "DRAFT", "STANDARD_API", "2026-04-23T08:00:00");
        record.setCategoryCode(null);
        record.setCategoryName(null);
        when(mapper.selectPage(null, null, "uncategorized", 20, 0)).thenReturn(List.of(record));

        ApiAssetSummaryModel result = queryPort.findPage(null, null, "uncategorized", 1, 20).get(0);

        assertNull(result.getCategoryCode());
        assertNull(result.getCategoryName());
    }

    private ApiAssetManagementQueryRecord record(
            String apiCode,
            String status,
            String assetType,
            String updatedAt) {
        ApiAssetManagementQueryRecord record = new ApiAssetManagementQueryRecord();
        record.setApiCode(apiCode);
        record.setAssetName("Asset " + apiCode);
        record.setAssetType(assetType);
        record.setStatus(status);
        record.setUpdatedAt(LocalDateTime.parse(updatedAt));
        return record;
    }
}
