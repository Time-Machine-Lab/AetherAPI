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

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.net.Authenticator;
import java.net.CookieHandler;
import java.net.InetSocketAddress;
import java.net.ProxySelector;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.http.HttpClient;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpTimeoutException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLParameters;

import io.github.timemachinelab.service.model.ProxyProfileSnapshotModel;

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
    @DisplayName("forward should resolve proxied clients through the proxy-aware resolver")
    void shouldResolveProxiedClientThroughProxyAwareResolver() {
        ProxyProfileSnapshotModel proxyProfile = new ProxyProfileSnapshotModel(
                "proxy-1",
                "credentialed-proxy",
                "HTTP",
                "127.0.0.1",
                8888,
                null,
                null,
                1L
        );
        FixedHttpClient selectedClient = new FixedHttpClient(
                200,
                Map.of("Content-Type", List.of("application/json")),
                "{\"proxied\":true}".getBytes(StandardCharsets.UTF_8)
        );
        AtomicReference<ProxyProfileSnapshotModel> resolvedProfile = new AtomicReference<>();
        JdkUnifiedAccessDownstreamProxyPort proxyPort = new JdkUnifiedAccessDownstreamProxyPort(
                profile -> {
                    resolvedProfile.set(profile);
                    return selectedClient;
                },
                Duration.ofMillis(500),
                Duration.ofSeconds(2)
        );

        UnifiedAccessProxyResponseModel response = proxyPort.forward(
                invocation("http://upstream.example.com/v1/chat-completions", false, proxyProfile)
        );

        assertEquals(proxyProfile, resolvedProfile.get());
        assertEquals("http://upstream.example.com/v1/chat-completions?stream=true", selectedClient.lastRequest.uri().toString());
        assertEquals(UnifiedAccessExecutionOutcomeType.SUCCESS, response.getOutcomeType());
        assertEquals(200, response.getStatusCode());
        assertEquals("application/json", response.getContentType());
        assertArrayEquals("{\"proxied\":true}".getBytes(StandardCharsets.UTF_8), response.getResponseBody());
    }

    @Test
    @DisplayName("forward should preempt proxy auth and pass through upstream 401 without auth challenge")
    void shouldPassThroughUpstream401WithoutWwwAuthenticateWhenProxyHasCredentials() throws Exception {
        AtomicReference<String> proxyAuthorizationHeader = new AtomicReference<>();
        AtomicReference<String> upstreamAuthorizationHeader = new AtomicReference<>();
        startServer(exchange -> {
            proxyAuthorizationHeader.set(exchange.getRequestHeaders().getFirst("Proxy-Authorization"));
            upstreamAuthorizationHeader.set(exchange.getRequestHeaders().getFirst("Authorization"));

            byte[] responseBody = "{\"error\":\"invalid_api_key\"}".getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(401, responseBody.length);
            try (OutputStream outputStream = exchange.getResponseBody()) {
                outputStream.write(responseBody);
            }
        });
        ProxyProfileSnapshotModel proxyProfile = new ProxyProfileSnapshotModel(
                "proxy-1",
                "credentialed-proxy",
                "HTTP",
                "127.0.0.1",
                server.getAddress().getPort(),
                "proxy-user",
                "proxy-secret",
                1L
        );
        JdkUnifiedAccessDownstreamProxyPort proxyPort = new JdkUnifiedAccessDownstreamProxyPort(HttpClient.newHttpClient());

        UnifiedAccessProxyResponseModel response = proxyPort.forward(
                invocation("http://api.bltcy.top/v1/images/generations", false, proxyProfile)
        );

        String expectedProxyAuth = "Basic " + Base64.getEncoder()
                .encodeToString("proxy-user:proxy-secret".getBytes(StandardCharsets.UTF_8));
        assertEquals(expectedProxyAuth, proxyAuthorizationHeader.get());
        assertEquals("Bearer upstream-token", upstreamAuthorizationHeader.get());
        assertEquals(UnifiedAccessExecutionOutcomeType.UPSTREAM_FAILURE, response.getOutcomeType());
        assertEquals(401, response.getStatusCode());
        assertEquals("application/json", response.getContentType());
        assertFalse(new String(response.getResponseBody(), StandardCharsets.UTF_8)
                .contains("WWW-Authenticate header missing"));
    }

    @Test
    @DisplayName("forward should send proxy authorization during https CONNECT")
    void shouldSendProxyAuthorizationDuringHttpsConnect() throws Exception {
        AtomicReference<String> connectLine = new AtomicReference<>();
        AtomicReference<String> proxyAuthorizationHeader = new AtomicReference<>();
        try (ServerSocket proxyServer = new ServerSocket(0)) {
            CompletableFuture<Void> proxyExchange = CompletableFuture.runAsync(() -> {
                try (Socket socket = proxyServer.accept()) {
                    socket.setSoTimeout(2000);
                    BufferedReader reader = new BufferedReader(new InputStreamReader(
                            socket.getInputStream(),
                            StandardCharsets.ISO_8859_1
                    ));
                    connectLine.set(reader.readLine());
                    String line;
                    while ((line = reader.readLine()) != null && !line.isEmpty()) {
                        if (line.toLowerCase(Locale.ROOT).startsWith("proxy-authorization:")) {
                            proxyAuthorizationHeader.set(line.substring(line.indexOf(':') + 1).trim());
                        }
                    }
                    byte[] response = "HTTP/1.1 407 Proxy Authentication Required\r\nContent-Length: 0\r\n\r\n"
                            .getBytes(StandardCharsets.ISO_8859_1);
                    socket.getOutputStream().write(response);
                    socket.getOutputStream().flush();
                } catch (IOException ex) {
                    throw new UncheckedIOException(ex);
                }
            });
            ProxyProfileSnapshotModel proxyProfile = new ProxyProfileSnapshotModel(
                    "proxy-1",
                    "credentialed-proxy",
                    "HTTP",
                    "127.0.0.1",
                    proxyServer.getLocalPort(),
                    "proxy-user",
                    "proxy-secret",
                    1L
            );
            JdkUnifiedAccessDownstreamProxyPort proxyPort = new JdkUnifiedAccessDownstreamProxyPort(HttpClient.newHttpClient());

            UnifiedAccessProxyResponseModel response = proxyPort.forward(
                    invocation("https://api.wuyinkeji.com/api/async/image_gpt", false, proxyProfile)
            );

            proxyExchange.get(3, TimeUnit.SECONDS);
            String expectedProxyAuth = "Basic " + Base64.getEncoder()
                    .encodeToString("proxy-user:proxy-secret".getBytes(StandardCharsets.UTF_8));
            assertEquals("CONNECT api.wuyinkeji.com:443 HTTP/1.1", connectLine.get());
            assertEquals(expectedProxyAuth, proxyAuthorizationHeader.get());
            assertEquals(UnifiedAccessExecutionOutcomeType.UPSTREAM_FAILURE, response.getOutcomeType());
        }
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

    @Test
    @DisplayName("forward should use streaming setup timeout for streaming-capable targets")
    void shouldUseStreamingSetupTimeoutForStreamingTargets() throws Exception {
        FixedStreamingHttpClient httpClient = new FixedStreamingHttpClient(
                200,
                Map.of("Content-Type", List.of("text/event-stream")),
                new ByteArrayInputStream("data: hello\n\n".getBytes(StandardCharsets.UTF_8))
        );
        JdkUnifiedAccessDownstreamProxyPort proxyPort = new JdkUnifiedAccessDownstreamProxyPort(
                httpClient,
                Duration.ofMillis(50),
                Duration.ofSeconds(7)
        );

        UnifiedAccessProxyResponseModel response = proxyPort.forward(
                invocation("https://secure-upstream.example.com/v1/chat-completions", true)
        );

        assertEquals(Optional.of(Duration.ofSeconds(7)), httpClient.lastRequest.timeout());
        assertEquals(UnifiedAccessExecutionOutcomeType.SUCCESS, response.getOutcomeType());
        assertTrue(response.isStreaming());
        try (response; var inputStream = response.getResponseStream()) {
            assertEquals("data: hello\n\n", new String(inputStream.readAllBytes(), StandardCharsets.UTF_8));
        }
    }

    @Test
    @DisplayName("forward should not pre-read streaming response bodies")
    void shouldNotBufferStreamingResponseBeforeReturning() throws Exception {
        CountingInputStream upstreamStream = new CountingInputStream(
                "data: deferred\n\n".getBytes(StandardCharsets.UTF_8)
        );
        FixedStreamingHttpClient httpClient = new FixedStreamingHttpClient(
                200,
                Map.of("Content-Type", List.of("text/event-stream")),
                upstreamStream
        );
        JdkUnifiedAccessDownstreamProxyPort proxyPort = new JdkUnifiedAccessDownstreamProxyPort(httpClient);

        UnifiedAccessProxyResponseModel response = proxyPort.forward(
                invocation("https://secure-upstream.example.com/v1/chat-completions", true)
        );

        assertTrue(response.isStreaming());
        assertNotNull(response.getResponseStream());
        assertEquals(0, upstreamStream.readCount());
        try (response; var inputStream = response.getResponseStream()) {
            assertEquals("data: deferred\n\n", new String(inputStream.readAllBytes(), StandardCharsets.UTF_8));
        }
        assertTrue(upstreamStream.readCount() > 0);
    }

    @Test
    @DisplayName("forward should allow long-lived streams within streaming setup budget")
    void shouldKeepLongLivedStreamingResponseWithinStreamingBudget() throws Exception {
        startServer(exchange -> {
            try {
                Thread.sleep(150);
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
            exchange.getResponseHeaders().add("Content-Type", "text/event-stream");
            exchange.sendResponseHeaders(200, 0);
            try (OutputStream outputStream = exchange.getResponseBody()) {
                outputStream.write("data: hello\n\n".getBytes(StandardCharsets.UTF_8));
                outputStream.flush();
                try {
                    Thread.sleep(150);
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }
                outputStream.write("data: world\n\n".getBytes(StandardCharsets.UTF_8));
                outputStream.flush();
            }
        });

        JdkUnifiedAccessDownstreamProxyPort proxyPort = new JdkUnifiedAccessDownstreamProxyPort(
                HttpClient.newBuilder().connectTimeout(Duration.ofMillis(50)).build(),
                Duration.ofMillis(50),
                Duration.ofSeconds(2)
        );
        UnifiedAccessProxyResponseModel response = proxyPort.forward(invocation(serverUrl("/v1/chat-completions"), true));

        assertEquals(UnifiedAccessExecutionOutcomeType.SUCCESS, response.getOutcomeType());
        assertTrue(response.isStreaming());
        try (response; var inputStream = response.getResponseStream()) {
            assertEquals("data: hello\n\ndata: world\n\n", new String(inputStream.readAllBytes(), StandardCharsets.UTF_8));
        }
    }

    @Test
    @DisplayName("forward should classify streaming setup timeout distinctly")
    void shouldClassifyStreamingSetupTimeout() {
        TimeoutHttpClient httpClient = new TimeoutHttpClient();
        JdkUnifiedAccessDownstreamProxyPort proxyPort = new JdkUnifiedAccessDownstreamProxyPort(
                httpClient,
                Duration.ofSeconds(30),
                Duration.ofMillis(25)
        );

        UnifiedAccessProxyResponseModel response = proxyPort.forward(
                invocation("https://secure-upstream.example.com/v1/chat-completions", true)
        );

        assertEquals(Optional.of(Duration.ofMillis(25)), httpClient.lastRequest.timeout());
        assertEquals(UnifiedAccessExecutionOutcomeType.UPSTREAM_TIMEOUT, response.getOutcomeType());
        assertEquals(504, response.getStatusCode());
        assertEquals("application/json", response.getContentType());
        assertTrue(new String(response.getResponseBody(), StandardCharsets.UTF_8).contains("UPSTREAM_TIMEOUT"));
    }

    private UnifiedAccessInvocationModel invocation(String upstreamUrl, boolean streamingSupported) {
        return invocation(upstreamUrl, streamingSupported, null);
    }

    private UnifiedAccessInvocationModel invocation(
            String upstreamUrl,
            boolean streamingSupported,
            ProxyProfileSnapshotModel proxyProfile) {
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
                        "gpt-4.1",
                        proxyProfile == null ? null : proxyProfile.getProfileId(),
                        proxyProfile
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

    private static final class FixedStreamingHttpClient extends HttpClient {

        private final int statusCode;
        private final Map<String, List<String>> responseHeaders;
        private final InputStream responseBody;
        private HttpRequest lastRequest;

        private FixedStreamingHttpClient(
                int statusCode,
                Map<String, List<String>> responseHeaders,
                InputStream responseBody) {
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
            return (HttpResponse<T>) new FixedInputStreamHttpResponse(statusCode, responseHeaders, responseBody, request);
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

    private static final class TimeoutHttpClient extends HttpClient {

        private HttpRequest lastRequest;

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
        public <T> HttpResponse<T> send(
                HttpRequest request,
                HttpResponse.BodyHandler<T> responseBodyHandler) throws HttpTimeoutException {
            this.lastRequest = request;
            throw new HttpTimeoutException("request timed out");
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

    private static final class FixedInputStreamHttpResponse implements HttpResponse<InputStream> {

        private final int statusCode;
        private final Map<String, List<String>> responseHeaders;
        private final InputStream responseBody;
        private final HttpRequest request;

        private FixedInputStreamHttpResponse(
                int statusCode,
                Map<String, List<String>> responseHeaders,
                InputStream responseBody,
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
        public Optional<HttpResponse<InputStream>> previousResponse() {
            return Optional.empty();
        }

        @Override
        public HttpHeaders headers() {
            return HttpHeaders.of(responseHeaders, (name, value) -> true);
        }

        @Override
        public InputStream body() {
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

    private static final class CountingInputStream extends InputStream {

        private final ByteArrayInputStream delegate;
        private final AtomicInteger readCount = new AtomicInteger();

        private CountingInputStream(byte[] source) {
            this.delegate = new ByteArrayInputStream(source);
        }

        @Override
        public int read() {
            readCount.incrementAndGet();
            return delegate.read();
        }

        @Override
        public int read(byte[] target, int offset, int length) {
            readCount.incrementAndGet();
            return delegate.read(target, offset, length);
        }

        private int readCount() {
            return readCount.get();
        }
    }
}
