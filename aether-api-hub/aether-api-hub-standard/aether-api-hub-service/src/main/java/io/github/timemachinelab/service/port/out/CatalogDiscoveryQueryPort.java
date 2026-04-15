package io.github.timemachinelab.service.port.out;

import io.github.timemachinelab.service.model.CatalogDiscoveryAssetDetailModel;
import io.github.timemachinelab.service.model.CatalogDiscoveryAssetSummaryModel;

import java.util.List;
import java.util.Optional;

/**
 * Catalog discovery query port.
 */
public interface CatalogDiscoveryQueryPort {

    List<CatalogDiscoveryAssetSummaryModel> listDiscoverableAssets();

    Optional<CatalogDiscoveryAssetDetailModel> findDiscoverableAssetDetail(String apiCode);
}
