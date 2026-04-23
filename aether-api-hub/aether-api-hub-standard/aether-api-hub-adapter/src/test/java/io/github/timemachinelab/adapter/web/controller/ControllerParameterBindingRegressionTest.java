package io.github.timemachinelab.adapter.web.controller;

import io.github.timemachinelab.adapter.web.delegate.ApiAssetWebDelegate;
import io.github.timemachinelab.adapter.web.delegate.ApiCallLogWebDelegate;
import io.github.timemachinelab.adapter.web.delegate.ApiCredentialWebDelegate;
import io.github.timemachinelab.adapter.web.delegate.CatalogDiscoveryWebDelegate;
import io.github.timemachinelab.adapter.web.delegate.CategoryWebDelegate;
import io.github.timemachinelab.adapter.web.delegate.UnifiedAccessWebDelegate;
import io.github.timemachinelab.api.req.ListApiCallLogReq;
import org.springframework.http.ResponseEntity;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.security.Principal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ControllerParameterBindingRegressionTest {

    private static final Principal CURRENT_USER = () -> "user-1";

    @Test
    void categoryListBindsPagingParameters() throws Exception {
        CategoryWebDelegate delegate = mock(CategoryWebDelegate.class);
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new CategoryController(delegate)).build();

        mockMvc.perform(get("/api/v1/categories")
                        .param("status", "ENABLED")
                        .param("page", "2")
                        .param("size", "10"))
                .andExpect(status().isOk());

        verify(delegate).listCategories("ENABLED", 2, 10);
    }

    @Test
    void categoryDetailBindsPathVariable() throws Exception {
        CategoryWebDelegate delegate = mock(CategoryWebDelegate.class);
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new CategoryController(delegate)).build();

        mockMvc.perform(get("/api/v1/categories/tools"))
                .andExpect(status().isOk());

        verify(delegate).getCategoryByCode("tools");
    }

    @Test
    void assetDetailBindsPathVariable() throws Exception {
        ApiAssetWebDelegate delegate = mock(ApiAssetWebDelegate.class);
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new ApiAssetController(delegate)).build();

        mockMvc.perform(get("/api/v1/assets/deepseek-v3"))
                .andExpect(status().isOk());

        verify(delegate).getAssetByCode("deepseek-v3");
    }

    @Test
    void currentUserApiKeyListBindsPagingParameters() throws Exception {
        ApiCredentialWebDelegate delegate = mock(ApiCredentialWebDelegate.class);
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new ApiCredentialController(delegate)).build();

        mockMvc.perform(get("/api/v1/current-user/api-keys")
                        .principal(CURRENT_USER)
                        .param("status", "ENABLED")
                        .param("page", "1")
                        .param("size", "20"))
                .andExpect(status().isOk());

        verify(delegate).listApiCredentials("user-1", "ENABLED", 1, 20);
    }

    @Test
    void discoveryDetailBindsPathVariable() throws Exception {
        CatalogDiscoveryWebDelegate delegate = mock(CatalogDiscoveryWebDelegate.class);
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new CatalogDiscoveryController(delegate)).build();

        mockMvc.perform(get("/api/v1/discovery/assets/chat-completions"))
                .andExpect(status().isOk());

        verify(delegate).getAssetDetail("chat-completions");
    }

    @Test
    void callLogListBindsQueryParameters() throws Exception {
        ApiCallLogWebDelegate delegate = mock(ApiCallLogWebDelegate.class);
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new ApiCallLogController(delegate)).build();

        mockMvc.perform(get("/api/v1/current-user/api-call-logs")
                        .principal(CURRENT_USER)
                        .param("targetApiCode", "chat-completions")
                        .param("invocationStartAt", "2026-04-19T08:00:00Z")
                        .param("invocationEndAt", "2026-04-19T12:00:00Z")
                        .param("page", "3")
                        .param("size", "15"))
                .andExpect(status().isOk());

        verify(delegate).listApiCallLogs(eq("user-1"), argThat(this::matchesCallLogQuery));
    }

    @Test
    void unifiedAccessBindsApiCodePathVariableExplicitly() throws Exception {
        UnifiedAccessWebDelegate delegate = mock(UnifiedAccessWebDelegate.class);
        when(delegate.invoke(eq("unknown-api"), eq("GET"), any(), any(), eq(null), eq(null)))
                .thenReturn(ResponseEntity.ok().build());
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new UnifiedAccessController(delegate)).build();

        mockMvc.perform(get("/api/v1/access/unknown-api")
                        .header("X-Aether-Api-Key", "ak_live_validation_key"))
                .andExpect(status().isOk());

        verify(delegate).invoke(eq("unknown-api"), eq("GET"), any(), any(), eq(null), eq(null));
    }

    private boolean matchesCallLogQuery(ListApiCallLogReq req) {
        return req != null
                && "chat-completions".equals(req.getTargetApiCode())
                && "2026-04-19T08:00:00Z".equals(req.getInvocationStartAt())
                && "2026-04-19T12:00:00Z".equals(req.getInvocationEndAt())
                && req.getPage() == 3
                && req.getSize() == 15;
    }
}
