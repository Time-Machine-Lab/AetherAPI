package io.github.timemachinelab.infrastructure.external.unifiedaccess;

import io.github.timemachinelab.api.error.UnifiedAccessErrorCodes;
import io.github.timemachinelab.service.model.ProxyProfileSnapshotModel;
import io.github.timemachinelab.service.model.TargetApiSnapshotModel;
import io.github.timemachinelab.service.model.UnifiedAccessInvocationModel;
import io.github.timemachinelab.service.model.UnifiedAccessProxyResponseModel;
import io.github.timemachinelab.service.port.out.UnifiedAccessDownstreamProxyPort;

import java.io.IOException;
import java.io.FilterInputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpTimeoutException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.StringJoiner;

/**
 * JDK HTTP client implementation for unified access upstream forwarding.
 */
public class JdkUnifiedAccessDownstreamProxyPort implements UnifiedAccessDownstreamProxyPort {

    private static final Duration DEFAULT_REQUEST_TIMEOUT = Duration.ofSeconds(30);
    private static final Duration DEFAULT_STREAMING_SETUP_TIMEOUT = Duration.ofMinutes(5);
    private static final String JSON_CONTENT_TYPE = "application/json";
    private static final Set<String> HOP_BY_HOP_HEADERS = Set.of(
            "connection",
            "keep-alive",
            "proxy-authenticate",
            "proxy-authorization",
            "te",
            "trailer",
            "transfer-encoding",
            "upgrade",
            "host",
            "content-length"
    );

    private final HttpClient httpClient;
    private final UnifiedAccessHttpClientResolver httpClientResolver;
    private final Duration requestTimeout;
    private final Duration streamingSetupTimeout;

    public JdkUnifiedAccessDownstreamProxyPort(HttpClient httpClient) {
        this(httpClient, DEFAULT_REQUEST_TIMEOUT, DEFAULT_STREAMING_SETUP_TIMEOUT);
    }

    public JdkUnifiedAccessDownstreamProxyPort(HttpClient httpClient, Duration requestTimeout) {
        this(httpClient, requestTimeout, DEFAULT_STREAMING_SETUP_TIMEOUT);
    }

    public JdkUnifiedAccessDownstreamProxyPort(
            HttpClient httpClient,
            Duration requestTimeout,
            Duration streamingSetupTimeout) {
        this.httpClient = Objects.requireNonNull(httpClient, "HTTP client must not be null");
        this.httpClientResolver = proxyProfile -> this.httpClient;
        this.requestTimeout = requestTimeout == null ? DEFAULT_REQUEST_TIMEOUT : requestTimeout;
        this.streamingSetupTimeout = streamingSetupTimeout == null
                ? DEFAULT_STREAMING_SETUP_TIMEOUT
                : streamingSetupTimeout;
    }

    public JdkUnifiedAccessDownstreamProxyPort(
            UnifiedAccessHttpClientResolver httpClientResolver,
            Duration requestTimeout,
            Duration streamingSetupTimeout) {
        this.httpClient = null;
        this.httpClientResolver = Objects.requireNonNull(httpClientResolver, "HTTP client resolver must not be null");
        this.requestTimeout = requestTimeout == null ? DEFAULT_REQUEST_TIMEOUT : requestTimeout;
        this.streamingSetupTimeout = streamingSetupTimeout == null
                ? DEFAULT_STREAMING_SETUP_TIMEOUT
                : streamingSetupTimeout;
    }

