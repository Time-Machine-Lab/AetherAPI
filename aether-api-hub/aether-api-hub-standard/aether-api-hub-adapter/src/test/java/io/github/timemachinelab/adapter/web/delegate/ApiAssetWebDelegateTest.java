package io.github.timemachinelab.adapter.web.delegate;

import io.github.timemachinelab.api.req.AttachAiCapabilityProfileReq;
import io.github.timemachinelab.api.req.AsyncTaskConfigReq;
import io.github.timemachinelab.api.req.RegisterApiAssetReq;
import io.github.timemachinelab.api.req.ReviseApiAssetReq;
import io.github.timemachinelab.api.resp.ApiAssetResp;
import io.github.timemachinelab.domain.catalog.model.AsyncTaskAuthMode;
import io.github.timemachinelab.domain.catalog.model.AssetStatus;
import io.github.timemachinelab.domain.catalog.model.AssetType;
import io.github.timemachinelab.domain.catalog.model.AuthScheme;
import io.github.timemachinelab.domain.catalog.model.RequestMethod;
import io.github.timemachinelab.service.model.ApiAssetModel;
import io.github.timemachinelab.service.model.AsyncTaskConfigModel;
import io.github.timemachinelab.service.model.AttachAiCapabilityProfileCommand;
import io.github.timemachinelab.service.model.RegisterApiAssetCommand;
import io.github.timemachinelab.service.model.ReviseApiAssetCommand;
import io.github.timemachinelab.service.port.in.ApiAssetUseCase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ApiAssetWebDelegateTest {

    @Test
    @DisplayName("register binding should carry async task config to use case and response")
    void shouldBindAsyncTaskConfigWhenRegisteringAsset() {
        ApiAssetUseCase useCase = mock(ApiAssetUseCase.class);
        when(useCase.registerAsset(any(RegisterApiAssetCommand.class))).thenReturn(asyncAssetModel());
        ApiAssetWebDelegate delegate = new ApiAssetWebDelegate(useCase);
        RegisterApiAssetReq req = new RegisterApiAssetReq("image-generation", AssetType.STANDARD_API, "Image Generation");
        req.setRequestJsonSchema("{\"type\":\"object\",\"required\":[\"prompt\"]}");
        req.setResponseJsonSchema("{\"type\":\"object\",\"properties\":{\"taskId\":{\"type\":\"string\"}}}");
        AsyncTaskConfigReq asyncTaskConfig = new AsyncTaskConfigReq();
        asyncTaskConfig.setEnabled(true);
        asyncTaskConfig.setQueryMethod(RequestMethod.GET);
        asyncTaskConfig.setQueryUrlTemplate("https://provider.example.com/tasks/{taskId}");
        asyncTaskConfig.setAuthMode(AsyncTaskAuthMode.SAME_AS_SUBMIT);
        asyncTaskConfig.setStatusPath("$.status");
        asyncTaskConfig.setResultPath("$.result");
        asyncTaskConfig.setErrorPath("$.error");
        req.setAsyncTaskConfig(asyncTaskConfig);
        req.setCapabilityExtensions(extensionBlock("streaming", true));
        req.setPolicyExtensions(extensionBlock("rateLimitQps", 10));
        req.setMetadataExtensions(extensionBlock("source", "import"));

        ApiAssetResp response = delegate.registerAsset("user-1", "Alice", req);

        ArgumentCaptor<RegisterApiAssetCommand> commandCaptor =
                ArgumentCaptor.forClass(RegisterApiAssetCommand.class);
        verify(useCase).registerAsset(commandCaptor.capture());
        AsyncTaskConfigModel commandConfig = commandCaptor.getValue().getAsyncTaskConfig();
        assertEquals("{\"type\":\"object\",\"required\":[\"prompt\"]}", commandCaptor.getValue().getRequestJsonSchema());
        assertEquals("{\"type\":\"object\",\"properties\":{\"taskId\":{\"type\":\"string\"}}}", commandCaptor.getValue().getResponseJsonSchema());
        assertEquals(Boolean.TRUE, commandConfig.getEnabled());
        assertEquals("GET", commandConfig.getQueryMethod());
        assertEquals("https://provider.example.com/tasks/{taskId}", commandConfig.getQueryUrlTemplate());
        assertEquals("SAME_AS_SUBMIT", commandConfig.getAuthMode());
        assertEquals("$.status", commandConfig.getStatusPath());
        assertEquals("$.result", commandConfig.getResultPath());
        assertEquals("$.error", commandConfig.getErrorPath());
                assertEquals("{\"streaming\":true}", commandCaptor.getValue().getCapabilityExtensions());
                assertEquals("{\"rateLimitQps\":10}", commandCaptor.getValue().getPolicyExtensions());
                assertEquals("{\"source\":\"import\"}", commandCaptor.getValue().getMetadataExtensions());

        assertEquals(Boolean.TRUE, response.getAsyncTaskConfig().getEnabled());
        assertEquals("{\"type\":\"object\",\"required\":[\"prompt\"]}", response.getRequestJsonSchema());
        assertEquals("{\"type\":\"object\",\"properties\":{\"taskId\":{\"type\":\"string\"}}}", response.getResponseJsonSchema());
        assertEquals(RequestMethod.GET, response.getAsyncTaskConfig().getQueryMethod());
        assertEquals("https://provider.example.com/tasks/{taskId}", response.getAsyncTaskConfig().getQueryUrlTemplate());
        assertEquals(AsyncTaskAuthMode.SAME_AS_SUBMIT, response.getAsyncTaskConfig().getAuthMode());
                assertEquals("{\"streaming\":true}", response.getCapabilityExtensions());
                assertEquals("{\"rateLimitQps\":10}", response.getPolicyExtensions());
                assertEquals("{\"source\":\"import\"}", response.getMetadataExtensions());
        }

        @Test
        @DisplayName("revise binding should preserve nullable extension set flags")
        void shouldBindExtensionSetFlagsWhenRevisingAsset() {
                ApiAssetUseCase useCase = mock(ApiAssetUseCase.class);
                when(useCase.reviseAsset(any(ReviseApiAssetCommand.class))).thenReturn(asyncAssetModel());
                ApiAssetWebDelegate delegate = new ApiAssetWebDelegate(useCase);
                ReviseApiAssetReq req = new ReviseApiAssetReq();
                req.setCapabilityExtensions(null);
                req.setPolicyExtensions(extensionBlock("visibility", "internal"));

                delegate.reviseAsset("user-1", "Alice", "image-generation", req);

                ArgumentCaptor<ReviseApiAssetCommand> commandCaptor =
                                ArgumentCaptor.forClass(ReviseApiAssetCommand.class);
                verify(useCase).reviseAsset(commandCaptor.capture());
                assertTrue(commandCaptor.getValue().isCapabilityExtensionsSet());
                assertNull(commandCaptor.getValue().getCapabilityExtensions());
                assertTrue(commandCaptor.getValue().isPolicyExtensionsSet());
                assertEquals("{\"visibility\":\"internal\"}", commandCaptor.getValue().getPolicyExtensions());
                assertFalse(commandCaptor.getValue().isMetadataExtensionsSet());
    }

    @Test
    @DisplayName("ai profile binding should return complete asset response")
    void shouldReturnCompleteAssetResponseWhenAttachingAiProfile() {
        ApiAssetUseCase useCase = mock(ApiAssetUseCase.class);
        when(useCase.attachAiCapabilityProfile(any(AttachAiCapabilityProfileCommand.class)))
                .thenReturn(completeAiAssetModel());
        ApiAssetWebDelegate delegate = new ApiAssetWebDelegate(useCase);

        ApiAssetResp response = delegate.attachAiCapabilityProfile(
                "user-1",
                "AI Alice",
                "chat-completion",
                new AttachAiCapabilityProfileReq("OpenAI", "gpt-4.1", true, List.of("chat", "vision"))
        );

        ArgumentCaptor<AttachAiCapabilityProfileCommand> commandCaptor =
                ArgumentCaptor.forClass(AttachAiCapabilityProfileCommand.class);
        verify(useCase).attachAiCapabilityProfile(commandCaptor.capture());
        assertEquals("user-1", commandCaptor.getValue().getOwnerUserId());
        assertEquals("AI Alice", commandCaptor.getValue().getPublisherDisplayName());
        assertEquals("chat-completion", commandCaptor.getValue().getApiCode());
        assertEquals("OpenAI", commandCaptor.getValue().getProvider());
        assertEquals("gpt-4.1", commandCaptor.getValue().getModel());
        assertEquals(Boolean.TRUE, commandCaptor.getValue().isStreamingSupported());
        assertEquals(List.of("chat", "vision"), commandCaptor.getValue().getCapabilityTags());

        assertEquals("asset-1", response.getId());
        assertEquals("chat-completion", response.getApiCode());
        assertEquals("Chat Completion", response.getAssetName());
        assertEquals(AssetType.AI_API, response.getAssetType());
        assertEquals("tools", response.getCategoryCode());
        assertEquals(AssetStatus.DRAFT, response.getStatus());
        assertEquals("AI Alice", response.getPublisherDisplayName());
        assertEquals(RequestMethod.POST, response.getRequestMethod());
        assertEquals("https://upstream.example.com/chat", response.getUpstreamUrl());
        assertEquals(AuthScheme.HEADER_TOKEN, response.getAuthScheme());
        assertEquals("{\"headerName\":\"Authorization\",\"token\":\"secret\"}", response.getAuthConfig());
        assertEquals("chat-template", response.getRequestTemplate());
        assertEquals("{\"messages\":[{\"role\":\"user\",\"content\":\"hi\"}]}", response.getRequestExample());
        assertEquals("{\"choices\":[{\"message\":{\"content\":\"hello\"}}]}", response.getResponseExample());
        assertEquals("OpenAI", response.getAiCapabilityProfile().getProvider());
        assertEquals("gpt-4.1", response.getAiCapabilityProfile().getModel());
        assertEquals(Boolean.TRUE, response.getAiCapabilityProfile().getStreamingSupported());
        assertEquals(List.of("chat", "vision"), response.getAiCapabilityProfile().getCapabilityTags());
    }

    private ApiAssetModel completeAiAssetModel() {
        return new ApiAssetModel(
                "asset-1",
                "chat-completion",
                "Chat Completion",
                "AI_API",
                "tools",
                "DRAFT",
                "AI Alice",
                null,
                "POST",
                "https://upstream.example.com/chat",
                "HEADER_TOKEN",
                "{\"headerName\":\"Authorization\",\"token\":\"secret\"}",
                "chat-template",
                "{\"messages\":[{\"role\":\"user\",\"content\":\"hi\"}]}",
                "{\"choices\":[{\"message\":{\"content\":\"hello\"}}]}",
                "OpenAI",
                "gpt-4.1",
                true,
                List.of("chat", "vision"),
                false,
                "2026-04-29T08:00:00Z",
                "2026-04-29T08:30:00Z"
        );
    }

    private ApiAssetModel asyncAssetModel() {
        return new ApiAssetModel(
                "asset-2",
                "image-generation",
                "Image Generation",
                "STANDARD_API",
                null,
                "DRAFT",
                "Alice",
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                "{\"type\":\"object\",\"required\":[\"prompt\"]}",
                "{\"type\":\"object\",\"properties\":{\"taskId\":{\"type\":\"string\"}}}",
                new AsyncTaskConfigModel(
                        true,
                        "GET",
                        "https://provider.example.com/tasks/{taskId}",
                        "SAME_AS_SUBMIT",
                        null,
                        null,
                        "$.status",
                        "$.result",
                        "$.error"
                ),
                "{\"streaming\":true}",
                "{\"rateLimitQps\":10}",
                "{\"source\":\"import\"}",
                null,
                null,
                null,
                null,
                false,
                "2026-04-29T08:00:00Z",
                "2026-04-29T08:30:00Z"
        );
    }

        private Map<String, Object> extensionBlock(String key, Object value) {
                Map<String, Object> block = new LinkedHashMap<>();
                block.put(key, value);
                return block;
        }
}
