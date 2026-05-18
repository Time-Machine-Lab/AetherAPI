package io.github.timemachinelab.infrastructure.importagent.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.github.timemachinelab.infrastructure.importagent.persistence.entity.ApiImportAgentTurnDo;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * Import agent turn mapper.
 */
public interface ApiImportAgentTurnMapper extends BaseMapper<ApiImportAgentTurnDo> {

    @Select("SELECT * FROM api_import_agent_turn WHERE session_id = #{sessionId} ORDER BY turn_index ASC")
    List<ApiImportAgentTurnDo> selectBySessionId(@Param("sessionId") String sessionId);

    @Select("SELECT COUNT(*) FROM api_import_agent_turn WHERE session_id = #{sessionId}")
    int countBySessionId(@Param("sessionId") String sessionId);
}