    @Override
    public UnifiedAccessProxyResponseModel forward(UnifiedAccessInvocationModel invocation) {
        Objects.requireNonNull(invocation, "Unified access invocation must not be null");

        try {
            if (requiresPreemptiveProxyAuth(invocation.getTargetApi())) {
                return forwardWithUrlConnection(invocation);
            }
            HttpRequest request = buildRequest(invocation);
            HttpClient selectedClient = httpClientResolver.resolve(invocation.getTargetApi().getProxyProfile());
            boolean streaming = invocation.getTargetApi().isStreamingSupported();
            if (streaming) {
                HttpResponse<InputStream> response = selectedClient.send(request, HttpResponse.BodyHandlers.ofInputStream());
                return toStreamingOutcome(response);
            }
            HttpResponse<byte[]> response = selectedClient.send(request, HttpResponse.BodyHandlers.ofByteArray());
            return toByteArrayOutcome(response);
        } catch (IllegalArgumentException ex) {
            return UnifiedAccessProxyResponseModel.upstreamFailure(
                    502,
                    Map.of(),
                    transportFailurePayload(invocation, ex).getBytes(StandardCharsets.UTF_8),
                    JSON_CONTENT_TYPE,
                    false,
                    ex.getMessage()
            );
        } catch (HttpTimeoutException ex) {
            return UnifiedAccessProxyResponseModel.upstreamTimeout(
                    504,
                    timeoutPayload(invocation, ex).getBytes(StandardCharsets.UTF_8),
                    JSON_CONTENT_TYPE,
                    ex.getMessage()
            );
        } catch (IOException ex) {
            return UnifiedAccessProxyResponseModel.upstreamFailure(
                    502,
                    Map.of(),
                    transportFailurePayload(invocation, ex).getBytes(StandardCharsets.UTF_8),
                    JSON_CONTENT_TYPE,
                    false,
                    ex.getMessage()
            );
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            return UnifiedAccessProxyResponseModel.upstreamFailure(
                    502,
                    Map.of(),
                    transportFailurePayload(invocation, ex).getBytes(StandardCharsets.UTF_8),
                    JSON_CONTENT_TYPE,
                    false,
                    ex.getMessage()
            );
        }
    }

    private UnifiedAccessProxyResponseModel forwardWithUrlConnection(UnifiedAccessInvocationModel invocation)
            throws IOException {
        TargetApiSnapshotModel targetApi = invocation.getTargetApi();
        HttpURLConnection connection = openProxyConnection(invocation);
        try {
            int statusCode = connection.getResponseCode();
            String contentType = connection.getContentType();
            Map<String, List<String>> responseHeaders = toHeaderMap(connection.getHeaderFields());
            InputStream responseBody = responseStream(connection, statusCode);
            if (targetApi.isStreamingSupported()) {
                InputStream disconnectingStream = new DisconnectingInputStream(responseBody, connection);
                if (isSuccessful(statusCode)) {
                    return UnifiedAccessProxyResponseModel.successStream(
                            statusCode,
                            responseHeaders,
                            disconnectingStream,
                            contentType
                    );
                }
                return UnifiedAccessProxyResponseModel.upstreamFailureStream(
                        statusCode,
                        responseHeaders,
                        disconnectingStream,
                        contentType,
                        "Upstream endpoint returned a failure response"
                );
            }

            byte[] responseBytes;
            try (InputStream inputStream = responseBody) {
                responseBytes = inputStream.readAllBytes();
            }
            if (isSuccessful(statusCode)) {
                return UnifiedAccessProxyResponseModel.success(
                        statusCode,
                        responseHeaders,
                        responseBytes,
                        contentType,
                        false
                );
            }
            return UnifiedAccessProxyResponseModel.upstreamFailure(
                    statusCode,
                    responseHeaders,
                    responseBytes,
                    contentType,
                    false,
                    "Upstream endpoint returned a failure response"
            );
        } finally {
            if (!targetApi.isStreamingSupported()) {
                connection.disconnect();
            }
        }
    }

    private HttpURLConnection openProxyConnection(UnifiedAccessInvocationModel invocation) throws IOException {
        TargetApiSnapshotModel targetApi = invocation.getTargetApi();
        ProxyProfileSnapshotModel proxyProfile = targetApi.getProxyProfile();
        URL url = buildUri(targetApi, invocation.getQueryParameters()).toURL();
        Proxy proxy = new Proxy(
                Proxy.Type.HTTP,
                new InetSocketAddress(proxyProfile.getProxyHost(), proxyProfile.getProxyPort())
        );
        HttpURLConnection connection = (HttpURLConnection) url.openConnection(proxy);
        int timeoutMillis = timeoutMillis(resolveRequestTimeout(targetApi));
        connection.setConnectTimeout(timeoutMillis);
        connection.setReadTimeout(timeoutMillis);
        connection.setInstanceFollowRedirects(true);
        connection.setRequestMethod(resolveOutgoingMethod(invocation));
        applyForwardedHeaders(connection, invocation);
        applyHeaderAuth(connection, targetApi);
        connection.setRequestProperty("Proxy-Authorization", basicProxyAuthValue(proxyProfile));
        byte[] requestBody = invocation.getRequestBody();
        if (requestBody != null && requestBody.length > 0) {
            connection.setDoOutput(true);
            connection.setFixedLengthStreamingMode(requestBody.length);
            try (var outputStream = connection.getOutputStream()) {
                outputStream.write(requestBody);
            }
        }
        return connection;
    }

