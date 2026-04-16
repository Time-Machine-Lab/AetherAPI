package io.github.timemachinelab.api.req;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Size;

/**
 * 创建 API 凭证请求。
 */
public class CreateApiCredentialReq {

    @Size(min = 1, max = 128, message = "Credential name must be 1-128 characters")
    @JsonProperty("credentialName")
    private String credentialName;

    @Size(max = 512, message = "Credential description must not exceed 512 characters")
    @JsonProperty("credentialDescription")
    private String credentialDescription;

    @JsonProperty("expireAt")
    private String expireAt;

    public CreateApiCredentialReq() {
    }

    public CreateApiCredentialReq(String credentialName, String credentialDescription, String expireAt) {
        this.credentialName = credentialName;
        this.credentialDescription = credentialDescription;
        this.expireAt = expireAt;
    }

    public String getCredentialName() {
        return credentialName;
    }

    public void setCredentialName(String credentialName) {
        this.credentialName = credentialName;
    }

    public String getCredentialDescription() {
        return credentialDescription;
    }

    public void setCredentialDescription(String credentialDescription) {
        this.credentialDescription = credentialDescription;
    }

    public String getExpireAt() {
        return expireAt;
    }

    public void setExpireAt(String expireAt) {
        this.expireAt = expireAt;
    }
}
