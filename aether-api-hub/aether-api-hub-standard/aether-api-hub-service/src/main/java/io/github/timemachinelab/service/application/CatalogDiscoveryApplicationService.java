package io.github.timemachinelab.service.application;

import io.github.timemachinelab.domain.catalog.model.ApiCode;
import io.github.timemachinelab.domain.catalog.model.AssetDomainException;
import io.github.timemachinelab.service.model.CatalogDiscoveryAssetDetailModel;
import io.github.timemachinelab.service.model.CatalogDiscoveryListModel;
import io.github.timemachinelab.service.port.in.CatalogDiscoveryUseCase;
import io.github.timemachinelab.service.port.out.CatalogDiscoveryQueryPort;

/**
 * Catalog discovery application service.
 */
public class CatalogDiscoveryApplicationService implements CatalogDiscoveryUseCase {

    private final CatalogDiscoveryQueryPort catalogDiscoveryQueryPort;

    public CatalogDiscoveryApplicationService(CatalogDiscoveryQueryPort catalogDiscoveryQueryPort) {
        this.catalogDiscoveryQueryPort = catalogDiscoveryQueryPort;
    }

    @Override
    public CatalogDiscoveryListModel listAssets() {
        return new CatalogDiscoveryListModel(catalogDiscoveryQueryPort.listDiscoverableAssets());
    }

    @Override
    public CatalogDiscoveryAssetDetailModel getAssetDetail(String apiCode) {
        String normalizedApiCode = normalizeApiCode(apiCode);
        return catalogDiscoveryQueryPort.findDiscoverableAssetDetail(normalizedApiCode)
                .orElseThrow(() -> new AssetDomainException("Asset not found: " + normalizedApiCode));
    }

    private String normalizeApiCode(String apiCode) {
        try {
            return ApiCode.of(apiCode).getValue();
        } catch (IllegalArgumentException ex) {
            throw new AssetDomainException("Invalid API code: " + apiCode, ex);
        }
    }
}
