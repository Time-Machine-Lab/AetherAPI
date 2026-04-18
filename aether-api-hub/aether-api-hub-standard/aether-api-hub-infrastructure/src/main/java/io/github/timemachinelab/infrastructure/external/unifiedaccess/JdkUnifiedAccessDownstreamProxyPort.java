package io.github.timemachinelab.infrastructure.external.unifiedaccess;

import io.github.timemachinelab.api.error.UnifiedAccessErrorCodes;
import io.github.timemachinelab.service.model.TargetApiSnapshotModel;
import io.github.timemachinelab.service.model.UnifiedAccessInvocationModel;
import io.github.timemachinelab.service.model.UnifiedAccessProxyResponseModel;
import io.github.timemachinelab.service.port.out.UnifiedAccessDownstreamProxyPort;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpTimeoutException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
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
    private final Duration requestTimeout;

    public JdkUnifiedAccessDownstreamProxyPort(HttpClient httpClient) {
        this(httpClient, DEFAULT_REQUEST_TIMEOUT);
    }

    public JdkUnifiedAccessDownstreamProxyPort(HttpClient httpClient, Duration requestTimeout) {
        this.httpClient = Objects.requireNonNull(httpClient, "HTTP client must not be null");
        this.requestTimeout = requestTimeout == null ? DEFAULT_REQUEST_TIMEOUT : requestTimeout;
    }

    @Override
    public UnifiedAccessProxyResponseModel forward(UnifiedAccessInvocationModel invocation) {
        Objects.requireNonNull(invocation, "Unified access invocation must not be null");

        HttpRequest request = buildRequest(invocation);
        boolean streaming = invocation.getTargetApi().isStreamingSupported();
        try {
            if (streaming) {
                HttpResponse<InputStream> response = httpClient.send(request, HttpResponse.BodyHandlers.ofInputStream());
                return toStreamingOutcome(response);
            }
            HttpResponse<byte[]> response = httpClient.send(request, HttpResponse.BodyHandlers.ofByteArray());
            return toByteArrayOutcome(response);
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

    private HttpRequest buildRequest(UnifiedAccessInvocationModel invocation) {
        TargetApiSnapshotModel targetApi = invocation.getTargetApi();
        URI uri = buildUri(targetApi, invocation.getQueryParameters());
        HttpRequest.Builder builder = HttpRequest.newBuilder(uri).timeout(requestTimeout);
        applyForwardedHeaders(builder, invocation);
        applyHeaderAuth(builder, targetApi);
        return builder.method(resolveOutgoingMethod(invocation), bodyPublisher(invocation.getRequestBody())).build();
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

    private void applyHeaderAuth(HttpRequest.Builder builder, TargetApiSnapshotModel targetApi) {
        if (!"HEADER_TOKEN".equalsIgnoreCase(targetApi.getAuthScheme()) || targetApi.getAuthConfig() == null) {
            return;
        }
        String[] parts = splitAuthConfig(targetApi.getAuthConfig(), ":");
        String headerName = parts[0].isBlank() ? "Authorization" : parts[0];
        builder.header(headerName, parts[1]);
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
                UnifiedAccessErrorCodes.UPSTREAM_TIMEOUT,
                "UPSTREAM_TIMEOUT",
                "Upstream request timed out for apiCode " + invocation.getTargetApi().getApiCode(),
                ex
        );
    }

    private String transportFailurePayload(UnifiedAccessInvocationModel invocation, Exception ex) {
        return executionPayload(
                UnifiedAccessErrorCodes.UPSTREAM_EXECUTION_FAILURE,
                "UPSTREAM_FAILURE",
                "Upstream request failed for apiCode " + invocation.getTargetApi().getApiCode(),
                ex
        );
    }

    private String executionPayload(String code, String outcomeType, String message, Exception ex) {
        return "{\"code\":\"" + escapeJson(code)
                + "\",\"message\":\"" + escapeJson(message)
                + "\",\"executionOutcome\":\"" + escapeJson(outcomeType)
                + "\",\"detail\":\"" + escapeJson(ex.getMessage())
                + "\"}";
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
}
