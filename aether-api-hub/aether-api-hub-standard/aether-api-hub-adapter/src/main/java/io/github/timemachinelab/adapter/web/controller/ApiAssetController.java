package io.github.timemachinelab.adapter.web.controller;

import io.github.timemachinelab.adapter.web.delegate.ApiAssetWebDelegate;
import io.github.timemachinelab.api.req.AttachAiCapabilityProfileReq;
import io.github.timemachinelab.api.req.ListApiAssetReq;
import io.github.timemachinelab.api.req.RegisterApiAssetReq;
import io.github.timemachinelab.api.req.ReviseApiAssetReq;
import io.github.timemachinelab.api.resp.ApiAssetPageResp;
import io.github.timemachinelab.api.resp.ApiAssetResp;
import io.github.timemachinelab.common.annotation.AutoResp;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * API 资产管理 HTTP 接入点。
 */
@RestController
@RequestMapping("/api/v1/assets")
@AutoResp
public class ApiAssetController {

    private final ApiAssetWebDelegate delegate;

    public ApiAssetController(ApiAssetWebDelegate delegate) {
        this.delegate = delegate;
    }

    @GetMapping
    public ApiAssetPageResp listAssets(@Valid ListApiAssetReq req) {
        return delegate.listAssets(req);
    }

    @PostMapping
    public ApiAssetResp registerAsset(@Valid @RequestBody RegisterApiAssetReq req) {
        return delegate.registerAsset(req);
    }

    @GetMapping("/{apiCode}")
    public ApiAssetResp getAssetByCode(@PathVariable("apiCode") String apiCode) {
        return delegate.getAssetByCode(apiCode);
    }

    @PutMapping("/{apiCode}")
    public ApiAssetResp reviseAsset(
            @PathVariable("apiCode") String apiCode, @Valid @RequestBody ReviseApiAssetReq req) {
        return delegate.reviseAsset(apiCode, req);
    }

    @PatchMapping("/{apiCode}/enable")
    public ApiAssetResp enableAsset(@PathVariable("apiCode") String apiCode) {
        return delegate.enableAsset(apiCode);
    }

    @PatchMapping("/{apiCode}/disable")
    public ApiAssetResp disableAsset(@PathVariable("apiCode") String apiCode) {
        return delegate.disableAsset(apiCode);
    }

    @PutMapping("/{apiCode}/ai-profile")
    public ApiAssetResp attachAiCapabilityProfile(
            @PathVariable("apiCode") String apiCode, @Valid @RequestBody AttachAiCapabilityProfileReq req) {
        return delegate.attachAiCapabilityProfile(apiCode, req);
    }
}
