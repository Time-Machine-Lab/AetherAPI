package io.github.timemachinelab.adapter.web.controller;

import io.github.timemachinelab.adapter.web.auth.ConsoleSessionPrincipal;
import io.github.timemachinelab.adapter.web.delegate.ApiSubscriptionWebDelegate;
import io.github.timemachinelab.api.req.SubscribeApiReq;
import io.github.timemachinelab.api.resp.ApiSubscriptionPageResp;
import io.github.timemachinelab.api.resp.ApiSubscriptionResp;
import io.github.timemachinelab.api.resp.ApiSubscriptionStatusResp;
import io.github.timemachinelab.common.annotation.AutoResp;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

/**
 * Current-user API subscription HTTP entry point.
 */
@RestController
@RequestMapping("/api/v1/current-user/api-subscriptions")
@AutoResp
public class ApiSubscriptionController {

    private final ApiSubscriptionWebDelegate delegate;

    public ApiSubscriptionController(ApiSubscriptionWebDelegate delegate) {
        this.delegate = delegate;
    }

    @PostMapping
    public ApiSubscriptionResp subscribe(
            @Valid @RequestBody SubscribeApiReq req,
            @RequestAttribute(name = ConsoleSessionPrincipal.REQUEST_ATTRIBUTE, required = false)
            ConsoleSessionPrincipal consoleSessionPrincipal,
            Principal principal) {
        return delegate.subscribe(currentUserId(consoleSessionPrincipal, principal), req);
    }

    @GetMapping
    public ApiSubscriptionPageResp listSubscriptions(
            @RequestParam(name = "page", defaultValue = "1") int page,
            @RequestParam(name = "size", defaultValue = "20") int size,
            @RequestAttribute(name = ConsoleSessionPrincipal.REQUEST_ATTRIBUTE, required = false)
            ConsoleSessionPrincipal consoleSessionPrincipal,
            Principal principal) {
        return delegate.listSubscriptions(currentUserId(consoleSessionPrincipal, principal), page, size);
    }

    @GetMapping("/status")
    public ApiSubscriptionStatusResp getSubscriptionStatus(
            @RequestParam("apiCode") String apiCode,
            @RequestAttribute(name = ConsoleSessionPrincipal.REQUEST_ATTRIBUTE, required = false)
            ConsoleSessionPrincipal consoleSessionPrincipal,
            Principal principal) {
        return delegate.getSubscriptionStatus(currentUserId(consoleSessionPrincipal, principal), apiCode);
    }

    @PatchMapping("/{subscriptionId}/cancel")
    public ApiSubscriptionResp cancelSubscription(
            @PathVariable("subscriptionId") String subscriptionId,
            @RequestAttribute(name = ConsoleSessionPrincipal.REQUEST_ATTRIBUTE, required = false)
            ConsoleSessionPrincipal consoleSessionPrincipal,
            Principal principal) {
        return delegate.cancelSubscription(currentUserId(consoleSessionPrincipal, principal), subscriptionId);
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
}
