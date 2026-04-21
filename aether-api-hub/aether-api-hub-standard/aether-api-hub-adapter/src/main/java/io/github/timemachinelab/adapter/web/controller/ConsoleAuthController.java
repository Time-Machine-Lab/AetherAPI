package io.github.timemachinelab.adapter.web.controller;

import io.github.timemachinelab.adapter.web.auth.ConsoleSessionPrincipal;
import io.github.timemachinelab.adapter.web.delegate.ConsoleAuthWebDelegate;
import io.github.timemachinelab.api.req.ConsoleSignInReq;
import io.github.timemachinelab.api.resp.ConsoleCurrentSessionResp;
import io.github.timemachinelab.api.resp.ConsoleSignInResp;
import io.github.timemachinelab.common.annotation.AutoResp;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RestController;

/**
 * Console auth HTTP entry point.
 */
@RestController
@RequestMapping("/api/v1/console/auth")
@AutoResp
public class ConsoleAuthController {

    private final ConsoleAuthWebDelegate delegate;

    public ConsoleAuthController(ConsoleAuthWebDelegate delegate) {
        this.delegate = delegate;
    }

    @PostMapping("/sign-in")
    public ConsoleSignInResp signIn(@Valid @RequestBody ConsoleSignInReq req) {
        return delegate.signIn(req);
    }

    @GetMapping("/current-session")
    public ConsoleCurrentSessionResp getCurrentSession(
            @RequestAttribute(ConsoleSessionPrincipal.REQUEST_ATTRIBUTE) ConsoleSessionPrincipal principal) {
        return delegate.getCurrentSession(principal);
    }
}
