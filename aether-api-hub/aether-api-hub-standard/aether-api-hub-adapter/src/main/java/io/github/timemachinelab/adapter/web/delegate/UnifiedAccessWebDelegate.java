package io.github.timemachinelab.adapter.web.delegate;

import io.github.timemachinelab.service.model.ResolveUnifiedAccessInvocationCommand;
import io.github.timemachinelab.service.model.UnifiedAccessProxyResponseModel;
import io.github.timemachinelab.service.port.in.UnifiedAccessUseCase;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Unified access HTTP delegate.
 */
@Component
public class UnifiedAccessWebDelegate {

    private static final String API_KEY_HEADER = "X-Aether-Api-Key";
    private static final String DEFAULT_ACCESS_CHANNEL = "UNIFIED_ACCESS";

    private final UnifiedAccessUseCase unifiedAccessUseCase;

    public UnifiedAccessWebDelegate(UnifiedAccessUseCase unifiedAccessUseCase) {
        this.unifiedAccessUseCase = unifiedAccessUseCase;
    }

    public ResponseEntity<byte[]> invoke(
            String apiCode,
            String httpMethod,
            HttpHeaders headers,
            MultiValueMap<String, String> queryParameters,
            byte[] requestBody,
            String contentType) {
        ResolveUnifiedAccessInvocationCommand command = new ResolveUnifiedAccessInvocationCommand(
                apiCode,
                headers == null ? null : headers.getFirst(API_KEY_HEADER),
                httpMethod,
                copyMultiValueMap(headers),
                copyMultiValueMap(queryParameters),
                requestBody,
                contentType,
                DEFAULT_ACCESS_CHANNEL
        );
        UnifiedAccessProxyResponseModel response = unifiedAccessUseCase.invoke(command);
        return ResponseEntity.status(response.getStatusCode())
                .headers(toHttpHeaders(response))
                .body(response.getResponseBody());
    }

    private HttpHeaders toHttpHeaders(UnifiedAccessProxyResponseModel response) {
        HttpHeaders headers = new HttpHeaders();
        for (Map.Entry<String, List<String>> entry : response.getResponseHeaders().entrySet()) {
            headers.put(entry.getKey(), new ArrayList<>(entry.getValue()));
        }
        if (response.getContentType() != null && !response.getContentType().isBlank()) {
            headers.setContentType(MediaType.parseMediaType(response.getContentType()));
        }
        return headers;
    }

    private Map<String, List<String>> copyMultiValueMap(MultiValueMap<String, String> source) {
        if (source == null || source.isEmpty()) {
            return Map.of();
        }
        Map<String, List<String>> copied = new LinkedHashMap<>();
        for (Map.Entry<String, List<String>> entry : source.entrySet()) {
            copied.put(entry.getKey(), entry.getValue() == null ? List.of() : List.copyOf(entry.getValue()));
        }
        return copied;
    }
}
