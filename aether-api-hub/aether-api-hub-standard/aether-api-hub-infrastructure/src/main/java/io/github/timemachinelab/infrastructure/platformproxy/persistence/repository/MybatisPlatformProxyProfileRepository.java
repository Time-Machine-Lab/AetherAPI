package io.github.timemachinelab.infrastructure.platformproxy.persistence.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.github.timemachinelab.domain.platformproxy.model.PlatformProxyProfileAggregate;
import io.github.timemachinelab.domain.platformproxy.model.PlatformProxyProfileDomainException;
import io.github.timemachinelab.domain.platformproxy.model.PlatformProxyProfileId;
import io.github.timemachinelab.domain.platformproxy.repository.PlatformProxyProfileRepository;
import io.github.timemachinelab.infrastructure.platformproxy.persistence.converter.PlatformProxyProfileConverter;
import io.github.timemachinelab.infrastructure.platformproxy.persistence.entity.PlatformProxyProfileDo;
import io.github.timemachinelab.infrastructure.platformproxy.persistence.mapper.PlatformProxyProfileMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * MyBatis platform proxy profile repository implementation.
 */
@Repository
public class MybatisPlatformProxyProfileRepository implements PlatformProxyProfileRepository {

    private final PlatformProxyProfileMapper mapper;

    public MybatisPlatformProxyProfileRepository(PlatformProxyProfileMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public Optional<PlatformProxyProfileAggregate> findById(PlatformProxyProfileId id) {
        return Optional.ofNullable(PlatformProxyProfileConverter.toAggregate(mapper.selectById(id.getValue())));
    }

    @Override
    public Optional<PlatformProxyProfileAggregate> findByCode(String profileCode) {
        LambdaQueryWrapper<PlatformProxyProfileDo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PlatformProxyProfileDo::getIsDeleted, false)
                .eq(PlatformProxyProfileDo::getProfileCode, profileCode)
                .last("LIMIT 1");
        return Optional.ofNullable(PlatformProxyProfileConverter.toAggregate(mapper.selectOne(wrapper)));
    }

    @Override
    public List<PlatformProxyProfileAggregate> findPage(Boolean enabled, String keyword, int page, int size) {
        Page<PlatformProxyProfileDo> pageRequest = new Page<>(page, size);
        return mapper.selectPage(pageRequest, query(enabled, keyword))
                .getRecords()
                .stream()
                .map(PlatformProxyProfileConverter::toAggregate)
                .toList();
    }

    @Override
    public long count(Boolean enabled, String keyword) {
        return mapper.selectCount(query(enabled, keyword));
    }

    @Override
    public boolean existsByCode(String profileCode) {
        LambdaQueryWrapper<PlatformProxyProfileDo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PlatformProxyProfileDo::getIsDeleted, false)
                .eq(PlatformProxyProfileDo::getProfileCode, profileCode);
        return mapper.selectCount(wrapper) > 0;
    }

    @Override
    public void save(PlatformProxyProfileAggregate aggregate) {
        PlatformProxyProfileDo existing = mapper.selectById(aggregate.getId().getValue());
        if (existing == null) {
            mapper.insert(PlatformProxyProfileConverter.toDo(aggregate));
            return;
        }
        Long persistedVersion = existing.getVersion();
        PlatformProxyProfileConverter.updateDo(existing, aggregate);
        existing.setVersion(persistedVersion);
        int updatedRows = mapper.updateById(existing);
        if (updatedRows == 0) {
            throw new PlatformProxyProfileDomainException("Proxy profile update conflict: " + aggregate.getId().getValue());
        }
    }

    private LambdaQueryWrapper<PlatformProxyProfileDo> query(Boolean enabled, String keyword) {
        LambdaQueryWrapper<PlatformProxyProfileDo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PlatformProxyProfileDo::getIsDeleted, false);
        if (enabled != null) {
            wrapper.eq(PlatformProxyProfileDo::getEnabled, enabled);
        }
        if (keyword != null && !keyword.isBlank()) {
            wrapper.and(nested -> nested
                    .like(PlatformProxyProfileDo::getProfileCode, keyword)
                    .or()
                    .like(PlatformProxyProfileDo::getProfileName, keyword));
        }
        wrapper.orderByDesc(PlatformProxyProfileDo::getUpdatedAt);
        return wrapper;
    }
}
