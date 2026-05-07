package io.github.timemachinelab.service.adapter;

import io.github.timemachinelab.domain.platformproxy.model.PlatformProxyProfileAggregate;
import io.github.timemachinelab.domain.platformproxy.model.PlatformProxyProfileId;
import io.github.timemachinelab.domain.platformproxy.repository.PlatformProxyProfileRepository;
import io.github.timemachinelab.service.port.out.PlatformProxyProfileRepositoryPort;

import java.util.List;
import java.util.Optional;

/**
 * Platform proxy profile repository adapter.
 */
public class PlatformProxyProfileRepositoryAdapter implements PlatformProxyProfileRepositoryPort {

    private final PlatformProxyProfileRepository delegate;

    public PlatformProxyProfileRepositoryAdapter(PlatformProxyProfileRepository delegate) {
        this.delegate = delegate;
    }

    @Override
    public Optional<PlatformProxyProfileAggregate> findById(PlatformProxyProfileId id) {
        return delegate.findById(id);
    }

    @Override
    public Optional<PlatformProxyProfileAggregate> findByCode(String profileCode) {
        return delegate.findByCode(profileCode);
    }

    @Override
    public List<PlatformProxyProfileAggregate> findPage(Boolean enabled, String keyword, int page, int size) {
        return delegate.findPage(enabled, keyword, page, size);
    }

    @Override
    public long count(Boolean enabled, String keyword) {
        return delegate.count(enabled, keyword);
    }

    @Override
    public boolean existsByCode(String profileCode) {
        return delegate.existsByCode(profileCode);
    }

    @Override
    public void save(PlatformProxyProfileAggregate aggregate) {
        delegate.save(aggregate);
    }
}
