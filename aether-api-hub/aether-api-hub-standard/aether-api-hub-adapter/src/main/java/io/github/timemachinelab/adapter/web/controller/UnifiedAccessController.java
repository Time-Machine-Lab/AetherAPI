package io.github.timemachinelab.adapter.web.controller;

import io.github.timemachinelab.adapter.web.delegate.UnifiedAccessWebDelegate;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
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
    public void get(
            @PathVariable("apiCode") String apiCode,
            @RequestHeader HttpHeaders headers,
            @RequestParam MultiValueMap<String, String> queryParameters,
            HttpServletResponse response) {
        delegate.invokeToResponse(apiCode, "GET", headers, queryParameters, null, headers.getFirst(HttpHeaders.CONTENT_TYPE), response);
    }

    @PostMapping("/{apiCode}")
    public void post(
            @PathVariable("apiCode") String apiCode,
            @RequestHeader HttpHeaders headers,
            @RequestParam MultiValueMap<String, String> queryParameters,
            @RequestBody(required = false) byte[] requestBody,
            HttpServletResponse response) {
        delegate.invokeToResponse(apiCode, "POST", headers, queryParameters, requestBody, headers.getFirst(HttpHeaders.CONTENT_TYPE), response);
    }

    @PutMapping("/{apiCode}")
    public void put(
            @PathVariable("apiCode") String apiCode,
            @RequestHeader HttpHeaders headers,
            @RequestParam MultiValueMap<String, String> queryParameters,
            @RequestBody(required = false) byte[] requestBody,
            HttpServletResponse response) {
        delegate.invokeToResponse(apiCode, "PUT", headers, queryParameters, requestBody, headers.getFirst(HttpHeaders.CONTENT_TYPE), response);
    }

    @PatchMapping("/{apiCode}")
    public void patch(
            @PathVariable("apiCode") String apiCode,
            @RequestHeader HttpHeaders headers,
            @RequestParam MultiValueMap<String, String> queryParameters,
            @RequestBody(required = false) byte[] requestBody,
            HttpServletResponse response) {
        delegate.invokeToResponse(apiCode, "PATCH", headers, queryParameters, requestBody, headers.getFirst(HttpHeaders.CONTENT_TYPE), response);
    }

    @DeleteMapping("/{apiCode}")
    public void delete(
            @PathVariable("apiCode") String apiCode,
            @RequestHeader HttpHeaders headers,
            @RequestParam MultiValueMap<String, String> queryParameters,
            HttpServletResponse response) {
        delegate.invokeToResponse(apiCode, "DELETE", headers, queryParameters, null, headers.getFirst(HttpHeaders.CONTENT_TYPE), response);
    }
}
