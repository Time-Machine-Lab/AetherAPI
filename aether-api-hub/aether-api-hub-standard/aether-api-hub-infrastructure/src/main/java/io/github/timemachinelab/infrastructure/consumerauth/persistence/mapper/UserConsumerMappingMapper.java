package io.github.timemachinelab.infrastructure.consumerauth.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.github.timemachinelab.infrastructure.consumerauth.persistence.entity.UserConsumerMappingDo;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 用户-Consumer 映射 Mapper。
 */
public interface UserConsumerMappingMapper extends BaseMapper<UserConsumerMappingDo> {

    @Select("""
            SELECT * FROM user_consumer_mapping
            WHERE user_id = #{userId}
              AND mapping_status = 'ACTIVE'
              AND is_deleted = FALSE
            LIMIT 1
            """)
    UserConsumerMappingDo selectActiveByUserId(@Param("userId") String userId);
}