    private InputStream responseStream(HttpURLConnection connection, int statusCode) throws IOException {
        InputStream inputStream = isSuccessful(statusCode) ? connection.getInputStream() : connection.getErrorStream();
        return inputStream == null ? InputStream.nullInputStream() : inputStream;
    }

    private int timeoutMillis(Duration timeout) {
        long millis = timeout.toMillis();
        if (millis > Integer.MAX_VALUE) {
            return Integer.MAX_VALUE;
        }
        return Math.max(1, (int) millis);
    }

    private HttpRequest buildRequest(UnifiedAccessInvocationModel invocation) {
        TargetApiSnapshotModel targetApi = invocation.getTargetApi();
        URI uri = buildUri(targetApi, invocation.getQueryParameters());
        HttpRequest.Builder builder = HttpRequest.newBuilder(uri).timeout(resolveRequestTimeout(targetApi));
        applyForwardedHeaders(builder, invocation);
        applyHeaderAuth(builder, targetApi);
        return builder.method(resolveOutgoingMethod(invocation), bodyPublisher(invocation.getRequestBody())).build();
    }

    private Duration resolveRequestTimeout(TargetApiSnapshotModel targetApi) {
        if (targetApi.isStreamingSupported()) {
            return streamingSetupTimeout;
        }
        return requestTimeout;
    }

    private URI buildUri(TargetApiSnapshotModel targetApi, Map<String, List<String>> queryParameters) {
        StringBuilder uri = new StringBuilder(targetApi.getUpstreamUrl());
        String queryString = toQueryString(queryParameters, targetApi);
        if (!queryString.isBlank()) {
            uri.append(targetApi.getUpstreamUrl().contains("?") ? "&" : "?").append(queryString);
        }
        return URI.create(uri.toString());
    }

    private String toQueryString(Map<String, List<String>> queryParameters, TargetApiSnapshotModel targetApi) {
        StringJoiner joiner = new StringJoiner("&");
        if (queryParameters != null) {
            for (Map.Entry<String, List<String>> entry : queryParameters.entrySet()) {
                if (entry.getValue() == null || entry.getValue().isEmpty()) {
                    joiner.add(encode(entry.getKey()) + "=");
                    continue;
                }
                for (String value : entry.getValue()) {
                    joiner.add(encode(entry.getKey()) + "=" + encode(value));
                }
            }
        }
        if ("QUERY_TOKEN".equalsIgnoreCase(targetApi.getAuthScheme()) && targetApi.getAuthConfig() != null) {
            String[] parts = splitAuthConfig(targetApi.getAuthConfig(), "=");
            String paramName = parts[0].isBlank() ? "access_token" : parts[0];
            joiner.add(encode(paramName) + "=" + encode(parts[1]));
        }
        return joiner.toString();
    }

    private void applyForwardedHeaders(HttpRequest.Builder builder, UnifiedAccessInvocationModel invocation) {
        for (Map.Entry<String, List<String>> entry : invocation.getHeaders().entrySet()) {
            if (!shouldForwardRequestHeader(entry.getKey())) {
                continue;
            }
            for (String value : entry.getValue()) {
                builder.header(entry.getKey(), value);
            }
        }
        if (invocation.getContentType() != null && !invocation.getContentType().isBlank()) {
            builder.header("Content-Type", invocation.getContentType());
        }
    }

    private void applyForwardedHeaders(HttpURLConnection connection, UnifiedAccessInvocationModel invocation) {
        for (Map.Entry<String, List<String>> entry : invocation.getHeaders().entrySet()) {
            if (!shouldForwardRequestHeader(entry.getKey())) {
                continue;
            }
            for (String value : entry.getValue()) {
                connection.addRequestProperty(entry.getKey(), value);
            }
        }
        if (invocation.getContentType() != null && !invocation.getContentType().isBlank()) {
            connection.setRequestProperty("Content-Type", invocation.getContentType());
        }
    }

    private void applyHeaderAuth(HttpRequest.Builder builder, TargetApiSnapshotModel targetApi) {
        if (!"HEADER_TOKEN".equalsIgnoreCase(targetApi.getAuthScheme()) || targetApi.getAuthConfig() == null) {
            return;
        }
        String[] parts = splitAuthConfig(targetApi.getAuthConfig(), ":");
        String headerName = parts[0].isBlank() ? "Authorization" : parts[0];
        builder.header(headerName, parts[1]);
    }

