package io.github.timemachinelab.infrastructure.importagent.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.github.timemachinelab.infrastructure.importagent.persistence.entity.ApiImportAgentRunDo;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * Import agent run mapper.
 */
public interface ApiImportAgentRunMapper extends BaseMapper<ApiImportAgentRunDo> {

    @Select("SELECT * FROM api_import_agent_run WHERE id = #{runId} AND owner_user_id = #{ownerUserId} LIMIT 1")
    ApiImportAgentRunDo selectOwnedById(@Param("ownerUserId") String ownerUserId, @Param("runId") String runId);
}