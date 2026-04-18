package io.github.timemachinelab.adapter.web.controller;

import io.github.timemachinelab.adapter.web.delegate.UnifiedAccessWebDelegate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Unified access HTTP entry point.
 */
@RestController
@RequestMapping("/api/v1/access")
public class UnifiedAccessController {

    private final UnifiedAccessWebDelegate delegate;

    public UnifiedAccessController(UnifiedAccessWebDelegate delegate) {
        this.delegate = delegate;
    }

    @GetMapping("/{apiCode}")
    public ResponseEntity<byte[]> get(
            @PathVariable String apiCode,
            @RequestHeader HttpHeaders headers,
            @RequestParam MultiValueMap<String, String> queryParameters) {
        return delegate.invoke(apiCode, "GET", headers, queryParameters, null, headers.getFirst(HttpHeaders.CONTENT_TYPE));
    }

    @PostMapping("/{apiCode}")
    public ResponseEntity<byte[]> post(
            @PathVariable String apiCode,
            @RequestHeader HttpHeaders headers,
            @RequestParam MultiValueMap<String, String> queryParameters,
            @RequestBody(required = false) byte[] requestBody) {
        return delegate.invoke(apiCode, "POST", headers, queryParameters, requestBody, headers.getFirst(HttpHeaders.CONTENT_TYPE));
    }

    @PutMapping("/{apiCode}")
    public ResponseEntity<byte[]> put(
            @PathVariable String apiCode,
            @RequestHeader HttpHeaders headers,
            @RequestParam MultiValueMap<String, String> queryParameters,
            @RequestBody(required = false) byte[] requestBody) {
        return delegate.invoke(apiCode, "PUT", headers, queryParameters, requestBody, headers.getFirst(HttpHeaders.CONTENT_TYPE));
    }

    @PatchMapping("/{apiCode}")
    public ResponseEntity<byte[]> patch(
            @PathVariable String apiCode,
            @RequestHeader HttpHeaders headers,
            @RequestParam MultiValueMap<String, String> queryParameters,
            @RequestBody(required = false) byte[] requestBody) {
        return delegate.invoke(apiCode, "PATCH", headers, queryParameters, requestBody, headers.getFirst(HttpHeaders.CONTENT_TYPE));
    }

    @DeleteMapping("/{apiCode}")
    public ResponseEntity<byte[]> delete(
            @PathVariable String apiCode,
            @RequestHeader HttpHeaders headers,
            @RequestParam MultiValueMap<String, String> queryParameters) {
        return delegate.invoke(apiCode, "DELETE", headers, queryParameters, null, headers.getFirst(HttpHeaders.CONTENT_TYPE));
    }
}
