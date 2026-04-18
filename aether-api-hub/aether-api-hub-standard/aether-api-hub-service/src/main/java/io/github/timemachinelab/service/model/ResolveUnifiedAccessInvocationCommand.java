package io.github.timemachinelab.service.model;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Command for resolving a unified access invocation before upstream forwarding.
 */
public class ResolveUnifiedAccessInvocationCommand {

    private final String apiCode;
    private final String plaintextApiKey;
    private final String httpMethod;
    private final Map<String, List<String>> headers;
    private final Map<String, List<String>> queryParameters;
    private final byte[] requestBody;
    private final String contentType;
    private final String accessChannel;

    public ResolveUnifiedAccessInvocationCommand(
            String apiCode,
            String plaintextApiKey,
            String httpMethod,
            Map<String, List<String>> headers,
            Map<String, List<String>> queryParameters,
            byte[] requestBody,
            String contentType,
            String accessChannel) {
        this.apiCode = apiCode;
        this.plaintextApiKey = plaintextApiKey;
        this.httpMethod = httpMethod;
        this.headers = copyMultiValueMap(headers);
        this.queryParameters = copyMultiValueMap(queryParameters);
        this.requestBody = requestBody == null ? null : requestBody.clone();
        this.contentType = contentType;
        this.accessChannel = accessChannel;
    }

    public String getApiCode() {
        return apiCode;
    }

    public String getPlaintextApiKey() {
        return plaintextApiKey;
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
