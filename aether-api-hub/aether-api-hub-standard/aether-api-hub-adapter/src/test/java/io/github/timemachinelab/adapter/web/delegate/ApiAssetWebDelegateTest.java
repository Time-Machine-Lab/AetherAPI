package io.github.timemachinelab.adapter.web.delegate;

import io.github.timemachinelab.api.req.AttachAiCapabilityProfileReq;
import io.github.timemachinelab.api.resp.ApiAssetResp;
import io.github.timemachinelab.domain.catalog.model.AssetStatus;
import io.github.timemachinelab.domain.catalog.model.AssetType;
import io.github.timemachinelab.domain.catalog.model.AuthScheme;
import io.github.timemachinelab.domain.catalog.model.RequestMethod;
import io.github.timemachinelab.service.model.ApiAssetModel;
import io.github.timemachinelab.service.model.AttachAiCapabilityProfileCommand;
import io.github.timemachinelab.service.port.in.ApiAssetUseCase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ApiAssetWebDelegateTest {

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
}
