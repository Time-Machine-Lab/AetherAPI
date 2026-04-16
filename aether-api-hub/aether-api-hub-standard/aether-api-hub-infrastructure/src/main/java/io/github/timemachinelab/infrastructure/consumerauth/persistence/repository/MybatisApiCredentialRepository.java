package io.github.timemachinelab.infrastructure.consumerauth.persistence.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.github.timemachinelab.domain.consumerauth.model.ApiCredentialAggregate;
import io.github.timemachinelab.domain.consumerauth.model.ApiCredentialId;
import io.github.timemachinelab.domain.consumerauth.model.ApiCredentialStatus;
import io.github.timemachinelab.domain.consumerauth.model.ConsumerId;
import io.github.timemachinelab.domain.consumerauth.repository.ApiCredentialRepository;
import io.github.timemachinelab.infrastructure.consumerauth.persistence.converter.ApiCredentialConverter;
import io.github.timemachinelab.infrastructure.consumerauth.persistence.entity.ApiCredentialDo;
import io.github.timemachinelab.infrastructure.consumerauth.persistence.mapper.ApiCredentialMapper;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * API 凭证 MyBatis 仓储实现。
 */
@Repository
public class MybatisApiCredentialRepository implements ApiCredentialRepository {

    private final ApiCredentialMapper mapper;

    public MybatisApiCredentialRepository(ApiCredentialMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public Optional<ApiCredentialAggregate> findByFingerprintHash(String fingerprintHash) {
        return Optional.ofNullable(ApiCredentialConverter.toAggregate(mapper.selectByFingerprintHash(fingerprintHash)));
    }

    @Override
    public Optional<ApiCredentialAggregate> findByIdAndConsumerId(ApiCredentialId credentialId, ConsumerId consumerId) {
        return Optional.ofNullable(ApiCredentialConverter.toAggregate(
                mapper.selectByIdAndConsumerId(credentialId.getValue(), consumerId.getValue())));
    }

    @Override
    public List<ApiCredentialAggregate> findPageByConsumerId(
            ConsumerId consumerId, ApiCredentialStatus status, boolean expiredOnly, int page, int size, Instant now) {
        Page<ApiCredentialDo> pageResult = new Page<>(page, size);
        LambdaQueryWrapper<ApiCredentialDo> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ApiCredentialDo::getIsDeleted, false)
                .eq(ApiCredentialDo::getConsumerId, consumerId.getValue());
        applyStatusFilter(queryWrapper, status, expiredOnly, now);
        queryWrapper.orderByDesc(ApiCredentialDo::getCreatedAt);
        return mapper.selectPage(pageResult, queryWrapper)
                .getRecords()
                .stream()
                .map(ApiCredentialConverter::toAggregate)
                .collect(Collectors.toList());
    }

    @Override
    public long countByConsumerId(ConsumerId consumerId, ApiCredentialStatus status, boolean expiredOnly, Instant now) {
        QueryWrapper<ApiCredentialDo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("is_deleted", false)
                .eq("consumer_id", consumerId.getValue());
        if (status != null) {
            queryWrapper.eq("status", status.name());
        }
        if (expiredOnly) {
            queryWrapper.lt("expire_at", toLocalDateTime(now));
        }
        return mapper.selectCount(queryWrapper);
    }

    @Override
    public void save(ApiCredentialAggregate aggregate) {
        ApiCredentialDo existing = mapper.selectById(aggregate.getId().getValue());
        if (existing == null) {
            mapper.insert(ApiCredentialConverter.toDo(aggregate));
            return;
        }
        ApiCredentialConverter.updateDo(existing, aggregate);
        mapper.updateById(existing);
    }

    private void applyStatusFilter(
            LambdaQueryWrapper<ApiCredentialDo> queryWrapper, ApiCredentialStatus status, boolean expiredOnly, Instant now) {
        if (status != null) {
            queryWrapper.eq(ApiCredentialDo::getStatus, status.name());
        }
        if (expiredOnly) {
            queryWrapper.lt(ApiCredentialDo::getExpireAt, toLocalDateTime(now));
        }
    }

    private LocalDateTime toLocalDateTime(Instant instant) {
        return LocalDateTime.ofInstant(instant, ZoneOffset.UTC);
    }
}
