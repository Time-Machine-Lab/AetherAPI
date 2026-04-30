package io.github.timemachinelab.adapter.web.controller;

import io.github.timemachinelab.adapter.web.delegate.ApiAssetWebDelegate;
import io.github.timemachinelab.adapter.web.handler.GlobalExceptionHandler;
import io.github.timemachinelab.api.resp.ApiAssetResp;
import io.github.timemachinelab.domain.catalog.model.AssetStatus;
import io.github.timemachinelab.domain.catalog.model.AssetType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.security.Principal;

import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ApiAssetControllerWebMvcTest {

    private static final Principal CURRENT_USER = () -> "user-1";

    @Test
    @DisplayName("asset list should reject invalid paging parameters")
    void shouldRejectInvalidPagingParameters() throws Exception {
        ApiAssetWebDelegate delegate = mock(ApiAssetWebDelegate.class);
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new ApiAssetController(delegate))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        mockMvc.perform(get("/api/v1/current-user/assets")
                        .principal(CURRENT_USER)
                        .param("page", "0")
                        .param("size", "101"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("ASSET_INVALID_QUERY"))
                .andExpect(jsonPath("$.message").value("Invalid asset list query parameters"));
    }

    @Test
    @DisplayName("asset list should reject invalid status filter")
    void shouldRejectInvalidStatusFilter() throws Exception {
        ApiAssetWebDelegate delegate = mock(ApiAssetWebDelegate.class);
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new ApiAssetController(delegate))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        mockMvc.perform(get("/api/v1/current-user/assets")
                        .principal(CURRENT_USER)
                        .param("status", "ARCHIVED"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("ASSET_INVALID_QUERY"))
                .andExpect(jsonPath("$.message").value("Invalid asset list query parameters"));
    }

    @Test
    @DisplayName("delete should delegate current user asset deletion and return deleted marker")
    void shouldDelegateCurrentUserAssetDeletionAndReturnDeletedMarker() throws Exception {
        ApiAssetWebDelegate delegate = mock(ApiAssetWebDelegate.class);
        when(delegate.deleteAsset(eq("user-1"), eq("weather-forecast"))).thenReturn(new ApiAssetResp(
                "asset-1",
                "weather-forecast",
                "Weather Forecast",
                AssetType.STANDARD_API,
                "tools",
                AssetStatus.PUBLISHED,
                "Alice",
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                true,
                "2026-04-29T08:00:00Z",
                "2026-04-29T08:30:00Z"
        ));
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new ApiAssetController(delegate))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        mockMvc.perform(delete("/api/v1/current-user/assets/weather-forecast")
                        .principal(CURRENT_USER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.apiCode").value("weather-forecast"))
                .andExpect(jsonPath("$.deleted").value(true));
        verify(delegate).deleteAsset("user-1", "weather-forecast");
    }
}
