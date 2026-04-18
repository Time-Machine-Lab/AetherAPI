package io.github.timemachinelab.service.model;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Resolved unified access invocation passed to downstream proxy execution.
 */
public class UnifiedAccessInvocationModel {

    private final ConsumerContextModel consumerContext;
    private final TargetApiSnapshotModel targetApi;
    private final String httpMethod;
    private final Map<String, List<String>> headers;
    private final Map<String, List<String>> queryParameters;
    private final byte[] requestBody;
    private final String contentType;
    private final String accessChannel;

    public UnifiedAccessInvocationModel(
            ConsumerContextModel consumerContext,
            TargetApiSnapshotModel targetApi,
            String httpMethod,
            Map<String, List<String>> headers,
            Map<String, List<String>> queryParameters,
            byte[] requestBody,
            String contentType,
            String accessChannel) {
        this.consumerContext = Objects.requireNonNull(consumerContext, "Consumer context must not be null");
        this.targetApi = Objects.requireNonNull(targetApi, "Target API snapshot must not be null");
        this.httpMethod = httpMethod;
        this.headers = copyMultiValueMap(headers);
        this.queryParameters = copyMultiValueMap(queryParameters);
        this.requestBody = requestBody == null ? null : requestBody.clone();
        this.contentType = contentType;
        this.accessChannel = accessChannel;
    }

    public ConsumerContextModel getConsumerContext() {
        return consumerContext;
    }

    public TargetApiSnapshotModel getTargetApi() {
        return targetApi;
    }

    public String getHttpMethod() {
        return httpMethod;
    }

    public Map<String, List<String>> getHeaders() {
        return headers;
    }

    public Map<String, List<String>> getQueryParameters() {
        return queryParameters;
    }

    public byte[] getRequestBody() {
        return requestBody == null ? null : requestBody.clone();
    }

    public String getContentType() {
        return contentType;
    }

    public String getAccessChannel() {
        return accessChannel;
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
