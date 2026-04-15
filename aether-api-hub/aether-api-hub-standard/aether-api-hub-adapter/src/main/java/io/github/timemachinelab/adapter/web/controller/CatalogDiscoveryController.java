package io.github.timemachinelab.adapter.web.controller;

import io.github.timemachinelab.adapter.web.delegate.CatalogDiscoveryWebDelegate;
import io.github.timemachinelab.api.resp.CatalogDiscoveryAssetDetailResp;
import io.github.timemachinelab.api.resp.CatalogDiscoveryListResp;
import io.github.timemachinelab.common.annotation.AutoResp;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Catalog discovery HTTP entry point.
 */
@RestController
@RequestMapping("/api/v1/discovery/assets")
@AutoResp
public class CatalogDiscoveryController {

    private final CatalogDiscoveryWebDelegate delegate;

    public CatalogDiscoveryController(CatalogDiscoveryWebDelegate delegate) {
        this.delegate = delegate;
    }

    @GetMapping
    public CatalogDiscoveryListResp listAssets() {
        return delegate.listAssets();
    }

    @GetMapping("/{apiCode}")
    public CatalogDiscoveryAssetDetailResp getAssetDetail(@PathVariable String apiCode) {
        return delegate.getAssetDetail(apiCode);
    }
}
