package io.github.timemachinelab.adapter.web.controller;

import io.github.timemachinelab.adapter.web.delegate.ApiCallLogWebDelegate;
import io.github.timemachinelab.adapter.web.delegate.ApiCredentialWebDelegate;
import io.github.timemachinelab.adapter.web.delegate.ConsoleAuthWebDelegate;
import io.github.timemachinelab.adapter.web.delegate.UnifiedAccessWebDelegate;
import io.github.timemachinelab.adapter.web.handler.GlobalExceptionHandler;
import io.github.timemachinelab.adapter.web.interceptor.ConsoleSessionAuthInterceptor;
import io.github.timemachinelab.adapter.web.resolver.ConsoleSessionPrincipalArgumentResolver;
import io.github.timemachinelab.api.error.CatalogErrorCodes;
import io.github.timemachinelab.domain.consumerauth.model.ConsumerAuthDomainException;
import io.github.timemachinelab.service.application.ConsoleSessionAuthApplicationService;
import io.github.timemachinelab.service.model.ConsoleSignInCommand;
import io.github.timemachinelab.service.model.PlatformPreForwardFailureType;
import io.github.timemachinelab.service.model.UnifiedAccessPlatformFailureException;
import io.github.timemachinelab.service.model.UnifiedAccessPlatformFailureModel;
import io.github.timemachinelab.service.port.out.ConsoleSessionSettingsPort;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.servlet.HandlerInterceptor;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ConsoleSessionAuthWebMvcTest {

    private final ConsoleSessionSettingsPort settingsPort = new FixedConsoleSessionSettingsPort();

    @Test
    @DisplayName("console sign-in should return token and current user profile")
    void shouldSignInSuccessfully() throws Exception {
        MockMvc mockMvc = buildMockMvc(mock(ApiCredentialWebDelegate.class), mock(ApiCallLogWebDelegate.class), mock(UnifiedAccessWebDelegate.class));

        mockMvc.perform(post("/api/v1/console/auth/sign-in")
                        .contentType("application/json")
                        .content("""
                                {"loginName":"console@aetherapi.local","password":"change-me-console-password"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.currentUser.userId").value("console-operator"))
                .andExpect(jsonPath("$.currentUser.role").value("OWNER"));
    }

    @Test
    @DisplayName("console sign-in should reject invalid credentials")
    void shouldRejectInvalidSignIn() throws Exception {
        MockMvc mockMvc = buildMockMvc(mock(ApiCredentialWebDelegate.class), mock(ApiCallLogWebDelegate.class), mock(UnifiedAccessWebDelegate.class));

        mockMvc.perform(post("/api/v1/console/auth/sign-in")
                        .contentType("application/json")
                        .content("""
                                {"loginName":"console@aetherapi.local","password":"wrong-password"}
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("CONSOLE_SIGN_IN_CREDENTIALS_INVALID"));
    }

    @Test
    @DisplayName("current session should return current user when token is valid")
    void shouldReturnCurrentSession() throws Exception {
        MockMvc mockMvc = buildMockMvc(mock(ApiCredentialWebDelegate.class), mock(ApiCallLogWebDelegate.class), mock(UnifiedAccessWebDelegate.class));

        mockMvc.perform(get("/api/v1/console/auth/current-session")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + issueToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currentUser.displayName").value("Aether Console Operator"));
    }

    @Test
    @DisplayName("current session should reject missing token")
    void shouldRejectMissingCurrentSessionToken() throws Exception {
        MockMvc mockMvc = buildMockMvc(mock(ApiCredentialWebDelegate.class), mock(ApiCallLogWebDelegate.class), mock(UnifiedAccessWebDelegate.class));

        mockMvc.perform(get("/api/v1/console/auth/current-session"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("CONSOLE_SESSION_UNAUTHORIZED"));
    }

    @Test
    @DisplayName("current-user api credential endpoint should resolve principal from console token")
    void shouldResolvePrincipalForCurrentUserApiKeys() throws Exception {
        ApiCredentialWebDelegate credentialDelegate = mock(ApiCredentialWebDelegate.class);
        when(credentialDelegate.listApiCredentials(eq("console-operator"), eq("ENABLED"), eq(1), eq(20))).thenReturn(null);
        MockMvc mockMvc = buildMockMvc(credentialDelegate, mock(ApiCallLogWebDelegate.class), mock(UnifiedAccessWebDelegate.class));

        mockMvc.perform(get("/api/v1/current-user/api-keys")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + issueToken())
                        .param("status", "ENABLED")
                        .param("page", "1")
                        .param("size", "20"))
                .andExpect(status().isOk());

        verify(credentialDelegate).listApiCredentials("console-operator", "ENABLED", 1, 20);
    }

    @Test
    @DisplayName("current-user api credential lifecycle endpoint should resolve principal from console token")
    void shouldResolvePrincipalForApiKeyLifecycleActions() throws Exception {
        ApiCredentialWebDelegate credentialDelegate = mock(ApiCredentialWebDelegate.class);
        when(credentialDelegate.disableApiCredential(eq("console-operator"), eq("credential-1"))).thenReturn(null);
        MockMvc mockMvc = buildMockMvc(credentialDelegate, mock(ApiCallLogWebDelegate.class), mock(UnifiedAccessWebDelegate.class));

        mockMvc.perform(patch("/api/v1/current-user/api-keys/credential-1/disable")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + issueToken()))
                .andExpect(status().isOk());

        verify(credentialDelegate).disableApiCredential("console-operator", "credential-1");
    }

    @Test
    @DisplayName("current-user api credential lifecycle conflict should return business error")
    void shouldReturnBusinessErrorForApiKeyLifecycleConflict() throws Exception {
        ApiCredentialWebDelegate credentialDelegate = mock(ApiCredentialWebDelegate.class);
        when(credentialDelegate.disableApiCredential(eq("console-operator"), eq("credential-1")))
                .thenThrow(new ConsumerAuthDomainException("API credential is already disabled"));
        MockMvc mockMvc = buildMockMvc(credentialDelegate, mock(ApiCallLogWebDelegate.class), mock(UnifiedAccessWebDelegate.class));

        mockMvc.perform(patch("/api/v1/current-user/api-keys/credential-1/disable")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + issueToken()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("API_CREDENTIAL_ALREADY_DISABLED"));
    }

    @Test
    @DisplayName("current-user api call log endpoint should resolve principal from console token")
    void shouldResolvePrincipalForCurrentUserApiCallLogs() throws Exception {
        ApiCallLogWebDelegate callLogDelegate = mock(ApiCallLogWebDelegate.class);
        when(callLogDelegate.listApiCallLogs(eq("console-operator"), any())).thenReturn(null);
        MockMvc mockMvc = buildMockMvc(mock(ApiCredentialWebDelegate.class), callLogDelegate, mock(UnifiedAccessWebDelegate.class));

        mockMvc.perform(get("/api/v1/current-user/api-call-logs")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + issueToken())
                        .param("targetApiCode", "chat-completions")
                        .param("page", "1")
                        .param("size", "20"))
                .andExpect(status().isOk());

        verify(callLogDelegate).listApiCallLogs(eq("console-operator"), any());
    }

    @Test
    @DisplayName("unified access unknown api should return platform failure response")
    void shouldReturnUnifiedAccessPlatformFailureForUnknownApi() throws Exception {
        UnifiedAccessWebDelegate unifiedAccessDelegate = mock(UnifiedAccessWebDelegate.class);
        when(unifiedAccessDelegate.invoke(eq("unknown-api"), eq("GET"), any(), any(), eq(null), eq(null)))
                .thenThrow(new UnifiedAccessPlatformFailureException(new UnifiedAccessPlatformFailureModel(
                        CatalogErrorCodes.ASSET_NOT_FOUND,
                        "Asset not found: unknown-api",
                        PlatformPreForwardFailureType.TARGET_NOT_FOUND,
                        "unknown-api",
                        404
                )));
        MockMvc mockMvc = buildMockMvc(
                mock(ApiCredentialWebDelegate.class),
                mock(ApiCallLogWebDelegate.class),
                unifiedAccessDelegate
        );

        mockMvc.perform(get("/api/v1/access/unknown-api")
                        .header("X-Aether-Api-Key", "ak_live_validation_key"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("ASSET_NOT_FOUND"))
                .andExpect(jsonPath("$.failureType").value("TARGET_NOT_FOUND"))
                .andExpect(jsonPath("$.apiCode").value("unknown-api"));
    }

    private MockMvc buildMockMvc(
            ApiCredentialWebDelegate credentialDelegate,
            ApiCallLogWebDelegate callLogDelegate,
            UnifiedAccessWebDelegate unifiedAccessDelegate) {
        ConsoleSessionAuthApplicationService useCase = new ConsoleSessionAuthApplicationService(settingsPort);
        ConsoleAuthWebDelegate consoleAuthWebDelegate = new ConsoleAuthWebDelegate(useCase);
        ConsoleSessionPrincipalArgumentResolver argumentResolver = new ConsoleSessionPrincipalArgumentResolver();
        ConsoleSessionAuthInterceptor authInterceptor = new ConsoleSessionAuthInterceptor(useCase);

        return MockMvcBuilders.standaloneSetup(
                        new ConsoleAuthController(consoleAuthWebDelegate),
                        new ApiCredentialController(credentialDelegate),
                        new ApiCallLogController(callLogDelegate),
                        new UnifiedAccessController(unifiedAccessDelegate))
                .setCustomArgumentResolvers(argumentResolver)
                .addInterceptors(new PathScopedInterceptor(authInterceptor))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    private String issueToken() {
        ConsoleSessionAuthApplicationService useCase = new ConsoleSessionAuthApplicationService(settingsPort);
        return useCase.signIn(new ConsoleSignInCommand(
                "console@aetherapi.local",
                "change-me-console-password"
        )).getAccessToken();
    }

    private static final class FixedConsoleSessionSettingsPort implements ConsoleSessionSettingsPort {

        @Override
        public String getUserId() {
            return "console-operator";
        }

        @Override
        public String getLoginName() {
            return "console@aetherapi.local";
        }

        @Override
        public String getPassword() {
            return "change-me-console-password";
        }

        @Override
        public String getDisplayName() {
            return "Aether Console Operator";
        }

        @Override
        public String getEmail() {
            return "console@aetherapi.local";
        }

        @Override
        public String getRole() {
            return "OWNER";
        }

        @Override
        public String getTokenSecret() {
            return "change-me-console-session-secret";
        }

        @Override
        public long getTokenTtlSeconds() {
            return 43200;
        }
    }

    private static final class PathScopedInterceptor implements HandlerInterceptor {

        private final HandlerInterceptor delegate;

        private PathScopedInterceptor(HandlerInterceptor delegate) {
            this.delegate = delegate;
        }

        @Override
        public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
            String requestUri = request.getRequestURI();
            if (requestUri != null && (requestUri.startsWith("/api/v1/current-user/")
                    || "/api/v1/console/auth/current-session".equals(requestUri))) {
                return delegate.preHandle(request, response, handler);
            }
            return true;
        }
    }
}
