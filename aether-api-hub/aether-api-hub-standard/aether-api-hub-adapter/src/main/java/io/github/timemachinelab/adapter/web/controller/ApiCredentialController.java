package io.github.timemachinelab.adapter.web.controller;

import io.github.timemachinelab.adapter.web.delegate.ApiCredentialWebDelegate;
import io.github.timemachinelab.api.req.CreateApiCredentialReq;
import io.github.timemachinelab.api.resp.ApiCredentialPageResp;
import io.github.timemachinelab.api.resp.ApiCredentialResp;
import io.github.timemachinelab.api.resp.IssuedApiCredentialResp;
import io.github.timemachinelab.common.annotation.AutoResp;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

/**
 * 当前用户 API Key 管理 HTTP 接入点。
 */
@RestController
@RequestMapping("/api/v1/current-user/api-keys")
@AutoResp
public class ApiCredentialController {

    private final ApiCredentialWebDelegate delegate;

    public ApiCredentialController(ApiCredentialWebDelegate delegate) {
        this.delegate = delegate;
    }

    @PostMapping
    public IssuedApiCredentialResp createApiCredential(@Valid @RequestBody CreateApiCredentialReq req, Principal principal) {
        return delegate.createApiCredential(currentUserId(principal), req);
    }

    @GetMapping
    public ApiCredentialPageResp listApiCredentials(
            @RequestParam(name = "status", required = false) String status,
            @RequestParam(name = "page", defaultValue = "1") int page,
            @RequestParam(name = "size", defaultValue = "20") int size,
            Principal principal) {
        return delegate.listApiCredentials(currentUserId(principal), status, page, size);
    }

    @GetMapping("/{credentialId}")
    public ApiCredentialResp getApiCredentialDetail(
            @PathVariable("credentialId") String credentialId, Principal principal) {
        return delegate.getApiCredentialDetail(currentUserId(principal), credentialId);
    }

    @PatchMapping("/{credentialId}/enable")
    public ApiCredentialResp enableApiCredential(
            @PathVariable("credentialId") String credentialId, Principal principal) {
        return delegate.enableApiCredential(currentUserId(principal), credentialId);
    }

    @PatchMapping("/{credentialId}/disable")
    public ApiCredentialResp disableApiCredential(
            @PathVariable("credentialId") String credentialId, Principal principal) {
        return delegate.disableApiCredential(currentUserId(principal), credentialId);
    }

    @PatchMapping("/{credentialId}/revoke")
    public ApiCredentialResp revokeApiCredential(
            @PathVariable("credentialId") String credentialId, Principal principal) {
        return delegate.revokeApiCredential(currentUserId(principal), credentialId);
    }

    private String currentUserId(Principal principal) {
        if (principal == null || principal.getName() == null || principal.getName().isBlank()) {
            throw new IllegalArgumentException("Current user id must not be blank");
        }
        return principal.getName();
    }
}
