package io.github.timemachinelab.adapter.web.controller;

import io.github.timemachinelab.adapter.web.auth.ConsoleSessionPrincipal;
import io.github.timemachinelab.adapter.web.delegate.PlatformProxyProfileWebDelegate;
import io.github.timemachinelab.api.req.BindProxyProfileReq;
import io.github.timemachinelab.api.req.CreatePlatformProxyProfileReq;
import io.github.timemachinelab.api.req.UpdatePlatformProxyProfileReq;
import io.github.timemachinelab.api.resp.AssetProxyBindingResp;
import io.github.timemachinelab.api.resp.PlatformProxyProfilePageResp;
import io.github.timemachinelab.api.resp.PlatformProxyProfileResp;
import io.github.timemachinelab.common.annotation.AutoResp;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Platform proxy profile HTTP entry point.
 */
@RestController
@RequestMapping("/api/v1/platform/proxy-profiles")
@AutoResp
public class PlatformProxyProfileController {

    private final PlatformProxyProfileWebDelegate delegate;

    public PlatformProxyProfileController(PlatformProxyProfileWebDelegate delegate) {
        this.delegate = delegate;
    }

    @GetMapping
    public PlatformProxyProfilePageResp listProfiles(
            @RequestParam(name = "enabled", required = false) Boolean enabled,
            @RequestParam(name = "keyword", required = false) String keyword,
            @RequestParam(name = "page", defaultValue = "1") int page,
            @RequestParam(name = "size", defaultValue = "20") int size,
            @RequestAttribute(name = ConsoleSessionPrincipal.REQUEST_ATTRIBUTE, required = false)
            ConsoleSessionPrincipal principal) {
        return delegate.listProfiles(role(principal), enabled, keyword, page, size);
    }

    @GetMapping("/{profileId}")
    public PlatformProxyProfileResp getProfile(
            @PathVariable("profileId") String profileId,
            @RequestAttribute(name = ConsoleSessionPrincipal.REQUEST_ATTRIBUTE, required = false)
            ConsoleSessionPrincipal principal) {
        return delegate.getProfile(role(principal), profileId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PlatformProxyProfileResp createProfile(
            @Valid @RequestBody CreatePlatformProxyProfileReq req,
            @RequestAttribute(name = ConsoleSessionPrincipal.REQUEST_ATTRIBUTE, required = false)
            ConsoleSessionPrincipal principal) {
        return delegate.createProfile(role(principal), req);
    }

    @PutMapping("/{profileId}")
    public PlatformProxyProfileResp updateProfile(
            @PathVariable("profileId") String profileId,
            @Valid @RequestBody UpdatePlatformProxyProfileReq req,
            @RequestAttribute(name = ConsoleSessionPrincipal.REQUEST_ATTRIBUTE, required = false)
            ConsoleSessionPrincipal principal) {
        return delegate.updateProfile(role(principal), profileId, req);
    }

    @PatchMapping("/{profileId}/enable")
    public PlatformProxyProfileResp enableProfile(
            @PathVariable("profileId") String profileId,
            @RequestAttribute(name = ConsoleSessionPrincipal.REQUEST_ATTRIBUTE, required = false)
            ConsoleSessionPrincipal principal) {
        return delegate.enableProfile(role(principal), profileId);
    }

    @PatchMapping("/{profileId}/disable")
    public PlatformProxyProfileResp disableProfile(
            @PathVariable("profileId") String profileId,
            @RequestAttribute(name = ConsoleSessionPrincipal.REQUEST_ATTRIBUTE, required = false)
            ConsoleSessionPrincipal principal) {
        return delegate.disableProfile(role(principal), profileId);
    }

    @DeleteMapping("/{profileId}")
    public PlatformProxyProfileResp deleteProfile(
            @PathVariable("profileId") String profileId,
            @RequestAttribute(name = ConsoleSessionPrincipal.REQUEST_ATTRIBUTE, required = false)
            ConsoleSessionPrincipal principal) {
        return delegate.deleteProfile(role(principal), profileId);
    }

    @PutMapping("/asset-bindings/{apiCode}")
    public AssetProxyBindingResp bindProxyProfile(
            @PathVariable("apiCode") String apiCode,
            @Valid @RequestBody BindProxyProfileReq req,
            @RequestAttribute(name = ConsoleSessionPrincipal.REQUEST_ATTRIBUTE, required = false)
            ConsoleSessionPrincipal principal) {
        return delegate.bindProxyProfile(role(principal), apiCode, req);
    }

    @DeleteMapping("/asset-bindings/{apiCode}")
    public AssetProxyBindingResp unbindProxyProfile(
            @PathVariable("apiCode") String apiCode,
            @RequestAttribute(name = ConsoleSessionPrincipal.REQUEST_ATTRIBUTE, required = false)
            ConsoleSessionPrincipal principal) {
        return delegate.unbindProxyProfile(role(principal), apiCode);
    }

    private String role(ConsoleSessionPrincipal principal) {
        return principal == null ? null : principal.getRole();
    }
}
