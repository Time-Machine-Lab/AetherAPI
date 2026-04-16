package io.github.timemachinelab.infrastructure.consumerauth.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.github.timemachinelab.infrastructure.consumerauth.persistence.entity.ApiCredentialDo;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * API 凭证 Mapper。
 */
public interface ApiCredentialMapper extends BaseMapper<ApiCredentialDo> {

    @Select("""
            SELECT * FROM api_credential
            WHERE fingerprint_hash = #{fingerprintHash}
              AND is_deleted = FALSE
            LIMIT 1
            """)
    ApiCredentialDo selectByFingerprintHash(@Param("fingerprintHash") String fingerprintHash);

    @Select("""
            SELECT * FROM api_credential
            WHERE id = #{credentialId}
              AND consumer_id = #{consumerId}
              AND is_deleted = FALSE
            LIMIT 1
            """)
    ApiCredentialDo selectByIdAndConsumerId(
            @Param("credentialId") String credentialId, @Param("consumerId") String consumerId);
}
