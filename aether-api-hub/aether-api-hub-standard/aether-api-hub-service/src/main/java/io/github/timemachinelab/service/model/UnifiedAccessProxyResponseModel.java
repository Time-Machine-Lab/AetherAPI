package io.github.timemachinelab.service.model;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Downstream proxy execution result returned to the unified access entry adapter.
 */
public class UnifiedAccessProxyResponseModel implements AutoCloseable {

    private final UnifiedAccessExecutionOutcomeType outcomeType;
    private final int statusCode;
    private final Map<String, List<String>> responseHeaders;
    private final byte[] responseBody;
    private final InputStream responseStream;
    private final String contentType;
    private final boolean streaming;
    private final String message;

    private UnifiedAccessProxyResponseModel(
            UnifiedAccessExecutionOutcomeType outcomeType,
            int statusCode,
            Map<String, List<String>> responseHeaders,
            byte[] responseBody,
            InputStream responseStream,
            String contentType,
            boolean streaming,
            String message) {
        this.outcomeType = Objects.requireNonNull(outcomeType, "Outcome type must not be null");
        this.statusCode = statusCode;
        this.responseHeaders = copyMultiValueMap(responseHeaders);
        this.responseBody = responseBody == null ? null : responseBody.clone();
        this.responseStream = responseStream;
        this.contentType = contentType;
        this.streaming = streaming;
        this.message = message;
    }

    public static UnifiedAccessProxyResponseModel success(
            int statusCode,
            Map<String, List<String>> responseHeaders,
            byte[] responseBody,
            String contentType,
            boolean streaming) {
        return new UnifiedAccessProxyResponseModel(
                UnifiedAccessExecutionOutcomeType.SUCCESS,
                statusCode,
                responseHeaders,
                responseBody,
                null,
                contentType,
                streaming,
                null
        );
    }

    public static UnifiedAccessProxyResponseModel successStream(
            int statusCode,
            Map<String, List<String>> responseHeaders,
            InputStream responseStream,
            String contentType) {
        return new UnifiedAccessProxyResponseModel(
                UnifiedAccessExecutionOutcomeType.SUCCESS,
                statusCode,
                responseHeaders,
                null,
                responseStream,
                contentType,
                true,
                null
        );
    }

    public static UnifiedAccessProxyResponseModel upstreamFailure(
            int statusCode,
            Map<String, List<String>> responseHeaders,
            byte[] responseBody,
            String contentType,
            boolean streaming,
            String message) {
        return new UnifiedAccessProxyResponseModel(
                UnifiedAccessExecutionOutcomeType.UPSTREAM_FAILURE,
                statusCode,
                responseHeaders,
                responseBody,
                null,
                contentType,
                streaming,
                message
        );
    }

    public static UnifiedAccessProxyResponseModel upstreamFailureStream(
            int statusCode,
            Map<String, List<String>> responseHeaders,
            InputStream responseStream,
            String contentType,
            String message) {
        return new UnifiedAccessProxyResponseModel(
                UnifiedAccessExecutionOutcomeType.UPSTREAM_FAILURE,
                statusCode,
                responseHeaders,
                null,
                responseStream,
                contentType,
                true,
                message
        );
    }

    public static UnifiedAccessProxyResponseModel upstreamTimeout(
            int statusCode,
            byte[] responseBody,
            String contentType,
            String message) {
        return new UnifiedAccessProxyResponseModel(
                UnifiedAccessExecutionOutcomeType.UPSTREAM_TIMEOUT,
                statusCode,
                Map.of(),
                responseBody,
                null,
                contentType,
                false,
                message
        );
    }

    public UnifiedAccessExecutionOutcomeType getOutcomeType() {
        return outcomeType;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public Map<String, List<String>> getResponseHeaders() {
        return responseHeaders;
    }

    public byte[] getResponseBody() {
        return responseBody == null ? null : responseBody.clone();
    }

    public InputStream getResponseStream() {
        return responseStream;
    }

    public boolean hasResponseStream() {
        return responseStream != null;
    }

    public String getContentType() {
        return contentType;
    }

    public boolean isStreaming() {
        return streaming;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public void close() throws IOException {
        if (responseStream != null) {
            responseStream.close();
        }
    }

    private static Map<String, List<String>> copyMultiValueMap(Map<String, List<String>> source) {
        if (source == null || source.isEmpty()) {
            return Map.of();
        }
        Map<String, List<String>> copied = new LinkedHashMap<>();
        for (Map.Entry<String, List<String>> entry : source.entrySet()) {
            List<String> values = entry.getValue() == null ? List.of() : List.copyOf(new ArrayList<>(entry.getValue()));
            copied.put(entry.getKey(), values);
        }
        return Map.copyOf(copied);
    }
}
