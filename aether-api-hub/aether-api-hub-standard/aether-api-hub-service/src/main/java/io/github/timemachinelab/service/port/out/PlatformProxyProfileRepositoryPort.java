package io.github.timemachinelab.service.port.out;

import io.github.timemachinelab.domain.platformproxy.model.PlatformProxyProfileAggregate;
import io.github.timemachinelab.domain.platformproxy.model.PlatformProxyProfileId;

import java.util.List;
import java.util.Optional;

/**
 * Platform proxy profile repository outbound port.
 */
public interface PlatformProxyProfileRepositoryPort {

    Optional<PlatformProxyProfileAggregate> findById(PlatformProxyProfileId id);

    Optional<PlatformProxyProfileAggregate> findByCode(String profileCode);

    List<PlatformProxyProfileAggregate> findPage(Boolean enabled, String keyword, int page, int size);

    long count(Boolean enabled, String keyword);

    boolean existsByCode(String profileCode);

    void save(PlatformProxyProfileAggregate aggregate);
}
