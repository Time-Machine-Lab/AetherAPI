package io.github.timemachinelab.domain.catalog.repository;

import io.github.timemachinelab.domain.catalog.model.ApiAssetAggregate;
import io.github.timemachinelab.domain.catalog.model.ApiCode;

import java.util.Optional;

/**
 * API 资产仓储接口。
 */
public interface ApiAssetRepository {

    Optional<ApiAssetAggregate> findByCode(ApiCode code);

    Optional<ApiAssetAggregate> findByCodeIncludingDeleted(ApiCode code);

    boolean existsByCode(ApiCode code);

    void save(ApiAssetAggregate aggregate);
}
