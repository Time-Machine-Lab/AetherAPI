package io.github.timemachinelab.adapter.web.delegate;

import io.github.timemachinelab.domain.catalog.model.AssetType;
import io.github.timemachinelab.domain.catalog.model.AuthScheme;
import io.github.timemachinelab.domain.catalog.model.RequestMethod;
import io.github.timemachinelab.adapter.web.config.ImportAgentStreamProperties;
import io.github.timemachinelab.api.req.CreateImportAgentSessionReq;
import io.github.timemachinelab.service.model.ApiImportAgentRunModel;
import io.github.timemachinelab.service.model.ApiImportAgentSessionModel;
import io.github.timemachinelab.service.model.AsyncTaskConfigModel;
import io.github.timemachinelab.service.model.CreateImportAgentSessionCommand;
import io.github.timemachinelab.service.model.ImportAgentActorType;
import io.github.timemachinelab.service.model.ImportAgentPlanModel;
import io.github.timemachinelab.service.model.ImportAgentRunStatus;
import io.github.timemachinelab.service.model.ImportAgentSessionStatus;
import io.github.timemachinelab.service.model.ImportAgentStepType;
import io.github.timemachinelab.service.model.ImportAgentTurnModel;
import io.github.timemachinelab.service.model.ImportAiProfileModel;
import io.github.timemachinelab.service.model.ImportAssetPlanModel;
import io.github.timemachinelab.service.model.ImportCategoryPlanAction;
import io.github.timemachinelab.service.model.ImportCategoryPlanModel;
import io.github.timemachinelab.service.model.ImportStepResultModel;
import io.github.timemachinelab.service.model.ImportStepResultStatus;
import io.github.timemachinelab.service.port.in.ApiImportAgentUseCase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ApiImportAgentWebDelegateTest {

    private static final ImportAgentStreamProperties STREAM_PROPERTIES = streamProperties();

    @Test
    @DisplayName("create session should map request into command and nested response")
    void shouldCreateSessionWithMappedResponse() {
        ApiImportAgentUseCase useCase = mock(ApiImportAgentUseCase.class);
        when(useCase.createSession(any(CreateImportAgentSessionCommand.class))).thenReturn(sessionModel());
        ApiImportAgentWebDelegate delegate = new ApiImportAgentWebDelegate(useCase, STREAM_PROPERTIES);

        CreateImportAgentSessionReq req = new CreateImportAgentSessionReq();
        req.setDocumentSource("https://docs.example.com/weather");
        req.setDocumentSummary("summary");
        req.setImportIntent("import weather api");

        var response = delegate.createSession("user-1", "Alice", req);

        ArgumentCaptor<CreateImportAgentSessionCommand> captor = ArgumentCaptor.forClass(CreateImportAgentSessionCommand.class);
        verify(useCase).createSession(captor.capture());
        assertEquals("user-1", captor.getValue().getOwnerUserId());
        assertEquals("Alice", captor.getValue().getPublisherDisplayName());
        assertEquals("import weather api", captor.getValue().getImportIntent());
        assertEquals("session-1", response.getSessionId());
        assertEquals("WAITING_FOR_CONFIRMATION", response.getStatus());
        assertEquals("tools", response.getCurrentPlan().getCategoryPlans().get(0).getCategoryCode());
        assertEquals("HEADER_TOKEN", response.getCurrentPlan().getAssetPlans().get(0).getAuthScheme());
        assertEquals("Authorization: Bearer upstream-token", response.getCurrentPlan().getAssetPlans().get(0).getAuthConfig());
        assertEquals("OpenAI", response.getCurrentPlan().getAssetPlans().get(0).getAiProfile().getProvider());
        assertEquals("AGENT", response.getTurns().get(0).getActorType());
    }

    @Test
    @DisplayName("get run should map step results and affected assets")
    void shouldMapRunResponse() {
        ApiImportAgentUseCase useCase = mock(ApiImportAgentUseCase.class);
        when(useCase.getRun("user-1", "run-1")).thenReturn(runModel());
        ApiImportAgentWebDelegate delegate = new ApiImportAgentWebDelegate(useCase, STREAM_PROPERTIES);

        var response = delegate.getRun("user-1", "run-1");

        assertEquals("run-1", response.getRunId());
        assertEquals("SUCCEEDED", response.getStatus());
        assertEquals(List.of("weather-forecast"), response.getAffectedApiCodes());
        assertEquals("ATTACH_AI_PROFILE", response.getStepResults().get(1).getStepType());
        assertEquals("AI profile attached", response.getStepResults().get(1).getMessage());
    }

    @Test
    @DisplayName("get session should tolerate malformed async authMode values stored in existing plans")
    void shouldMapMalformedAsyncAuthModeAlias() {
        ApiImportAgentUseCase useCase = mock(ApiImportAgentUseCase.class);
        when(useCase.getSession("user-1", "session-1")).thenReturn(sessionModelWithMalformedAsyncAuthMode());
        ApiImportAgentWebDelegate delegate = new ApiImportAgentWebDelegate(useCase, STREAM_PROPERTIES);

        var response = delegate.getSession("user-1", "session-1");

        assertEquals("OVERRIDE", response.getCurrentPlan().getAssetPlans().get(0).getAsyncTaskConfig().getAuthMode().name());
        assertEquals("HEADER_TOKEN", response.getCurrentPlan().getAssetPlans().get(0).getAsyncTaskConfig().getAuthScheme().name());
    }

    private ApiImportAgentSessionModel sessionModel() {
        return new ApiImportAgentSessionModel(
                "session-1",
                "user-1",
                ImportAgentSessionStatus.WAITING_FOR_CONFIRMATION,
                "https://docs.example.com/weather",
                "summary",
                "import weather api",
                "Alice",
                1,
                null,
                null,
                null,
                new ImportAgentPlanModel(
                        1,
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
                                "template",
                                "{\"city\":\"Shanghai\"}",
                                "{\"temperature\":26}",
                                 "{\"type\":\"object\"}",
                                 "{\"type\":\"object\"}",
                                 true,
                                 null,
                                 new ImportAiProfileModel("OpenAI", "gpt-4.1", true, List.of("chat", "reasoning"))
                        ))
                ),
                List.of(new ImportAgentTurnModel("turn-1", "session-1", 1, ImportAgentActorType.AGENT, "ready", 1, "2026-05-18T10:00:00Z")),
                "2026-05-18T10:00:00Z",
                "2026-05-18T10:05:00Z"
        );
    }

    private ApiImportAgentRunModel runModel() {
        return new ApiImportAgentRunModel(
                "run-1",
                "session-1",
                "user-1",
                1,
                ImportAgentRunStatus.SUCCEEDED,
                "done",
                null,
                List.of("weather-forecast"),
                List.of(
                        new ImportStepResultModel(ImportAgentStepType.REGISTER_ASSET, "weather-forecast", ImportStepResultStatus.SUCCEEDED, "Asset draft created"),
                        new ImportStepResultModel(ImportAgentStepType.ATTACH_AI_PROFILE, "weather-forecast", ImportStepResultStatus.SUCCEEDED, "AI profile attached")
                ),
                "2026-05-18T10:10:00Z",
                "2026-05-18T10:11:00Z"
        );
    }

            private ApiImportAgentSessionModel sessionModelWithMalformedAsyncAuthMode() {
            return new ApiImportAgentSessionModel(
                "session-1",
                "user-1",
                ImportAgentSessionStatus.WAITING_FOR_CONFIRMATION,
                "https://docs.example.com/video",
                "summary",
                "import video api",
                "Alice",
                1,
                null,
                null,
                null,
                new ImportAgentPlanModel(
                    1,
                    true,
                    "ready",
                    List.of(),
                    List.of(new ImportCategoryPlanModel("video", "Video", ImportCategoryPlanAction.CREATE_IF_MISSING)),
                    List.of(new ImportAssetPlanModel(
                        "video-submit",
                        "Video Submit",
                        AssetType.STANDARD_API,
                        "video",
                        RequestMethod.POST,
                        "https://upstream.example.com/video/submit",
                        AuthScheme.NONE,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        true,
                        new AsyncTaskConfigModel(
                            true,
                            "GET",
                            "https://upstream.example.com/tasks/{taskId}",
                            "HEADER_TOKEN",
                            null,
                            "Authorization: Bearer upstream-token",
                            null,
                            null,
                            null),
                        null
                    ))
                ),
                List.of(new ImportAgentTurnModel("turn-1", "session-1", 1, ImportAgentActorType.AGENT, "ready", 1, "2026-05-18T10:00:00Z")),
                "2026-05-18T10:00:00Z",
                "2026-05-18T10:05:00Z"
            );
            }

    private static ImportAgentStreamProperties streamProperties() {
        ImportAgentStreamProperties properties = new ImportAgentStreamProperties();
        properties.setTimeoutSeconds(180);
        return properties;
    }
}
