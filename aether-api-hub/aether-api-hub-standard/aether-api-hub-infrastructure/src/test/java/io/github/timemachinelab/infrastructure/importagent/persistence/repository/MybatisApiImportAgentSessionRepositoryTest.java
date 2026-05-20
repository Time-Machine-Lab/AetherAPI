package io.github.timemachinelab.infrastructure.importagent.persistence.repository;

import io.github.timemachinelab.domain.catalog.model.AssetType;
import io.github.timemachinelab.domain.catalog.model.AuthScheme;
import io.github.timemachinelab.domain.catalog.model.RequestMethod;
import io.github.timemachinelab.service.model.ApiImportAgentSessionModel;
import io.github.timemachinelab.service.model.ImportAgentActorType;
import io.github.timemachinelab.service.model.ImportAgentPlanModel;
import io.github.timemachinelab.service.model.ImportAgentSessionStatus;
import io.github.timemachinelab.service.model.ImportAgentTurnModel;
import io.github.timemachinelab.service.model.ImportAiProfileModel;
import io.github.timemachinelab.service.model.ImportAssetPlanModel;
import io.github.timemachinelab.service.model.ImportCategoryPlanAction;
import io.github.timemachinelab.service.model.ImportCategoryPlanModel;
import io.github.timemachinelab.infrastructure.importagent.persistence.entity.ApiImportAgentSessionDo;
import io.github.timemachinelab.infrastructure.importagent.persistence.entity.ApiImportAgentTurnDo;
import io.github.timemachinelab.infrastructure.importagent.persistence.mapper.ApiImportAgentSessionMapper;
import io.github.timemachinelab.infrastructure.importagent.persistence.mapper.ApiImportAgentTurnMapper;
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
class MybatisApiImportAgentSessionRepositoryTest {

    @Mock
    private ApiImportAgentSessionMapper sessionMapper;

    @Mock
    private ApiImportAgentTurnMapper turnMapper;

    private MybatisApiImportAgentSessionRepository repository;

    @BeforeEach
    void setUp() {
        repository = new MybatisApiImportAgentSessionRepository(sessionMapper, turnMapper);
    }

    @Test
    @DisplayName("saveSession should persist structured plan snapshot on update")
    void shouldPersistPlanSnapshotOnUpdate() {
        ApiImportAgentSessionDo existing = new ApiImportAgentSessionDo();
        existing.setId("session-1");
        existing.setVersion(3L);
        when(sessionMapper.selectById("session-1")).thenReturn(existing);

        repository.saveSession(sessionModel());

        ArgumentCaptor<ApiImportAgentSessionDo> captor = ArgumentCaptor.forClass(ApiImportAgentSessionDo.class);
        verify(sessionMapper).updateById(captor.capture());
        ApiImportAgentSessionDo saved = captor.getValue();
        assertEquals("user-1", saved.getOwnerUserId());
        assertEquals(Integer.valueOf(2), saved.getCurrentPlanVersion());
        assertEquals(Integer.valueOf(1), saved.getConfirmedPlanVersion());
        assertTrue(saved.getPlanSnapshotJson().contains("weather-forecast"));
        assertTrue(saved.getPlanSnapshotJson().contains("OpenAI"));
    }

