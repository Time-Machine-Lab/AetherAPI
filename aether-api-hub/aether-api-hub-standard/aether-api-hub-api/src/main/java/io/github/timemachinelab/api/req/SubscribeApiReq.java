package io.github.timemachinelab.api.req;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Subscribe current user to a published API asset.
 */
public class SubscribeApiReq {

    @NotBlank(message = "API code must not be blank")
    @Size(max = 64, message = "API code must not exceed 64 characters")
    @JsonProperty("apiCode")
    private String apiCode;

    public SubscribeApiReq() {
    }

    public SubscribeApiReq(String apiCode) {
        this.apiCode = apiCode;
    }

    public String getApiCode() {
        return apiCode;
    }

    public void setApiCode(String apiCode) {
        this.apiCode = apiCode;
    }
}
