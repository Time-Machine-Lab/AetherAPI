package io.github.timemachinelab.infrastructure.catalog.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.github.timemachinelab.infrastructure.catalog.persistence.entity.ApiAssetDo;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * API asset mapper.
 */
public interface ApiAssetMapper extends BaseMapper<ApiAssetDo> {

    @Select("SELECT * FROM api_asset WHERE api_code = #{apiCode} AND is_deleted = FALSE LIMIT 1")
    ApiAssetDo selectByCode(@Param("apiCode") String apiCode);

    @Select("SELECT * FROM api_asset WHERE api_code = #{apiCode} LIMIT 1")
    ApiAssetDo selectByCodeIncludingDeleted(@Param("apiCode") String apiCode);

    @Select("SELECT COUNT(*) FROM api_asset WHERE api_code = #{apiCode} AND is_deleted = FALSE")
    int existsByCode(@Param("apiCode") String apiCode);
}
