package io.github.timemachinelab.adapter.web.delegate;

import io.github.timemachinelab.service.model.ResolveUnifiedAccessInvocationCommand;
import io.github.timemachinelab.service.model.UnifiedAccessInvocationModel;
import io.github.timemachinelab.service.model.UnifiedAccessProxyResponseModel;
import io.github.timemachinelab.service.port.in.UnifiedAccessUseCase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

class UnifiedAccessWebDelegateTest {

    @Test
    @DisplayName("invoke should return raw upstream response without Result wrapping")
    void shouldReturnRawUpstreamResponse() {
        byte[] upstreamBody = "{\"accepted\":true}".getBytes(StandardCharsets.UTF_8);
        StubUnifiedAccessUseCase useCase = new StubUnifiedAccessUseCase(
                UnifiedAccessProxyResponseModel.success(
                        202,
                        Map.of("X-Upstream-Trace", List.of("up-1")),
                        upstreamBody,
                        "application/json",
                        false
                )
        );
        UnifiedAccessWebDelegate delegate = new UnifiedAccessWebDelegate(useCase);

        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Aether-Api-Key", "ak_live_validation_key");
        headers.add("X-Trace-Id", "trace-1");
        LinkedMultiValueMap<String, String> queryParameters = new LinkedMultiValueMap<>();
        queryParameters.add("stream", "false");

        ResponseEntity<?> response = delegate.invoke(
                "chat-completions",
                "POST",
                headers,
                queryParameters,
                "{\"message\":\"hello\"}".getBytes(StandardCharsets.UTF_8),
                "application/json"
        );

        assertEquals(202, response.getStatusCode().value());
        assertArrayEquals(upstreamBody, assertInstanceOf(byte[].class, response.getBody()));
        assertEquals(List.of("up-1"), response.getHeaders().get("X-Upstream-Trace"));
        assertEquals("ak_live_validation_key", useCase.lastCommand.getPlaintextApiKey());
        assertEquals(List.of("trace-1"), useCase.lastCommand.getHeaders().get("X-Trace-Id"));
    }

    @Test
    @DisplayName("invoke should expose streaming response bodies for streaming-capable outcomes")
    void shouldReturnStreamingResponseBody() throws Exception {
        StubUnifiedAccessUseCase useCase = new StubUnifiedAccessUseCase(
                UnifiedAccessProxyResponseModel.successStream(
                        200,
                        Map.of("X-Accel-Buffering", List.of("no")),
                        new ByteArrayInputStream("data: hello\n\n".getBytes(StandardCharsets.UTF_8)),
                        "text/event-stream"
                )
        );
        UnifiedAccessWebDelegate delegate = new UnifiedAccessWebDelegate(useCase);

        ResponseEntity<?> response = delegate.invoke(
                "chat-completions",
                "POST",
                new HttpHeaders(),
                new LinkedMultiValueMap<>(),
                "{\"stream\":true}".getBytes(StandardCharsets.UTF_8),
                "application/json"
        );

        assertEquals(200, response.getStatusCode().value());
        assertEquals("text/event-stream", response.getHeaders().getContentType().toString());
        assertEquals(List.of("no"), response.getHeaders().get("X-Accel-Buffering"));

        StreamingResponseBody body = assertInstanceOf(StreamingResponseBody.class, response.getBody());
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        body.writeTo(outputStream);
        assertEquals("data: hello\n\n", outputStream.toString(StandardCharsets.UTF_8));
    }

    @Test
    @DisplayName("invoke should expose upstream execution failure status and payload")
    void shouldExposeUpstreamExecutionFailureStatusAndPayload() {
        byte[] failureBody = "{\"code\":\"UPSTREAM_EXECUTION_FAILURE\",\"executionOutcome\":\"UPSTREAM_FAILURE\"}"
                .getBytes(StandardCharsets.UTF_8);
        StubUnifiedAccessUseCase useCase = new StubUnifiedAccessUseCase(
                UnifiedAccessProxyResponseModel.upstreamFailure(
                        502,
                        Map.of(),
                        failureBody,
                        "application/json",
                        false,
                        "URI with undefined scheme"
                )
        );
        UnifiedAccessWebDelegate delegate = new UnifiedAccessWebDelegate(useCase);

        ResponseEntity<?> response = delegate.invoke(
                "chat-completions",
                "POST",
                new HttpHeaders(),
                new LinkedMultiValueMap<>(),
                "{\"message\":\"hello\"}".getBytes(StandardCharsets.UTF_8),
                "application/json"
        );

        assertEquals(502, response.getStatusCode().value());
        assertArrayEquals(failureBody, assertInstanceOf(byte[].class, response.getBody()));
        assertEquals("application/json", response.getHeaders().getContentType().toString());
    }

    @Test
    @DisplayName("invoke should not treat console bearer token as unified access api key")
    void shouldKeepConsoleBearerTokenSeparateFromApiKeyAuth() {
        StubUnifiedAccessUseCase useCase = new StubUnifiedAccessUseCase(
                UnifiedAccessProxyResponseModel.success(
                        200,
                        Map.of(),
                        "{}".getBytes(StandardCharsets.UTF_8),
                        "application/json",
                        false
                )
        );
        UnifiedAccessWebDelegate delegate = new UnifiedAccessWebDelegate(useCase);

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.AUTHORIZATION, "Bearer console-session-token");

        delegate.invoke(
                "chat-completions",
                "GET",
                headers,
                new LinkedMultiValueMap<>(),
                null,
                null
        );

        assertEquals(null, useCase.lastCommand.getPlaintextApiKey());
        assertEquals(List.of("Bearer console-session-token"), useCase.lastCommand.getHeaders().get(HttpHeaders.AUTHORIZATION));
    }

    private static final class StubUnifiedAccessUseCase implements UnifiedAccessUseCase {

        private final UnifiedAccessProxyResponseModel response;
        private ResolveUnifiedAccessInvocationCommand lastCommand;

        private StubUnifiedAccessUseCase(UnifiedAccessProxyResponseModel response) {
            this.response = response;
        }

        @Override
        public UnifiedAccessInvocationModel resolveInvocation(ResolveUnifiedAccessInvocationCommand command) {
            throw new UnsupportedOperationException("resolveInvocation is not used in this test");
        }

        @Override
        public UnifiedAccessProxyResponseModel invoke(ResolveUnifiedAccessInvocationCommand command) {
            this.lastCommand = command;
            return response;
        }
    }
}
