package io.github.timemachinelab.infrastructure.consumerauth.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.github.timemachinelab.infrastructure.consumerauth.persistence.entity.ConsumerIdentityDo;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * Consumer Mapper。
 */
public interface ConsumerIdentityMapper extends BaseMapper<ConsumerIdentityDo> {

    @Select("SELECT * FROM consumer_identity WHERE consumer_code = #{consumerCode} AND is_deleted = FALSE LIMIT 1")
    ConsumerIdentityDo selectByCode(@Param("consumerCode") String consumerCode);
}