    private void applyHeaderAuth(HttpURLConnection connection, TargetApiSnapshotModel targetApi) {
        if (!"HEADER_TOKEN".equalsIgnoreCase(targetApi.getAuthScheme()) || targetApi.getAuthConfig() == null) {
            return;
        }
        String[] parts = splitAuthConfig(targetApi.getAuthConfig(), ":");
        String headerName = parts[0].isBlank() ? "Authorization" : parts[0];
        connection.setRequestProperty(headerName, parts[1]);
    }

    private boolean requiresPreemptiveProxyAuth(TargetApiSnapshotModel targetApi) {
        ProxyProfileSnapshotModel proxyProfile = targetApi.getProxyProfile();
        return proxyProfile != null
                && proxyProfile.getUsername() != null
                && !proxyProfile.getUsername().isBlank()
                && proxyProfile.getPasswordSecret() != null
                && !proxyProfile.getPasswordSecret().isBlank();
    }

    private String basicProxyAuthValue(ProxyProfileSnapshotModel proxyProfile) {
        return "Basic " + basicProxyCredential(proxyProfile);
    }

    private HttpRequest.BodyPublisher bodyPublisher(byte[] requestBody) {
        if (requestBody == null || requestBody.length == 0) {
            return HttpRequest.BodyPublishers.noBody();
        }
        return HttpRequest.BodyPublishers.ofByteArray(requestBody);
    }

    private String resolveOutgoingMethod(UnifiedAccessInvocationModel invocation) {
        if (invocation.getHttpMethod() != null && !invocation.getHttpMethod().isBlank()) {
            return invocation.getHttpMethod();
        }
        return invocation.getTargetApi().getRequestMethod();
    }

    private UnifiedAccessProxyResponseModel toByteArrayOutcome(HttpResponse<byte[]> response) {
        String contentType = response.headers().firstValue("content-type").orElse(null);
        if (isSuccessful(response.statusCode())) {
            return UnifiedAccessProxyResponseModel.success(
                    response.statusCode(),
                    toHeaderMap(response.headers().map()),
                    response.body(),
                    contentType,
                    false
            );
        }
        return UnifiedAccessProxyResponseModel.upstreamFailure(
                response.statusCode(),
                toHeaderMap(response.headers().map()),
                response.body(),
                contentType,
                false,
                "Upstream endpoint returned a failure response"
        );
    }

    private UnifiedAccessProxyResponseModel toStreamingOutcome(HttpResponse<InputStream> response) {
        String contentType = response.headers().firstValue("content-type").orElse(null);
        if (isSuccessful(response.statusCode())) {
            return UnifiedAccessProxyResponseModel.successStream(
                    response.statusCode(),
                    toHeaderMap(response.headers().map()),
                    response.body(),
                    contentType
            );
        }
        return UnifiedAccessProxyResponseModel.upstreamFailureStream(
                response.statusCode(),
                toHeaderMap(response.headers().map()),
                response.body(),
                contentType,
                "Upstream endpoint returned a failure response"
        );
    }

    private Map<String, List<String>> toHeaderMap(Map<String, List<String>> source) {
        if (source == null || source.isEmpty()) {
            return Map.of();
        }
        Map<String, List<String>> copied = new LinkedHashMap<>();
        for (Map.Entry<String, List<String>> entry : source.entrySet()) {
            if (!shouldForwardResponseHeader(entry.getKey())) {
                continue;
            }
            copied.put(entry.getKey(), new ArrayList<>(entry.getValue()));
        }
        return copied;
    }

    private boolean shouldForwardRequestHeader(String headerName) {
        if (headerName == null || headerName.isBlank()) {
            return false;
        }
        String normalized = headerName.toLowerCase(Locale.ROOT);
        return !HOP_BY_HOP_HEADERS.contains(normalized)
                && !normalized.startsWith("x-aether-")
                && !"authorization".equals(normalized);
    }

    private boolean shouldForwardResponseHeader(String headerName) {
        if (headerName == null || headerName.isBlank()) {
            return false;
        }
        return !HOP_BY_HOP_HEADERS.contains(headerName.toLowerCase(Locale.ROOT));
    }

    private boolean isSuccessful(int statusCode) {
        return statusCode >= 200 && statusCode < 300;
    }

    private String timeoutPayload(UnifiedAccessInvocationModel invocation, Exception ex) {
        return executionPayload(
                invocation,
                UnifiedAccessErrorCodes.UPSTREAM_TIMEOUT,
                "UPSTREAM_TIMEOUT",
                "Upstream request timed out for apiCode " + invocation.getTargetApi().getApiCode(),
                ex
        );
    }

