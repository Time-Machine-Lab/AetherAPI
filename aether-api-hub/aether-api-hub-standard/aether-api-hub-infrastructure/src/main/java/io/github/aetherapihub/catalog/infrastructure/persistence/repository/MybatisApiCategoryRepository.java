package io.github.aetherapihub.catalog.infrastructure.persistence.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.github.aetherapihub.catalog.domain.model.ApiCategoryAggregate;
import io.github.aetherapihub.catalog.domain.model.ApiCategoryRepository;
import io.github.aetherapihub.catalog.domain.model.CategoryCode;
import io.github.aetherapihub.catalog.domain.model.CategoryId;
import io.github.aetherapihub.catalog.domain.model.CategoryStatus;
import io.github.aetherapihub.catalog.infrastructure.persistence.converter.ApiCategoryConverter;
import io.github.aetherapihub.catalog.infrastructure.persistence.entity.ApiCategoryDo;
import io.github.aetherapihub.catalog.infrastructure.persistence.mapper.ApiCategoryMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 分类仓储适配器（MyBatis-Plus 实现）。
 */
@Repository
@RequiredArgsConstructor
public class MybatisApiCategoryRepository implements ApiCategoryRepository {

    private final ApiCategoryMapper mapper;

    @Override
    public Optional<ApiCategoryAggregate> findByCode(CategoryCode code) {
        LambdaQueryWrapper<ApiCategoryDo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ApiCategoryDo::getCategoryCode, code.getValue());
        return Optional.ofNullable(mapper.selectOne(wrapper))
                .map(ApiCategoryConverter::toAggregate);
    }

    @Override
    public Optional<ApiCategoryAggregate> findById(CategoryId id) {
        return Optional.ofNullable(mapper.selectById(id.getValue()))
                .map(ApiCategoryConverter::toAggregate);
    }

    @Override
    public ApiCategoryAggregate save(ApiCategoryAggregate category) {
        ApiCategoryDo record = ApiCategoryConverter.toDo(category);
        if (mapper.selectById(record.getId()) == null) {
            mapper.insert(record);
        } else {
            mapper.updateById(record);
        }
        return ApiCategoryConverter.toAggregate(mapper.selectById(record.getId()));
    }

    @Override
    public boolean existsByCode(CategoryCode code) {
        LambdaQueryWrapper<ApiCategoryDo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ApiCategoryDo::getCategoryCode, code.getValue());
        return mapper.selectCount(wrapper) > 0;
    }

    @Override
    public List<ApiCategoryAggregate> findPage(CategoryStatus status, int page, int size) {
        LambdaQueryWrapper<ApiCategoryDo> wrapper = new LambdaQueryWrapper<>();
        if (status != null) {
            wrapper.eq(ApiCategoryDo::getStatus, status.name());
        }
        wrapper.orderByDesc(ApiCategoryDo::getCreatedAt);
        IPage<ApiCategoryDo> result = mapper.selectPage(new Page<>(page, size), wrapper);
        return result.getRecords().stream()
                .map(ApiCategoryConverter::toAggregate)
                .toList();
    }

    @Override
    public long count(CategoryStatus status) {
        LambdaQueryWrapper<ApiCategoryDo> wrapper = new LambdaQueryWrapper<>();
        if (status != null) {
            wrapper.eq(ApiCategoryDo::getStatus, status.name());
        }
        return mapper.selectCount(wrapper);
    }
}