    @Test
    @DisplayName("findOwnedSession and listTurns should restore plan and turn history")
    void shouldRestoreSessionAndTurns() {
        ApiImportAgentSessionDo sessionDo = new ApiImportAgentSessionDo();
        sessionDo.setId("session-1");
        sessionDo.setOwnerUserId("user-1");
        sessionDo.setStatus("CONFIRMED");
        sessionDo.setDocumentSource("https://docs.example.com/weather");
        sessionDo.setDocumentSummary("summary");
        sessionDo.setImportIntent("import weather api");
        sessionDo.setPublisherDisplayName("Alice");
        sessionDo.setCurrentPlanVersion(2);
        sessionDo.setConfirmedPlanVersion(1);
        sessionDo.setPlanSnapshotJson("""
            {"version":2,"executable":true,"summary":"ready","clarificationQuestions":[],"categoryPlans":[{"categoryCode":"tools","categoryName":"Tools","action":"CREATE_IF_MISSING"}],"assetPlans":[{"apiCode":"weather-forecast","assetName":"Weather Forecast","assetType":"AI_API","categoryCode":"tools","requestMethod":"GET","upstreamUrl":"https://upstream.example.com/weather","authScheme":"BEARER_TOKEN","authConfig":"Authorization: Bearer upstream-token","publishAfterImport":true,"aiProfile":{"provider":"OpenAI","model":"gpt-4.1","streamingSupported":true,"capabilityTags":["chat"]}}]}
            """);
        sessionDo.setCreatedAt(LocalDateTime.of(2026, 5, 18, 10, 0));
        sessionDo.setUpdatedAt(LocalDateTime.of(2026, 5, 18, 10, 5));
        when(sessionMapper.selectOwnedById("user-1", "session-1")).thenReturn(sessionDo);

        ApiImportAgentTurnDo turnDo = new ApiImportAgentTurnDo();
        turnDo.setId("turn-1");
        turnDo.setSessionId("session-1");
        turnDo.setTurnIndex(1);
        turnDo.setActorType("AGENT");
        turnDo.setMessageText("ready");
        turnDo.setPlanVersion(2);
        turnDo.setCreatedAt(LocalDateTime.of(2026, 5, 18, 10, 4));
        when(turnMapper.selectBySessionId("session-1")).thenReturn(List.of(turnDo));
        when(turnMapper.countBySessionId("session-1")).thenReturn(1);

        var session = repository.findOwnedSession("user-1", "session-1").orElseThrow();
        var turns = repository.listTurns("session-1");

        assertEquals(ImportAgentSessionStatus.CONFIRMED, session.getStatus());
        assertEquals("weather-forecast", session.getCurrentPlan().getAssetPlans().get(0).getApiCode());
        assertEquals(AuthScheme.HEADER_TOKEN, session.getCurrentPlan().getAssetPlans().get(0).getAuthScheme());
        assertEquals("OpenAI", session.getCurrentPlan().getAssetPlans().get(0).getAiProfile().getProvider());
        assertEquals(1, repository.countTurns("session-1"));
        assertEquals("AGENT", turns.get(0).getActorType().name());
        assertEquals(Integer.valueOf(2), turns.get(0).getPlanVersion());
    }

    private ApiImportAgentSessionModel sessionModel() {
        return new ApiImportAgentSessionModel(
                "session-1",
                "user-1",
                ImportAgentSessionStatus.CONFIRMED,
                "https://docs.example.com/weather",
                "summary",
                "import weather api",
                "Alice",
                2,
                1,
                "run-1",
                "2026-05-18T10:04:00Z",
                new ImportAgentPlanModel(
                        2,
                        true,
                        "ready",
                        List.of(),
                        List.of(new ImportCategoryPlanModel("tools", "Tools", ImportCategoryPlanAction.CREATE_IF_MISSING)),
                        List.of(new ImportAssetPlanModel(
                                "weather-forecast",
                                "Weather Forecast",
                                AssetType.AI_API,
                                "tools",
                                RequestMethod.GET,
                                "https://upstream.example.com/weather",
                                AuthScheme.HEADER_TOKEN,
                                "Authorization: Bearer upstream-token",
                                null,
                                null,
                                null,
                                 null,
                                 null,
                                 true,
                                 null,
                                 new ImportAiProfileModel("OpenAI", "gpt-4.1", true, List.of("chat"))
                        ))
                ),
                List.of(new ImportAgentTurnModel("turn-1", "session-1", 1, ImportAgentActorType.AGENT, "ready", 2, "2026-05-18T10:04:00Z")),
                "2026-05-18T10:00:00Z",
                "2026-05-18T10:05:00Z"
        );
    }
}
