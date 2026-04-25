package io.github.timemachinelab.infrastructure.catalog.persistence.query;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * Asset workspace query mapper.
 */
@Mapper
public interface ApiAssetManagementQueryMapper {

    @Select({
            "<script>",
            "SELECT",
            "  a.api_code AS apiCode,",
            "  a.asset_name AS assetName,",
            "  a.asset_type AS assetType,",
            "  a.category_code AS categoryCode,",
            "  c.category_name AS categoryName,",
            "  a.status AS status,",
            "  a.publisher_display_name AS publisherDisplayName,",
            "  a.published_at AS publishedAt,",
            "  a.updated_at AS updatedAt",
            "FROM api_asset a",
            "LEFT JOIN api_category c",
            "  ON c.category_code = a.category_code",
            " AND c.is_deleted = FALSE",
            "WHERE a.is_deleted = FALSE",
            "  AND a.owner_user_id = #{ownerUserId}",
            "  <if test='status != null and status != \"\"'>",
            "    AND a.status = #{status}",
            "  </if>",
            "  <if test='categoryCode != null and categoryCode != \"\"'>",
            "    AND a.category_code = #{categoryCode}",
            "  </if>",
            "  <if test='keyword != null and keyword != \"\"'>",
            "    AND (",
            "      LOWER(a.api_code) LIKE CONCAT('%', CONCAT(LOWER(#{keyword}), '%'))",
            "      OR LOWER(COALESCE(a.asset_name, '')) LIKE CONCAT('%', CONCAT(LOWER(#{keyword}), '%'))",
            "    )",
            "  </if>",
            "ORDER BY a.updated_at DESC, a.created_at DESC",
            "LIMIT #{size} OFFSET #{offset}",
            "</script>"
    })
    List<ApiAssetManagementQueryRecord> selectPage(
            @Param("ownerUserId") String ownerUserId,
            @Param("status") String status,
            @Param("categoryCode") String categoryCode,
            @Param("keyword") String keyword,
            @Param("size") int size,
            @Param("offset") int offset);

    @Select({
            "<script>",
            "SELECT COUNT(1)",
            "FROM api_asset a",
            "WHERE a.is_deleted = FALSE",
            "  AND a.owner_user_id = #{ownerUserId}",
            "  <if test='status != null and status != \"\"'>",
            "    AND a.status = #{status}",
            "  </if>",
            "  <if test='categoryCode != null and categoryCode != \"\"'>",
            "    AND a.category_code = #{categoryCode}",
            "  </if>",
            "  <if test='keyword != null and keyword != \"\"'>",
            "    AND (",
            "      LOWER(a.api_code) LIKE CONCAT('%', CONCAT(LOWER(#{keyword}), '%'))",
            "      OR LOWER(COALESCE(a.asset_name, '')) LIKE CONCAT('%', CONCAT(LOWER(#{keyword}), '%'))",
            "    )",
            "  </if>",
            "</script>"
    })
    long count(
            @Param("ownerUserId") String ownerUserId,
            @Param("status") String status,
            @Param("categoryCode") String categoryCode,
            @Param("keyword") String keyword);
}
