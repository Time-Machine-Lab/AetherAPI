package io.github.timemachinelab.adapter.web.controller;

import io.github.timemachinelab.adapter.web.auth.ConsoleSessionPrincipal;
import io.github.timemachinelab.adapter.web.delegate.ApiCallLogWebDelegate;
import io.github.timemachinelab.api.req.ListApiCallLogReq;
import io.github.timemachinelab.api.resp.ApiCallLogDetailResp;
import io.github.timemachinelab.api.resp.ApiCallLogPageResp;
import io.github.timemachinelab.common.annotation.AutoResp;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

/**
 * Current-user API call log HTTP entry point.
 */
@RestController
@RequestMapping("/api/v1/current-user/api-call-logs")
@AutoResp
public class ApiCallLogController {

    private final ApiCallLogWebDelegate delegate;

    public ApiCallLogController(ApiCallLogWebDelegate delegate) {
        this.delegate = delegate;
    }

    @GetMapping
    public ApiCallLogPageResp listApiCallLogs(
            @Valid ListApiCallLogReq req,
            @RequestAttribute(name = ConsoleSessionPrincipal.REQUEST_ATTRIBUTE, required = false)
            ConsoleSessionPrincipal consoleSessionPrincipal,
            Principal principal) {
        return delegate.listApiCallLogs(currentUserId(consoleSessionPrincipal, principal), req);
    }

    @GetMapping("/{logId}")
    public ApiCallLogDetailResp getApiCallLogDetail(
            @PathVariable("logId") String logId,
            @RequestAttribute(name = ConsoleSessionPrincipal.REQUEST_ATTRIBUTE, required = false)
            ConsoleSessionPrincipal consoleSessionPrincipal,
            Principal principal) {
        return delegate.getApiCallLogDetail(currentUserId(consoleSessionPrincipal, principal), logId);
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
