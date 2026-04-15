package io.github.timemachinelab.domain.catalog.model;

import java.util.Objects;

/**
 * 请求/响应示例快照。
 */
public final class ExampleSnapshot {

    private final String requestExample;
    private final String responseExample;

    private ExampleSnapshot(String requestExample, String responseExample) {
        this.requestExample = requestExample;
        this.responseExample = responseExample;
    }

    public static ExampleSnapshot of(String requestExample, String responseExample) {
        String normalizedRequest = normalize(requestExample);
        String normalizedResponse = normalize(responseExample);
        if (normalizedRequest == null && normalizedResponse == null) {
            return null;
        }
        return new ExampleSnapshot(normalizedRequest, normalizedResponse);
    }

    public String getRequestExample() {
        return requestExample;
    }

    public String getResponseExample() {
        return responseExample;
    }

    private static String normalize(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ExampleSnapshot that = (ExampleSnapshot) o;
        return Objects.equals(requestExample, that.requestExample)
                && Objects.equals(responseExample, that.responseExample);
    }

    @Override
    public int hashCode() {
        return Objects.hash(requestExample, responseExample);
    }
}

