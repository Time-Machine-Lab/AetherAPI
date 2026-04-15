package io.github.aetherapihub.catalog.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.github.aetherapihub.catalog.infrastructure.persistence.entity.ApiCategoryDo;
import org.apache.ibatis.annotations.Mapper;

/**
 * 分类 MyBatis-Plus Mapper。
 */
@Mapper
public interface ApiCategoryMapper extends BaseMapper<ApiCategoryDo> {
}
