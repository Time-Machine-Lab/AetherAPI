package io.github.timemachinelab.adapter.web.controller;

import io.github.timemachinelab.adapter.web.delegate.PlatformProxyProfileWebDelegate;
import io.github.timemachinelab.adapter.web.handler.GlobalExceptionHandler;
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
        when(delegate.listProfiles(eq(null), eq(true), eq("cn"), eq(1), eq(20)))
                .thenReturn(new PlatformProxyProfilePageResp(List.of(), 1, 20, 0L));
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new PlatformProxyProfileController(delegate))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        mockMvc.perform(get("/api/v1/platform/proxy-profiles")
                        .param("enabled", "true")
                        .param("keyword", "cn"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page").value(1))
                .andExpect(jsonPath("$.size").value(20));

        verify(delegate).listProfiles(null, true, "cn", 1, 20);
    }
}
