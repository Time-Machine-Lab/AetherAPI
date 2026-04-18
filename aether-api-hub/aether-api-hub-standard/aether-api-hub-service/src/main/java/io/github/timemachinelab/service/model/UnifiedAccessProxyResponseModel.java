package io.github.timemachinelab.service.model;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Downstream proxy execution result returned to the unified access entry adapter.
 */
public class UnifiedAccessProxyResponseModel {

    private final int statusCode;
    private final Map<String, List<String>> responseHeaders;
    private final byte[] responseBody;
    private final String contentType;
    private final boolean streaming;

    public UnifiedAccessProxyResponseModel(
            int statusCode,
            Map<String, List<String>> responseHeaders,
            byte[] responseBody,
            String contentType,
            boolean streaming) {
        this.statusCode = statusCode;
        this.responseHeaders = copyMultiValueMap(responseHeaders);
        this.responseBody = responseBody == null ? null : responseBody.clone();
        this.contentType = contentType;
        this.streaming = streaming;
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

    public String getContentType() {
        return contentType;
    }

    public boolean isStreaming() {
        return streaming;
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
