package io.github.timemachinelab.adapter.web.controller;

import io.github.timemachinelab.adapter.web.delegate.ApiCallLogWebDelegate;
import io.github.timemachinelab.api.req.ListApiCallLogReq;
import io.github.timemachinelab.api.resp.ApiCallLogDetailResp;
import io.github.timemachinelab.api.resp.ApiCallLogPageResp;
import io.github.timemachinelab.common.annotation.AutoResp;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
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
    public ApiCallLogPageResp listApiCallLogs(@Valid ListApiCallLogReq req, Principal principal) {
        return delegate.listApiCallLogs(currentUserId(principal), req);
    }

    @GetMapping("/{logId}")
    public ApiCallLogDetailResp getApiCallLogDetail(
            @PathVariable("logId") String logId, Principal principal) {
        return delegate.getApiCallLogDetail(currentUserId(principal), logId);
    }

    private String currentUserId(Principal principal) {
        if (principal == null || principal.getName() == null || principal.getName().isBlank()) {
            throw new IllegalArgumentException("Current user id must not be blank");
        }
        return principal.getName();
    }
}
