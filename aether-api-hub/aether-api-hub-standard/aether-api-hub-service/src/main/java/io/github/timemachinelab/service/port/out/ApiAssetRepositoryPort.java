package io.github.timemachinelab.service.port.out;

import io.github.timemachinelab.domain.catalog.model.ApiAssetAggregate;
import io.github.timemachinelab.domain.catalog.model.ApiCode;

import java.util.Optional;

/**
 * API 资产仓储出口端口。
 */
public interface ApiAssetRepositoryPort {

    Optional<ApiAssetAggregate> findByCode(ApiCode code);

    Optional<ApiAssetAggregate> findByCodeIncludingDeleted(ApiCode code);

    boolean existsByCode(ApiCode code);

    void save(ApiAssetAggregate aggregate);
}

