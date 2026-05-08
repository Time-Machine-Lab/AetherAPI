package io.github.timemachinelab.adapter.web.controller;

import io.github.timemachinelab.adapter.web.auth.ConsoleSessionPrincipal;
import io.github.timemachinelab.adapter.web.delegate.PlatformProxyProfileWebDelegate;
import io.github.timemachinelab.adapter.web.handler.GlobalExceptionHandler;
import io.github.timemachinelab.api.resp.PlatformProxyAssetCandidatePageResp;
import io.github.timemachinelab.api.resp.PlatformProxyAssetCandidateResp;
import io.github.timemachinelab.api.resp.PlatformProxyProfilePageResp;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class PlatformProxyProfileControllerWebMvcTest {

    @Test
    @DisplayName("list should delegate platform proxy profile query with explicit binding parameters")
    void shouldDelegateListProfiles() throws Exception {
        PlatformProxyProfileWebDelegate delegate = mock(PlatformProxyProfileWebDelegate.class);
        when(delegate.listProfiles(eq("OWNER"), eq(true), eq("cn"), eq(1), eq(20)))
                .thenReturn(new PlatformProxyProfilePageResp(List.of(), 1, 20, 0L));
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new PlatformProxyProfileController(delegate))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        mockMvc.perform(get("/api/v1/platform/proxy-profiles")
                        .requestAttr(
                                ConsoleSessionPrincipal.REQUEST_ATTRIBUTE,
                                new ConsoleSessionPrincipal(
                                        "console-operator",
                                        "console@aetherapi.local",
                                        "Aether Console Operator",
                                        "console@aetherapi.local",
                                        "OWNER"
                                ))
                        .param("enabled", "true")
                        .param("keyword", "cn"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page").value(1))
                .andExpect(jsonPath("$.size").value(20));

        verify(delegate).listProfiles("OWNER", true, "cn", 1, 20);
    }

    @Test
    @DisplayName("asset binding candidates should use static route and delegate query parameters")
    void shouldDelegateAssetBindingCandidateSearch() throws Exception {
        PlatformProxyProfileWebDelegate delegate = mock(PlatformProxyProfileWebDelegate.class);
        when(delegate.listAssetBindingCandidates(eq("OWNER"), org.mockito.ArgumentMatchers.argThat(req ->
                "weather".equals(req.getKeyword())
                        && "PUBLISHED".equals(req.getStatus())
                        && "profile-1".equals(req.getBoundProfileId())
                        && req.getPage() == 2
                        && req.getSize() == 10
        ))).thenReturn(new PlatformProxyAssetCandidatePageResp(List.of(
                new PlatformProxyAssetCandidateResp(
                        "weather-forecast",
                        "Weather Forecast",
                        "STANDARD_API",
                        "PUBLISHED",
                        "Owner",
                        "profile-1",
                        "default-cn",
                        "Default CN",
                        "2026-05-01T08:00:00Z",
                        "2026-05-02T08:00:00Z"
                )
        ), 2, 10, 1L));
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new PlatformProxyProfileController(delegate))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        mockMvc.perform(get("/api/v1/platform/proxy-profiles/asset-binding-candidates")
                        .requestAttr(
                                ConsoleSessionPrincipal.REQUEST_ATTRIBUTE,
                                new ConsoleSessionPrincipal(
                                        "console-operator",
                                        "console@aetherapi.local",
                                        "Aether Console Operator",
                                        "console@aetherapi.local",
                                        "OWNER"
                                ))
                        .param("keyword", "weather")
                        .param("status", "PUBLISHED")
                        .param("boundProfileId", "profile-1")
                        .param("page", "2")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].apiCode").value("weather-forecast"))
                .andExpect(jsonPath("$.items[0].proxyProfileCode").value("default-cn"))
                .andExpect(jsonPath("$.items[0].proxyHost").doesNotExist())
                .andExpect(jsonPath("$.items[0].proxyPort").doesNotExist())
                .andExpect(jsonPath("$.items[0].password").doesNotExist())
                .andExpect(jsonPath("$.page").value(2));
    }
}
