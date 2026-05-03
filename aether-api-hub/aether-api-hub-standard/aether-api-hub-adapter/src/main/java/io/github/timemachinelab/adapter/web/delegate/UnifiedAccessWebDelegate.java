package io.github.timemachinelab.adapter.web.delegate;

import io.github.timemachinelab.service.model.ResolveUnifiedAccessInvocationCommand;
import io.github.timemachinelab.service.model.UnifiedAccessProxyResponseModel;
import io.github.timemachinelab.service.port.in.UnifiedAccessUseCase;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Unified access HTTP delegate.
 */
@Component
public class UnifiedAccessWebDelegate {

    private static final Logger log = LoggerFactory.getLogger(UnifiedAccessWebDelegate.class);
    private static final String API_KEY_HEADER = "X-Aether-Api-Key";
    private static final String DEFAULT_ACCESS_CHANNEL = "UNIFIED_ACCESS";
    private static final int STREAM_BUFFER_SIZE = 8192;
    private static final Set<String> ADAPTER_CONTROLLED_RESPONSE_HEADERS = Set.of(
            "content-type",
            "content-length",
            "transfer-encoding",
            "connection"
    );

    private final UnifiedAccessUseCase unifiedAccessUseCase;

    public UnifiedAccessWebDelegate(UnifiedAccessUseCase unifiedAccessUseCase) {
        this.unifiedAccessUseCase = unifiedAccessUseCase;
    }

    public ResponseEntity<?> invoke(
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
        if (response.isStreaming() && response.hasResponseStream()) {
            StreamingResponseBody body = outputStream -> writeStreamingResponse(response, outputStream);
            return ResponseEntity.status(response.getStatusCode())
                    .headers(toHttpHeaders(response))
                    .body(body);
        }
        return ResponseEntity.status(response.getStatusCode())
                .headers(toHttpHeaders(response))
                .body(response.getResponseBody());
    }

    public void invokeToResponse(
            String apiCode,
            String httpMethod,
            HttpHeaders headers,
            MultiValueMap<String, String> queryParameters,
            byte[] requestBody,
            String contentType,
            HttpServletResponse servletResponse) {
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
        writeServletResponse(response, servletResponse);
    }

    private HttpHeaders toHttpHeaders(UnifiedAccessProxyResponseModel response) {
        HttpHeaders headers = new HttpHeaders();
        for (Map.Entry<String, List<String>> entry : response.getResponseHeaders().entrySet()) {
            if (isAdapterControlledResponseHeader(entry.getKey())) {
                continue;
            }
            headers.put(entry.getKey(), new ArrayList<>(entry.getValue()));
        }
        if (response.getContentType() != null && !response.getContentType().isBlank()) {
            try {
                headers.setContentType(MediaType.parseMediaType(response.getContentType()));
            } catch (IllegalArgumentException ex) {
                log.warn("Unified access upstream returned an invalid content type; skipping response Content-Type. contentType={}",
                        response.getContentType());
            }
        }
        return headers;
    }

    private void writeStreamingResponse(UnifiedAccessProxyResponseModel response, OutputStream outputStream) {
        try (InputStream inputStream = response.getResponseStream()) {
            byte[] buffer = new byte[STREAM_BUFFER_SIZE];
            int length;
            while ((length = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, length);
                outputStream.flush();
            }
        } catch (IOException ex) {
            log.warn(
                    "Unified access streaming response write failed after upstream response was established. statusCode={}, contentType={}",
                    response.getStatusCode(),
                    response.getContentType(),
                    ex
            );
        }
    }

    private void writeServletResponse(
            UnifiedAccessProxyResponseModel response,
            HttpServletResponse servletResponse) {
        servletResponse.setStatus(response.getStatusCode());
        for (Map.Entry<String, List<String>> entry : response.getResponseHeaders().entrySet()) {
            if (isAdapterControlledResponseHeader(entry.getKey())) {
                continue;
            }
            for (String value : entry.getValue()) {
                servletResponse.addHeader(entry.getKey(), value);
            }
        }
        if (response.getContentType() != null && !response.getContentType().isBlank()) {
            try {
                servletResponse.setContentType(MediaType.parseMediaType(response.getContentType()).toString());
            } catch (IllegalArgumentException ex) {
                log.warn("Unified access upstream returned an invalid content type; skipping servlet Content-Type. contentType={}",
                        response.getContentType());
            }
        }
        try {
            OutputStream outputStream = servletResponse.getOutputStream();
            if (response.isStreaming() && response.hasResponseStream()) {
                writeStreamingResponse(response, outputStream);
                return;
            }
            byte[] responseBody = response.getResponseBody();
            if (responseBody != null && responseBody.length > 0) {
                outputStream.write(responseBody);
            }
            outputStream.flush();
        } catch (IOException ex) {
            log.warn(
                    "Unified access servlet response write failed after upstream response was established. statusCode={}, contentType={}",
                    response.getStatusCode(),
                    response.getContentType(),
                    ex
            );
        }
    }

    private boolean isAdapterControlledResponseHeader(String headerName) {
        if (headerName == null || headerName.isBlank()) {
            return true;
        }
        return ADAPTER_CONTROLLED_RESPONSE_HEADERS.contains(headerName.toLowerCase(Locale.ROOT));
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