    private String transportFailurePayload(UnifiedAccessInvocationModel invocation, Exception ex) {
        return executionPayload(
                invocation,
                UnifiedAccessErrorCodes.UPSTREAM_EXECUTION_FAILURE,
                "UPSTREAM_FAILURE",
                "Upstream request failed for apiCode " + invocation.getTargetApi().getApiCode(),
                ex
        );
    }

    private String executionPayload(
            UnifiedAccessInvocationModel invocation,
            String code,
            String outcomeType,
            String message,
            Exception ex) {
        return "{\"code\":\"" + escapeJson(code)
                + "\",\"message\":\"" + escapeJson(message)
                + "\",\"executionOutcome\":\"" + escapeJson(outcomeType)
                + "\",\"detail\":\"" + escapeJson(sanitizeDetail(invocation, ex.getMessage()))
                + "\"}";
    }

    private String sanitizeDetail(UnifiedAccessInvocationModel invocation, String detail) {
        if (detail == null || detail.isBlank()) {
            return "";
        }
        String sanitized = detail;
        sanitized = redactValue(sanitized, invocation.getTargetApi().getAuthConfig());
        sanitized = redactAuthConfigToken(sanitized, invocation.getTargetApi().getAuthConfig());
        sanitized = redactProxyProfile(sanitized, invocation.getTargetApi().getProxyProfile());
        sanitized = redactValue(sanitized, invocation.getConsumerContext().getMaskedKey());
        sanitized = redactValue(sanitized, invocation.getConsumerContext().getKeyPrefix());
        for (Map.Entry<String, List<String>> entry : invocation.getHeaders().entrySet()) {
            if (isSensitiveHeader(entry.getKey())) {
                for (String value : entry.getValue()) {
                    sanitized = redactValue(sanitized, value);
                }
            }
        }
        return sanitized;
    }

    private String redactProxyProfile(String source, ProxyProfileSnapshotModel proxyProfile) {
        if (proxyProfile == null) {
            return source;
        }
        String sanitized = source;
        sanitized = redactValue(sanitized, proxyProfile.getUsername());
        sanitized = redactValue(sanitized, proxyProfile.getPasswordSecret());
        sanitized = redactValue(sanitized, basicProxyCredential(proxyProfile));
        return sanitized;
    }

    private String basicProxyCredential(ProxyProfileSnapshotModel proxyProfile) {
        if (proxyProfile.getUsername() == null || proxyProfile.getPasswordSecret() == null) {
            return null;
        }
        String credentials = proxyProfile.getUsername() + ":" + proxyProfile.getPasswordSecret();
        return Base64.getEncoder().encodeToString(credentials.getBytes(StandardCharsets.UTF_8));
    }

    private String redactAuthConfigToken(String source, String authConfig) {
        if (authConfig == null || authConfig.isBlank()) {
            return source;
        }
        String[] headerParts = splitAuthConfig(authConfig, ":");
        String[] queryParts = splitAuthConfig(authConfig, "=");
        return redactValue(redactValue(source, headerParts[1]), queryParts[1]);
    }

    private String redactValue(String source, String sensitiveValue) {
        if (source == null || source.isBlank() || sensitiveValue == null || sensitiveValue.isBlank()) {
            return source;
        }
        return source.replace(sensitiveValue, "[REDACTED]");
    }

    private boolean isSensitiveHeader(String headerName) {
        if (headerName == null || headerName.isBlank()) {
            return false;
        }
        String normalized = headerName.toLowerCase(Locale.ROOT);
        return "authorization".equals(normalized) || "x-aether-api-key".equals(normalized);
    }

    private String[] splitAuthConfig(String authConfig, String delimiter) {
        int index = authConfig.indexOf(delimiter);
        if (index < 0) {
            if (":".equals(delimiter)) {
                return new String[]{"Authorization", authConfig.trim()};
            }
            return new String[]{"access_token", authConfig.trim()};
        }
        return new String[]{
                authConfig.substring(0, index).trim(),
                authConfig.substring(index + 1).trim()
        };
    }

    private String encode(String value) {
        return URLEncoder.encode(value == null ? "" : value, StandardCharsets.UTF_8);
    }

    private String escapeJson(String value) {
        if (value == null) {
            return "";
        }
        return value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"");
    }

    private static final class DisconnectingInputStream extends FilterInputStream {

        private final HttpURLConnection connection;

        private DisconnectingInputStream(InputStream delegate, HttpURLConnection connection) {
            super(delegate);
            this.connection = connection;
        }

        @Override
        public void close() throws IOException {
            try {
                super.close();
            } finally {
                connection.disconnect();
            }
        }
    }
}
