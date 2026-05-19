package io.github.timemachinelab.adapter.web.controller;

import io.github.timemachinelab.adapter.web.auth.ConsoleSessionPrincipal;
import io.github.timemachinelab.adapter.web.delegate.ApiImportAgentWebDelegate;
import io.github.timemachinelab.adapter.web.handler.GlobalExceptionHandler;
import io.github.timemachinelab.api.req.AppendImportAgentTurnReq;
import io.github.timemachinelab.api.req.ConfirmImportAgentPlanReq;
import io.github.timemachinelab.api.req.CreateImportAgentSessionReq;
import io.github.timemachinelab.api.req.StartImportAgentRunReq;
import io.github.timemachinelab.api.resp.ApiImportAgentRunResp;
import io.github.timemachinelab.api.resp.ApiImportAgentSessionResp;
import io.github.timemachinelab.api.resp.ImportAgentPlanResp;
import io.github.timemachinelab.api.resp.ImportAgentTurnResp;
import io.github.timemachinelab.api.resp.ImportAssetPlanResp;
import io.github.timemachinelab.api.resp.ImportCategoryPlanResp;
import io.github.timemachinelab.api.resp.ImportStepResultResp;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.security.Principal;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ApiImportAgentControllerWebMvcTest {

    private static final Principal CURRENT_USER = () -> "fallback-user";

    @Test
    @DisplayName("create session should bind request body and use console principal identity")
    void shouldCreateSessionUsingConsolePrincipal() throws Exception {
        ApiImportAgentWebDelegate delegate = mock(ApiImportAgentWebDelegate.class);
        when(delegate.createSession(eq("user-1"), eq("Alice"), org.mockito.ArgumentMatchers.any(CreateImportAgentSessionReq.class)))
                .thenReturn(sessionResp());
        MockMvc mockMvc = mockMvc(delegate);

        mockMvc.perform(post("/api/v1/current-user/import-agent/sessions")
                        .principal(CURRENT_USER)
                        .requestAttr(ConsoleSessionPrincipal.REQUEST_ATTRIBUTE, consolePrincipal())
                        .contentType("application/json")
                        .content("""
                                {
                                  "documentSource": "https://docs.example.com/weather",
                                                                                                                                        "documentSummary": "summary",
                                  "importIntent": "import weather api"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.sessionId").value("session-1"))
                .andExpect(jsonPath("$.currentPlan.version").value(1))
                .andExpect(jsonPath("$.currentPlan.categoryPlans[0].categoryCode").value("tools"))
                .andExpect(jsonPath("$.turns[0].actorType").value("AGENT"));

        ArgumentCaptor<CreateImportAgentSessionReq> reqCaptor = ArgumentCaptor.forClass(CreateImportAgentSessionReq.class);
        verify(delegate).createSession(eq("user-1"), eq("Alice"), reqCaptor.capture());
        assertEquals("import weather api", reqCaptor.getValue().getImportIntent());
        assertEquals("https://docs.example.com/weather", reqCaptor.getValue().getDocumentSource());
    }

    @Test
    @DisplayName("session detail should delegate owner scoped lookup")
    void shouldGetSession() throws Exception {
        ApiImportAgentWebDelegate delegate = mock(ApiImportAgentWebDelegate.class);
        when(delegate.getSession("user-1", "session-1")).thenReturn(sessionResp());
        MockMvc mockMvc = mockMvc(delegate);

        mockMvc.perform(get("/api/v1/current-user/import-agent/sessions/session-1")
                        .principal(CURRENT_USER)
                        .requestAttr(ConsoleSessionPrincipal.REQUEST_ATTRIBUTE, consolePrincipal()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sessionId").value("session-1"))
                .andExpect(jsonPath("$.status").value("WAITING_FOR_CONFIRMATION"));

        verify(delegate).getSession("user-1", "session-1");
    }

    @Test
    @DisplayName("append turn should bind request body and return refreshed plan")
    void shouldAppendTurn() throws Exception {
        ApiImportAgentWebDelegate delegate = mock(ApiImportAgentWebDelegate.class);
        when(delegate.appendTurn(eq("user-1"), eq("session-1"), org.mockito.ArgumentMatchers.any(AppendImportAgentTurnReq.class)))
                .thenReturn(sessionResp());
        MockMvc mockMvc = mockMvc(delegate);

        mockMvc.perform(post("/api/v1/current-user/import-agent/sessions/session-1/turns")
                        .principal(CURRENT_USER)
                        .requestAttr(ConsoleSessionPrincipal.REQUEST_ATTRIBUTE, consolePrincipal())
                        .contentType("application/json")
                        .content("""
                                {
                                  "message": "please publish it"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currentPlan.assetPlans[0].apiCode").value("weather-forecast"));

        ArgumentCaptor<AppendImportAgentTurnReq> reqCaptor = ArgumentCaptor.forClass(AppendImportAgentTurnReq.class);
        verify(delegate).appendTurn(eq("user-1"), eq("session-1"), reqCaptor.capture());
        assertEquals("please publish it", reqCaptor.getValue().getMessage());
    }

                @Test
                @DisplayName("stream endpoints should expose text event stream payloads")
                void shouldExposeStreamEndpoints() throws Exception {
                                ApiImportAgentWebDelegate delegate = mock(ApiImportAgentWebDelegate.class);
                                doAnswer(invocation -> {
                                                var response = invocation.getArgument(4, jakarta.servlet.http.HttpServletResponse.class);
                                                response.setStatus(200);
                                                response.setContentType("text/event-stream");
                                                response.getOutputStream().write("event: status\ndata: {\"phase\":\"planning\"}\n\n".getBytes(StandardCharsets.UTF_8));
                                                response.getOutputStream().flush();
                                                return null;
                                }).when(delegate).createSessionStreamToResponse(
                                                                eq("user-1"),
                                                                eq("Alice"),
                                                                org.mockito.ArgumentMatchers.any(CreateImportAgentSessionReq.class),
                                                                org.mockito.ArgumentMatchers.any(),
                                                                org.mockito.ArgumentMatchers.any()
                                );
                                doAnswer(invocation -> {
                                                var response = invocation.getArgument(4, jakarta.servlet.http.HttpServletResponse.class);
                                                response.setStatus(200);
                                                response.setContentType("text/event-stream");
                                                response.getOutputStream().write("event: message\ndata: {\"delta\":\"hello\"}\n\n".getBytes(StandardCharsets.UTF_8));
                                                response.getOutputStream().flush();
                                                return null;
                                }).when(delegate).appendTurnStreamToResponse(
                                                                eq("user-1"),
                                                                eq("session-1"),
                                                                org.mockito.ArgumentMatchers.any(AppendImportAgentTurnReq.class),
                                                                org.mockito.ArgumentMatchers.any(),
                                                                org.mockito.ArgumentMatchers.any()
                                );

                                MockMvc mockMvc = mockMvc(delegate);

                                mockMvc.perform(post("/api/v1/current-user/import-agent/sessions/stream")
                                                                                                .principal(CURRENT_USER)
                                                                                                .requestAttr(ConsoleSessionPrincipal.REQUEST_ATTRIBUTE, consolePrincipal())
                                                                                                .contentType("application/json")
                                                                                                .content("""
                                                                                                                                {
                                                                                                                                        "importIntent": "import weather api"
                                                                                                                                }
                                                                                                                                """))
                                                                .andExpect(status().isOk())
                                                                                                                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.content().contentType("text/event-stream"))
                                                                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.content().string("event: status\ndata: {\"phase\":\"planning\"}\n\n"));

                                mockMvc.perform(post("/api/v1/current-user/import-agent/sessions/session-1/turns/stream")
                                                                                                .principal(CURRENT_USER)
                                                                                                .requestAttr(ConsoleSessionPrincipal.REQUEST_ATTRIBUTE, consolePrincipal())
                                                                                                .contentType("application/json")
                                                                                                .content("""
                                                                                                                                {
                                                                                                                                        "message": "please publish it"
                                                                                                                                }
                                                                                                                                """))
                                                                .andExpect(status().isOk())
                                                                                                                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.content().contentType("text/event-stream"))
                                                                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.content().string("event: message\ndata: {\"delta\":\"hello\"}\n\n"));
                }

    @Test
    @DisplayName("confirm should reject invalid plan version")
    void shouldRejectInvalidConfirmRequest() throws Exception {
        ApiImportAgentWebDelegate delegate = mock(ApiImportAgentWebDelegate.class);
        MockMvc mockMvc = mockMvc(delegate);

        mockMvc.perform(patch("/api/v1/current-user/import-agent/sessions/session-1/confirm")
                        .principal(CURRENT_USER)
                        .requestAttr(ConsoleSessionPrincipal.REQUEST_ATTRIBUTE, consolePrincipal())
                        .contentType("application/json")
                        .content("""
                                {
                                  "planVersion": 0
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("IMPORT_AGENT_INVALID_REQUEST"));
    }

    @Test
    @DisplayName("run endpoints should bind execution request and expose run detail")
    void shouldStartAndQueryRun() throws Exception {
        ApiImportAgentWebDelegate delegate = mock(ApiImportAgentWebDelegate.class);
        when(delegate.startRun(eq("user-1"), eq("Alice"), eq("session-1"), org.mockito.ArgumentMatchers.any(StartImportAgentRunReq.class)))
                .thenReturn(runResp());
        when(delegate.getRun("user-1", "run-1")).thenReturn(runResp());
        MockMvc mockMvc = mockMvc(delegate);

        mockMvc.perform(post("/api/v1/current-user/import-agent/sessions/session-1/runs")
                        .principal(CURRENT_USER)
                        .requestAttr(ConsoleSessionPrincipal.REQUEST_ATTRIBUTE, consolePrincipal())
                        .contentType("application/json")
                        .content("""
                                {
                                  "planVersion": 1
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.runId").value("run-1"))
                .andExpect(jsonPath("$.stepResults[0].stepType").value("REGISTER_ASSET"));

        mockMvc.perform(get("/api/v1/current-user/import-agent/runs/run-1")
                        .principal(CURRENT_USER)
                        .requestAttr(ConsoleSessionPrincipal.REQUEST_ATTRIBUTE, consolePrincipal()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCEEDED"))
                .andExpect(jsonPath("$.affectedApiCodes[0]").value("weather-forecast"));

        ArgumentCaptor<StartImportAgentRunReq> reqCaptor = ArgumentCaptor.forClass(StartImportAgentRunReq.class);
        verify(delegate).startRun(eq("user-1"), eq("Alice"), eq("session-1"), reqCaptor.capture());
        verify(delegate).getRun("user-1", "run-1");
        assertEquals(1, reqCaptor.getValue().getPlanVersion());
    }

    private MockMvc mockMvc(ApiImportAgentWebDelegate delegate) {
        return MockMvcBuilders.standaloneSetup(new ApiImportAgentController(delegate))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    private ConsoleSessionPrincipal consolePrincipal() {
        return new ConsoleSessionPrincipal("user-1", "alice", "Alice", "alice@example.com", "OWNER");
    }

    private ApiImportAgentSessionResp sessionResp() {
        return new ApiImportAgentSessionResp(
                "session-1",
                "WAITING_FOR_CONFIRMATION",
                "https://docs.example.com/weather",
                "summary",
                "import weather api",
                "Alice",
                1,
                null,
                null,
                new ImportAgentPlanResp(
                        1,
                        true,
                        "ready",
                        List.of(),
                        List.of(new ImportCategoryPlanResp("tools", "Tools", "CREATE_IF_MISSING")),
                        List.of(new ImportAssetPlanResp(
                                "weather-forecast",
                                "Weather Forecast",
                                "STANDARD_API",
                                "tools",
                                "GET",
                                "https://upstream.example.com/weather",
                                "NONE",
                                null,
                                null,
                                null,
                                null,
                                 null,
                                 null,
                                 true,
                                 null,
                                 null
                         ))
                ),
                List.of(new ImportAgentTurnResp("turn-1", "AGENT", "ready", 1, "2026-05-18T10:00:00Z")),
                "2026-05-18T10:00:00Z",
                "2026-05-18T10:05:00Z"
        );
    }

    private ApiImportAgentRunResp runResp() {
        return new ApiImportAgentRunResp(
                "run-1",
                "session-1",
                1,
                "SUCCEEDED",
                "done",
                null,
                List.of("weather-forecast"),
                List.of(new ImportStepResultResp("REGISTER_ASSET", "weather-forecast", "SUCCEEDED", "Asset draft created")),
                "2026-05-18T10:10:00Z",
                "2026-05-18T10:11:00Z"
        );
    }
}
