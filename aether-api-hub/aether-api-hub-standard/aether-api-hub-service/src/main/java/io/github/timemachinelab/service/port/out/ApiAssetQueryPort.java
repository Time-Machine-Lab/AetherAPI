package io.github.timemachinelab.service.port.out;

import io.github.timemachinelab.service.model.ApiAssetSummaryModel;

import java.util.List;

/**
 * Asset workspace query port.
 */
public interface ApiAssetQueryPort {

    List<ApiAssetSummaryModel> findPage(
            String ownerUserId,
            String status,
            String categoryCode,
            String keyword,
            int page,
            int size);

    long count(String ownerUserId, String status, String categoryCode, String keyword);
}
