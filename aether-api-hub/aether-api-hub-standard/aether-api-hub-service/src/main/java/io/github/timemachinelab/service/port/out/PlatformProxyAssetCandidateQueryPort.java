package io.github.timemachinelab.service.port.out;

import io.github.timemachinelab.service.model.PlatformProxyAssetCandidateModel;

import java.util.List;

/**
 * Platform proxy asset binding candidate query port.
 */
public interface PlatformProxyAssetCandidateQueryPort {

    List<PlatformProxyAssetCandidateModel> findPage(
            String keyword,
            String status,
            String boundProfileId,
            int page,
            int size);

    long count(String keyword, String status, String boundProfileId);
}
