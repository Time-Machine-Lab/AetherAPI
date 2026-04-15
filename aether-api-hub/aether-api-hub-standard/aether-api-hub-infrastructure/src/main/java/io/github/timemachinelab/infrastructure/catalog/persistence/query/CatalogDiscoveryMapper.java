package io.github.timemachinelab.infrastructure.catalog.persistence.query;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * Catalog discovery query mapper.
 */
@Mapper
public interface CatalogDiscoveryMapper {

    @Select({
            "SELECT",
            "  a.status AS status,",
            "  a.api_code AS apiCode,",
            "  a.asset_name AS assetName,",
            "  a.asset_type AS assetType,",
            "  a.category_code AS categoryCode,",
            "  c.category_name AS categoryName",
            "FROM api_asset a",
            "LEFT JOIN api_category c",
            "  ON c.category_code = a.category_code",
            " AND c.is_deleted = FALSE",
            "WHERE a.is_deleted = FALSE",
            "ORDER BY a.created_at DESC"
    })
    List<CatalogDiscoveryAssetRecord> selectAssetSummaries();

    @Select({
            "SELECT",
            "  a.status AS status,",
            "  a.api_code AS apiCode,",
            "  a.asset_name AS assetName,",
            "  a.asset_type AS assetType,",
            "  a.category_code AS categoryCode,",
            "  c.category_name AS categoryName,",
            "  a.request_method AS requestMethod,",
            "  a.auth_scheme AS authScheme,",
            "  a.request_template AS requestTemplate,",
            "  a.request_example AS requestExample,",
            "  a.response_example AS responseExample,",
            "  a.ai_provider AS aiProvider,",
            "  a.ai_model AS aiModel,",
            "  a.ai_streaming_supported AS aiStreamingSupported,",
            "  a.ai_capability_tags_json AS aiCapabilityTagsJson",
            "FROM api_asset a",
            "LEFT JOIN api_category c",
            "  ON c.category_code = a.category_code",
            " AND c.is_deleted = FALSE",
            "WHERE a.is_deleted = FALSE",
            "  AND a.api_code = #{apiCode}",
            "LIMIT 1"
    })
    CatalogDiscoveryAssetRecord selectAssetDetail(@Param("apiCode") String apiCode);
}
