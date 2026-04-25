package io.github.timemachinelab.adapter.web.delegate;

import io.github.timemachinelab.api.resp.CatalogDiscoveryAiCapabilityProfileResp;
import io.github.timemachinelab.api.resp.CatalogDiscoveryAssetDetailResp;
import io.github.timemachinelab.api.resp.CatalogDiscoveryAssetSummaryResp;
import io.github.timemachinelab.api.resp.CatalogDiscoveryCategoryResp;
import io.github.timemachinelab.api.resp.CatalogDiscoveryExampleSnapshotResp;
import io.github.timemachinelab.api.resp.CatalogDiscoveryListResp;
import io.github.timemachinelab.api.resp.CatalogDiscoveryPublisherResp;
import io.github.timemachinelab.domain.catalog.model.AssetType;
import io.github.timemachinelab.domain.catalog.model.AuthScheme;
import io.github.timemachinelab.domain.catalog.model.RequestMethod;
import io.github.timemachinelab.service.model.CatalogDiscoveryAiCapabilityProfileModel;
import io.github.timemachinelab.service.model.CatalogDiscoveryAssetDetailModel;
import io.github.timemachinelab.service.model.CatalogDiscoveryAssetSummaryModel;
import io.github.timemachinelab.service.model.CatalogDiscoveryCategoryModel;
import io.github.timemachinelab.service.model.CatalogDiscoveryExampleSnapshotModel;
import io.github.timemachinelab.service.model.CatalogDiscoveryListModel;
import io.github.timemachinelab.service.model.CatalogDiscoveryPublisherModel;
import io.github.timemachinelab.service.port.in.CatalogDiscoveryUseCase;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Catalog discovery web delegate.
 */
@Component
public class CatalogDiscoveryWebDelegate {

    private final CatalogDiscoveryUseCase catalogDiscoveryUseCase;

    public CatalogDiscoveryWebDelegate(CatalogDiscoveryUseCase catalogDiscoveryUseCase) {
        this.catalogDiscoveryUseCase = catalogDiscoveryUseCase;
    }

    public CatalogDiscoveryListResp listAssets() {
        CatalogDiscoveryListModel model = catalogDiscoveryUseCase.listAssets();
        List<CatalogDiscoveryAssetSummaryResp> items = model.getItems().stream()
                .map(this::toSummaryResp)
                .toList();
        return new CatalogDiscoveryListResp(items);
    }

    public CatalogDiscoveryAssetDetailResp getAssetDetail(String apiCode) {
        return toDetailResp(catalogDiscoveryUseCase.getAssetDetail(apiCode));
    }

    private CatalogDiscoveryAssetSummaryResp toSummaryResp(CatalogDiscoveryAssetSummaryModel model) {
        return new CatalogDiscoveryAssetSummaryResp(
                model.getApiCode(),
                model.getAssetName(),
                AssetType.valueOf(model.getAssetType()),
                toCategoryResp(model.getCategory()),
                toPublisherResp(model.getPublisher()),
                model.getPublishedAt()
        );
    }

    private CatalogDiscoveryAssetDetailResp toDetailResp(CatalogDiscoveryAssetDetailModel model) {
        return new CatalogDiscoveryAssetDetailResp(
                model.getApiCode(),
                model.getAssetName(),
                AssetType.valueOf(model.getAssetType()),
                toCategoryResp(model.getCategory()),
                toPublisherResp(model.getPublisher()),
                model.getPublishedAt(),
                model.getRequestMethod() == null ? null : RequestMethod.valueOf(model.getRequestMethod()),
                model.getAuthScheme() == null ? null : AuthScheme.valueOf(model.getAuthScheme()),
                model.getRequestTemplate(),
                toExampleSnapshotResp(model.getExampleSnapshot()),
                toAiProfileResp(model.getAiCapabilityProfile())
        );
    }

    private CatalogDiscoveryCategoryResp toCategoryResp(CatalogDiscoveryCategoryModel model) {
        if (model == null) {
            return null;
        }
        return new CatalogDiscoveryCategoryResp(model.getCategoryCode(), model.getCategoryName());
    }

    private CatalogDiscoveryPublisherResp toPublisherResp(CatalogDiscoveryPublisherModel model) {
        if (model == null) {
            return null;
        }
        return new CatalogDiscoveryPublisherResp(model.getDisplayName());
    }

    private CatalogDiscoveryExampleSnapshotResp toExampleSnapshotResp(CatalogDiscoveryExampleSnapshotModel model) {
        if (model == null) {
            return null;
        }
        return new CatalogDiscoveryExampleSnapshotResp(model.getRequestExample(), model.getResponseExample());
    }

    private CatalogDiscoveryAiCapabilityProfileResp toAiProfileResp(CatalogDiscoveryAiCapabilityProfileModel model) {
        if (model == null) {
            return null;
        }
        return new CatalogDiscoveryAiCapabilityProfileResp(
                model.getProvider(),
                model.getModel(),
                model.getStreamingSupported(),
                model.getCapabilityTags()
        );
    }
}
