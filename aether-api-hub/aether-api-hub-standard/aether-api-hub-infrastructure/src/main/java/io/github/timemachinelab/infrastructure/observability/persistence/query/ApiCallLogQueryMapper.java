package io.github.timemachinelab.infrastructure.observability.persistence.query;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;

/**
 * API call log query mapper.
 */
@Mapper
public interface ApiCallLogQueryMapper {

    @Select({
            "<script>",
            "SELECT",
            "  l.id AS logId,",
            "  l.target_api_code AS targetApiCode,",
            "  l.target_api_name AS targetApiName,",
            "  l.request_method AS requestMethod,",
            "  l.access_channel AS accessChannel,",
            "  l.invocation_time AS invocationTime,",
            "  l.duration_ms AS durationMs,",
            "  l.result_type AS resultType,",
            "  l.success AS success,",
            "  l.http_status_code AS httpStatusCode",
            "FROM api_call_log l",
            "WHERE l.is_deleted = FALSE",
            "  AND l.consumer_id = #{consumerId}",
            "  <if test='targetApiCode != null and targetApiCode != \"\"'>",
            "    AND l.target_api_code = #{targetApiCode}",
            "  </if>",
            "  <if test='invocationStartAt != null'>",
            "    AND l.invocation_time <![CDATA[>=]]> #{invocationStartAt}",
            "  </if>",
            "  <if test='invocationEndAt != null'>",
            "    AND l.invocation_time <![CDATA[<=]]> #{invocationEndAt}",
            "  </if>",
            "ORDER BY l.invocation_time DESC, l.created_at DESC",
            "LIMIT #{size} OFFSET #{offset}",
            "</script>"
    })
    List<ApiCallLogQueryRecord> selectPageByConsumerId(
            @Param("consumerId") String consumerId,
            @Param("targetApiCode") String targetApiCode,
            @Param("invocationStartAt") LocalDateTime invocationStartAt,
            @Param("invocationEndAt") LocalDateTime invocationEndAt,
            @Param("size") int size,
            @Param("offset") int offset);

    @Select({
            "<script>",
            "SELECT COUNT(1)",
            "FROM api_call_log l",
            "WHERE l.is_deleted = FALSE",
            "  AND l.consumer_id = #{consumerId}",
            "  <if test='targetApiCode != null and targetApiCode != \"\"'>",
            "    AND l.target_api_code = #{targetApiCode}",
            "  </if>",
            "  <if test='invocationStartAt != null'>",
            "    AND l.invocation_time <![CDATA[>=]]> #{invocationStartAt}",
            "  </if>",
            "  <if test='invocationEndAt != null'>",
            "    AND l.invocation_time <![CDATA[<=]]> #{invocationEndAt}",
            "  </if>",
            "</script>"
    })
    long countByConsumerId(
            @Param("consumerId") String consumerId,
            @Param("targetApiCode") String targetApiCode,
            @Param("invocationStartAt") LocalDateTime invocationStartAt,
            @Param("invocationEndAt") LocalDateTime invocationEndAt);

    @Select({
            "SELECT",
            "  l.id AS logId,",
            "  l.target_api_code AS targetApiCode,",
            "  l.target_api_name AS targetApiName,",
            "  l.request_method AS requestMethod,",
            "  l.access_channel AS accessChannel,",
            "  l.invocation_time AS invocationTime,",
            "  l.duration_ms AS durationMs,",
            "  l.result_type AS resultType,",
            "  l.success AS success,",
            "  l.http_status_code AS httpStatusCode,",
            "  l.credential_code AS credentialCode,",
            "  l.credential_status AS credentialStatus,",
            "  l.error_code AS errorCode,",
            "  l.error_type AS errorType,",
            "  l.error_summary AS errorSummary,",
            "  l.ai_provider AS aiProvider,",
            "  l.ai_model AS aiModel,",
            "  l.ai_streaming AS aiStreaming,",
            "  l.ai_usage_snapshot AS aiUsageSnapshot,",
            "  l.created_at AS createdAt,",
            "  l.updated_at AS updatedAt",
            "FROM api_call_log l",
            "WHERE l.is_deleted = FALSE",
            "  AND l.id = #{logId}",
            "  AND l.consumer_id = #{consumerId}",
            "LIMIT 1"
    })
    ApiCallLogQueryRecord selectDetailByIdAndConsumerId(
            @Param("logId") String logId,
            @Param("consumerId") String consumerId);
}
