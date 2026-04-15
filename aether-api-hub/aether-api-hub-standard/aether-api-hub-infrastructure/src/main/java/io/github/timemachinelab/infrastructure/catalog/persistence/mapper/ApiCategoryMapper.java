package io.github.timemachinelab.infrastructure.catalog.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.github.timemachinelab.infrastructure.catalog.persistence.entity.ApiCategoryDo;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * API 分类 MyBatis-Plus Mapper 接口。
 */
public interface ApiCategoryMapper extends BaseMapper<ApiCategoryDo> {

    /**
     * 根据分类编码查询分类（排除已删除）。
     *
     * @param categoryCode 分类编码
     * @return 分类 DO
     */
    @Select("SELECT * FROM api_category WHERE category_code = #{categoryCode} AND is_deleted = FALSE LIMIT 1")
    ApiCategoryDo selectByCode(@Param("categoryCode") String categoryCode);

    /**
     * 根据分类编码查询分类（包含已删除）。
     *
     * @param categoryCode 分类编码
     * @return 分类 DO
     */
    @Select("SELECT * FROM api_category WHERE category_code = #{categoryCode} LIMIT 1")
    ApiCategoryDo selectByCodeIncludingDeleted(@Param("categoryCode") String categoryCode);

    /**
     * 判断分类编码是否存在（排除已删除）。
     *
     * @param categoryCode 分类编码
     * @return 存在返回 1，否则返回 0
     */
    @Select("SELECT COUNT(*) FROM api_category WHERE category_code = #{categoryCode} AND is_deleted = FALSE")
    int existsByCode(@Param("categoryCode") String categoryCode);
}
