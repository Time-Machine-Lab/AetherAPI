package io.github.timemachinelab.infrastructure.catalog.persistence.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.github.timemachinelab.domain.catalog.model.ApiCategoryAggregate;
import io.github.timemachinelab.domain.catalog.model.CategoryCode;
import io.github.timemachinelab.domain.catalog.model.CategoryStatus;
import io.github.timemachinelab.domain.catalog.repository.ApiCategoryRepository;
import io.github.timemachinelab.infrastructure.catalog.persistence.converter.ApiCategoryConverter;
import io.github.timemachinelab.infrastructure.catalog.persistence.entity.ApiCategoryDo;
import io.github.timemachinelab.infrastructure.catalog.persistence.mapper.ApiCategoryMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * MyBatis-Plus 实现的分类仓储。
 */
@Repository
public class MybatisApiCategoryRepository implements ApiCategoryRepository {

    private final ApiCategoryMapper mapper;

    public MybatisApiCategoryRepository(ApiCategoryMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public Optional<ApiCategoryAggregate> findByCode(CategoryCode code) {
        ApiCategoryDo entity = mapper.selectByCode(code.getValue());
        return Optional.ofNullable(ApiCategoryConverter.toAggregate(entity));
    }

    @Override
    public Optional<ApiCategoryAggregate> findByCodeIncludingDeleted(CategoryCode code) {
        ApiCategoryDo entity = mapper.selectByCodeIncludingDeleted(code.getValue());
        return Optional.ofNullable(ApiCategoryConverter.toAggregate(entity));
    }

    @Override
    public List<ApiCategoryAggregate> findAll(CategoryStatus status, int page, int size) {
        Page<ApiCategoryDo> pageResult = new Page<>(page, size);
        LambdaQueryWrapper<ApiCategoryDo> queryWrapper = new LambdaQueryWrapper<>();

        queryWrapper.eq(ApiCategoryDo::getIsDeleted, false);
        if (status != null) {
            queryWrapper.eq(ApiCategoryDo::getStatus, status.name());
        }
        queryWrapper.orderByDesc(ApiCategoryDo::getCreatedAt);

        Page<ApiCategoryDo> result = mapper.selectPage(pageResult, queryWrapper);

        return result.getRecords().stream()
                .map(ApiCategoryConverter::toAggregate)
                .collect(Collectors.toList());
    }

    @Override
    public long count(CategoryStatus status) {
        QueryWrapper<ApiCategoryDo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("is_deleted", false);
        if (status != null) {
            queryWrapper.eq("status", status.name());
        }
        return mapper.selectCount(queryWrapper);
    }

    @Override
    public boolean existsByCode(CategoryCode code) {
        return mapper.existsByCode(code.getValue()) > 0;
    }

    @Override
    public void save(ApiCategoryAggregate aggregate) {
        ApiCategoryDo existing = mapper.selectByCodeIncludingDeleted(aggregate.getCode().getValue());

        if (existing == null) {
            ApiCategoryDo entity = ApiCategoryConverter.toDo(aggregate);
            mapper.insert(entity);
        } else {
            ApiCategoryConverter.updateDo(existing, aggregate);
            mapper.updateById(existing);
        }
    }
}
