package io.github.timemachinelab.service.port.out;

import io.github.timemachinelab.service.model.ApiAssetSummaryModel;

import java.util.List;

/**
 * Asset management query port.
 */
public interface ApiAssetQueryPort {

    List<ApiAssetSummaryModel> findPage(
            String status,
            String categoryCode,
            String keyword,
            int page,
            int size);

    long count(String status, String categoryCode, String keyword);
}
