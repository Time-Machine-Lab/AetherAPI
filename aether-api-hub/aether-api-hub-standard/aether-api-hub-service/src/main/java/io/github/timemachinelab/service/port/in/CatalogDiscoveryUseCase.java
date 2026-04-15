package io.github.timemachinelab.service.port.in;

import io.github.timemachinelab.service.model.CatalogDiscoveryAssetDetailModel;
import io.github.timemachinelab.service.model.CatalogDiscoveryListModel;

/**
 * Catalog discovery use case.
 */
public interface CatalogDiscoveryUseCase {

    CatalogDiscoveryListModel listAssets();

    CatalogDiscoveryAssetDetailModel getAssetDetail(String apiCode);
}
