package io.github.timemachinelab.infrastructure.importagent.persistence.repository;

import io.github.timemachinelab.service.model.ApiImportAgentRunModel;
import io.github.timemachinelab.service.model.ImportAgentRunStatus;
import io.github.timemachinelab.service.model.ImportAgentStepType;
import io.github.timemachinelab.service.model.ImportStepResultModel;
import io.github.timemachinelab.service.model.ImportStepResultStatus;
import io.github.timemachinelab.infrastructure.importagent.persistence.entity.ApiImportAgentRunDo;
import io.github.timemachinelab.infrastructure.importagent.persistence.mapper.ApiImportAgentRunMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MybatisApiImportAgentRunRepositoryTest {

    @Mock
    private ApiImportAgentRunMapper runMapper;

    private MybatisApiImportAgentRunRepository repository;

    @BeforeEach
    void setUp() {
        repository = new MybatisApiImportAgentRunRepository(runMapper);
    }

    @Test
    @DisplayName("saveRun should serialize affected asset ids and step results")
    void shouldSerializeRunPayload() {
        ApiImportAgentRunDo existing = new ApiImportAgentRunDo();
        existing.setId("run-1");
        existing.setVersion(1L);
        when(runMapper.selectById("run-1")).thenReturn(existing);

        repository.saveRun(runModel());

        ArgumentCaptor<ApiImportAgentRunDo> captor = ArgumentCaptor.forClass(ApiImportAgentRunDo.class);
        verify(runMapper).updateById(captor.capture());
        ApiImportAgentRunDo saved = captor.getValue();
        assertEquals("PARTIALLY_FAILED", saved.getStatus());
        assertTrue(saved.getAffectedApiCodes().contains("weather-forecast"));
        assertTrue(saved.getStepResultsJson().contains("REGISTER_ASSET"));
        assertTrue(saved.getStepResultsJson().contains("FAILED"));
    }

    @Test
    @DisplayName("findOwnedRun should restore step results and affected asset ids")
    void shouldRestoreRunPayload() {
        ApiImportAgentRunDo runDo = new ApiImportAgentRunDo();
        runDo.setId("run-1");
        runDo.setSessionId("session-1");
        runDo.setOwnerUserId("user-1");
        runDo.setPlanVersion(2);
        runDo.setStatus("PARTIALLY_FAILED");
        runDo.setSummary("done");
        runDo.setFailureReason("register failed");
        runDo.setAffectedApiCodes("[\"weather-forecast\"]");
        runDo.setStepResultsJson("[{\"stepType\":\"REGISTER_ASSET\",\"targetRef\":\"weather-forecast\",\"status\":\"FAILED\",\"message\":\"register failed\"}]");
        runDo.setCreatedAt(LocalDateTime.of(2026, 5, 18, 10, 10));
        runDo.setUpdatedAt(LocalDateTime.of(2026, 5, 18, 10, 11));
        when(runMapper.selectOwnedById("user-1", "run-1")).thenReturn(runDo);

        var run = repository.findOwnedRun("user-1", "run-1").orElseThrow();

        assertEquals(ImportAgentRunStatus.PARTIALLY_FAILED, run.getStatus());
        assertEquals(List.of("weather-forecast"), run.getAffectedApiCodes());
        assertEquals(ImportAgentStepType.REGISTER_ASSET, run.getStepResults().get(0).getStepType());
        assertEquals(ImportStepResultStatus.FAILED, run.getStepResults().get(0).getStatus());
        assertEquals("register failed", run.getFailureReason());
    }

    private ApiImportAgentRunModel runModel() {
        return new ApiImportAgentRunModel(
                "run-1",
                "session-1",
                "user-1",
                2,
                ImportAgentRunStatus.PARTIALLY_FAILED,
                "done",
                "register failed",
                List.of("weather-forecast"),
                List.of(new ImportStepResultModel(
                        ImportAgentStepType.REGISTER_ASSET,
                        "weather-forecast",
                        ImportStepResultStatus.FAILED,
                        "register failed"
                )),
                "2026-05-18T10:10:00Z",
                "2026-05-18T10:11:00Z"
        );
    }
}