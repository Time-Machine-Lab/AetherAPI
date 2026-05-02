package io.github.timemachinelab.infrastructure.subscription.persistence.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.github.timemachinelab.domain.catalog.model.ApiCode;
import io.github.timemachinelab.domain.consumerauth.model.ConsumerId;
import io.github.timemachinelab.domain.subscription.model.ApiSubscriptionAggregate;
import io.github.timemachinelab.domain.subscription.model.ApiSubscriptionDomainException;
import io.github.timemachinelab.domain.subscription.model.ApiSubscriptionId;
import io.github.timemachinelab.domain.subscription.model.ApiSubscriptionStatus;
import io.github.timemachinelab.domain.subscription.repository.ApiSubscriptionRepository;
import io.github.timemachinelab.infrastructure.subscription.persistence.converter.ApiSubscriptionConverter;
import io.github.timemachinelab.infrastructure.subscription.persistence.entity.ApiSubscriptionDo;
import io.github.timemachinelab.infrastructure.subscription.persistence.mapper.ApiSubscriptionMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * MyBatis API subscription repository implementation.
 */
@Repository
public class MybatisApiSubscriptionRepository implements ApiSubscriptionRepository {

    private final ApiSubscriptionMapper mapper;

    public MybatisApiSubscriptionRepository(ApiSubscriptionMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public Optional<ApiSubscriptionAggregate> findById(ApiSubscriptionId id) {
        return Optional.ofNullable(ApiSubscriptionConverter.toAggregate(mapper.selectById(id.getValue())));
    }

    @Override
    public Optional<ApiSubscriptionAggregate> findActiveByConsumerIdAndApiCode(ConsumerId consumerId, ApiCode apiCode) {
        LambdaQueryWrapper<ApiSubscriptionDo> queryWrapper = activeQuery(consumerId, apiCode);
        queryWrapper.last("LIMIT 1");
        return Optional.ofNullable(ApiSubscriptionConverter.toAggregate(mapper.selectOne(queryWrapper)));
    }

    @Override
    public List<ApiSubscriptionAggregate> findPageByConsumerId(ConsumerId consumerId, int page, int size) {
        Page<ApiSubscriptionDo> pageResult = new Page<>(page, size);
        LambdaQueryWrapper<ApiSubscriptionDo> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ApiSubscriptionDo::getIsDeleted, false)
                .eq(ApiSubscriptionDo::getSubscriberConsumerId, consumerId.getValue())
                .orderByDesc(ApiSubscriptionDo::getCreatedAt);
        return mapper.selectPage(pageResult, queryWrapper)
                .getRecords()
                .stream()
                .map(ApiSubscriptionConverter::toAggregate)
                .collect(Collectors.toList());
    }

    @Override
    public long countByConsumerId(ConsumerId consumerId) {
        QueryWrapper<ApiSubscriptionDo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("is_deleted", false)
                .eq("subscriber_consumer_id", consumerId.getValue());
        return mapper.selectCount(queryWrapper);
    }

    @Override
    public boolean hasActiveSubscription(ConsumerId consumerId, ApiCode apiCode) {
        return mapper.selectCount(activeQuery(consumerId, apiCode)) > 0;
    }

    @Override
    public void save(ApiSubscriptionAggregate aggregate) {
        ApiSubscriptionDo existing = mapper.selectById(aggregate.getId().getValue());
        if (existing == null) {
            mapper.insert(ApiSubscriptionConverter.toDo(aggregate));
            return;
        }
        Long persistedVersion = existing.getVersion();
        ApiSubscriptionConverter.updateDo(existing, aggregate);
        existing.setVersion(persistedVersion);
        int updatedRows = mapper.updateById(existing);
        if (updatedRows == 0) {
            throw new ApiSubscriptionDomainException("API subscription update conflict: " + aggregate.getId().getValue());
        }
    }

    private LambdaQueryWrapper<ApiSubscriptionDo> activeQuery(ConsumerId consumerId, ApiCode apiCode) {
        LambdaQueryWrapper<ApiSubscriptionDo> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ApiSubscriptionDo::getIsDeleted, false)
                .eq(ApiSubscriptionDo::getSubscriberConsumerId, consumerId.getValue())
                .eq(ApiSubscriptionDo::getApiCode, apiCode.getValue())
                .eq(ApiSubscriptionDo::getStatus, ApiSubscriptionStatus.ACTIVE.name());
        return queryWrapper;
    }
}
