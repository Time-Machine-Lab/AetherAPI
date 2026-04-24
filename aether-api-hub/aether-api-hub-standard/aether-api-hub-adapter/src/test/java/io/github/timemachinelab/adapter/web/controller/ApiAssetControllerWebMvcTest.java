package io.github.timemachinelab.adapter.web.controller;

import io.github.timemachinelab.adapter.web.delegate.ApiAssetWebDelegate;
import io.github.timemachinelab.adapter.web.handler.GlobalExceptionHandler;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ApiAssetControllerWebMvcTest {

    @Test
    @DisplayName("asset list should reject invalid paging parameters")
    void shouldRejectInvalidPagingParameters() throws Exception {
        ApiAssetWebDelegate delegate = mock(ApiAssetWebDelegate.class);
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new ApiAssetController(delegate))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        mockMvc.perform(get("/api/v1/assets")
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

        mockMvc.perform(get("/api/v1/assets")
                        .param("status", "ARCHIVED"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("ASSET_INVALID_QUERY"))
                .andExpect(jsonPath("$.message").value("Invalid asset list query parameters"));
    }
}
