package io.github.timemachinelab.adapter.web.controller;

import io.github.timemachinelab.adapter.web.auth.ConsoleSessionPrincipal;
import io.github.timemachinelab.adapter.web.delegate.ApiAssetWebDelegate;
import io.github.timemachinelab.api.req.AttachAiCapabilityProfileReq;
import io.github.timemachinelab.api.req.ListApiAssetReq;
import io.github.timemachinelab.api.req.RegisterApiAssetReq;
import io.github.timemachinelab.api.req.ReviseApiAssetReq;
import io.github.timemachinelab.api.resp.ApiAssetPageResp;
import io.github.timemachinelab.api.resp.ApiAssetResp;
import io.github.timemachinelab.common.annotation.AutoResp;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

/**
 * Current-user asset workspace HTTP entry point.
 */
@RestController
@RequestMapping("/api/v1/current-user/assets")
@AutoResp
public class ApiAssetController {

    private final ApiAssetWebDelegate delegate;

    public ApiAssetController(ApiAssetWebDelegate delegate) {
        this.delegate = delegate;
    }

    @GetMapping
    public ApiAssetPageResp listAssets(
            @Valid ListApiAssetReq req,
            @RequestAttribute(name = ConsoleSessionPrincipal.REQUEST_ATTRIBUTE, required = false)
            ConsoleSessionPrincipal consoleSessionPrincipal,
            Principal principal) {
        return delegate.listAssets(currentUserId(consoleSessionPrincipal, principal), req);
    }

    @PostMapping
    public ApiAssetResp registerAsset(
            @Valid @RequestBody RegisterApiAssetReq req,
            @RequestAttribute(name = ConsoleSessionPrincipal.REQUEST_ATTRIBUTE, required = false)
            ConsoleSessionPrincipal consoleSessionPrincipal,
            Principal principal) {
        return delegate.registerAsset(
                currentUserId(consoleSessionPrincipal, principal),
                publisherDisplayName(consoleSessionPrincipal, principal),
                req
        );
    }

    @GetMapping("/{apiCode}")
    public ApiAssetResp getAssetByCode(
            @PathVariable("apiCode") String apiCode,
            @RequestAttribute(name = ConsoleSessionPrincipal.REQUEST_ATTRIBUTE, required = false)
            ConsoleSessionPrincipal consoleSessionPrincipal,
            Principal principal) {
        return delegate.getAssetByCode(currentUserId(consoleSessionPrincipal, principal), apiCode);
    }

    @PutMapping("/{apiCode}")
    public ApiAssetResp reviseAsset(
            @PathVariable("apiCode") String apiCode,
            @Valid @RequestBody ReviseApiAssetReq req,
            @RequestAttribute(name = ConsoleSessionPrincipal.REQUEST_ATTRIBUTE, required = false)
            ConsoleSessionPrincipal consoleSessionPrincipal,
            Principal principal) {
        return delegate.reviseAsset(
                currentUserId(consoleSessionPrincipal, principal),
                publisherDisplayName(consoleSessionPrincipal, principal),
                apiCode,
                req
        );
    }

    @PatchMapping("/{apiCode}/publish")
    public ApiAssetResp publishAsset(
            @PathVariable("apiCode") String apiCode,
            @RequestAttribute(name = ConsoleSessionPrincipal.REQUEST_ATTRIBUTE, required = false)
            ConsoleSessionPrincipal consoleSessionPrincipal,
            Principal principal) {
        return delegate.publishAsset(
                currentUserId(consoleSessionPrincipal, principal),
                publisherDisplayName(consoleSessionPrincipal, principal),
                apiCode
        );
    }

    @PatchMapping("/{apiCode}/unpublish")
    public ApiAssetResp unpublishAsset(
            @PathVariable("apiCode") String apiCode,
            @RequestAttribute(name = ConsoleSessionPrincipal.REQUEST_ATTRIBUTE, required = false)
            ConsoleSessionPrincipal consoleSessionPrincipal,
            Principal principal) {
        return delegate.unpublishAsset(currentUserId(consoleSessionPrincipal, principal), apiCode);
    }

    @DeleteMapping("/{apiCode}")
    public ApiAssetResp deleteAsset(
            @PathVariable("apiCode") String apiCode,
            @RequestAttribute(name = ConsoleSessionPrincipal.REQUEST_ATTRIBUTE, required = false)
            ConsoleSessionPrincipal consoleSessionPrincipal,
            Principal principal) {
        return delegate.deleteAsset(currentUserId(consoleSessionPrincipal, principal), apiCode);
    }

    @PutMapping("/{apiCode}/ai-profile")
    public ApiAssetResp attachAiCapabilityProfile(
            @PathVariable("apiCode") String apiCode,
            @Valid @RequestBody AttachAiCapabilityProfileReq req,
            @RequestAttribute(name = ConsoleSessionPrincipal.REQUEST_ATTRIBUTE, required = false)
            ConsoleSessionPrincipal consoleSessionPrincipal,
            Principal principal) {
        return delegate.attachAiCapabilityProfile(
                currentUserId(consoleSessionPrincipal, principal),
                publisherDisplayName(consoleSessionPrincipal, principal),
                apiCode,
                req
        );
    }

    private String currentUserId(ConsoleSessionPrincipal consoleSessionPrincipal, Principal principal) {
        if (consoleSessionPrincipal != null
                && consoleSessionPrincipal.getUserId() != null
                && !consoleSessionPrincipal.getUserId().isBlank()) {
            return consoleSessionPrincipal.getUserId();
        }
        if (principal == null || principal.getName() == null || principal.getName().isBlank()) {
            throw new IllegalArgumentException("Current user id must not be blank");
        }
        return principal.getName();
    }

    private String publisherDisplayName(ConsoleSessionPrincipal consoleSessionPrincipal, Principal principal) {
        if (consoleSessionPrincipal != null) {
            if (consoleSessionPrincipal.getDisplayName() != null && !consoleSessionPrincipal.getDisplayName().isBlank()) {
                return consoleSessionPrincipal.getDisplayName();
            }
            if (consoleSessionPrincipal.getLoginName() != null && !consoleSessionPrincipal.getLoginName().isBlank()) {
                return consoleSessionPrincipal.getLoginName();
            }
        }
        return currentUserId(consoleSessionPrincipal, principal);
    }
}
