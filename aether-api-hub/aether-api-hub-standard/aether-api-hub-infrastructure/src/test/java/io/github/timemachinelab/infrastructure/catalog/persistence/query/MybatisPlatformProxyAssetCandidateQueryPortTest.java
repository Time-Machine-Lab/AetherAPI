package io.github.timemachinelab.infrastructure.catalog.persistence.query;

import io.github.timemachinelab.service.model.PlatformProxyAssetCandidateModel;
import org.apache.ibatis.annotations.Select;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * MyBatis platform proxy asset candidate query port tests.
 */
@ExtendWith(MockitoExtension.class)
class MybatisPlatformProxyAssetCandidateQueryPortTest {

    @Mock
    private PlatformProxyAssetCandidateQueryMapper mapper;

    private MybatisPlatformProxyAssetCandidateQueryPort queryPort;

    @BeforeEach
    void setUp() {
        queryPort = new MybatisPlatformProxyAssetCandidateQueryPort(mapper);
    }

    @Test
    @DisplayName("candidate query should map cross-owner asset binding summaries")
    void shouldMapCrossOwnerAssetBindingSummaries() {
        PlatformProxyAssetCandidateQueryRecord record = record();
        when(mapper.selectPage("weather", "PUBLISHED", "profile-1", 10, 10)).thenReturn(List.of(record));
        when(mapper.count("weather", "PUBLISHED", "profile-1")).thenReturn(1L);

        List<PlatformProxyAssetCandidateModel> result = queryPort.findPage(
                "weather",
                "PUBLISHED",
                "profile-1",
                2,
                10
        );
        long total = queryPort.count("weather", "PUBLISHED", "profile-1");

        verify(mapper).selectPage("weather", "PUBLISHED", "profile-1", 10, 10);
        verify(mapper).count("weather", "PUBLISHED", "profile-1");
        assertEquals(1L, total);
        assertEquals("weather-forecast", result.get(0).getApiCode());
        assertEquals("owner-display", result.get(0).getPublisherDisplayName());
        assertEquals("profile-1", result.get(0).getProxyProfileId());
        assertEquals("default-cn", result.get(0).getProxyProfileCode());
        assertEquals("2026-05-02T08:30:00Z", result.get(0).getUpdatedAt());
    }

    @Test
    @DisplayName("candidate SQL should exclude deleted assets and avoid sensitive fields")
    void shouldExcludeDeletedAssetsAndAvoidSensitiveFields() throws NoSuchMethodException {
        Method selectPage = PlatformProxyAssetCandidateQueryMapper.class.getMethod(
                "selectPage",
                String.class,
                String.class,
                String.class,
                int.class,
                int.class
        );
        Method count = PlatformProxyAssetCandidateQueryMapper.class.getMethod(
                "count",
                String.class,
                String.class,
                String.class
        );

        String selectSql = annotationSql(selectPage);
        assertTrue(selectSql.contains("WHERE a.is_deleted = FALSE"));
        assertTrue(selectSql.contains("a.proxy_profile_id = #{boundProfileId}"));
        assertTrue(selectSql.contains("a.publisher_display_name"));
        assertFalse(selectSql.contains("proxy_host"));
        assertFalse(selectSql.contains("proxy_port"));
        assertFalse(selectSql.contains("username"));
        assertFalse(selectSql.contains("password_secret"));
        assertTrue(annotationSql(count).contains("WHERE a.is_deleted = FALSE"));
        assertFalse(Arrays.stream(PlatformProxyAssetCandidateModel.class.getMethods())
                .anyMatch(method -> method.getName().equals("getProxyHost")
                        || method.getName().equals("getProxyPort")
                        || method.getName().equals("getPassword")));
    }

    private PlatformProxyAssetCandidateQueryRecord record() {
        PlatformProxyAssetCandidateQueryRecord record = new PlatformProxyAssetCandidateQueryRecord();
        record.setApiCode("weather-forecast");
        record.setAssetName("Weather Forecast");
        record.setAssetType("STANDARD_API");
        record.setStatus("PUBLISHED");
        record.setPublisherDisplayName("owner-display");
        record.setProxyProfileId("profile-1");
        record.setProxyProfileCode("default-cn");
        record.setProxyProfileName("Default CN");
        record.setCreatedAt(LocalDateTime.parse("2026-05-01T08:30:00"));
        record.setUpdatedAt(LocalDateTime.parse("2026-05-02T08:30:00"));
        return record;
    }

    private String annotationSql(Method method) {
        return String.join("\n", method.getAnnotation(Select.class).value());
    }
}
