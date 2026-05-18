package io.github.timemachinelab.infrastructure.importagent.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.github.timemachinelab.infrastructure.importagent.persistence.entity.ApiImportAgentSessionDo;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * Import agent session mapper.
 */
public interface ApiImportAgentSessionMapper extends BaseMapper<ApiImportAgentSessionDo> {

    @Select("SELECT * FROM api_import_agent_session WHERE id = #{sessionId} AND owner_user_id = #{ownerUserId} LIMIT 1")
    ApiImportAgentSessionDo selectOwnedById(@Param("ownerUserId") String ownerUserId, @Param("sessionId") String sessionId);
}