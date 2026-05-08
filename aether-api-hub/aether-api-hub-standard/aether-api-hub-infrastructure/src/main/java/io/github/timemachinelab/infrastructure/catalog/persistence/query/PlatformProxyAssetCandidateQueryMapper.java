package io.github.timemachinelab.infrastructure.catalog.persistence.query;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * Platform proxy asset binding candidate query mapper.
 */
@Mapper
public interface PlatformProxyAssetCandidateQueryMapper {

    @Select({
            "<script>",
            "SELECT",
            "  a.api_code AS apiCode,",
            "  a.asset_name AS assetName,",
            "  a.asset_type AS assetType,",
            "  a.status AS status,",
            "  a.publisher_display_name AS publisherDisplayName,",
            "  a.proxy_profile_id AS proxyProfileId,",
            "  p.profile_code AS proxyProfileCode,",
            "  p.profile_name AS proxyProfileName,",
            "  a.created_at AS createdAt,",
            "  a.updated_at AS updatedAt",
            "FROM api_asset a",
            "LEFT JOIN platform_proxy_profile p",
            "  ON p.id COLLATE utf8mb4_unicode_ci = a.proxy_profile_id COLLATE utf8mb4_unicode_ci",
            " AND p.is_deleted = FALSE",
            "WHERE a.is_deleted = FALSE",
            "  <if test='keyword != null and keyword != \"\"'>",
            "    AND (",
            "      LOWER(a.api_code) LIKE CONCAT('%', CONCAT(LOWER(#{keyword}), '%'))",
            "      OR LOWER(COALESCE(a.asset_name, '')) LIKE CONCAT('%', CONCAT(LOWER(#{keyword}), '%'))",
            "      OR LOWER(COALESCE(a.publisher_display_name, '')) LIKE CONCAT('%', CONCAT(LOWER(#{keyword}), '%'))",
            "    )",
            "  </if>",
            "  <if test='status != null and status != \"\"'>",
            "    AND a.status = #{status}",
            "  </if>",
            "  <if test='boundProfileId != null and boundProfileId != \"\"'>",
            "    AND a.proxy_profile_id COLLATE utf8mb4_unicode_ci = #{boundProfileId} COLLATE utf8mb4_unicode_ci",
            "  </if>",
            "ORDER BY a.updated_at DESC, a.created_at DESC",
            "LIMIT #{size} OFFSET #{offset}",
            "</script>"
    })
    List<PlatformProxyAssetCandidateQueryRecord> selectPage(
            @Param("keyword") String keyword,
            @Param("status") String status,
            @Param("boundProfileId") String boundProfileId,
            @Param("size") int size,
            @Param("offset") int offset);

    @Select({
            "<script>",
            "SELECT COUNT(1)",
            "FROM api_asset a",
            "WHERE a.is_deleted = FALSE",
            "  <if test='keyword != null and keyword != \"\"'>",
            "    AND (",
            "      LOWER(a.api_code) LIKE CONCAT('%', CONCAT(LOWER(#{keyword}), '%'))",
            "      OR LOWER(COALESCE(a.asset_name, '')) LIKE CONCAT('%', CONCAT(LOWER(#{keyword}), '%'))",
            "      OR LOWER(COALESCE(a.publisher_display_name, '')) LIKE CONCAT('%', CONCAT(LOWER(#{keyword}), '%'))",
            "    )",
            "  </if>",
            "  <if test='status != null and status != \"\"'>",
            "    AND a.status = #{status}",
            "  </if>",
            "  <if test='boundProfileId != null and boundProfileId != \"\"'>",
            "    AND a.proxy_profile_id COLLATE utf8mb4_unicode_ci = #{boundProfileId} COLLATE utf8mb4_unicode_ci",
            "  </if>",
            "</script>"
    })
    long count(
            @Param("keyword") String keyword,
            @Param("status") String status,
            @Param("boundProfileId") String boundProfileId);
}
