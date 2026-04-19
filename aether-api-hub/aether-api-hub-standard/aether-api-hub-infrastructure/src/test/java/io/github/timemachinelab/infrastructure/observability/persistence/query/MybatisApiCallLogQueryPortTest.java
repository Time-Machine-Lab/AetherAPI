package io.github.timemachinelab.infrastructure.observability.persistence.query;

import io.github.timemachinelab.service.model.ApiCallLogDetailModel;
import io.github.timemachinelab.service.model.ApiCallLogModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MybatisApiCallLogQueryPortTest {

    @Mock
    private ApiCallLogQueryMapper mapper;

    private MybatisApiCallLogQueryPort queryPort;

    @BeforeEach
    void setUp() {
        queryPort = new MybatisApiCallLogQueryPort(mapper);
    }

    @Test
    @DisplayName("page query should map records to summary models")
    void shouldMapPageRecordsToSummaryModels() {
        when(mapper.selectPageByConsumerId(eq("consumer-1"), eq("chat-completions"), any(), any(), eq(20), eq(0)))
                .thenReturn(List.of(record("log-1")));

        List<ApiCallLogModel> result = queryPort.findPageByConsumerId(
                "consumer-1",
                "chat-completions",
                Instant.parse("2026-04-19T08:00:00Z"),
                Instant.parse("2026-04-19T10:00:00Z"),
                1,
                20
        );

        assertEquals(1, result.size());
        assertEquals("log-1", result.get(0).getLogId());
        assertEquals("2026-04-19T09:00:00Z", result.get(0).getInvocationTime());
    }

    @Test
    @DisplayName("detail query should expose nested error and AI extension fields")
    void shouldMapDetailRecord() {
        ApiCallLogQueryRecord record = record("log-2");
        record.setErrorCode("UPSTREAM_TIMEOUT");
        record.setErrorType("TIMEOUT");
        record.setErrorSummary("Upstream request timed out");
        record.setAiProvider("OpenAI");
        record.setAiModel("gpt-4.1");
        record.setAiStreaming(true);
        record.setAiUsageSnapshot("{\"promptTokens\":12}");
        when(mapper.selectDetailByIdAndConsumerId("log-2", "consumer-1")).thenReturn(record);

        ApiCallLogDetailModel result = queryPort.findDetailByIdAndConsumerId("log-2", "consumer-1").orElseThrow();

        assertEquals("log-2", result.getLogId());
        assertNotNull(result.getError());
        assertEquals("UPSTREAM_TIMEOUT", result.getError().getErrorCode());
        assertNotNull(result.getAiExtension());
        assertTrue(result.getAiExtension().getStreaming());
    }

    @Test
    @DisplayName("detail query should keep nullable nested sections empty when no data exists")
    void shouldKeepNullableSectionsEmpty() {
        ApiCallLogQueryRecord record = record("log-3");
        record.setErrorCode(null);
        record.setErrorType(null);
        record.setErrorSummary(null);
        record.setAiProvider(null);
        record.setAiModel(null);
        record.setAiStreaming(null);
        record.setAiUsageSnapshot(null);
        when(mapper.selectDetailByIdAndConsumerId("log-3", "consumer-1")).thenReturn(record);

        ApiCallLogDetailModel result = queryPort.findDetailByIdAndConsumerId("log-3", "consumer-1").orElseThrow();

        assertNull(result.getError());
        assertNull(result.getAiExtension());
    }

    private ApiCallLogQueryRecord record(String logId) {
        ApiCallLogQueryRecord record = new ApiCallLogQueryRecord();
        record.setLogId(logId);
        record.setTargetApiCode("chat-completions");
        record.setTargetApiName("OpenAI Chat");
        record.setRequestMethod("POST");
        record.setAccessChannel("UNIFIED_ACCESS");
        record.setInvocationTime(LocalDateTime.ofInstant(Instant.parse("2026-04-19T09:00:00Z"), ZoneOffset.UTC));
        record.setDurationMs(820L);
        record.setResultType("SUCCESS");
        record.setSuccess(true);
        record.setHttpStatusCode(200);
        record.setCredentialCode("cred_alpha");
        record.setCredentialStatus("ENABLED");
        record.setCreatedAt(LocalDateTime.ofInstant(Instant.parse("2026-04-19T09:00:00Z"), ZoneOffset.UTC));
        record.setUpdatedAt(LocalDateTime.ofInstant(Instant.parse("2026-04-19T09:00:01Z"), ZoneOffset.UTC));
        return record;
    }
}
