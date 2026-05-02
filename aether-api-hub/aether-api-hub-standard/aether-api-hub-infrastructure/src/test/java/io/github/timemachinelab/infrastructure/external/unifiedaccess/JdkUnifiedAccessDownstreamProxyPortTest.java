package io.github.timemachinelab.infrastructure.external.unifiedaccess;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import io.github.timemachinelab.service.model.ConsumerContextModel;
import io.github.timemachinelab.service.model.TargetApiSnapshotModel;
import io.github.timemachinelab.service.model.UnifiedAccessExecutionOutcomeType;
import io.github.timemachinelab.service.model.UnifiedAccessInvocationModel;
import io.github.timemachinelab.service.model.UnifiedAccessProxyResponseModel;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Authenticator;
import java.net.CookieHandler;
import java.net.InetSocketAddress;
import java.net.ProxySelector;
import java.net.ServerSocket;
import java.net.http.HttpClient;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLParameters;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JdkUnifiedAccessDownstreamProxyPortTest {

    private HttpServer server;

    @AfterEach
    void tearDown() {
        if (server != null) {
            server.stop(0);
        }
    }

    @Test
    @DisplayName("forward should proxy request data, inject upstream auth, and strip internal headers")
    void shouldForwardSuccessfulUpstreamProxyRequest() throws Exception {
        AtomicReference<String> authorizationHeader = new AtomicReference<>();
        AtomicReference<String> traceHeader = new AtomicReference<>();
        AtomicReference<String> aetherHeader = new AtomicReference<>();
        AtomicReference<String> queryString = new AtomicReference<>();
        AtomicReference<String> requestBody = new AtomicReference<>();
        startServer(exchange -> {
            authorizationHeader.set(exchange.getRequestHeaders().getFirst("Authorization"));
            traceHeader.set(exchange.getRequestHeaders().getFirst("X-Trace-Id"));
            aetherHeader.set(exchange.getRequestHeaders().getFirst("X-Aether-Api-Key"));
            queryString.set(exchange.getRequestURI().getRawQuery());
            requestBody.set(new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));

            byte[] responseBody = "{\"accepted\":true}".getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.getResponseHeaders().add("X-Upstream-Trace", "up-1");
            exchange.sendResponseHeaders(201, responseBody.length);
            try (OutputStream outputStream = exchange.getResponseBody()) {
                outputStream.write(responseBody);
            }
        });

        JdkUnifiedAccessDownstreamProxyPort proxyPort = new JdkUnifiedAccessDownstreamProxyPort(HttpClient.newHttpClient());
        UnifiedAccessProxyResponseModel response = proxyPort.forward(invocation(serverUrl("/v1/chat-completions"), false));

        assertEquals(UnifiedAccessExecutionOutcomeType.SUCCESS, response.getOutcomeType());
        assertEquals(201, response.getStatusCode());
        assertEquals("Bearer upstream-token", authorizationHeader.get());
        assertEquals("trace-1", traceHeader.get());
        assertEquals(null, aetherHeader.get());
        assertTrue(queryString.get().contains("stream=true"));
        assertEquals("{\"message\":\"hello\"}", requestBody.get());
        assertArrayEquals("{\"accepted\":true}".getBytes(StandardCharsets.UTF_8), response.getResponseBody());
        assertEquals(List.of("up-1"), response.getResponseHeaders().get("x-upstream-trace"));
    }

    @Test
    @DisplayName("forward should preserve successful https upstream semantics")
    void shouldPreserveSuccessfulHttpsUpstreamSemantics() {
        FixedHttpClient httpClient = new FixedHttpClient(
                203,
                Map.of("Content-Type", List.of("application/json"), "X-Upstream-Trace", List.of("https-up-1")),
                "{\"secure\":true}".getBytes(StandardCharsets.UTF_8)
        );
        JdkUnifiedAccessDownstreamProxyPort proxyPort = new JdkUnifiedAccessDownstreamProxyPort(httpClient);

        UnifiedAccessProxyResponseModel response = proxyPort.forward(
                invocation("https://secure-upstream.example.com/v1/chat-completions", false)
        );

        assertEquals("https", httpClient.lastRequest.uri().getScheme());
        assertEquals("POST", httpClient.lastRequest.method());
        assertEquals(Optional.of("Bearer upstream-token"), httpClient.lastRequest.headers().firstValue("Authorization"));
        assertEquals(Optional.empty(), httpClient.lastRequest.headers().firstValue("X-Aether-Api-Key"));
        assertEquals(UnifiedAccessExecutionOutcomeType.SUCCESS, response.getOutcomeType());
        assertEquals(203, response.getStatusCode());
        assertEquals("application/json", response.getContentType());
        assertArrayEquals("{\"secure\":true}".getBytes(StandardCharsets.UTF_8), response.getResponseBody());
        assertEquals(List.of("https-up-1"), response.getResponseHeaders().get("X-Upstream-Trace"));
    }

    @Test
    @DisplayName("forward should classify upstream timeout distinctly")
    void shouldClassifyUpstreamTimeout() throws Exception {
        startServer(exchange -> {
            try {
                Thread.sleep(250);
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
            byte[] responseBody = "{\"late\":true}".getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, responseBody.length);
            try (OutputStream outputStream = exchange.getResponseBody()) {
                outputStream.write(responseBody);
            }
        });

        JdkUnifiedAccessDownstreamProxyPort proxyPort = new JdkUnifiedAccessDownstreamProxyPort(
                HttpClient.newBuilder().connectTimeout(Duration.ofMillis(50)).build(),
                Duration.ofMillis(50)
        );
        UnifiedAccessProxyResponseModel response = proxyPort.forward(invocation(serverUrl("/v1/chat-completions"), false));

        assertEquals(UnifiedAccessExecutionOutcomeType.UPSTREAM_TIMEOUT, response.getOutcomeType());
        assertEquals(504, response.getStatusCode());
        assertEquals("application/json", response.getContentType());
        assertTrue(new String(response.getResponseBody(), StandardCharsets.UTF_8).contains("UPSTREAM_TIMEOUT"));
    }

    @Test
    @DisplayName("forward should classify https connection failure as execution failure")
    void shouldClassifyHttpsConnectionFailureAsExecutionFailure() throws Exception {
        int unusedPort = unusedLocalPort();
        JdkUnifiedAccessDownstreamProxyPort proxyPort = new JdkUnifiedAccessDownstreamProxyPort(
                HttpClient.newBuilder().connectTimeout(Duration.ofMillis(200)).build(),
                Duration.ofMillis(500)
        );

        UnifiedAccessProxyResponseModel response = proxyPort.forward(
                invocation("https://127.0.0.1:" + unusedPort + "/v1/chat-completions", false)
        );

        assertEquals(UnifiedAccessExecutionOutcomeType.UPSTREAM_FAILURE, response.getOutcomeType());
        assertEquals(502, response.getStatusCode());
        assertEquals("application/json", response.getContentType());
        String body = new String(response.getResponseBody(), StandardCharsets.UTF_8);
        assertTrue(body.contains("UPSTREAM_EXECUTION_FAILURE"));
        assertTrue(body.contains("UPSTREAM_FAILURE"));
    }

    @Test
    @DisplayName("forward should keep upstream failure responses as execution outcomes")
    void shouldClassifyUpstreamFailureResponse() throws Exception {
        startServer(exchange -> {
            byte[] responseBody = "{\"error\":\"too_many_requests\"}".getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.getResponseHeaders().add("Retry-After", "30");
            exchange.sendResponseHeaders(429, responseBody.length);
            try (OutputStream outputStream = exchange.getResponseBody()) {
                outputStream.write(responseBody);
            }
        });

        JdkUnifiedAccessDownstreamProxyPort proxyPort = new JdkUnifiedAccessDownstreamProxyPort(HttpClient.newHttpClient());
        UnifiedAccessProxyResponseModel response = proxyPort.forward(invocation(serverUrl("/v1/chat-completions"), false));

        assertEquals(UnifiedAccessExecutionOutcomeType.UPSTREAM_FAILURE, response.getOutcomeType());
        assertEquals(429, response.getStatusCode());
        assertArrayEquals("{\"error\":\"too_many_requests\"}".getBytes(StandardCharsets.UTF_8), response.getResponseBody());
        assertEquals(List.of("30"), response.getResponseHeaders().get("retry-after"));
    }

    @Test
    @DisplayName("forward should preserve streaming responses for streaming-capable targets")
    void shouldKeepStreamingResponseInStreamingMode() throws Exception {
        startServer(exchange -> {
            exchange.getResponseHeaders().add("Content-Type", "text/event-stream");
            exchange.sendResponseHeaders(200, 0);
            try (OutputStream outputStream = exchange.getResponseBody()) {
                outputStream.write("data: hello\n\n".getBytes(StandardCharsets.UTF_8));
                outputStream.flush();
                outputStream.write("data: world\n\n".getBytes(StandardCharsets.UTF_8));
            }
        });

        JdkUnifiedAccessDownstreamProxyPort proxyPort = new JdkUnifiedAccessDownstreamProxyPort(HttpClient.newHttpClient());
        UnifiedAccessProxyResponseModel response = proxyPort.forward(invocation(serverUrl("/v1/chat-completions"), true));

        assertEquals(UnifiedAccessExecutionOutcomeType.SUCCESS, response.getOutcomeType());
        assertTrue(response.isStreaming());
        assertNotNull(response.getResponseStream());
        assertFalse(response.getResponseHeaders().isEmpty());
        try (response; var inputStream = response.getResponseStream()) {
            assertEquals("data: hello\n\ndata: world\n\n", new String(inputStream.readAllBytes(), StandardCharsets.UTF_8));
        }
    }

    @Test
    @DisplayName("forward should classify invalid upstream uri as execution failure")
    void shouldClassifyInvalidUpstreamUriAsExecutionFailure() {
        JdkUnifiedAccessDownstreamProxyPort proxyPort = new JdkUnifiedAccessDownstreamProxyPort(HttpClient.newHttpClient());

        UnifiedAccessProxyResponseModel response = proxyPort.forward(invocation("upstream.example.com/v1/chat-completions", false));

        assertEquals(UnifiedAccessExecutionOutcomeType.UPSTREAM_FAILURE, response.getOutcomeType());
        assertEquals(502, response.getStatusCode());
        assertEquals("application/json", response.getContentType());
        String body = new String(response.getResponseBody(), StandardCharsets.UTF_8);
        assertTrue(body.contains("UPSTREAM_EXECUTION_FAILURE"));
        assertTrue(body.contains("URI with undefined scheme"));
    }

    @Test
    @DisplayName("forward should sanitize secrets from execution failure payload")
    void shouldSanitizeSecretsFromExecutionFailurePayload() {
        JdkUnifiedAccessDownstreamProxyPort proxyPort = new JdkUnifiedAccessDownstreamProxyPort(HttpClient.newHttpClient());

        UnifiedAccessProxyResponseModel response = proxyPort.forward(invocation(
                "https:// upstream.example.com/ak_live_validation_key/Bearer upstream-token",
                false
        ));

        String body = new String(response.getResponseBody(), StandardCharsets.UTF_8);
        assertEquals(UnifiedAccessExecutionOutcomeType.UPSTREAM_FAILURE, response.getOutcomeType());
        assertEquals(502, response.getStatusCode());
        assertTrue(body.contains("UPSTREAM_EXECUTION_FAILURE"));
        assertFalse(body.contains("ak_live_validation_key"));
        assertFalse(body.contains("Bearer upstream-token"));
        assertFalse(body.contains("Authorization: Bearer upstream-token"));
    }

    private void startServer(ExchangeHandler handler) throws IOException {
        server = HttpServer.create(new InetSocketAddress(0), 0);
        server.createContext("/", exchange -> {
            try {
                handler.handle(exchange);
            } finally {
                exchange.close();
            }
        });
        server.start();
    }

    private String serverUrl(String path) {
        return "http://127.0.0.1:" + server.getAddress().getPort() + path;
    }

    private int unusedLocalPort() throws IOException {
        try (ServerSocket socket = new ServerSocket(0)) {
            return socket.getLocalPort();
        }
    }

    private UnifiedAccessInvocationModel invocation(String upstreamUrl, boolean streamingSupported) {
        return new UnifiedAccessInvocationModel(
                new ConsumerContextModel(
                        "consumer-1",
                        "consumer_code_1",
                        "consumer-one",
                        "USER_ACCOUNT",
                        "credential-1",
                        "cred_code_1",
                        "ENABLED",
                        "ak_live",
                        "ak_live_****1234"
                ),
                new TargetApiSnapshotModel(
                        "asset-1",
                        "chat-completions",
                        "Chat Completions",
                        "AI_API",
                        "POST",
                        upstreamUrl,
                        "HEADER_TOKEN",
                        "Authorization: Bearer upstream-token",
                        streamingSupported,
                        "OpenAI",
                        "gpt-4.1"
                ),
                "POST",
                Map.of(
                        "X-Aether-Api-Key", List.of("ak_live_validation_key"),
                        "X-Trace-Id", List.of("trace-1"),
                        "Authorization", List.of("Bearer caller-token")
                ),
                Map.of("stream", List.of("true")),
                "{\"message\":\"hello\"}".getBytes(StandardCharsets.UTF_8),
                "application/json",
                "UNIFIED_ACCESS"
        );
    }

    @FunctionalInterface
    private interface ExchangeHandler {
        void handle(HttpExchange exchange) throws IOException;
    }

    private static final class FixedHttpClient extends HttpClient {

        private final int statusCode;
        private final Map<String, List<String>> responseHeaders;
        private final byte[] responseBody;
        private HttpRequest lastRequest;

        private FixedHttpClient(int statusCode, Map<String, List<String>> responseHeaders, byte[] responseBody) {
            this.statusCode = statusCode;
            this.responseHeaders = responseHeaders;
            this.responseBody = responseBody;
        }

        @Override
        public Optional<CookieHandler> cookieHandler() {
            return Optional.empty();
        }

        @Override
        public Optional<Duration> connectTimeout() {
            return Optional.empty();
        }

        @Override
        public Redirect followRedirects() {
            return Redirect.NEVER;
        }

        @Override
        public Optional<ProxySelector> proxy() {
            return Optional.empty();
        }

        @Override
        public SSLContext sslContext() {
            return null;
        }

        @Override
        public SSLParameters sslParameters() {
            return null;
        }

        @Override
        public Optional<Authenticator> authenticator() {
            return Optional.empty();
        }

        @Override
        public HttpClient.Version version() {
            return HttpClient.Version.HTTP_1_1;
        }

        @Override
        public Optional<Executor> executor() {
            return Optional.empty();
        }

        @Override
        @SuppressWarnings("unchecked")
        public <T> HttpResponse<T> send(HttpRequest request, HttpResponse.BodyHandler<T> responseBodyHandler) {
            this.lastRequest = request;
            return (HttpResponse<T>) new FixedHttpResponse(statusCode, responseHeaders, responseBody, request);
        }

        @Override
        public <T> CompletableFuture<HttpResponse<T>> sendAsync(
                HttpRequest request,
                HttpResponse.BodyHandler<T> responseBodyHandler) {
            throw new UnsupportedOperationException("sendAsync is not used in this test");
        }

        @Override
        public <T> CompletableFuture<HttpResponse<T>> sendAsync(
                HttpRequest request,
                HttpResponse.BodyHandler<T> responseBodyHandler,
                HttpResponse.PushPromiseHandler<T> pushPromiseHandler) {
            throw new UnsupportedOperationException("sendAsync is not used in this test");
        }
    }

    private static final class FixedHttpResponse implements HttpResponse<byte[]> {

        private final int statusCode;
        private final Map<String, List<String>> responseHeaders;
        private final byte[] responseBody;
        private final HttpRequest request;

        private FixedHttpResponse(
                int statusCode,
                Map<String, List<String>> responseHeaders,
                byte[] responseBody,
                HttpRequest request) {
            this.statusCode = statusCode;
            this.responseHeaders = responseHeaders;
            this.responseBody = responseBody;
            this.request = request;
        }

        @Override
        public int statusCode() {
            return statusCode;
        }

        @Override
        public HttpRequest request() {
            return request;
        }

        @Override
        public Optional<HttpResponse<byte[]>> previousResponse() {
            return Optional.empty();
        }

        @Override
        public HttpHeaders headers() {
            return HttpHeaders.of(responseHeaders, (name, value) -> true);
        }

        @Override
        public byte[] body() {
            return responseBody;
        }

        @Override
        public Optional<javax.net.ssl.SSLSession> sslSession() {
            return Optional.empty();
        }

        @Override
        public java.net.URI uri() {
            return request.uri();
        }

        @Override
        public HttpClient.Version version() {
            return HttpClient.Version.HTTP_1_1;
        }
    }
}
